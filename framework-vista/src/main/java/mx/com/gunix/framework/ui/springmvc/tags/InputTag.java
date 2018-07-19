package mx.com.gunix.framework.ui.springmvc.tags;

import java.util.Map;

import javax.servlet.jsp.JspException;

public class InputTag extends org.springframework.web.servlet.tags.form.InputTag {
	private static final long serialVersionUID = 1L;

	public Map<String, Object> getDynAttrsMap() {
		return getDynamicAttributes();
	}

	public void setDynAttrsMap(Map<String, Object> dynAttrsMap) {
		if (dynAttrsMap != null) {
			dynAttrsMap.forEach((localName, value) -> {
				try {
					setDynamicAttribute(null, localName, value);
				} catch (JspException e) {
					throw new RuntimeException(e);
				}
			});

		}
	}

}
