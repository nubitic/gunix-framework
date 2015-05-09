package mx.com.gunix.domain.persistence;

import mx.com.gunix.domain.Cliente;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface ClienteMapper {
	@Insert("insert into CLIENTE(nombre) values(#{nombre})")
	@Options(useGeneratedKeys=true, keyColumn="id_cliente")
	public void guarda(Cliente cliente);
	
	@Select("Select * from CLIENTE where nombre=#{nombre}")
	@Results({@Result(id=true,column="id",property="id"),
			  @Result(column="nombre",property="nombre")})
	public Cliente getClienteByNombre(String nombre);
}
