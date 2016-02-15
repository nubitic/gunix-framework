package mx.com.gunix.framework.ui.vaadin.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import mx.com.gunix.framework.util.spreadsheetmlexporter.CollectionSSMLExporter;

import com.vaadin.server.Extension;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

public class GunixData2ExcelFileDownloader<T extends List<S>, S extends Serializable> extends CustomComponent {
	private static final long serialVersionUID = 1L;
	private Button downloadInvisibleButton;
	private Button downloadVisibleButton;
	private VerticalLayout root = new VerticalLayout();
	private String downloadInvisibleButtonId;
	private String fileName;
	private Class<S> clase;

	private static final SimpleDateFormat timeStampFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss_");

	public GunixData2ExcelFileDownloader(String caption, String fileName, T datos, Class<S> clase, LinkedHashMap<String, String> mapping) {
		super();
		doInit(caption, fileName, datos, clase, mapping);
	}

	public GunixData2ExcelFileDownloader(String caption, String fileName, Class<S> clase) {
		super();
		doInit(caption, fileName, null, clase, null);
	}

	private void doInit(String caption, String fileName, T datos, Class<S> clase, LinkedHashMap<String, String> mapping) {
		this.setCompositionRoot(root);
		this.fileName = fileName;
		this.clase = clase;

		downloadVisibleButton = new Button(caption);
		root.addComponent(downloadVisibleButton);
		downloadInvisibleButtonId = new StringBuilder(fileName).append(System.currentTimeMillis()).toString();
		downloadInvisibleButton = new Button();
		downloadInvisibleButton.setId(downloadInvisibleButtonId);
		downloadInvisibleButton.addStyleName("invisibleDownloadButton");
		root.addComponent(downloadInvisibleButton);

		if (datos != null && mapping != null) {
			downloadVisibleButton.addClickListener(clickEvent -> {
				doDownload(datos, mapping);
			});
		}
	}

	public void doDownload(T datos, LinkedHashMap<String, String> mapping) {
		try {
			File tempExportFile = File.createTempFile("exportaExcel", "tmp");
			CollectionSSMLExporter<T, S> cssmle = new CollectionSSMLExporter<T, S>(datos, clase, mapping);
			cssmle.exporta(fileName, new FileOutputStream(tempExportFile));
			FileDownloader fileDownloader = new FileDownloader(new FileResource(tempExportFile) {
				private static final long serialVersionUID = 1L;

				@Override
				public String getFilename() {
					return new StringBuilder(timeStampFormatter.format(new Date())).append(fileName).append(".zip").toString();
				}

			});
			if (downloadInvisibleButton.getExtensions() != null) {
				Extension ext = null;
				Iterator<Extension> extIt = downloadInvisibleButton.getExtensions().iterator();
				while (extIt.hasNext()) {
					ext = extIt.next();
					break;
				}
				if (ext != null) {
					downloadInvisibleButton.removeExtension(ext);
				}
			}
			fileDownloader.extend(downloadInvisibleButton);
			Page.getCurrent().getJavaScript().execute(new StringBuilder("document.getElementById('").append(downloadInvisibleButtonId).append("').click();").toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addClickListener(ClickListener listener) {
		downloadVisibleButton.addClickListener(listener);
	}
}
