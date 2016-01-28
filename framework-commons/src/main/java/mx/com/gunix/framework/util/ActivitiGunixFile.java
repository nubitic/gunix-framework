package mx.com.gunix.framework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

public class ActivitiGunixFile implements Serializable {
	private static final long serialVersionUID = 1L;

	private String nombreVariable;
	private String nombreArchivo;
	private String mimeType;
	private InputStream is;

	public ActivitiGunixFile() {

	}

	public ActivitiGunixFile(String nombreVariable, String fileName, String mimeType, File file) {
		init(nombreVariable, fileName, mimeType, file);
	}

	private void init(String nombreVariable, String fileName, String mimeType, File file) {
		this.nombreVariable = nombreVariable;
		this.nombreArchivo = fileName;
		this.mimeType = mimeType;
		try {
			this.is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public String getNombreVariable() {
		return nombreVariable;
	}

	public void setNombreVariable(String nombreVariable) {
		this.nombreVariable = nombreVariable;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public InputStream getIs() {
		return is;
	}

	public void setIs(InputStream is) {
		this.is = is;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
		result = prime * result + ((nombreArchivo == null) ? 0 : nombreArchivo.hashCode());
		result = prime * result + ((nombreVariable == null) ? 0 : nombreVariable.hashCode());
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
		ActivitiGunixFile other = (ActivitiGunixFile) obj;
		if (mimeType == null) {
			if (other.mimeType != null)
				return false;
		} else if (!mimeType.equals(other.mimeType))
			return false;
		if (nombreArchivo == null) {
			if (other.nombreArchivo != null)
				return false;
		} else if (!nombreArchivo.equals(other.nombreArchivo))
			return false;
		if (nombreVariable == null) {
			if (other.nombreVariable != null)
				return false;
		} else if (!nombreVariable.equals(other.nombreVariable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActivitiGunixFile [nombreVariable=" + nombreVariable + ", nombreArchivo=" + nombreArchivo + ", mimeType=" + mimeType + "]";
	}

}
