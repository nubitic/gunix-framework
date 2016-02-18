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

import mx.com.gunix.framework.ui.vaadin.view.MainViewLayout;
import mx.com.gunix.framework.util.spreadsheetmlexporter.CollectionSSMLExporter;
import mx.com.gunix.framework.util.spreadsheetmlexporter.Progreso;

import com.vaadin.server.Extension;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
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
		if (datos != null && mapping != null) {
			downloadVisibleButton.addClickListener(clickEvent -> {
				doDownload(datos, mapping);
			});
		}
		root.addComponent(downloadVisibleButton);
	}

	public void doDownload(T datos, LinkedHashMap<String, String> mapping) {
		try {
			String finalFileName = new StringBuilder(timeStampFormatter.format(new Date())).append(fileName).append(".zip").toString();
			downloadInvisibleButtonId = new StringBuilder(fileName).append(System.currentTimeMillis()).toString();
			downloadInvisibleButton = new Button();
			downloadInvisibleButton.setId(downloadInvisibleButtonId);
			downloadInvisibleButton.addStyleName("invisibleDownloadButton");
			File tempExportFile = File.createTempFile("exportaExcel", "tmp");
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
			FileDownloader fileDownloader = new FileDownloader(new FileResource(tempExportFile) {
				private static final long serialVersionUID = 1L;

				@Override
				public String getFilename() {
					return finalFileName;
				}

			});
			fileDownloader.extend(downloadInvisibleButton);
			MainViewLayout.registraExportacion(downloadInvisibleButton, finalFileName, datos.size());
			new Thread(() -> {
				CollectionSSMLExporter<T, S> cssmle = new CollectionSSMLExporter<T, S>(datos, clase, mapping);
				final Integer datosSize = datos.size();
				try {
					cssmle.exporta(fileName, new FileOutputStream(tempExportFile), new Progreso() {

						@Override
						public void despliegaMensaje(String string) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void registrosProcesados(int conTotal) {
							MainViewLayout.actualizaProgresoExportacion(downloadInvisibleButtonId, (int) (conTotal*0.9));	
						}

						@Override
						public void terminarProceso(Estatus error) {
							MainViewLayout.actualizaProgresoExportacion(downloadInvisibleButtonId, datosSize);
						}
						
					});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}).start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addClickListener(ClickListener listener) {
		downloadVisibleButton.addClickListener(listener);
	}
}
