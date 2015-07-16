package mx.com.gunix.framework.service;

import java.util.List;

import mx.com.gunix.framework.security.domain.ACLType;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ACLTypeService<T extends ACLType> {
	/**
	  *  Retrieves a single ACLType.
	  *  <p>
	  *  Access-control will be evaluated after this method is invoked.
	  *  returnObject refers to the returned object.
	  */
	 @PostAuthorize("hasPermission(returnObject, 'WRITE')")
	 public T getSingle(Long id);
	 
	 /**
	  *  Retrieves all ACLType.
	  *  <p>
	  *  Access-control will be evaluated after this method is invoked.
	  *  filterObject refers to the returned object list.
	  */
	 @PostFilter("hasPermission(filterObject, 'READ')")
	 public List<T> getAll();
	 
	 /**
	  * Edits an ACLType.
	  * <p>
	  * Access-control will be evaluated before this method is invoked.
	  * <b>#post</b> refers to the current object in the method argument. 
	  */
	 @PreAuthorize("hasPermission(#aclType, 'WRITE')")
	 public Boolean edit(T aclType);
	 
	 /**
	  * Deletes a ACLType.
	  * <p>
	  * Access-control will be evaluated before this method is invoked.
	  * <b>#post</b> refers to the current object in the method argument. 
	  */
	 @PreAuthorize("hasPermission(#aclType, 'WRITE')")
	 public Boolean delete(T aclType);	 
}
