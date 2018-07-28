package mx.com.gunix.framework.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.hunteron.core.Context;

import mx.com.gunix.framework.util.EmbeddedServerUtils;
import mx.com.gunix.framework.util.ZipEntryWorker;

public final class EmbeddedPostgreSQLManager {
	private static Logger log = Logger.getLogger(EmbeddedPostgreSQLManager.class);
	private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	
	public static void start(String pgsqlHome, String usuario, String password, String database, String puerto, ClassLoader classLoader) {
		log.warn("Iniciando servidor local PostgreSQL (no se recomienda el uso de este servidor para un ambiente diferente a DESARROLLO)");
		// Validando si en el home indicado ya existe una instalación de postgreSQL
		File pgsqlHomeFile = null;
		String os = null;
		String esMXLocale=null;
		
		if (puerto == null || "".equals(puerto)) {
			puerto = "5432";
		}

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

		if (!started(pgsqlHomeFile, usuario, passwordFile.getAbsolutePath(), puerto)) {
			log.info("Inicializando Servidor");
			// Iniciando el servidor
			start(pgsqlHomeFile, dataDir.getAbsolutePath(), usuario, passwordFile.getAbsolutePath(), puerto);
		}
		if (isNewServer) {
			log.info("Creando Base de Datos");
			crearBD(pgsqlHomeFile, usuario, passwordFile.getAbsolutePath(), database, puerto);
		}

		if (!EmbeddedServerUtils.existeEsquema(pgsqlHomeFile, usuario, database, puerto,"seguridad")) {
			log.info("Creando Tablas, Secuencias...");
			initDB(pgsqlHomeFile, usuario, database, puerto, classLoader);
		}
		
		ejecutaAppScripts(pgsqlHomeFile, usuario, database, puerto);

		passwordFile.delete();

		log.info("PostgreSQL iniciado!");
		log.info("host: localhost");
		log.info("puerto: " + puerto);
		log.info("usuario: " + usuario);
		log.info("contraseña: " + password);
		log.info("base de datos: " + database);
	}

