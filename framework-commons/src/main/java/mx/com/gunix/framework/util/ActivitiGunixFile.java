package mx.com.gunix.framework.util;

import java.io.File;

public class ActivitiGunixFile extends GunixFile {
	private static final long serialVersionUID = 1L;

	private String nombreVariable;

	public ActivitiGunixFile() {
		super(null);
	}

	public ActivitiGunixFile(String nombreVariable, String fileName, String mimeType, File file) {
		super(fileName);
		init(nombreVariable, fileName, mimeType, file);
	}

	private void init(String nombreVariable, String fileName, String mimeType, File file) {
		this.nombreVariable = nombreVariable;
		setMimeType(mimeType);
		setFile(file);
		fetchInputStream();
	}

	public String getNombreVariable() {
		return nombreVariable;
	}

	public void setNombreVariable(String nombreVariable) {
		this.nombreVariable = nombreVariable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((nombreVariable == null) ? 0 : nombreVariable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivitiGunixFile other = (ActivitiGunixFile) obj;
		if (nombreVariable == null) {
			if (other.nombreVariable != null)
				return false;
		} else if (!nombreVariable.equals(other.nombreVariable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActivitiGunixFile [nombreVariable=" + nombreVariable + ", getFileName()=" + getFileName() + ", getMimeType()=" + getMimeType() + "]";
	}

}
