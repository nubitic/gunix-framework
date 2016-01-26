package mx.com.gunix.framework.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public final class EmbeddedPostgreSQLManager {
	private static Logger log = Logger.getLogger(EmbeddedPostgreSQLManager.class);
	private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	
	public static void start(String pgsqlHome, String usuario, String password, String database, ClassLoader classLoader) {
		log.warn("Iniciando servidor local PostgreSQL (no se recomienda el uso de este servidor para un ambiente diferente a DESARROLLO)");
		// Validando si en el home indicado ya existe una instalación de postgreSQL
		File pgsqlHomeFile = null;
		String os = null;
		String esMXLocale=null;

		pgsqlHomeFile = new File(pgsqlHome);

		if (pgsqlHomeFile.exists()) {
			if (!pgsqlHomeFile.isDirectory()) {
				throw new IllegalArgumentException(String.format("'%s' no es un directorio.", pgsqlHome));
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
			esMXLocale="es-MX";
		} else {
			if (os.indexOf("mac") >= 0) {
				esMXLocale="es_ES.UTF-8";
				osType = "mac";
			} else {
				if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 || os.indexOf("sunos") >= 0) {
					esMXLocale="es_MX.utf8";
					arch = System.getProperty("os.arch");
					arch = arch.endsWith("64") ? "64" : "32";
					osType = "*nix";
				}
			}
		}
		
		File realPgsqlHomeFile = "pgsql".equalsIgnoreCase(pgsqlHomeFile.getName()) ? pgsqlHomeFile : new File(pgsqlHomeFile, "pgsql");
		if (!realPgsqlHomeFile.exists()) {
			// Como no existe una instalación de postgresql se procede a instalarlo
			try {
				log.info("Descargando PostgreSQL");
				downloadPostgreSQLDist(pgsqlHomeFile, osType, arch);
			} catch (IOException e) {
				throw new RuntimeException("No fue posible descargar la distribución de PostgreSQL", e);
			}
		}
		pgsqlHomeFile = realPgsqlHomeFile;

		File dataDir = new File(pgsqlHomeFile, "data");
		File passwordFile = null;
		try {
			passwordFile = File.createTempFile("postgreSQLPass", null);
			FileWriter passwordWriter = new FileWriter(passwordFile);
			passwordWriter.write(password);
			passwordWriter.close();
		} catch (IOException e) {
			throw new RuntimeException("No fue posible crear el archivo para la contraseña");
		}
		boolean isNewServer = false;
		if (!dataDir.exists() || !(new File(dataDir, "postgresql.conf").exists())) {
			log.info("Instalando PostgreSQL");
			// Si no existe un servidor configurado, se procede a inicializar
			// uno
			dataDir.mkdir();
			initServer(pgsqlHomeFile, usuario, passwordFile.getAbsolutePath(), database, dataDir.getAbsolutePath(), esMXLocale);
			isNewServer = true;
		}

		if (!started(pgsqlHomeFile, usuario, passwordFile.getAbsolutePath())) {
			log.info("Inicializando Servidor");
			// Iniciando el servidor
			start(pgsqlHomeFile, dataDir.getAbsolutePath(), usuario, passwordFile.getAbsolutePath());
		}
		if (isNewServer) {
			log.info("Creando Base de Datos");
			crearBD(pgsqlHomeFile, usuario, passwordFile.getAbsolutePath(), database);
		}

		if (!bdInicializada(pgsqlHomeFile, usuario, passwordFile.getAbsolutePath(), database)) {
			log.info("Creando Tablas, Secuencias...");
			initDB(pgsqlHomeFile, usuario, passwordFile.getAbsolutePath(), database, classLoader);
		}
		
		ejecutaAppScripts(pgsqlHomeFile, usuario, passwordFile.getAbsolutePath(), database);

		passwordFile.delete();

		log.info("PostgreSQL iniciado!");
		log.info("host: localhost");
		log.info("puerto: 5432");
		log.info("usuario: " + usuario);
		log.info("contraseña: " + password);
		log.info("base de datos: " + database);
	}

	private static void ejecutaAppScripts(File pgsqlHomeFile, String usuario, String password, String database) {
		try {
			Resource[] appScriptsResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/domain/persistence/scripts/**/*.sql");
			if (appScriptsResources != null) {
				for (Resource appScriptResource : appScriptsResources) {
					ejecutaScript(appScriptResource.getInputStream(), pgsqlHomeFile, usuario, password, database);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void initDB(File pgsqlHomeFile, String usuario, String password, String database, ClassLoader classLoader) {
		ejecutaScript(classLoader.getResourceAsStream("/mx/com/gunix/framework/persistence/01_SEGURIDAD_ACL_ROL_FUNCION.sql"), pgsqlHomeFile, usuario, password, database);
		ejecutaScript(classLoader.getResourceAsStream("/mx/com/gunix/framework/persistence/02_ACTIVITI.sql"), pgsqlHomeFile, usuario, password, database);
		ejecutaScript(classLoader.getResourceAsStream("/mx/com/gunix/framework/persistence/03_ADMON_SEG.sql"), pgsqlHomeFile, usuario, password, database);
	}

	private static void ejecutaScript(InputStream scriptStream, File pgsqlHomeFile, String usuario, String password, String database) {
		try {
			File scriptFile = File.createTempFile("script", ".sql");

			IOUtils.copy(scriptStream, new FileOutputStream(scriptFile));

			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(pgsqlHomeFile, "psql"));
			cmd.add("-e");
			cmd.add("-f");
			cmd.add(scriptFile.getAbsolutePath());
			cmd.add("-U");
			cmd.add(usuario);
			cmd.add("-d");
			cmd.add(database);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			final Process process = processBuilder.start();
			log(process);
			process.waitFor(5, TimeUnit.SECONDS);
			scriptFile.delete();
			if (process.exitValue() != 0) {
				log(process);
				throw new RuntimeException("No fue posible ejecuar el script ");
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible verificar si la base de datos ya se está ejecutando", e);
		}
	}

	private static boolean bdInicializada(File pgsqlHomeFile, String usuario, String absolutePath, String database) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(pgsqlHomeFile, "psql"));
			cmd.add("-c");
			cmd.add("select count(*) as existe_seguridad from information_schema.schemata where schema_name='seguridad';");
			cmd.add("-U");
			cmd.add(usuario);
			cmd.add("-d");
			cmd.add(database);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			final Process process = processBuilder.start();
			process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String readedLine = null;
			while ((readedLine = reader.readLine()) != null) {
				// Si la línea actual es la columna existe_seguridad, leo dos posiciones más para determinar si el esquema existe o no
				if (readedLine.contains("existe_seguridad") && (readedLine = reader.readLine()) != null && "1".equals(readedLine = reader.readLine().trim())) {
					reader.close();
					return true;
				}
			}
			return false;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible verificar si la base de datos ya se está ejecutando", e);
		}
	}

	private static void initServer(File pgsqlHomeFile, String usuario, String password, String database, String dataDir, String esMXLocale) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(pgsqlHomeFile, "initdb"));
			cmd.add("-U");
			cmd.add(usuario);
			cmd.add("-D");
			cmd.add(dataDir);
			cmd.add("-E");
			cmd.add("UTF8");
			cmd.add("--locale");
			cmd.add(esMXLocale);
			cmd.add("--pwfile");
			cmd.add(password);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			process.waitFor();
			log(process);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No se pudo inicializar el servidor de base de datos", e);
		}
	}

	private static void crearBD(File pgsqlHomeFile, String usuario, String password, String database) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(pgsqlHomeFile, "createdb"));
			cmd.add("-U");
			cmd.add(usuario);
			cmd.add(database);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			process.waitFor();
			log(process);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible iniciar la base de datos", e);
		}

	}

	private static String getCommandPath(File pgsqlHomeFile, String command) {
		return new File(new File(pgsqlHomeFile, "bin"), command).getAbsolutePath();
	}

	private static void start(File pgsqlHomeFile, String dataDir, String usuario, String contraseña) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(pgsqlHomeFile, "postgres"));

			cmd.add("-D");
			cmd.add(dataDir);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			if (process.isAlive()) {
				while (!started(pgsqlHomeFile, usuario, contraseña)) {
					Thread.sleep(3000);
				}
			} else {
				log(process);
				if (!started(pgsqlHomeFile, usuario, contraseña)) {
					throw new RuntimeException("No fue posible iniciar la base de datos");
				}
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible iniciar la base de datos", e);
		}

	}

	private static boolean started(File pgsqlHomeFile, String usuario, String contraseña) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(pgsqlHomeFile, "psql"));
			cmd.add("-l");
			cmd.add("-U");
			cmd.add(usuario);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			final Process process = processBuilder.start();
			process.waitFor();
			log(process);
			return process.exitValue() == 0;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible verificar si la base de datos ya se está ejecutando", e);
		}
	}

	private static void log(Process process) throws IOException {
		if (log.isInfoEnabled()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String linea = null;
			while ((linea = reader.readLine()) != null)
				log.info(linea);
			reader.close();
		}
	}

	private static File descargaArchivo(String fileURL, String tempFilePrefix, String tempFileSufix) throws IOException {

		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		int responseCode = httpConn.getResponseCode();
		File downloadedFile = File.createTempFile(tempFilePrefix, tempFileSufix);

		// always check HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String fileName = "";
			String contentType = httpConn.getContentType();
			long contentLength = httpConn.getContentLengthLong();
			fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());

			log.info("Content-Type = " + contentType);
			log.info("Content-Length = " + contentLength);
			log.info("fileName = " + fileName);

			// opens input stream from the HTTP connection
			InputStream inputStream = httpConn.getInputStream();

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(downloadedFile);

			int bytesRead = -1;
			byte[] buffer = new byte[8192];
			int steps = 16;
			long totalBytesReaded = 0;
			int avanceAnterior = -1;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				totalBytesReaded += bytesRead;
				outputStream.write(buffer, 0, bytesRead);
				if (log.isInfoEnabled()) {
					int avance = (int) ((totalBytesReaded * steps) / contentLength);
					if (avance > avanceAnterior && (avance % 4) == 0) {
						StringBuilder progress = new StringBuilder("Progreso: |");
						for (int i = 0; i < steps; i++) {
							if (avance > i) {
								progress.append("=");
							} else {
								progress.append(" ");
							}
						}
						progress.append("|\r");
						System.out.print(progress.toString());
						avanceAnterior = avance;
					}
				}
			}
			outputStream.close();
			inputStream.close();

			log.info("Archivo Descargado");
		} else {
			throw new RuntimeException("No fue posible descargar el archivo, codigo HTTP:" + responseCode);
		}
		httpConn.disconnect();
		return downloadedFile;
	}

	private static void downloadPostgreSQLDist(File pgsqlHomeFile, String osType, String arch) throws IOException {
		String fileURL = "http://get.enterprisedb.com/postgresql/postgresql-9.4.5-1-"
				+ ("win".equals(osType) ? "windows" + ("64".equals(arch) ? "-x64" : "") : "mac".equals(osType) ? "osx" : "*nix".equals(osType) ? "linux" + ("64".equals(arch) ? "-x64" : "") : "")
				+ "-binaries." + ("*nix".equals(osType) ? "tar.gz" : "zip");

		String postgreSQLAbsPath = pgsqlHomeFile.getAbsolutePath() + File.separator;
		File downloadedFile = descargaArchivo(fileURL, "postgreSQLDist", null);
		if ("*nix".equals(osType) || "mac".equals(osType)) {
			extractTar(downloadedFile, postgreSQLAbsPath);
		} else {
			extractZip(downloadedFile, postgreSQLAbsPath);
		}

		downloadedFile.delete();

		if ("win".equals(osType)) {
			fileURL = "https://download.microsoft.com/download/B/0/1/B0105F32-5B05-41D0-81A1-DD87A5A05D00/vcredist_x" + ("64".equals(arch) ? "64" : "86") + ".exe";
			downloadedFile = descargaArchivo(fileURL, "vcredist", ".exe");
			log.info("Instalando Visual C++ Redistributable Package");
			try {
				List<String> cmd = new ArrayList<String>();
				cmd.add(downloadedFile.getAbsolutePath());
				cmd.add("/install");
				cmd.add("/passive");
				cmd.add("/norestart");
				ProcessBuilder processBuilder = new ProcessBuilder(cmd);
				final Process process = processBuilder.start();
				while (!process.waitFor(10, TimeUnit.SECONDS))
					;

			} catch (Exception e) {
				throw new RuntimeException("No fue posible instalar las librerías de Visual Studio C++ necesarias para ejecutar postgreSQL", e);
			}
		}
	}

	private static void extractZip(File downloadedFile, String postgreSQLAbsPath) throws IOException {
		log.info("Extrayendo: " + downloadedFile.getName());
		// get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(downloadedFile));
		// get the zipped file list entry
		ZipEntry ze = zis.getNextEntry();
		byte[] buffer = new byte[4096];
		while (ze != null) {

			String fileName = ze.getName();
			File newFile = new File(postgreSQLAbsPath + fileName);

			if (ze.isDirectory()) {
				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				newFile.mkdirs();
			} else {
				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
			}
			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();

		log.info("extracción completada con éxito!!");
	}

	private static void extractTar(File downloadedFile, String postgreSQLAbsPath) throws IOException {
		log.info("Extrayendo: " + downloadedFile.getName());
		
		new File(postgreSQLAbsPath).mkdirs();

		List<String> cmd = new ArrayList<String>();
		
		cmd.add("tar");
		cmd.add("-zxvf");
		cmd.add(downloadedFile.getAbsolutePath());
		cmd.add("-C");
		cmd.add(postgreSQLAbsPath);

		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
		processBuilder.redirectErrorStream(true);
		final Process process = processBuilder.start();
		try {
			log(process);
			process.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		log.info("extracción completada con éxito!!");
	}
}
