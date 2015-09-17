package mx.com.gunix.framework.service;

import java.util.List;

import mx.com.gunix.framework.security.domain.ACLType;
import mx.com.gunix.framework.security.domain.persistence.SequenceHelperMapper;

import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class ACLTypeService<T extends ACLType> extends GunixActivitServiceSupport{

	@Autowired
	SequenceHelperMapper shm;

	@Autowired
	MutableAclService aclService;

	/**
	 * Retrieves a single ACLType.
	 * <p>
	 * Access-control will be evaluated after this method is invoked.
	 * returnObject refers to the returned object.
	 */
	@PostAuthorize("hasPermission(returnObject, 'READ')")
	public abstract T getById(Long id);

	/**
	 * Retrieves all ACLType.
	 * <p>
	 * Access-control will be evaluated after this method is invoked.
	 * filterObject refers to the returned object list.
	 */
	@PostFilter("hasPermission(filterObject, 'READ')")
	public abstract List<T> getAll();

	/**
	 * Edits an ACLType.
	 * <p>
	 * Access-control will be evaluated before this method is invoked.
	 * <b>#post</b> refers to the current object in the method argument.
	 */
	@PreAuthorize("hasPermission(#aclType, 'WRITE')")
	public abstract void update(T aclType);

	/**
	 * Deletes a ACLType.
	 * <p>
	 * Access-control will be evaluated before this method is invoked.
	 * <b>#post</b> refers to the current object in the method argument.
	 */
	@PreAuthorize("hasPermission(#aclType, 'DELETE')")
	public abstract void delete(T aclType);

	@SuppressWarnings("unchecked")
	@PreAuthorize("hasRole('ACL_ADMIN')")
	public final long insert(T aclType) {
		if (AopUtils.isCglibProxy(this)) {
			try {
				ACLTypeService<T> target = (ACLTypeService<T>) ((TargetSource) getClass().getMethod("getTargetSource", new Class<?>[] {}).invoke(this, new Object[] {})).getTarget();
				return target.insert(aclType);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

		} else {
			Long newId = shm.nextVal("acl_object_identity", "object_id_identity");
			ObjectIdentity oi = new ObjectIdentityImpl(aclType.getClass(), newId);
			MutableAcl mAcl = aclService.createAcl(oi);
			Sid sid = new PrincipalSid(SecurityContextHolder.getContext().getAuthentication());
			int currIndex = mAcl.getEntries().size();
			mAcl.insertAce(currIndex, BasePermission.ADMINISTRATION, sid, true);
			mAcl.insertAce(currIndex + 1, BasePermission.READ, sid, true);
			mAcl.insertAce(currIndex + 2, BasePermission.WRITE, sid, true);
			mAcl.insertAce(currIndex + 3, BasePermission.DELETE, sid, true);
			aclService.updateAcl(mAcl);
			aclType.setId(newId);
			doInsert(aclType);
			return newId;
		}
	}

	protected abstract void doInsert(T aclType);
}
