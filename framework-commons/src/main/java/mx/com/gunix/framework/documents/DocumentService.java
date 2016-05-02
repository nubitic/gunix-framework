package mx.com.gunix.framework.documents;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;

import mx.com.gunix.framework.documents.domain.Carpeta;
import mx.com.gunix.framework.documents.domain.Documento;
import mx.com.gunix.framework.security.UserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logicaldoc.webservice.WSAttribute;
import com.logicaldoc.webservice.auth.AuthService;
import com.logicaldoc.webservice.document.WSDocument;
import com.logicaldoc.webservice.folder.FolderService;
import com.logicaldoc.webservice.folder.WSFolder;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocumentService {

	@Autowired
	AuthService as;

	@Autowired
	FolderService fs;

	@Autowired
	com.logicaldoc.webservice.document.DocumentService ds;

	static final String usuarioLD;
	static final String passwordLD;

	static Long DEFAULT_WORKSPACE_ID;
	static String DEFAULT_WORKSPACE_NAME;

	static {
		usuarioLD = System.getenv("LOGICALDOC_USER") == null ? "admin" : System.getenv("LOGICALDOC_USER");
		passwordLD = System.getenv("LOGICALDOC_PASSWORD") == null ? "admin" : System.getenv("LOGICALDOC_PASSWORD");
	}

	/**
	 * Crea una carpeta en la raíz
	 * */
	public Carpeta mkdir(String nombre) {
		return inSession((sid, root) -> {
			return doMkdir(sid, root, nombre);
		});
	}

	/**
	 * Crea tantas carpetas sean necesarias para completar la ruta
	 * */
	public Carpeta mkdirs(String ruta) {
		return inSession((sid, root) -> {
			WSFolder folder = fs.createPath(sid, root.getId(), ruta);
			Carpeta newCarpeta = new Carpeta();
			newCarpeta.setId(folder.getId());
			newCarpeta.setNombre(folder.getName());
			completaRuta(sid, root, newCarpeta, folder.getParentId());
			return newCarpeta;
		});
	}

	private void completaRuta(String sid, Carpeta root, Carpeta newCarpeta, long parentId) throws Exception {
		Carpeta padre = null;
		if (root.getId() != parentId) {
			WSFolder folder = fs.getFolder(sid, parentId);
			padre = new Carpeta();
			padre.setId(parentId);
			padre.setNombre(folder.getName());
			completaRuta(sid, root, padre, folder.getParentId());
		} else {
			padre = root;
		}
		newCarpeta.setPadre(padre);
	}

	/**
	 * Crea una carpeta dentro de la especificada en padre
	 */
	public Carpeta mkdir(Carpeta padre, String nombre) {
		return inSession((sid, root) -> {
			return doMkdir(sid, padre, nombre);
		});
	}

	private Carpeta doMkdir(String sid, Carpeta padre, String nombre) throws Exception {
		long newFolderId = fs.createFolder(sid, padre.getId(), nombre);
		Carpeta newCarpeta = new Carpeta();
		newCarpeta.setId(newFolderId);
		newCarpeta.setNombre(nombre);
		newCarpeta.setPadre(padre);
		return newCarpeta;
	}

	public Documento save(Carpeta padre, String nombre, InputStream documento) {
		return inSession((sid, root) -> {
			String customId = padre.getPath() + "/" + nombre;

			WSDocument document = ds.getDocumentByCustomId(sid, customId);
			if (document == null) {
				document = new WSDocument();
				document.setFolderId(padre.getId());
				document.setFileName(nombre);
				document.setLanguage("es_MX");
				document.setCustomId(customId);
			}

			/*document.setExtendedAttributes(toExtendedAttributes(atributos));
			document.setFileSize((atributos != null && atributos.get(Documento.SIZE) != null) ? Long.parseLong(atributos.get(Documento.SIZE)) : 0);*/
			
			if (document.getId() == 0) {
				document = ds.create(sid, document, new DataHandler(new InputStreamDataSource(nombre, documento, /*atributos != null ? atributos.get(Documento.CONTENT_TYPE) :*/ null)));
			} else {
				ds.update(sid, document);
				document.setId(ds.upload(sid, document.getId(), padre.getId(), false, nombre, "es_MX", new DataHandler(new InputStreamDataSource(nombre, documento, /*atributos != null ? atributos.get(Documento.CONTENT_TYPE) :*/ null))));
				ds.reindex(sid, document.getId(), null);
			}

			Documento doc = new Documento();
			doc.setCarpeta(padre);
			doc.setFileName(nombre);
			doc.setId(document.getId());
			//doc.setAtributos(atributos != null ? new LinkedHashMap<String, String>(atributos) : null);
			return doc;
		});
	}

	private Map<String, String> fromExtendedAttributes(WSAttribute[] attrs) {
		if (attrs != null) {
			Map<String, String> atributos = new LinkedHashMap<String, String>();
			for (WSAttribute attr : attrs) {
				atributos.put(attr.getName(), attr.getStringValue());
			}
			return atributos;
		} else {
			return null;
		}
	}

	private WSAttribute[] toExtendedAttributes(Map<String, String> atributos) {
		if (atributos != null) {
			WSAttribute[] attrs = new WSAttribute[atributos.size()];
			Iterator<String> attrIt = atributos.keySet().iterator();
			int i = 0;
			while (attrIt.hasNext()) {
				WSAttribute newAttr = new WSAttribute();
				newAttr.setType(WSAttribute.TYPE_STRING);
				String attrName = attrIt.next();
				newAttr.setName(attrName);
				newAttr.setStringValue(atributos.get(attrName));
				attrs[i] = newAttr;
				i++;
			}
			return attrs;
		} else {
			return null;
		}
	}

	public Carpeta from(String rutaCarpeta) {
		return inSession((sid, root) -> {
			WSFolder folder = fs.findByPath(sid, "/" + root.getPath() + (rutaCarpeta.charAt(0) == '/' ? "" : "/") + rutaCarpeta);
			Carpeta newCarpeta = new Carpeta();
			newCarpeta.setId(folder.getId());
			newCarpeta.setNombre(folder.getName());
			completaRuta(sid, root, newCarpeta, folder.getParentId());
			return newCarpeta;
		});
	}

	public Documento get(Carpeta padre, String nombreDocumento) {
		return inSession((sid, root) -> {
			return doGetDocumento(sid, root, padre.getPath() + "/" + nombreDocumento);
		});
	}

	private Documento doGetDocumento(String sid, Carpeta root, String rutaDocumento) throws Exception {
		WSDocument document = ds.getDocumentByCustomId(sid, rutaDocumento);
		Documento doc = new Documento();
		doc.setId(document.getId());
		doc.setFileName(document.getFileName());
		//doc.setAtributos(fromExtendedAttributes(document.getExtendedAttributes()));
		Carpeta parentHolder = new Carpeta();
		completaRuta(sid, root, parentHolder, document.getFolderId());
		doc.setCarpeta(parentHolder.getPadre());
		return doc;
	}

	public Documento get(String rutaDocumento) {
		return inSession((sid, root) -> {
			return doGetDocumento(sid, root, rutaDocumento);
		});
	}

	public InputStream getContent(long idDocumento) {
		return inSession((sid, root) -> {
			return ds.getContent(sid, idDocumento).getInputStream();
		});
	}

	private <T> T inSession(Funcion<T> sessionFunction) {
		String token = null;
		try {
			token = as.login(usuarioLD, passwordLD);

			String appID = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSelectedAuthority().split("_")[0];

			if (DEFAULT_WORKSPACE_ID == null) {
				WSFolder defaultWkSp = fs.listWorkspaces(token)[0];
				DEFAULT_WORKSPACE_ID = defaultWkSp.getId();
				DEFAULT_WORKSPACE_NAME = defaultWkSp.getName();
			}

			WSFolder appRootFolder = fs.findByPath(token, "/" + DEFAULT_WORKSPACE_NAME + "/" + appID);
			Long appRootFolderId = null;
			if (appRootFolder == null) {
				appRootFolderId = fs.createFolder(token, DEFAULT_WORKSPACE_ID, appID);
			} else {
				appRootFolderId = appRootFolder.getId();
			}

			Carpeta root = new Carpeta();
			root.setId(DEFAULT_WORKSPACE_ID);
			root.setNombre(DEFAULT_WORKSPACE_NAME);

			Carpeta wkSpc = new Carpeta();
			wkSpc.setId(appRootFolderId);
			wkSpc.setNombre(appID);
			wkSpc.setPadre(root);

			return sessionFunction.doApply(token, wkSpc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (token != null) {
				as.logout(token);
			}
		}
	}

	public final class InputStreamDataSource implements DataSource, Serializable {
		private static final long serialVersionUID = 1L;

		private String nombre;
		private InputStream documento;
		private String contentType;

		public InputStreamDataSource(String nombre, InputStream documento, String contentType) {
			this.nombre = nombre;
			this.documento = documento;
			this.contentType = contentType == null ? MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(nombre) : contentType;
		}

		public String getContentType() {
			return contentType;
		}

		public InputStream getInputStream() throws IOException {
			return documento;
		}

		public String getName() {
			return nombre;
		}

		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
	}

	public interface Funcion<T> {
		T doApply(String token, Carpeta root) throws Exception;
	}
}
