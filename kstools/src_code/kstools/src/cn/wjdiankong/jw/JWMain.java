package cn.wjdiankong.jw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

/**
 * 
 * ��һ������ѹapk��ȡ�����е�dex�ļ�
 * �ڶ�����ʹ��dex2jar�����࣬��Ӵ���
 * ��������ʹ��dx�������ɶ�Ӧ��dex�ļ�
 * ���Ĳ���ѹ�����޸�֮���apk�ļ�
 * ���岽��ʹ��jarsigner����apkǩ��
 * 
 * @author jiangwei1-g
 *
 */
public class JWMain {

	public static String aaptCmdDir;
	public static String srcApkPath;
	public static String unSignedApkPath = "unsigned.apk";
	public static String rootPath;
	
	public static void main(String[] args) {
		
		if(args.length < 1){
			System.out.println("args error");
			return;
		}
		
		String optionStr = args[0];
		if("++sign".equals(optionStr)){
			generateApkSign(args);
		}else if("++hook".equals(optionStr)){
			hookWork(args);
			return;
		}else{
			System.out.println("option args error!");
			return;
		}
		
	}
	
	private static void generateApkSign(String[] args){
		if(args.length < 2){
			System.out.println("args error");
			return;
		}
		File srcSignApkFile = new File(args[1]+".apk");
		if(!srcSignApkFile.exists()){
			System.out.println("apk file " + srcSignApkFile.getAbsolutePath()+" is not exist!");
			return;
		}
		boolean isSucc = DoWorkUtils.getAppSign(srcSignApkFile);
		if(isSucc){
			System.out.println("��ȡǩ����Ϣ�ɹ�:"+Const.appSign);
		}else{
			System.out.println("��ȡǩ����Ϣʧ��,���Գ����ֶ���ȡǩ����Ϣ,���巽���μ�˵���ĵ�");
			return;
		}
		
		File signFile = new File("apksign.txt");
		if(signFile.exists()){
			signFile.delete();
		}
		
		FileWriter writer = null;
		try{
			writer = new FileWriter(signFile);
			writer.write(Const.appSign);
			System.out.println("��дǩ����Ϣ�ɹ�,������apksign.txt�ĵ���(����ɾ��)");
		}catch(Exception e){
			System.out.println("");
		}finally{
			if(writer != null){
				try{
					writer.close();
				}catch(Exception e){
				}
			}
		}
	}
	
	private static void hookWork(String[] args){
		
		if(args.length < 4){
			System.out.println("args error");
			return;
		}
		
		rootPath = args[1];
		srcApkPath = args[2];

		File srcApkFile = new File(srcApkPath);
		if(!srcApkFile.exists()){
			System.out.println("src apk file is not exist");
			return;
		}
		
		aaptCmdDir = args[3];
		
		File aaptCmdDirFile = new File(aaptCmdDir);
		if(!aaptCmdDirFile.exists()){
			System.out.println("aapt���߲�����,�������Ҫ�ֶ�����bat�ļ��е�aapt·��(·����ò�Ҫ���õ�C����)!");
			return;
		}
		
		String javaHome = System.getenv("JAVA_HOME");
		if(javaHome == null || "".equals(javaHome)){
			System.out.println("δ����JAVA_HOME��������,�Ҳ���jarsigner.exe����!");
			return;
		}
		
		//�鿴��û�����µ�ǩ����Ϣ����
		File signFile = new File("apksign.txt");
		if(signFile.exists()){
			System.out.println("������ǩ��ֵ,��ʼ��ȡ����ת��...");
			FileInputStream fis = null;
			try{
				fis = new FileInputStream("apksign.txt");
				int size = fis.available();
		        byte[] buffer=new byte[size];
		        fis.read(buffer);
		        Const.appSign = new String(buffer);
				if(Const.appSign != null && Const.appSign.length() != 0){
					System.out.println("��ȡǩ��������Ϣ�ɹ���"+Const.appSign);
				}else{
					System.out.println("��ȡǩ������ǩ����Ϣʧ��,Ĭ�ϲ���apk�Դ���ǩ����Ϣ...\n");
				}
			}catch(Exception e){
			}
		}else{
			System.out.println("û���ҵ�����ǩ����Ϣ,Ĭ�ϲ���apk�Դ���ǩ����Ϣ...\n");
		}

		File unZipFile = new File(Const.unZipDir);
		if(!unZipFile.exists()){
			unZipFile.mkdirs();
		}
		
		//����ԭʼapk�ļ�һ������Ϊunsigned.apk
		if(!FileUtils.fileCopy(srcApkPath, unSignedApkPath)){
			unSignedApkPath = srcApkPath;
		}
		
		if(Const.appSign == null || Const.appSign.length() == 0){
			if(!DoWorkUtils.getAppSign(srcApkFile)){
				DoWorkUtils.deleteTmpFile(rootPath);
				return;
			}
		}
		
		if(!DoWorkUtils.getAppEnter(srcApkFile)){
			DoWorkUtils.deleteTmpFile(rootPath);
			return;
		}
		
		if(!DoWorkUtils.zipApkWork(srcApkFile, rootPath+Const.unZipDir)){
			DoWorkUtils.deleteTmpFile(rootPath);
			return;
		}
		
		if(!DoWorkUtils.deleteMetaInf(rootPath+Const.unZipDir, aaptCmdDir, unSignedApkPath)){
			DoWorkUtils.deleteTmpFile(rootPath);
			return;
		}
		
		if(!DoWorkUtils.dexToSmali(rootPath + Const.unZipDir+File.separator+"classes.dex", rootPath+Const.smaliTmpDir)){
			DoWorkUtils.deleteTmpFile(rootPath);
			return;
		}
		
		if(!DoWorkUtils.setSignAndPkgName()){
			DoWorkUtils.deleteTmpFile(rootPath);
			return;
		}
		
		if(!DoWorkUtils.insertHookCode()){
			DoWorkUtils.deleteTmpFile(rootPath);
			return;
		}
		
		if(!DoWorkUtils.smaliToDex(rootPath+File.separator+Const.smaliTmpDir, rootPath + File.separator+"classes.dex")){
			DoWorkUtils.deleteTmpFile(rootPath);
			return;
		}
		
		if(!DoWorkUtils.addDexToApk(aaptCmdDir, rootPath+Const.unZipDir, unSignedApkPath)){
			DoWorkUtils.deleteTmpFile(rootPath);
			return;
		}
		
		if(!DoWorkUtils.signApk(unSignedApkPath, rootPath)){
			DoWorkUtils.deleteTmpFile(rootPath);
			return;
		}
		
		DoWorkUtils.deleteTmpFile(rootPath);
		
	}

}
