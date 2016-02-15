package mx.com.gunix.framework.util.spreadsheetmlexporter;


public interface Progreso {

	enum Estatus {ERROR,OK};

	void despliegaMensaje(String string);

	void registrosProcesados(int conTotal);

	void terminarProceso(Estatus error);

}
