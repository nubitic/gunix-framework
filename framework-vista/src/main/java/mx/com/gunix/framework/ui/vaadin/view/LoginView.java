package mx.com.gunix.framework.ui.vaadin.view;

import mx.com.gunix.framework.ui.vaadin.LoginUI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.vaadin.spring.navigator.annotation.VaadinView;
import org.vaadin.spring.security.VaadinSecurity;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@VaadinView(name="",ui=LoginUI.class)
public class LoginView extends VerticalLayout implements View {
	private static final long serialVersionUID = 1L;
    
    @Autowired
    private VaadinSecurity security;
    
    private TextField       username;
    private PasswordField   password;
    private CheckBox        rememberMe = new CheckBox("Remember me", true);
    
    public LoginView() {
        setSizeFull();

        Component loginForm = buildLoginForm();
        addComponent(loginForm);
        setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);

    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        username.focus();
    }

    private Component buildLoginForm() {
        final VerticalLayout loginPanel = new VerticalLayout();
        loginPanel.setSizeUndefined();
        loginPanel.setSpacing(true);
        Responsive.makeResponsive(loginPanel);

        loginPanel.addComponent(buildFields());
        loginPanel.addComponent(rememberMe);
        return loginPanel;
    }

    private Component buildFields() {
        HorizontalLayout fields = new HorizontalLayout();
        fields.setSpacing(true);

        username = new TextField("Username");
        username.setIcon(FontAwesome.USER);

        password = new PasswordField("Password");
        password.setIcon(FontAwesome.LOCK);

        final Button signin = new Button("Sign In");
 
        signin.setClickShortcut(KeyCode.ENTER);
        signin.focus();

        fields.addComponents(username, password, signin);
        fields.setComponentAlignment(signin, Alignment.BOTTOM_LEFT);

        signin.addClickListener(evnt ->{
            try {
                
                security.login(username.getValue(), password.getValue(), rememberMe.getValue());
                
            } catch(AuthenticationException e) {
                e.printStackTrace();
            } catch(Exception e) {
                e.printStackTrace();
            }
            // TODO Register Remember me Token
            
            /*
             * Redirect is handled by the VaadinRedirectStrategy
             * User is redirected to either always the default
             * or the URL the user request before authentication
             * 
             * Strategy is configured within SecurityConfiguration
             * Defaults to User request URL.
             */
        });
        
        return fields;
    }
}