	private static void ejecutaAppScripts(File pgsqlHomeFile, String usuario, String database, String puerto) {
		try {			
			Resource[] appScriptsResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/domain/persistence/scripts/**/*.sql");
			Resource[] appZippedScriptsResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/domain/persistence/scripts/**/*.zip");
			Resource[] unifiedScriptResources = new Resource[(appScriptsResources != null ? appScriptsResources.length : 0) + 
			                                                 (appZippedScriptsResources != null ? appZippedScriptsResources.length : 0)];
			
			if (appScriptsResources != null) {
				System.arraycopy(appScriptsResources, 0, unifiedScriptResources, 0, appScriptsResources.length);
			}
			if (appZippedScriptsResources != null) {
				System.arraycopy(appZippedScriptsResources, 0, unifiedScriptResources, (appScriptsResources != null ? appScriptsResources.length : 0), appZippedScriptsResources.length);
			}

			
			appScriptsResources = unifiedScriptResources;
			if (appScriptsResources != null) {
				List<Resource> appScriptsResourcesList = new ArrayList<Resource>(Arrays.asList(appScriptsResources));
				Collections.sort(appScriptsResourcesList, Comparator.comparing(Resource::getFilename));
				
				//Los scripts para las funciones se agregan al final para que siempre se ejecuten al último, planchanco cualquier versión definida previamente en los SQLs normales.
				Resource[] appFunctionScriptsResources = resourcePatternResolver.getResources("classpath*:/mx/com/gunix/domain/persistence/db_functions/**/*.sql");
				if (appFunctionScriptsResources != null) {
					appScriptsResourcesList.addAll(Arrays.asList(appFunctionScriptsResources));
				}
				
				List<Resource> currentAppScriptsResourcesList = new ArrayList<Resource>();
				String appSchemaName = Context.DB_APP_SCHEMA.get().toUpperCase();
				
				File prevExecutedScripts = new File(pgsqlHomeFile, "gunixPrevExecutedScripts");
				if (!prevExecutedScripts.exists()) {
					prevExecutedScripts.mkdirs();
					currentAppScriptsResourcesList=appScriptsResourcesList;
				} else {
					File[] executedScripts = prevExecutedScripts.listFiles();
					if (executedScripts != null) {
						
						Iterator<Resource> appScriptIt = appScriptsResourcesList.iterator();
						while (appScriptIt.hasNext()) {
							boolean found = false;
							Resource currRes = appScriptIt.next();

							String possiblyExecutedScript = appSchemaName + "_" + currRes.getFilename() + ".log";
							for (File executedScript : executedScripts) {
								if (possiblyExecutedScript.equals(executedScript.getName())) {
									if (!(currRes.getURL().getPath().contains("db_functions") && currRes.getFilename().startsWith("FUN_"))) {
										found = true;
									} else {
										executedScript.delete();
									}
									break;
								}
							}
							if (!found) {
								currentAppScriptsResourcesList.add(currRes);
							}
						}
					}
				}
				
				for (Resource appScriptResource : currentAppScriptsResourcesList) {
					log.info("<<<<<<<<<<<<<<<<<<< "+ appScriptResource.getFilename() +" >>>>>>>>>>>>>>>>>>>");
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(prevExecutedScripts, appSchemaName + "_" + appScriptResource.getFilename() + ".log"), false));
					if (appScriptResource.getFilename().toLowerCase().endsWith(".zip")) {
						ZipEntryWorker.withAllZipEntries(appScriptResource.getInputStream(), (sqlFileName, sqlIs)->{
							ejecutaScript(sqlIs, pgsqlHomeFile, usuario, database, puerto, appScriptResource.getFilename(), writer);
						});
					} else {
						ejecutaScript(appScriptResource.getInputStream(), pgsqlHomeFile, usuario, database, puerto, appScriptResource.getFilename(), writer);
					}
					writer.close();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void ejecutaScript(InputStream scriptIS, File pgsqlHomeFile, String usuario, String database, String puerto, String fileName, BufferedWriter writer) {
		EmbeddedServerUtils.ejecutaScript(scriptIS, pgsqlHomeFile, usuario, database, puerto, new Logger(fileName) {
			@Override
			public void info(Object message) {
				try {
					writer.write(message.toString());
					writer.newLine();
					log.info(message);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		});
	}

	private static void initDB(File pgsqlHomeFile, String usuario, String database, String puerto, ClassLoader classLoader) {
		EmbeddedServerUtils.ejecutaScript(classLoader.getResourceAsStream("/mx/com/gunix/framework/persistence/01_SEGURIDAD_ACL_ROL_FUNCION.sql"), pgsqlHomeFile, usuario, database, puerto, log);
		EmbeddedServerUtils.ejecutaScript(classLoader.getResourceAsStream("/mx/com/gunix/framework/persistence/02_ACTIVITI.sql"), pgsqlHomeFile, usuario, database, puerto, log);
		EmbeddedServerUtils.ejecutaScript(classLoader.getResourceAsStream("/mx/com/gunix/framework/persistence/03_ADMON_SEG.sql"), pgsqlHomeFile, usuario, database, puerto, log);
		EmbeddedServerUtils.ejecutaScript(classLoader.getResourceAsStream("/mx/com/gunix/framework/persistence/04_JOB_TOKENIZER.sql"), pgsqlHomeFile, usuario, database, puerto, log);

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
			EmbeddedServerUtils.log(log, process, null);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No se pudo inicializar el servidor de base de datos", e);
		}
	}

	private static void crearBD(File pgsqlHomeFile, String usuario, String password, String database, String port) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(pgsqlHomeFile, "createdb"));
			cmd.add("-p");
			cmd.add(port);
			cmd.add("-U");
			cmd.add(usuario);
			cmd.add(database);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			process.waitFor();
			EmbeddedServerUtils.log(log, process, null);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible iniciar la base de datos", e);
		}

	}

