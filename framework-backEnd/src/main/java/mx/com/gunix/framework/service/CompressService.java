package mx.com.gunix.framework.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

@Service
public class CompressService {
	
	
	private static String EXTENSION_XZ = ".xz";
	private static String EXTENSION_ZIP = ".zip";
	
	
	
	public File compress(File fileToCompress, CompressionType ct) throws IOException {
		InputStream is = new FileInputStream(fileToCompress);
		return compress(fileToCompress.getName(), is ,ct);
	}
	
	
	public File compress(String fileToCompress, InputStream is, CompressionType ct) throws IOException {
		File fileCompressed = null;
		
		
		switch (ct) {
		case ZIP:
			fileCompressed = compressToZip(fileToCompress,is);
			break;

		case XZ:
			fileCompressed = compressToXZ(is);
			break;	
		
		default:
			throw new IllegalArgumentException();
			
		}
		return fileCompressed;		
	}
	
	
	
	
	/**
	 * Método que comprime un File a  XZ
	 * 
	 * @param	file	Objeto file que hace referencia al archivo que se comprimirá
	 * @return	File	Objecto file que hace referencia al archivo xz 
	 * */
	public File compressToXZ(File file) throws IOException{
		InputStream inputStream = new FileInputStream(file);
		return compressToXZ(inputStream);			
	}
	
	
	
	/**
	 * Método que comprime un inputStream a  XZ
	 *
	 * @param	inputStream	InputStream con la información a comprimir
	 * @return	File		Objecto file que hace referencia al archivo xz
	 * @throws IOException 
	 * */
	public File compressToXZ(InputStream inputStream) throws IOException{
		

		XZOutputStream xzout = null;
		FileOutputStream fileOutputStream = null;
		File fileOutput = null;
		
		try {		
			byte[] buffer = new byte[1024*500];
			fileOutput = File.createTempFile("tmp"+EXTENSION_XZ,EXTENSION_XZ);
			
			fileOutputStream = new FileOutputStream(fileOutput);
			LZMA2Options options = new LZMA2Options();
			options.setPreset(7);
			
			xzout = new XZOutputStream(fileOutputStream, options);
			int len;
			while ((len = inputStream.read(buffer)) > 0) {
			   xzout.write(buffer, 0, len);  
			}
		}catch(Throwable ex){	
			throw ex;
		}finally{			
			if(inputStream != null)
			   inputStream.close();
		   if(xzout != null)
			   xzout.close();
		   if(fileOutputStream != null)
			   fileOutputStream.close();
		
	    }
		return fileOutput;		
	
	}
	
	public File compressToZip(String fileName, InputStream inputStream) throws IOException {
		
		File fileOutput = null;
		ZipOutputStream zos = null;
		FileOutputStream fos = null;
		try {
			fileOutput = File.createTempFile(fileName,EXTENSION_ZIP);
 
            fos = new FileOutputStream(fileOutput);
            zos = new ZipOutputStream(fos); 
            zos.putNextEntry(new ZipEntry(fileName));
 
            int len;
            byte[] buffer = new byte[1024*500];
			while ((len = inputStream.read(buffer)) > 0) {
				zos.write(buffer, 0, len);  
			}        
            
 
        } catch (IOException ex) {
        	throw ex;
        }finally {
        	if(inputStream != null) {
        		inputStream.close();
        	}
        	if(zos != null) {
        		zos.closeEntry();
                zos.close();
        	}
        	if(fos != null) {
        		fos.close();
        	}
        	
		}
		
		return fileOutput;
	}
	

}
