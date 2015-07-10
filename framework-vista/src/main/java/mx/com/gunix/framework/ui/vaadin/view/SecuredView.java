package mx.com.gunix.framework.ui.vaadin.view;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;

@Secured(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED)
public interface SecuredView extends mx.com.gunix.framework.security.Secured {

}
