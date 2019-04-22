package mx.com.gunix.framework.service.hessian;

import java.io.InputStream;

import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.FieldDeserializer2Factory;
import com.caucho.hessian.io.JavaDeserializer;
import com.caucho.hessian.io.UnsafeDeserializer;
import com.caucho.hessian.io.UnsafeSerializer;

public class SerializerFactory extends com.caucho.hessian.io.SerializerFactory {
	private boolean _isEnableUnsafeSerializer = (UnsafeSerializer.isEnabled() && UnsafeDeserializer.isEnabled());
	FieldDeserializer2Factory fieldFactory = FieldDeserializer2Factory.create();
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Deserializer getDefaultDeserializer(Class cl) {
		if (InputStream.class.equals(cl))
			return InputStreamDeserializer.DESER;

		if (_isEnableUnsafeSerializer) {
			return new UnsafeDeserializer(cl, fieldFactory);
		} else
			return new JavaDeserializer(cl, fieldFactory);
	}

}
