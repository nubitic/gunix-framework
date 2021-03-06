package mx.com.gunix.framework.security.domain;

import java.io.Serializable;

public interface ACLType extends Serializable {
	public Long getId();

	public void setId(Long id);

	public String getDescripcion();

	public String getClaveNegocio();

	default public ACLType getParent() {
		return null;
	}
}
