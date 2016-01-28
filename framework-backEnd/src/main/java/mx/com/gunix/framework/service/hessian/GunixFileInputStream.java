package mx.com.gunix.framework.service.hessian;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class GunixFileInputStream extends InputStream implements Serializable {
	private static final long serialVersionUID = 1L;

	private String absolutePath;

	public GunixFileInputStream() {
	}

	public GunixFileInputStream(File tempFile) {
		this.absolutePath = tempFile.getAbsolutePath();
	}

	@Override
	public int read() throws IOException {
		return -1;
	}

	public File createFile() {
		if (absolutePath == null) {
			throw new IllegalStateException("Esta instancia no se ha inicializado correctamente absolutePath no puede ser null");
		}
		return new File(absolutePath);
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((absolutePath == null) ? 0 : absolutePath.hashCode());
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
		if (absolutePath == null) {
			if (other.absolutePath != null)
				return false;
		} else if (!absolutePath.equals(other.absolutePath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GunixFileInputStream [absolutePath=" + absolutePath + "]";
	}

}
