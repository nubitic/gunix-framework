package mx.com.gunix.framework.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DescriptorCambios implements Serializable {
	private static final long serialVersionUID = 1L;
	private Class<?> clazz;
	private Map<String, Serializable> idMap;
	private Map<String, Serializable> cambios;
	private Map<String, List<Serializable>> inserciones;
	private Map<String, List<Serializable>> eliminaciones;

	public Map<String, Serializable> getIdMap() {
		return idMap;
	}

	public void setIdMap(Map<String, Serializable> idMap) {
		this.idMap = idMap;
	}

	public Map<String, Serializable> getCambios() {
		return cambios;
	}

	public void setCambios(Map<String, Serializable> cambios) {
		this.cambios = cambios;
	}

	public Map<String, List<Serializable>> getInserciones() {
		return inserciones;
	}

	public void setInserciones(Map<String, List<Serializable>> inserciones) {
		this.inserciones = inserciones;
	}

	public Map<String, List<Serializable>> getEliminaciones() {
		return eliminaciones;
	}

	public void setEliminaciones(Map<String, List<Serializable>> eliminaciones) {
		this.eliminaciones = eliminaciones;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

}
