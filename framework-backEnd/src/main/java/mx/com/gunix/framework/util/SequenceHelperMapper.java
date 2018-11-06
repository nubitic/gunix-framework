package mx.com.gunix.framework.util;

import org.apache.ibatis.annotations.Param;

public interface SequenceHelperMapper {
	public long nextVal(@Param("tabla")String tabla, @Param("columna")String columna);
	public long currVal(@Param("tabla")String tabla, @Param("columna")String columna);
}
