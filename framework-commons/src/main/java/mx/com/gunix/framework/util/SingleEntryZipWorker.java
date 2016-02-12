package mx.com.gunix.framework.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public final class SingleEntryZipWorker {
	static Charset ISO88591 = Charset.forName("ISO-8859-1");
	public static void process(File file, EntryWorker ew) throws ZipException, IOException {
		ZipFile outZF = null;
		try (ZipFile zipFile = (outZF = new ZipFile(file, ISO88591))) {
			zipFile.stream()
					.findFirst()
					.ifPresent(ze -> {
							try {
								ew.work(zipFile.getInputStream(ze));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
			});
		} catch (Exception ex) {
			throw new ZipException(ex.getMessage());
		} finally {
			if (outZF != null) {
				outZF.close();
			}
		}
	}
	
	public static void process(InputStream is, EntryWorker ew) {
		ew.work(new ZipInputStream(is));
	}

	public interface EntryWorker {
		void work(InputStream inputStream);
	}
}