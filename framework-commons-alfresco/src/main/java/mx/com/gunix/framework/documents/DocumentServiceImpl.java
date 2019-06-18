package mx.com.gunix.framework.documents;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hunteron.core.Context;

import mx.com.gunix.framework.documents.domain.Carpeta;
import mx.com.gunix.framework.documents.domain.Documento;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocumentServiceImpl implements DocumentService {

	static final String usuarioLD;
	static final String passwordLD;
	static final String url;

	static String DEFAULT_WORKSPACE_ID;
	static String DEFAULT_WORKSPACE_NAME;

	static {
		usuarioLD = Context.ALFRESCO_USER.get();
		passwordLD = Context.ALFRESCO_PASSWORD.get();
		url = Context.ALFRESCO_URL.get();
	}

	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#mkdir(java.lang.String)
	 */
	@Override
	public Carpeta mkdir(String nombre) {
		return inSession((sid, root) -> {
			return doMkdir(sid, root, nombre);
		});
	}

	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#mkdirs(java.lang.String)
	 */
	@Override
	public Carpeta mkdirs(String ruta) {
		return inSession((session, root) -> {
			Folder folder = (Folder) session.getObject(session.createPath(ruta, "cmis:folder").getId());
			Carpeta newCarpeta = new Carpeta();
			newCarpeta.setIdStr(folder.getId());
			newCarpeta.setNombre(folder.getName());
			completaRuta(session, root, newCarpeta, folder.getParentId());
			return newCarpeta;
		});
	}

	private void completaRuta(Session session, Carpeta root, Carpeta newCarpeta, String parentId) throws Exception {
		Carpeta padre = null;
		if (!root.getIdStr().equals(parentId)) {
			Folder folder = (Folder) session.getObject(parentId);
			padre = new Carpeta();
			padre.setIdStr(parentId);
			padre.setNombre(folder.getName());
			completaRuta(session, root, padre, folder.getParentId());
		} else {
			padre = root;
		}
		newCarpeta.setPadre(padre);
	}

	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#mkdir(mx.com.gunix.framework.documents.domain.Carpeta, java.lang.String)
	 */
	@Override
	public Carpeta mkdir(Carpeta padre, String nombre) {
		return inSession((sid, root) -> {
			return doMkdir(sid, padre, nombre);
		});
	}

	private Carpeta doMkdir(Session session, Carpeta padre, String nombre) throws Exception {
		Folder parentFolder = (Folder) session.getObject(padre.getIdStr());
		String newFolderId = null;
		Map<String, Object> newFolderProps = new HashMap<String, Object>();
		newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		newFolderProps.put(PropertyIds.NAME, nombre);
		newFolderId = parentFolder.createFolder(newFolderProps).getId();
		Carpeta newCarpeta = new Carpeta();
		newCarpeta.setIdStr(newFolderId);
		newCarpeta.setNombre(nombre);
		newCarpeta.setPadre(padre);
		return newCarpeta;
	}

	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#save(mx.com.gunix.framework.documents.domain.Carpeta, java.lang.String, java.io.InputStream)
	 */
	@Override
	public Documento save(Carpeta padre, String nombre, InputStream documento) {
		return inSession((session, root) -> {
			Carpeta fPadre = null;

			if (padre == null) {
				fPadre = root;
			} else {
				fPadre = padre;
			}
			
			Folder parentFolder = (Folder) session.getObject(fPadre.getIdStr());
			
	        // Make sure the user is allowed to create a document in the passed in folder
	        if (parentFolder.getAllowableActions().getAllowableActions().contains(Action.CAN_CREATE_DOCUMENT) == false) {
	            throw new CmisUnauthorizedException("Current user does not have permission to create a document in " + parentFolder.getPath());
	        }

            // Setup document content
            ContentStream contentStream = session.getObjectFactory().createContentStream(nombre, -1, "application/octet-stream", documento);
	        
	        // Check if document already exist, if not create it
	        Document newDocument = null;
	        if (!session.existsPath("/" + fPadre.getPath() + "/" + nombre)) {
	            // Setup document metadata
	            Map<String, Object> newDocumentProps = new HashMap<String, Object>();
	            newDocumentProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
	            newDocumentProps.put(PropertyIds.NAME, nombre);

	            // Check if we need versioning
	            VersioningState versioningState = VersioningState.NONE;
	            DocumentType docType = (DocumentType) session.getTypeDefinition("cmis:document");
	            if (Boolean.TRUE.equals(docType.isVersionable())) {
	                versioningState = VersioningState.MAJOR;
	            }

	            // Create versioned document object
	            newDocument = parentFolder.createDocument(newDocumentProps, contentStream, versioningState);
	        } else {
	        	newDocument = (Document) session.getObjectByPath("/" + fPadre.getPath() + "/" + nombre);
	        	newDocument = newDocument.setContentStream(contentStream, true);
	        }
			Documento doc = new Documento();
			doc.setCarpeta(fPadre);
			doc.setFileName(nombre);
			doc.setIdStr(newDocument.getId());
			
			contentStream.getStream().close();
			
			return doc;
		});
	}

	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#from(java.lang.String)
	 */
	@Override
	public Carpeta from(String rutaCarpeta) {
		return inSession((session, root) -> {
			Folder folder = (Folder) session.getObjectByPath("/" + root.getPath() + (rutaCarpeta.charAt(0) == '/' ? "" : "/") + rutaCarpeta);
			Carpeta newCarpeta = new Carpeta();
			newCarpeta.setIdStr(folder.getId());
			newCarpeta.setNombre(folder.getName());
			completaRuta(session, root, newCarpeta, folder.getParentId());
			return newCarpeta;
		});
	}

	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#get(mx.com.gunix.framework.documents.domain.Carpeta)
	 */
	@Override
	public List<Documento> get(Carpeta padre) {
		return inSession((session, root) -> {
			ItemIterable<CmisObject> docs = null;
			
			if (padre == null) {
				docs = ((Folder) session.getObject(root.getIdStr())).getChildren();
			} else {
				if (padre.getIdStr() == null) {
					docs = ((Folder) session.getObjectByPath("/" + padre.getPath())).getChildren();
				} else {
					docs = ((Folder) session.getObject(padre.getIdStr())).getChildren();
				}
			}
			
			List<Documento> docsEncontrados = new ArrayList<Documento>();
			if (docs != null) {
				docs.forEach(doc -> {
					if (doc instanceof Document)
						try {
							docsEncontrados.add(populateDocumento(session, root, (Document) doc));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
				});
			}
			return docsEncontrados;
		});
	}
	
	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#get(mx.com.gunix.framework.documents.domain.Carpeta, java.lang.String)
	 */
	@Override
	public Documento get(Carpeta padre, String nombreDocumento) {
		return inSession((session, root) -> {
			return doGetDocumentoByPath(session, root, padre.getPath() + "/" + nombreDocumento);
		});
	}

	private Documento doGetDocumento(Session session, Carpeta root, String idDocumento) throws Exception {
		Document document = (Document) session.getObject(idDocumento);
		Documento doc = null;
		if (document != null) {
			doc = populateDocumento(session, root, document);
		}
		return doc;
	}
	
	private Documento doGetDocumentoByPath(Session session, Carpeta root, String rutaDocumento) throws Exception {
		Document document = (Document) session.getObjectByPath(rutaDocumento.charAt(0) == '/' ? rutaDocumento : root.getPath() + "/" + rutaDocumento);
		Documento doc = null;
		if (document != null) {
			doc = populateDocumento(session, root, document);
		}
		return doc;
	}
	
	private Documento populateDocumento(Session session, Carpeta root, Document document) throws Exception{
		Documento doc = new Documento();
		doc.setIdStr(document.getId());
		doc.setFileName(document.getName());
		doc.setMimeType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType("archivo." + document.getType()));
		Carpeta parentHolder = new Carpeta();
		completaRuta(session, root, parentHolder, document.getParents().get(0).getId());
		doc.setCarpeta(parentHolder.getPadre());
		return doc;
	}

	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#get(java.lang.String)
	 */
	@Override
	public Documento get(String rutaDocumento) {
		return inSession((sid, root) -> {
			return doGetDocumentoByPath(sid, root, rutaDocumento);
		});
	}
	
	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#get(long)
	 */
	@Override
	public Documento getByStringId(String idDocumento) {
		return inSession((sid, root) -> {
			return doGetDocumento(sid, root, idDocumento);
		});
	}
	
	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#delete(long)
	 */
	@Override
	public void delete(String idDocumento) {
		inSession((session, root) -> {
			session.delete(session.getObject(idDocumento));
			return null;
		});
	}

	/* (non-Javadoc)
	 * @see mx.com.gunix.framework.documents.DocumentServiceIntfc#getContent(long)
	 */
	@Override
	public InputStream getContent(String idDocumento) {
		return inSession((sid, root) -> {
			return null;
		});
	}

	private <T> T inSession(Funcion<T> sessionFunction) {
		Session session = null;
		try {

			SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(SessionParameter.USER, usuarioLD);
			parameters.put(SessionParameter.PASSWORD, passwordLD);
			parameters.put(SessionParameter.ATOMPUB_URL, url);
			parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
			parameters.put(SessionParameter.COMPRESSION, "true");
			parameters.put(SessionParameter.CACHE_TTL_OBJECTS, "0"); // Caching is turned off

			// If there is only one repository exposed (e.g. Alfresco), these
			// lines will help detect it and its ID
			List<Repository> repositories = sessionFactory.getRepositories(parameters);
			Repository alfrescoRepository = null;
			if (repositories != null && repositories.size() > 0) {
				alfrescoRepository = repositories.get(0);
			} else {
				throw new CmisConnectionException("Could not connect to the Alfresco Server, no repository found!");
			}

			// Create a new session with the Alfresco repository
			session = alfrescoRepository.createSession();

			Folder parentFolder = (Folder) session.getObjectByPath("/Espacios personales de usuario/");

			if (DEFAULT_WORKSPACE_ID == null) {
				DEFAULT_WORKSPACE_ID = parentFolder.getId();
				DEFAULT_WORKSPACE_NAME = parentFolder.getName();
			}

			Folder appRootFolder = (Folder) session.getObjectByPath("/" + DEFAULT_WORKSPACE_NAME + "/" + usuarioLD);
			String appRootFolderId = null;
			if (appRootFolder == null) {
				throw new IllegalStateException("No existe la carpeta de usuario en Alfresco");
			}
			
			appRootFolderId = appRootFolder.getId();

			Carpeta root = new Carpeta();
			root.setNombre(DEFAULT_WORKSPACE_NAME);
			root.setIdStr(DEFAULT_WORKSPACE_ID);

			Carpeta wkSpc = new Carpeta();
			wkSpc.setNombre(usuarioLD);
			wkSpc.setIdStr(appRootFolderId);
			wkSpc.setPadre(root);

			return sessionFunction.doApply(session, wkSpc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (session != null) {
				session.clear();
			}
		}
	}

	public interface Funcion<T> {
		T doApply(Session session, Carpeta root) throws Exception;
	}

	@Override
	public Documento get(long idDocumento) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(long idDocumento) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InputStream getContent(long idDocumento) {
		// TODO Auto-generated method stub
		return null;
	}
}
