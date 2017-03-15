package mx.com.gunix.framework.service;

import java.util.List;

import mx.com.gunix.framework.security.domain.ACLType;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ACLTypeService<T extends ACLType> {

	@PostAuthorize("hasPermission(returnObject, 'READ')")
	public T getById(String id);

	@PostFilter("hasPermission(filterObject, 'READ')")
	public List<T> getAll();

	@PostFilter("hasPermission(filterObject, 'ADMINISTRATION')")
	public List<T> getAllForAdmin();

	@PostFilter("hasPermission(filterObject, 'READ')")
	public List<T> getByExample(Boolean esMaestro, T ejemplo);

	@PreAuthorize("hasPermission(#original, 'WRITE')")
	public void update(T original, T nuevo);

	@PreAuthorize("hasPermission(#objeto, 'DELETE')")
	public void delete(T objeto);

	public long insert(T objeto);

	@PostAuthorize("hasPermission(returnObject, 'READ') and hasPermission(#parent, 'READ')")
	default public T getByIdAndParent(String id, ACLType parent) {
		return null;
	}

	@PostFilter("hasPermission(filterObject, 'READ') and hasPermission(#parent, 'READ')")
	default public List<T> getAllByParent(ACLType parent) {
		return null;
	}

	@PostFilter("hasPermission(filterObject, 'ADMINISTRATION') and hasPermission(#parent, 'READ')")
	default public List<T> getAllForAdminByParent(ACLType parent) {
		return null;
	}
}