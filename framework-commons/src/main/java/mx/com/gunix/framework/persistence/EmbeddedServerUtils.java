package mx.com.gunix.framework.persistence;

import static mx.com.gunix.framework.util.EmbeddedServerUtils.getCommandPath;
import static mx.com.gunix.framework.util.EmbeddedServerUtils.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;

import com.hunteron.core.Context;

public final class EmbeddedServerUtils {
	public static void ejecutaScript(InputStream scriptStream, File pgsqlHomeFile, String usuario, String database, String puerto, Logger log) {
		try {
			File scriptFile = File.createTempFile("script", ".sql");

			IOUtils.copy(scriptStream, new FileOutputStream(scriptFile));

			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(new File(pgsqlHomeFile, "bin"), "psql"));
			cmd.add("-e");
			cmd.add("-f");
			cmd.add(scriptFile.getAbsolutePath());
			cmd.add("-U");
			cmd.add(usuario);
			cmd.add("-p");
			cmd.add(puerto);
			cmd.add("-d");
			cmd.add(database);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.environment().put("PGOPTIONS", "-c search_path=" + Context.DB_APP_SCHEMA.get());
			processBuilder.redirectErrorStream(true);
			final Process process = processBuilder.start();
			log(log, process, null);
			process.waitFor(5, TimeUnit.SECONDS);
			scriptFile.delete();
			if (process.exitValue() != 0) {
				log(log, process, null);
				throw new RuntimeException("No fue posible ejecuar el script ");
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible ejecuar el script", e);
		}
	}

	public static boolean existeEsquema(File pgsqlHomeFile, String usuario, String database, String puerto, String esquema) {
		try {
			List<String> cmd = new ArrayList<String>();
			cmd.add(getCommandPath(new File(pgsqlHomeFile, "bin"), "psql"));
			cmd.add("-c");
			cmd.add("select count(*) as existe from information_schema.schemata where schema_name='"+esquema+"';");
			cmd.add("-U");
			cmd.add(usuario);
			cmd.add("-p");
			cmd.add(puerto);
			cmd.add("-d");
			cmd.add(database);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			final Process process = processBuilder.start();
			process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String readedLine = null;
			while ((readedLine = reader.readLine()) != null) {
				// Si la línea actual es la columna existe, leo dos posiciones más para determinar si el esquema existe o no
				if (readedLine.contains("existe") && (readedLine = reader.readLine()) != null && "1".equals(readedLine = reader.readLine().trim())) {
					reader.close();
					return true;
				}
			}
			return false;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("No fue posible verificar si la base de datos ya se está ejecutando", e);
		}
	}
}
