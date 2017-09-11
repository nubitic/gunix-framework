package mx.com.gunix.framework.documents;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mx.com.gunix.framework.util.EmbeddedServerUtils;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;

public final class EmbeddedLogicalDocManager {
	private static Logger log = Logger.getLogger(EmbeddedLogicalDocManager.class);

	public static void start(String logicalDocHome, ClassLoader classLoader) throws InterruptedException, IOException {
		log.warn("LogicalDoc local");

		File logicalDocHomeFile = new File(logicalDocHome);

		File pgsqlHomeFile = new File(System.getenv("DB_EMBEDDED_HOME"));
		pgsqlHomeFile = "pgsql".equalsIgnoreCase(pgsqlHomeFile.getName()) ? pgsqlHomeFile : new File(pgsqlHomeFile, "pgsql");
		String usuario = System.getenv("DB_USER");
		String baseDatos = System.getenv("DB_NAME");
		String puerto = System.getenv("DB_PORT");
		if (puerto == null || "".equals(puerto)) {
			puerto = "5432";
		}

		if (logicalDocHomeFile.exists()) {
			if (!logicalDocHomeFile.isDirectory()) {
				throw new IllegalArgumentException(String.format("'%s' no es un directorio.", logicalDocHome));
			} 
			if (!EmbeddedServerUtils.existeEsquema(pgsqlHomeFile, usuario, baseDatos, puerto, "logicaldoc")) {
				//Puede ser que el directorio exista pero el esquema de bd no si se ha eliminado la base de datos para iniciar con una nueva
				EmbeddedServerUtils.ejecutaScript(classLoader.getResourceAsStream("/mx/com/gunix/framework/documents/persistence/logicaldoc-core.sql"), pgsqlHomeFile, usuario, baseDatos, puerto, log);
			}
		} else {
			log.info("Instalando LogicalDoc");
			logicalDocHomeFile.mkdirs();
			doInstall(logicalDocHomeFile, pgsqlHomeFile, usuario, baseDatos, puerto, classLoader);
		}
		log.info("Iniciando LogicalDoc");
		doStart(logicalDocHomeFile, false);
		log.info("LogicalDoc iniciado en: " + getLogicalDocURL());
		log.info("Usuario: admin");
		log.info("Pass: admin");
	}

