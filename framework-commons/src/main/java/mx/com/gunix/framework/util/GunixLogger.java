package mx.com.gunix.framework.util;

import java.util.function.Supplier;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class GunixLogger {
	private Logger log;

	protected GunixLogger(Class<?> clazz) {
		log = Logger.getLogger(clazz);
	}

	public void log(Level nivel, Supplier<String> mensajeSupplier, Supplier<Throwable> throwableSupplier) {
		if (log.isEnabledFor(nivel)) {
			log.log(nivel, mensajeSupplier != null ? mensajeSupplier.get() : null, throwableSupplier != null ? throwableSupplier.get() : null);
		}
	}

	public static GunixLogger getLogger(Class<?> clazz) {
		return new GunixLogger(clazz);
	}
}
