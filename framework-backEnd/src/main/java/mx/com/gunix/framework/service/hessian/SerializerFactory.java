package mx.com.gunix.framework.service.hessian;

import java.io.InputStream;

import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.JavaDeserializer;
import com.caucho.hessian.io.UnsafeDeserializer;
import com.caucho.hessian.io.UnsafeSerializer;

public class SerializerFactory extends com.caucho.hessian.io.SerializerFactory {
	private boolean _isEnableUnsafeSerializer = (UnsafeSerializer.isEnabled() && UnsafeDeserializer.isEnabled());

	@SuppressWarnings("rawtypes")
	@Override
	protected Deserializer getDefaultDeserializer(Class cl) {
		if (InputStream.class.equals(cl))
			return InputStreamDeserializer.DESER;

		if (_isEnableUnsafeSerializer) {
			return new UnsafeDeserializer(cl);
		} else
			return new JavaDeserializer(cl);
	}

}