	private static void doInstall(File logicalDocHomeFile, File pgsqlHomeFile, String usuario, String baseDatos, String puerto, ClassLoader classLoader) throws IOException, InterruptedException {
		File webAppRunner = new File(logicalDocHomeFile, "webapp-runner.jar");
		if (!webAppRunner.exists()) {
			webAppRunner = EmbeddedServerUtils.descargaArchivo("http://central.maven.org/maven2/com/github/jsimone/webapp-runner/7.0.57.2/webapp-runner-7.0.57.2.jar", webAppRunner, log);
			webAppRunner.setExecutable(true);
		}

		File logicalDocDir = new File(logicalDocHomeFile + File.separator + "logicalDoc");
		if (!logicalDocDir.exists()) {
			File logicalDocWar = EmbeddedServerUtils.descargaArchivo("https://svwh.dl.sourceforge.net/project/logicaldoc/distribution/LogicalDOC%20CE%207.4/logicaldoc-webapp-7.4.2.war", "logicalDoc", ".war", log);
			EmbeddedServerUtils.extractZip(logicalDocWar, logicalDocHomeFile + File.separator + "logicalDoc" + File.separator, log);
		}
		
		EmbeddedServerUtils.ejecutaScript(classLoader.getResourceAsStream("/mx/com/gunix/framework/documents/persistence/logicaldoc-core.sql"), pgsqlHomeFile, usuario, baseDatos, puerto, log);

		String webInfClasses = "WEB-INF" + File.separator + "classes" + File.separator;

		File contextProps = new File(logicalDocDir, webInfClasses + "context.properties");
		File contextXml = new File(logicalDocDir, webInfClasses + "context.xml");

		contextProps.delete();
		contextXml.delete();

		BufferedReader contextPropsReader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream("/mx/com/gunix/framework/documents/templates/contextProps")));

		String linea = null;
		BufferedWriter bfWrt = new BufferedWriter(new FileWriter(contextProps));
		while ((linea = contextPropsReader.readLine()) != null) {
			bfWrt.write(linea);
			bfWrt.newLine();
		}
		contextPropsReader.close();
		bfWrt.close();

		bfWrt = new BufferedWriter(new FileWriter(contextProps, true));

		File repoHomeF = new File(logicalDocHomeFile, "repository" + File.separator);
		repoHomeF.mkdirs();
		String repoHome = repoHomeF.getAbsolutePath();

		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
			repoHome = repoHome.replaceAll("\\\\", "/");
		}

		bfWrt.write("conf.dbdir=" + repoHome + "\\\\db\\\\");
		bfWrt.newLine();
		bfWrt.write("conf.exportdir=" + repoHome + "/impex/out/");
		bfWrt.newLine();
		bfWrt.write("conf.importdir=" + repoHome + "/impex/in/");
		bfWrt.newLine();
		bfWrt.write("conf.logdir=" + repoHome + "/logs/");
		bfWrt.newLine();
		bfWrt.write("conf.plugindir=" + repoHome + "/plugins/");
		bfWrt.newLine();
		bfWrt.write("conf.userdir=" + repoHome + "/users/");
		bfWrt.newLine();
		bfWrt.write("hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect");
		bfWrt.newLine();
		bfWrt.write("id=8a0306d0-9d10-486d-a9db-7808d81af53a");
		bfWrt.newLine();
		bfWrt.write("index.dir=" + repoHome + "/index/");
		bfWrt.newLine();
		bfWrt.write("jdbc.dbms=postgres");
		bfWrt.newLine();
		bfWrt.write("jdbc.driver=org.postgresql.Driver");
		bfWrt.newLine();
		bfWrt.write("jdbc.password=" + System.getenv("DB_PASSWORD"));
		bfWrt.newLine();
		bfWrt.write("jdbc.url=jdbc\\:postgresql\\://localhost:" + puerto + "/" + System.getenv("DB_NAME") + "?currentSchema\\=logicaldoc");
		bfWrt.newLine();
		bfWrt.write("jdbc.username=" + System.getenv("DB_USER"));
		bfWrt.newLine();
		bfWrt.write("jdbc.validationQuery=SELECT 1");
		bfWrt.newLine();
		bfWrt.write("store.1.dir=" + repoHome + "/docs/");
		bfWrt.newLine();
		bfWrt.close();

		OutputStream os = new FileOutputStream(contextXml);
		IOUtils.copy(classLoader.getResourceAsStream("/mx/com/gunix/framework/documents/templates/contextXml"), os);
		os.close();

		doStart(logicalDocHomeFile, true);
	}

	private static void doStart(File logicalDocHomeFile, boolean forRestart) throws InterruptedException, IOException {
		List<String> cmd = new ArrayList<String>();
		cmd.add("java");
		cmd.add("-jar");
		cmd.add(logicalDocHomeFile.getAbsolutePath() + File.separator + "webapp-runner.jar");

		cmd.add("--port");
		cmd.add((System.getenv("LOGICALDOC_PORT") != null ? System.getenv("LOGICALDOC_PORT") : "7080"));
		cmd.add("--temp-directory");
		cmd.add(logicalDocHomeFile.getAbsolutePath() + File.separator + "tomcat");
		cmd.add("--path");
		cmd.add(System.getenv("LOGICALDOC_CONTEXT") != null ? System.getenv("LOGICALDOC_CONTEXT") : "/logicaldoc");

		cmd.add(logicalDocHomeFile.getAbsolutePath() + File.separator + "logicalDoc");

		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
		processBuilder.redirectErrorStream(true);
		final Process process = processBuilder.start();
		EmbeddedServerUtils.log(log, process, "Starting ProtocolHandler");
		process.waitFor(5, TimeUnit.SECONDS);

		try {
			if (process.exitValue() != 0) {
				EmbeddedServerUtils.log(log, process, null);
				throw new RuntimeException("No fue posible iniciar LogicalDoc");
			}
		} catch (IllegalThreadStateException ignorar) {}// Still alive!

		if (forRestart) {
			process.destroy();
		}
	}

	public static String getLogicalDocURL() {
		return (System.getenv("LOGICALDOC_HOSTNAME") != null ? System.getenv("LOGICALDOC_HOSTNAME") : "http://localhost:") + (System.getenv("LOGICALDOC_PORT") != null ? System.getenv("LOGICALDOC_PORT") : "7080") + (System.getenv("LOGICALDOC_CONTEXT") != null ? System.getenv("LOGICALDOC_CONTEXT") : "/logicaldoc");
	}
}
