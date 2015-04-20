package mx.com.gunix.framework.domain;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import mx.com.gunix.framework.domain.Funcion;
import mx.com.gunix.framework.domain.Usuario;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

@Intercepts({@Signature(
		  type= ResultSetHandler.class,
		  method = "handleResultSets",
		  args = {Statement.class})})
public class UsuarioMapperInterceptor implements Interceptor {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object obj = invocation.proceed(); 
		if(obj instanceof Collection){
			Collection<Usuario> usuarios =
				    (Collection<Usuario>) ((Collection)obj)
			        .stream()
			        .filter(pu -> (pu instanceof Usuario))
			        .collect(Collectors.toList());
			usuarios
			    .parallelStream()
			    .forEach(this::jerarquizaFunciones);
		}
		return obj;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		System.out.println(properties);
	}
	
	private void jerarquizaFunciones(Usuario usuario){
		Objects.requireNonNull(usuario);
		Objects.requireNonNull(usuario.getRoles());
		usuario.getRoles()
					.parallelStream()
					.forEach(rol->{
						Objects.requireNonNull(rol.getModulos());
						rol.getModulos()
								.parallelStream()
								.forEach(modulo->{
									Objects.requireNonNull(modulo.getFunciones());
									List<Funcion> funcionesReacomodadas = new ArrayList<Funcion>();
									modulo.getFunciones()
												.stream()
												.forEach(funcion->{
													funcion.setModulo(modulo);
													if(funcion.getPadre()!=null){
														Optional<Funcion> padre = modulo.getFunciones()
																							.stream()
																							.filter(posiblePadre-> (posiblePadre.getIdFuncion().equals(funcion.getPadre().getIdFuncion())))
																							.findFirst();
														padre.ifPresent(p-> {
															funcion.setPadre(p);
															List<Funcion> hijas = p.getHijas();
															if(hijas==null){
																p.setHijas((hijas=new ArrayList<Funcion>()));
															}
															hijas.add(funcion);
															funcionesReacomodadas.add(funcion);
														});
													}
												});
									modulo.setFunciones(modulo.getFunciones()
																	.stream()
																	.filter(funcion->(!funcionesReacomodadas.contains(funcion)))
																	.collect(Collectors.toList()));
								});
					});
	}
}
