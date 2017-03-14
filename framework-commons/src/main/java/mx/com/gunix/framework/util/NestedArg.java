package mx.com.gunix.framework.util;

import org.springframework.context.support.DefaultMessageSourceResolvable;

public class NestedArg extends DefaultMessageSourceResolvable {
	private static final long serialVersionUID = 1L;

	public NestedArg(String mkey, String defaultMessage, Object... arguments) {
		super(new String[] { mkey }, arguments, defaultMessage);
	}
}
