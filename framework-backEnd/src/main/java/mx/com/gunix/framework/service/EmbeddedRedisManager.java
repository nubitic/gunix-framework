package mx.com.gunix.framework.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mx.com.gunix.framework.persistence.EmbeddedPostgreSQLManager;

import org.apache.log4j.Logger;

public final class EmbeddedRedisManager {
	private static Logger log = Logger.getLogger(EmbeddedRedisManager.class);

	public static void start(String redisHome) {
		log.warn("Iniciando servidor local Redis (no se recomienda el uso de este servidor para un ambiente diferente a DESARROLLO)");
		File redisHomeFile = null;
		String os = null;

		redisHomeFile = new File(redisHome);

		if (redisHomeFile.exists()) {
			if (!redisHomeFile.isDirectory()) {
				throw new IllegalArgumentException(String.format("'%s' no es un directorio.", redisHomeFile));
			}
		}

		os = System.getProperty("os.name").toLowerCase();
		String arch = null;
		String osType = null;
		if (os.indexOf("win") >= 0) {
			arch = System.getenv("PROCESSOR_ARCHITECTURE");
			String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
			arch = arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "64" : "32";
			osType = "win";
		} else {
			if (os.indexOf("mac") >= 0) {
				osType = "mac";
			} else {
				if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 || os.indexOf("sunos") >= 0) {
					arch = System.getProperty("os.arch");
					arch = arch.endsWith("64") ? "64" : "32";
					osType = "*nix";
				}
			}
		}

		if (!redisHomeFile.exists()) {
			// Como no existe una instalación de redis se procede a instalarlo
			try {
				log.info("Descargando Redis");
				redisHomeFile.mkdirs();
				downloadRedisDist(redisHomeFile, osType, arch);
			} catch (IOException e) {
				throw new RuntimeException("No fue posible descargar la distribución de Redis", e);
			}
		}

		if (!started(redisHomeFile)) {
			log.info("Inicializando Servidor");
			// Iniciando el servidor
			start(redisHomeFile);
		}
		log.info("Redis iniciado!");
		log.info("host: localhost");
		log.info("puerto: 6379");
	}

	private static void start(File redisHomeFile) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(redisHomeFile, "redis-server"));
			cmd.add("--timeout");
			cmd.add("120");

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			if (process.isAlive()) {
				while (!started(redisHomeFile)) {
					Thread.sleep(3000);
				}
			} else {
				EmbeddedPostgreSQLManager.log(process);
				if (!started(redisHomeFile)) {
					throw new RuntimeException("No fue posible iniciar Redis");
				}
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible iniciar Redis", e);
		}
	}

	private static void downloadRedisDist(File redisHomeFile, String osType, String arch) throws IOException {
		String fileURL = "win".equals(osType) ? "https://github.com/MSOpenTech/redis/releases/download/win-3.0.501/Redis-x64-3.0.501.zip" : "";

		String redisAbsPath = redisHomeFile.getAbsolutePath() + File.separator;
		File downloadedFile = EmbeddedPostgreSQLManager.descargaArchivo(fileURL, "redisDist", null);
		if ("*nix".equals(osType) || "mac".equals(osType)) {
			EmbeddedPostgreSQLManager.extractTar(downloadedFile, redisAbsPath);
		} else {
			EmbeddedPostgreSQLManager.extractZip(downloadedFile, redisAbsPath);
		}

		downloadedFile.delete();

	}

	private static boolean started(File redisHomeFile) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(redisHomeFile, "redis-cli"));
			cmd.add("--scan");
			
			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			final Process process = processBuilder.start();
			boolean ans = process.waitFor(2, TimeUnit.SECONDS);
			process.destroyForcibly();
			return ans;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible verificar si Redis ya se está ejecutando", e);
		}
	}

	private static String getCommandPath(File pgsqlHomeFile, String command) {
		return new File(pgsqlHomeFile, command).getAbsolutePath();
	}
}
