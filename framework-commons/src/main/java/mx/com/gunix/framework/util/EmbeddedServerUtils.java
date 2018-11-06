package mx.com.gunix.framework.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

public final class EmbeddedServerUtils {
	private static Logger log = Logger.getLogger(EmbeddedServerUtils.class);
	
	static {

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { 
												new X509TrustManager() {
													public X509Certificate[] getAcceptedIssuers() {
														return null;
													}
							
													public void checkClientTrusted(X509Certificate[] certs, String authType) {
													}
							
													public void checkServerTrusted(X509Certificate[] certs, String authType) {
													}
												}
											}
			, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			log.error("No se pudo inicializar el protocolo https para descargar archivos desde cualquier servidor HTTPS...", e);
		}
	}

	public static void log(Logger log, Process process, String controlString) throws IOException, InterruptedException {
		Boolean hasControlString = controlString != null && !"".equals(controlString);
		Thread stdOutReader = new Thread(() -> {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String linea = null;
			try {
				while ((linea = reader.readLine()) != null) {
					log.info(linea);

					if (hasControlString && linea.indexOf(controlString) != -1) {
						break;
					}
				}
				reader.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		Thread stdErrReader = new Thread(() -> {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String linea = null;
			try {
				while ((linea = reader.readLine()) != null) {
					log.info(linea);

					if (hasControlString && linea.indexOf(controlString) != -1) {
						break;
					}
				}
				reader.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		stdOutReader.start();
		stdErrReader.start();

		stdOutReader.join();
		stdErrReader.join();
	}

	public static File descargaArchivo(String fileURL, File downloadedFile, Logger log) throws IOException {
		log.info("Descargando " + fileURL);
		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		int responseCode = httpConn.getResponseCode();

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

	public static File descargaArchivo(String fileURL, String tempFilePrefix, String tempFileSufix, Logger log) throws IOException {
		return descargaArchivo(fileURL, File.createTempFile(tempFilePrefix, tempFileSufix), log);
	}

	public static void extractTar(File downloadedFile, String destPath, Logger log) throws IOException {
		log.info("Extrayendo: " + downloadedFile.getName());

		new File(destPath).mkdirs();

		List<String> cmd = new ArrayList<String>();

		cmd.add("tar");
		cmd.add("-zxvf");
		cmd.add(downloadedFile.getAbsolutePath());
		cmd.add("-C");
		cmd.add(destPath);

		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
		processBuilder.redirectErrorStream(true);
		final Process process = processBuilder.start();
		try {
			log(log, process, null);
			process.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		log.info("extracción completada con éxito!!");
	}

	public static void extractZip(File downloadedFile, String destPath, Logger log) throws IOException {
		log.info("Extrayendo: " + downloadedFile.getName());
		// get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(downloadedFile));
		// get the zipped file list entry
		ZipEntry ze = zis.getNextEntry();
		byte[] buffer = new byte[4096];
		while (ze != null) {

			String fileName = ze.getName();
			File newFile = new File(destPath + fileName);

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

	public static String getCommandPath(File baseDir, String command) {
		return new File(baseDir, command).getAbsolutePath();
	}

}
