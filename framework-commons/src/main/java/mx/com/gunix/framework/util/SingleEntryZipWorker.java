package mx.com.gunix.framework.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public final class SingleEntryZipWorker {

	public static void process(File file, EntryWorker ew) throws ZipException, IOException {
		ZipFile outZF = null;
		try (ZipFile zipFile = (outZF = new ZipFile(file))) {
			zipFile.stream()
					.findFirst()
					.ifPresent(ze -> {
							try {
								ew.work(zipFile.getInputStream(ze));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
			});
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