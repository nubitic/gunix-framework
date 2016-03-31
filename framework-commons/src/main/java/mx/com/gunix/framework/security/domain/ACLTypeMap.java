package mx.com.gunix.framework.security.domain;

import java.util.Map;

public class ACLTypeMap implements ACLType {
	private static final long serialVersionUID = 1L;
	private final Map<String, Object> map;
	private final String innerIdPropertyName;
	private final String innerDescripcionPropertyName;
	private final String innerClaveNegocioPropertyName;

	public ACLTypeMap(Map<String, Object> aclTypeMap, String idPropertyName, String descripcionPropertyName, String claveNegocioPropertyName) {
		innerIdPropertyName = idPropertyName;
		innerDescripcionPropertyName = descripcionPropertyName;
		innerClaveNegocioPropertyName = claveNegocioPropertyName;
		map = (Map<String, Object>) aclTypeMap;
	}

	@Override
	public Long getId() {
		return (Long) map.get(innerIdPropertyName);
	}

	@Override
	public void setId(Long id) {
		map.put(innerIdPropertyName, id);
	}

	@Override
	public String getDescripcion() {
		return (String) map.get(innerDescripcionPropertyName);
	}

	@Override
	public String getClaveNegocio() {
		return map.get(innerClaveNegocioPropertyName).toString();
	}
}