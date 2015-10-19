package mx.com.gunix.framework.ui.springmvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController {
	@RequestMapping(value = "/loginForm", method = RequestMethod.GET)
	public String login() {
		return "framework/loginForm";
	}
}