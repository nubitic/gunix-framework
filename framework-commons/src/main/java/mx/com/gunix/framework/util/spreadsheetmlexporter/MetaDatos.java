package mx.com.gunix.framework.util.spreadsheetmlexporter;

public interface MetaDatos {
	public int getCuentaColumnas();

	public String getNombreColumna(int indiceColumna);

	public int getTipoColumna(int indiceColumna);

	public static final int NUMERICO = 0;
	public static final int TEXTO = 1;
}
