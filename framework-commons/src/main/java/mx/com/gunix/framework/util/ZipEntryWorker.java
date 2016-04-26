package mx.com.gunix.framework.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public final class ZipEntryWorker {
	private static final class ZipInputStreamWrapper extends InputStream {
		ZipInputStream innerZipIs;
		public ZipInputStreamWrapper(ZipInputStream innerZipIs) {
			this.innerZipIs = innerZipIs;
		}

		@Override
		public long skip(long n) throws IOException {
			return innerZipIs.skip(n);
		}

		@Override
		public int available() throws IOException {
			return innerZipIs.available();
		}

		@Override
		public synchronized void mark(int readlimit) {
			innerZipIs.mark(readlimit);
		}

		@Override
		public synchronized void reset() throws IOException {
			innerZipIs.reset();
		}

		@Override
		public boolean markSupported() {
			return innerZipIs.markSupported();
		}

		@Override
		public int read(byte[] b) throws IOException {
			return innerZipIs.read(b);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return innerZipIs.read(b, off, len);
		}

		@Override
		public int read() throws IOException {
			return innerZipIs.read();
		}
	}

	static Charset ISO88591 = Charset.forName("ISO-8859-1");
	
	public static void withSingleZipEntry(File file, BiConsumer<String, InputStream> ew) throws ZipException, IOException {
		ZipFile outZF = null;
		try (ZipFile zipFile = (outZF = new ZipFile(file, ISO88591))) {
			zipFile.stream()
					.findFirst()
					.ifPresent(ze -> {
							try {
								ew.accept(ze.getName(), zipFile.getInputStream(ze));
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

	public static void withSingleZipEntry(InputStream zipIs, BiConsumer<String, InputStream> ew) throws ZipException, IOException {
		doProcessZipEntries(zipIs, ew, false);
	}

	public static void withAllZipEntries(InputStream zipIs, BiConsumer<String, InputStream> ew) throws ZipException, IOException {
		doProcessZipEntries(zipIs, ew, true);
	}

	private static void doProcessZipEntries(InputStream zipIs, BiConsumer<String, InputStream> ew, boolean isAllEntries) throws ZipException, IOException {
		if (!(zipIs instanceof ZipInputStream)) {
			zipIs = new ZipInputStream(zipIs, ISO88591);
		}

		ZipEntry ze = null;
		ZipInputStream innerZipIs = (ZipInputStream) zipIs;
		while ((ze = innerZipIs.getNextEntry()) != null) {
			ew.accept(ze.getName(), new ZipInputStreamWrapper(innerZipIs));
			innerZipIs.closeEntry();
			if (!isAllEntries) {
				break;
			}
		}
		innerZipIs.close();
	}
}