	private static String getCommandPath(File pgsqlHomeFile, String command) {
		return EmbeddedServerUtils.getCommandPath(new File(pgsqlHomeFile, "bin"), command);
	}

	private static void start(File pgsqlHomeFile, String dataDir, String usuario, String contraseña, String puerto) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(pgsqlHomeFile, "postgres"));

			cmd.add("-D");
			cmd.add(dataDir);

			cmd.add("-p");
			cmd.add(puerto);
			
			cmd.add("-c");
			cmd.add("checkpoint_segments=2025");
			cmd.add("-c");
			cmd.add("checkpoint_timeout=600");
			
			cmd.add("-c");
			cmd.add("log_destination=stderr");
			cmd.add("-c");
			cmd.add("logging_collector=on");
			cmd.add("-c");
			cmd.add("log_directory=pg_log");
			cmd.add("-c");
			cmd.add("log_filename=postgresql.log");
			cmd.add("-c");
			cmd.add("log_truncate_on_rotation=on");
			cmd.add("-c");
			cmd.add("log_rotation_age=1440"); //24 Hrs
			cmd.add("-c");
			cmd.add("log_rotation_size=10240"); //10 MB
			cmd.add("-c");
			cmd.add("log_min_duration_statement=3000");
			cmd.add("-c");
			cmd.add("log_min_messages=NOTICE");
			cmd.add("-c");
			cmd.add("log_line_prefix=[%t][%a-%i|%p:%l]");

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			
			EmbeddedServerUtils.log(log, process, "pg_log");
			
			if (process.isAlive()) {
				while (!started(pgsqlHomeFile, usuario, contraseña, puerto)) {
					Thread.sleep(3000);
				}
			} else {
				if (!started(pgsqlHomeFile, usuario, contraseña, puerto)) {
					throw new RuntimeException("No fue posible iniciar la base de datos");
				}
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible iniciar la base de datos", e);
		}

	}

	private static boolean started(File pgsqlHomeFile, String usuario, String contraseña, String port) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(pgsqlHomeFile, "psql"));
			cmd.add("-l");
			cmd.add("-U");
			cmd.add(usuario);
			cmd.add("-p");
			cmd.add(port);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			final Process process = processBuilder.start();
			process.waitFor();
			EmbeddedServerUtils.log(log, process, null);
			return process.exitValue() == 0;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible verificar si la base de datos ya se está ejecutando", e);
		}
	}

	private static void downloadPostgreSQLDist(File pgsqlHomeFile, String osType, String arch) throws IOException {
		String fileURL = "http://get.enterprisedb.com/postgresql/postgresql-9.4.5-1-"
				+ ("win".equals(osType) ? "windows" + ("64".equals(arch) ? "-x64" : "") : "mac".equals(osType) ? "osx" : "*nix".equals(osType) ? "linux" + ("64".equals(arch) ? "-x64" : "") : "")
				+ "-binaries." + ("*nix".equals(osType) ? "tar.gz" : "zip");

		String postgreSQLAbsPath = pgsqlHomeFile.getAbsolutePath() + File.separator;
		File downloadedFile = EmbeddedServerUtils.descargaArchivo(fileURL, "postgreSQLDist", null, log);
		if ("*nix".equals(osType) || "mac".equals(osType)) {
			EmbeddedServerUtils.extractTar(downloadedFile, postgreSQLAbsPath, log);
		} else {
			EmbeddedServerUtils.extractZip(downloadedFile, postgreSQLAbsPath, log);
		}

		downloadedFile.delete();

		if ("win".equals(osType)) {
			fileURL = "https://download.microsoft.com/download/B/0/1/B0105F32-5B05-41D0-81A1-DD87A5A05D00/vcredist_x" + ("64".equals(arch) ? "64" : "86") + ".exe";
			downloadedFile = EmbeddedServerUtils.descargaArchivo(fileURL, "vcredist", ".exe", log);
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



}
