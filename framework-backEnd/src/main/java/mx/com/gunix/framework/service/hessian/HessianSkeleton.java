package mx.com.gunix.framework.service.hessian;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.services.server.ServiceContext;

public class HessianSkeleton extends com.caucho.hessian.server.HessianSkeleton {
	private static final Logger log = Logger.getLogger(HessianSkeleton.class.getName());

	public HessianSkeleton(Object service, Class<?> apiClass) {
		super(service, apiClass);
	}

	@Override
	public void invoke(Object service, AbstractHessianInput in, AbstractHessianOutput out) throws Exception {
		ServiceContext context = ServiceContext.getContext();

		// backward compatibility for some frameworks that don't
		// read
		// the call type first
		in.skipOptionalCall();

		// Hessian 1.0 backward compatibility
		String header;
		while ((header = in.readHeader()) != null) {
			Object value = in.readObject();

			context.addHeader(header, value);
		}

		String methodName = in.readMethod();
		int argLength = in.readMethodArgLength();

		Method method;

		method = getMethod(methodName + "__" + argLength);

		if (method == null)
			method = getMethod(methodName);

		if (method != null) {
		} else if ("_hessian_getAttribute".equals(methodName)) {
			String attrName = in.readString();
			in.completeCall();

			String value = null;

			if ("java.api.class".equals(attrName))
				value = getAPIClassName();
			else if ("java.home.class".equals(attrName))
				value = getHomeClassName();
			else if ("java.object.class".equals(attrName))
				value = getObjectClassName();

			out.writeReply(value);
			out.close();
			return;
		} else if (method == null) {
			out.writeFault("NoSuchMethodException", escapeMessage("The service has no method named: " + in.getMethod()), null);
			out.close();
			return;
		}

		Class<?>[] args = method.getParameterTypes();

		if (argLength != args.length && argLength >= 0) {
			out.writeFault("NoSuchMethod", escapeMessage("method " + method + " argument length mismatch, received length=" + argLength), null);
			out.close();
			return;
		}

		Object[] values = new Object[args.length];

		for (int i = 0; i < args.length; i++) {
			// XXX: needs Marshal object
			values[i] = in.readObject(args[i]);
		}

		UserDetails ud = (UserDetails) in.readObject(UserDetails.class);
		if (ud != null) {
			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities()));
		}
		Object result = null;

		try {
			result = method.invoke(service, values);
		} catch (Exception e) {
			log.log(Level.SEVERE,"Error en invocación remota", e);
			Throwable e1 = e;
			if (e1 instanceof InvocationTargetException)
				e1 = ((InvocationTargetException) e).getTargetException();

			out.writeFault("ServiceException", escapeMessage(e1.getMessage()), new GunixHessianServiceException(e1));
			out.close();
			return;
		} finally {
			SecurityContextHolder.getContext().setAuthentication(null);
		}

		// The complete call needs to be after the invoke to
		// handle a
		// trailing InputStream
		in.completeCall();

		out.writeReply(result);

		out.close();
	}

	private String escapeMessage(String msg) {
		if (msg == null)
			return null;

		StringBuilder sb = new StringBuilder();

		int length = msg.length();
		for (int i = 0; i < length; i++) {
			char ch = msg.charAt(i);

			switch (ch) {
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case 0x0:
				sb.append("&#00;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			default:
				sb.append(ch);
				break;
			}
		}

		return sb.toString();
	}
}
