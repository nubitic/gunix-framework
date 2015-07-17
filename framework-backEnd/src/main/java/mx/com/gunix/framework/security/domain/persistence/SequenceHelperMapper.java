package mx.com.gunix.framework.security.domain.persistence;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SequenceHelperMapper {
	@Select("select nextval(pg_get_serial_sequence(#{tabla}, #{columna}))")
	public long nextVal(@Param("tabla")String tabla, @Param("columna")String columna);
	
	@Select("select currval(pg_get_serial_sequence(#{tabla}, #{columna}))")
	public long currVal(@Param("tabla")String tabla, @Param("columna")String columna);
}
