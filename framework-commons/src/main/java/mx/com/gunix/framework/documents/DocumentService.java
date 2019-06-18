package mx.com.gunix.framework.documents;

import java.io.InputStream;
import java.util.List;

import mx.com.gunix.framework.documents.domain.Carpeta;
import mx.com.gunix.framework.documents.domain.Documento;

public interface DocumentService {

	/**
	 * Crea una carpeta en la raíz
	 * */
	Carpeta mkdir(String nombre);

	/**
	 * Crea tantas carpetas sean necesarias para completar la ruta
	 * */
	Carpeta mkdirs(String ruta);

	/**
	 * Crea una carpeta dentro de la especificada en padre
	 */
	Carpeta mkdir(Carpeta padre, String nombre);

	Documento save(Carpeta padre, String nombre, InputStream documento);

	Carpeta from(String rutaCarpeta);

	List<Documento> get(Carpeta padre);

	Documento get(Carpeta padre, String nombreDocumento);

	Documento get(String rutaDocumento);

	Documento get(long idDocumento);

	void delete(long idDocumento);

	InputStream getContent(long idDocumento);

	Documento getByStringId(String idDocumento);

	void delete(String idDocumento);

	InputStream getContent(String idDocumento);
}