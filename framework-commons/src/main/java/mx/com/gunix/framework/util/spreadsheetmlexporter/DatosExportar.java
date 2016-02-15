package mx.com.gunix.framework.util.spreadsheetmlexporter;

public interface DatosExportar {
	/** Regresa true si todavia hay registros */
	public boolean siguiente();

	/** Regresa la información de las columnas */
	public MetaDatos getMetaDatos();

	public Double getDouble(int indiceColumna);

	public String getString(int indiceColumna);

	public void liberar();
}
