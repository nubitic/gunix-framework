package org.vaadin.easyuploads;

import java.io.File;

import mx.com.gunix.framework.ui.GunixFile;

import org.vaadin.easyuploads.FileBuffer;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;

public class GunixFileBuffer extends FileBuffer {
	private static final long serialVersionUID = 1L;
	private UploadField uf;
	private GunixFile value;

	@Override
	public boolean isEmpty() {
		return value == null && super.getValue() == null;
	}

	public GunixFileBuffer(UploadField uf) {
		super(FieldType.FILE);
		super.setFieldType(FieldType.FILE);
		if (uf == null) {
			throw new IllegalArgumentException("UploadField no puede ser null");
		}
		this.uf = uf;
		this.uf.setFieldType(FieldType.FILE);
	}

	@Override
	public FileFactory getFileFactory() {
		return uf.getFileFactory();
	}

	@Override
	public FieldType getFieldType() {
		return uf.getFieldType();
	}

	@Override
	public Object getValue() {
		File fileValue = null;
		if ((value == null && (fileValue = (File) super.getValue()) != null) || (fileValue != null && fileValue != value.getFile() && !value.getFile().equals(fileValue))) {
			value = new GunixFile(this.fileName);
			value.setFile(fileValue);
			value.setMimeType(this.mimeType);
		}
		return value;
	}

	@Override
	public void setValue(Object newValue) {
		if (newValue instanceof GunixFile) {
			this.value = (GunixFile) newValue;
			super.setValue(value.getFile());
			this.fileName = value.getFileName();
			this.mimeType = value.getMimeType();
		} else {
			if (newValue == null) {
				this.value = null;
				super.setValue(null);
				this.fileName = null;
				this.mimeType = null;
			} else {
				throw new IllegalArgumentException("newValue debe ser del tipo mx.com.gunix.framework.ui.GunixFile");
			}
		}
	};

	@Override
	public void setFieldType(FieldType fieldType) {
		
	}
}
