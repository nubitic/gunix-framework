package mx.com.gunix.framework.service.hessian;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;

public class InputStreamDeserializer extends AbstractDeserializer {
	public static final InputStreamDeserializer DESER = new InputStreamDeserializer();

	public Object readObject(AbstractHessianInput in) throws IOException {
		InputStream is = in.readInputStream();
		final File tempFile = File.createTempFile("hessianIS", ".tmp");
		FileOutputStream out = new FileOutputStream(tempFile);
		IOUtils.copy(is, out);
		out.close();
		return new GunixFileInputStream(tempFile);
	}
}
