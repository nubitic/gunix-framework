package mx.com.gunix.framework.service.hessian;

import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.util.Assert;

public class HessianServiceExporter extends org.springframework.remoting.caucho.HessianServiceExporter {
	private HessianSkeleton skeleton;

	public HessianServiceExporter() {
		super();
		this.setSerializerFactory(new SerializerFactory());
	}

	@Override
	public void prepare() {
		checkService();
		checkServiceInterface();
		this.skeleton = new HessianSkeleton(getProxyForService(), getServiceInterface());
	}

	@Override
	public void invoke(InputStream inputStream, OutputStream outputStream) throws Throwable {
		Assert.notNull(this.skeleton, "Hessian exporter has not been initialized");
		doInvoke(this.skeleton, inputStream, outputStream);
	}
}
