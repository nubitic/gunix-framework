package mx.com.gunix.framework.processes.domain;

import java.io.Serializable;

public final class ProgressUpdate implements Serializable {
	private static final long serialVersionUID = 1L;

	private String processInstanceId;
	private String mensaje;
	private long timeStamp;
	private float progreso;
	private boolean cancelado;

	public void setProcessId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getProcessId() {
		return this.processInstanceId;
	}
	
	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public float getProgreso() {
		return progreso;
	}

	public void setProgreso(float progreso) {
		this.progreso = progreso;
	}

	public boolean isCancelado() {
		return cancelado;
	}

	public void setCancelado(boolean cancelado) {
		this.cancelado = cancelado;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
		result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProgressUpdate other = (ProgressUpdate) obj;
		if (processInstanceId == null) {
			if (other.processInstanceId != null)
				return false;
		} else if (!processInstanceId.equals(other.processInstanceId))
			return false;
		if (timeStamp != other.timeStamp)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProgressUpdate [mensaje=" + mensaje + ", progreso=" + progreso + ", cancelado=" + cancelado + "]";
	}
}
