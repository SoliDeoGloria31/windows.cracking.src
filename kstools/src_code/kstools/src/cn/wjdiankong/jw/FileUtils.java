package cn.wjdiankong.jw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtils {
	
	//private final static String DEX_MAGIC = "dex\n035";
	private final static byte[] DEX_MAGIC = new byte[]{0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35,0x00};
	
	public static boolean isValidDexFile(String dexFile){
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(new File(dexFile));
			byte[] magic = new byte[8];
			fis.read(magic, 0, 8);
			boolean isValid = true;
			for(int i=0;i<8;i++){
				if(DEX_MAGIC[i] == magic[i]){
					isValid &= true;
				}else{
					isValid &= false;
				}
			}
			return isValid;
		}catch(Exception e){
		}finally{
			if(fis != null){
				try{
					fis.close();
				}catch(Exception e){
					
				}
			}
		}
		return false;
	}
	
	/** 
	 * �ļ������ķ��� 
	 */  
	public static boolean fileCopy(String src, String des) {  
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {  
			fis = new FileInputStream(src);
			fos = new FileOutputStream(des);
			int len = 0;
			byte[] buffer = new byte[10*1024];
			while((len=fis.read(buffer)) > 0){  
				fos.write(buffer, 0, len);
			}  
		} catch (Exception e) {  
			System.out.println("�����ļ�ʧ��:"+e.toString());
			return false;
		}finally{  
			try {  
				if(fis!=null)  fis.close();  
				if(fos!=null)  fos.close();  
			} catch (Exception e) {  
				System.out.println("�����ļ�ʧ��:"+e.toString());
				return false;
			} 
		}
		return true;

	}    
	
	private static void copy(InputStream input, OutputStream output) throws IOException {
        int bytesRead;
        final byte[] BUFFER = new byte[4096 * 1024];
        while ((bytesRead = input.read(BUFFER))!= -1) {
            output.write(BUFFER, 0, bytesRead);
        }
    }
	
	public static void addFileToZipFile(String fileName, String entryName, String zipFileName, String newZipFileName){
		try{
			@SuppressWarnings("resource")
			FileInputStream fis = new FileInputStream(fileName);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len = 0;
			byte[] buffer = new byte[1024];
			while((len=fis.read(buffer)) > 0){
				bos.write(buffer, 0, len);
			}
			// read war.zip and write to append.zip
	        ZipFile war = new ZipFile(zipFileName);
	        ZipOutputStream append = new ZipOutputStream(new FileOutputStream(newZipFileName));
	        // first, copy contents from existing war
	        Enumeration<? extends ZipEntry> entries = war.entries();
	        while (entries.hasMoreElements()) {
	            ZipEntry e = entries.nextElement();
	            append.putNextEntry(e);
	            if (!e.isDirectory()) {
	                copy(war.getInputStream(e), append);
	            }
	            append.closeEntry();
	        }
	        ZipEntry e = new ZipEntry(entryName);
	        append.putNextEntry(e);
	        append.write(bos.toByteArray());
	        append.closeEntry();
	        // close
	        war.close();
	        append.close();
		}catch(Exception e){
		}
	}

	public static void zip(String zipFileName, File inputFile) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		BufferedOutputStream bo = new BufferedOutputStream(out);
		zip(out, inputFile, inputFile.getName(), bo);
		bo.close();        
		out.close(); // ������ر�
	}    

	private static void zip(ZipOutputStream out, File f, String base, BufferedOutputStream bo) throws Exception { // ��������
		if (f.isDirectory()){
			File[] fl = f.listFiles();            
			if (fl.length == 0){
				out.putNextEntry(new ZipEntry(base + "/")); // ����zipѹ�������base
			}
			for (int i = 0; i < fl.length; i++) {
				zip(out, fl[i], base + "/" + fl[i].getName(), bo); // �ݹ�������ļ���
			}
		} else {
			out.putNextEntry(new ZipEntry(base)); // ����zipѹ�������base
			FileInputStream in = new FileInputStream(f);
			BufferedInputStream bi = new BufferedInputStream(in);
			int b; 
			while ((b = bi.read()) != -1) {
				bo.write(b); // ���ֽ���д�뵱ǰzipĿ¼
			}
			bi.close();
			in.close(); // �������ر�
		}
	} 
	
	public static void decompressZipFile(String zipPath, String targetPath) throws IOException  {      
		File file = new File(zipPath);    
		if (!file.isFile()) {    
			throw new FileNotFoundException("file not exist!");    
		}    
		if (targetPath == null || "".equals(targetPath)) {    
			targetPath = file.getParent();    
		}      
		@SuppressWarnings("resource")
		ZipFile zipFile = new ZipFile(file);    
		Enumeration<? extends ZipEntry> files = zipFile.entries();    
		ZipEntry entry = null;    
		File outFile = null;    
		BufferedInputStream bin = null;    
		BufferedOutputStream bout = null;    
		while (files.hasMoreElements()) {    
			entry = files.nextElement();    
			outFile = new File(targetPath + File.separator + entry.getName());    
			// �����ĿΪĿ¼����������һ��     
			if(entry.isDirectory()){  
				outFile.mkdirs();    
				continue;    
			}    
			// ����Ŀ¼    
			if (!outFile.getParentFile().exists()) {    
				outFile.getParentFile().mkdirs();    
			}    
			// �������ļ�    
			outFile.createNewFile();    
			// �������д����������һ����Ŀ    
			if (!outFile.canWrite()) {    
				continue;    
			}    
			try {    
				bin = new BufferedInputStream(zipFile.getInputStream(entry));    
				bout = new BufferedOutputStream(new FileOutputStream(outFile));    
				byte[] buffer = new byte[1024];    
				int readCount = -1;    
				while ((readCount = bin.read(buffer)) != -1) {    
					bout.write(buffer, 0, readCount);    
				}    
			} finally {    
				try {    
					bin.close();    
					bout.flush();    
					bout.close();    
				} catch (Exception e) {}    
			}    
		}    
	}  

	public static void decompressDexFile(String zipPath, String targetPath) throws IOException  {      
		File file = new File(zipPath);    
		if (!file.isFile()) {    
			throw new FileNotFoundException("file not exist!");    
		}    
		if (targetPath == null || "".equals(targetPath)) {    
			targetPath = file.getParent();    
		}      
		@SuppressWarnings("resource")
		ZipFile zipFile = new ZipFile(file);    
		Enumeration<? extends ZipEntry> files = zipFile.entries();    
		ZipEntry entry = null;    
		File outFile = null;    
		BufferedInputStream bin = null;    
		BufferedOutputStream bout = null;    
		while (files.hasMoreElements()) {    
			entry = files.nextElement();    
			outFile = new File(targetPath + File.separator + entry.getName());    
			// �����ĿΪĿ¼����������һ��     
			if(entry.isDirectory()){  
				outFile.mkdirs();
				continue;    
			}    
			//����ֻ���ѹdex�ļ���ǩ���ļ�
			if(!(entry.getName().endsWith("classes.dex") || entry.getName().startsWith("META-INF"))){
				continue;
			}
			// ����Ŀ¼    
			if (!outFile.getParentFile().exists()) {    
				outFile.getParentFile().mkdirs();    
			}    
			// �������ļ�    
			outFile.createNewFile();    
			// �������д����������һ����Ŀ    
			if (!outFile.canWrite()) {    
				continue;    
			}    
			try {    
				bin = new BufferedInputStream(zipFile.getInputStream(entry));    
				bout = new BufferedOutputStream(new FileOutputStream(outFile));    
				byte[] buffer = new byte[1024];    
				int readCount = -1;    
				while ((readCount = bin.read(buffer)) != -1) {    
					bout.write(buffer, 0, readCount);    
				}    
			} finally {    
				try {    
					bin.close();    
					bout.flush();    
					bout.close();    
				} catch (Exception e) {}    
			}    
		}    
	}  


	/** 
	 * ɾ�������ļ� 
	 *  
	 * @param fileName 
	 *            Ҫɾ�����ļ����ļ��� 
	 * @return �����ļ�ɾ���ɹ�����true�����򷵻�false 
	 */  
	public static boolean deleteFile(String fileName) {  
		File file = new File(fileName);  
		// ����ļ�·������Ӧ���ļ����ڣ�������һ���ļ�����ֱ��ɾ��  
		if (file.exists() && file.isFile()) {  
			if (file.delete()) {  
				return true;  
			} else {  
				return false;  
			}  
		} else {  
			return false;  
		}  
	}  

	/** 
	 * ɾ��Ŀ¼��Ŀ¼�µ��ļ� 
	 *  
	 * @param dir 
	 *            Ҫɾ����Ŀ¼���ļ�·�� 
	 * @return Ŀ¼ɾ���ɹ�����true�����򷵻�false 
	 */  
	public static boolean deleteDirectory(String dir) {  
		// ���dir�����ļ��ָ�����β���Զ�����ļ��ָ���  
		if (!dir.endsWith(File.separator))  
			dir = dir + File.separator;  
		File dirFile = new File(dir);  
		// ���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�  
		if ((!dirFile.exists()) || (!dirFile.isDirectory())) {  
			return false;  
		}  
		boolean flag = true;  
		// ɾ���ļ����е������ļ�������Ŀ¼  
		File[] files = dirFile.listFiles();  
		for (int i = 0; i < files.length; i++) {  
			// ɾ�����ļ�  
			if (files[i].isFile()) {  
				flag = deleteFile(files[i].getAbsolutePath());  
				if (!flag)  
					break;  
			}  
			// ɾ����Ŀ¼  
			else if (files[i].isDirectory()) {  
				flag = deleteDirectory(files[i]  
						.getAbsolutePath());  
				if (!flag)  
					break;  
			}  
		}  
		if (!flag) {  
			return false;  
		}  
		// ɾ����ǰĿ¼  
		if (dirFile.delete()) {  
			return true;  
		} else {  
			return false;  
		}  
	}  
	
	public static void printException(Throwable e){
		if(e == null){
			return;
		}
		StackTraceElement[] eles = e.getStackTrace();
		for(StackTraceElement ele : eles){
			System.out.println(ele.toString());
		}
	}
	
}
