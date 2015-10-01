package mx.com.gunix.framework.domain;

import java.io.Serializable;

public abstract class HashCodeByTimeStampAware implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long timeStampHash;

	public Long getTimeStampHash() {
		return timeStampHash;
	}

	public void setTimeStampHash(Long timeStampHash) {
		this.timeStampHash = timeStampHash;
	}

	@Override
	public final int hashCode() {
		if (timeStampHash == null) {
			timeStampHash = (long) (System.nanoTime() + (Math.random() * 1000.0));
		}

		return 31 * doHashCode() + (timeStampHash.intValue());
	}

	protected abstract int doHashCode();
}
