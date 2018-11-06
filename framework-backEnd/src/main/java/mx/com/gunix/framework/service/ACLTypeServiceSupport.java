package mx.com.gunix.framework.service;

import mx.com.gunix.framework.security.domain.ACLType;
import mx.com.gunix.framework.util.SequenceHelperMapper;

import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class ACLTypeServiceSupport<T extends ACLType> extends GunixActivitServiceSupport<T> implements ACLTypeService<T> {

	@Autowired
	SequenceHelperMapper shm;

	@Autowired
	MutableAclService aclService;

	@Override
	public void update(@P("original") T original, T anterior) {
		doUpdate(original, anterior);
	}

	@Override
	public void delete(@P("objeto") T objeto) {
		doDelete(objeto);
	}

	@SuppressWarnings("unchecked")
	public final long insert(@P("objeto") T aclType) {
		if (AopUtils.isCglibProxy(this)) {
			try {
				ACLTypeService<T> target = (ACLTypeService<T>) ((TargetSource) getClass().getMethod("getTargetSource", new Class<?>[] {}).invoke(this, new Object[] {})).getTarget();
				return target.insert(aclType);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		} else {
			Long newId = shm.nextVal("seguridad.acl_object_identity", "object_id_identity");
			ObjectIdentity oi = new ObjectIdentityImpl(aclType.getClass(), newId);
			MutableAcl mAcl = aclService.createAcl(oi);
			mAcl.setEntriesInheriting(false);
			if (aclType.getParent() != null) {
				mAcl.setParent(aclService.readAclById(new ObjectIdentityImpl(aclType.getParent().getClass(), aclType.getParent().getId())));
			}
			Sid sid = new PrincipalSid(SecurityContextHolder.getContext().getAuthentication());
			mAcl.insertAce(mAcl.getEntries().size(), new CumulativePermission().set(BasePermission.ADMINISTRATION).set(BasePermission.READ).set(BasePermission.WRITE).set(BasePermission.DELETE), sid, true);
			aclService.updateAcl(mAcl);
			aclType.setId(newId);
			doInsert(aclType);
			return newId;
		}
	}

	protected abstract void doInsert(T objeto);

	protected abstract void doUpdate(T original, T anterior);

	protected abstract void doDelete(T objeto);
}
