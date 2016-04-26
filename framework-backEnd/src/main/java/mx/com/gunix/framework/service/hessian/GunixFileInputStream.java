package mx.com.gunix.framework.service.hessian;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class GunixFileInputStream extends InputStream implements Serializable {
	private static final long serialVersionUID = 1L;

	private File tempFile;
	private transient FileInputStream fileIS;

	public GunixFileInputStream() {
		
	}

	public GunixFileInputStream(File tempFile) {
		if (!tempFile.exists()) {
			throw new IllegalArgumentException("No se puede crear un GunixFileInputStream para un archivo que no existe: " + tempFile);
		}
		this.tempFile = tempFile;
	}

	public File getTempFile() {
		return tempFile;
	}
	

	public void setTempFile(File tempFile) {
		this.tempFile = tempFile;
	}
	
	private void ensureFileISInitialized() {
		if (fileIS == null) {
			try {
				fileIS = new FileInputStream(tempFile);
			} catch (FileNotFoundException e) {
				throw new RuntimeException("No se ha encontrado el archivo " + tempFile, e);
			}
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		ensureFileISInitialized();
		return fileIS.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		ensureFileISInitialized();
		return fileIS.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		ensureFileISInitialized();
		return fileIS.skip(n);
	}

	@Override
	public int read() throws IOException {
		ensureFileISInitialized();
		return fileIS.read();
	}

	@Override
	public int available() throws IOException {
		ensureFileISInitialized();
		return fileIS.available();
	}

	@Override
	public void close() throws IOException {
		ensureFileISInitialized();
		fileIS.close();
		tempFile.delete();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tempFile == null) ? 0 : tempFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GunixFileInputStream other = (GunixFileInputStream) obj;
		if (tempFile == null) {
			if (other.tempFile != null)
				return false;
		} else if (!tempFile.equals(other.tempFile))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GunixFileInputStream [tempFile=" + tempFile + "]";
	}

}
