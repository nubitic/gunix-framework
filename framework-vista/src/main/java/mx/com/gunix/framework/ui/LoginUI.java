package mx.com.gunix.framework.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.annotation.VaadinUI;
import org.vaadin.spring.navigator.SpringViewProvider;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

@VaadinUI(path="/login")
@Theme("valo")
public class LoginUI extends UI {
	private static final long serialVersionUID = 1L;

	@Autowired
    private SpringViewProvider ViewProvider;
    
    @Override
    protected void init(VaadinRequest request) {
        Navigator navigator = new Navigator(this, this);
        navigator.addProvider(ViewProvider);
        setNavigator(navigator);
    }
}
