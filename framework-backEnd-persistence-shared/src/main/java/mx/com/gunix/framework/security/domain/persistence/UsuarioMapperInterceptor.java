package mx.com.gunix.framework.security.domain.persistence;

import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import com.hunteron.core.Context;

import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Usuario;

@Intercepts({ @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = { Statement.class }) })
public class UsuarioMapperInterceptor implements Interceptor {
	private final String ID_APLICACION = Context.ID_APLICACION.get();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object obj = invocation.proceed();
		if (obj instanceof Collection) {
			Collection<Usuario> usuarios = (Collection<Usuario>) ((Collection) obj).stream().filter(pu -> (pu instanceof Usuario)).collect(Collectors.toList());
			usuarios.parallelStream().forEach(this::jerarquizaFunciones);
		}
		return obj;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

	private void jerarquizaFunciones(Usuario usuario) {
		try {
			Objects.requireNonNull(usuario);
			Objects.requireNonNull(usuario.getAplicaciones());
			
			if (ID_APLICACION != null) {
				usuario.getAplicaciones().removeIf(app -> !(ID_APLICACION.equals(app.getIdAplicacion())));
			}
			
			usuario.getAplicaciones().parallelStream().forEach(aplicacion -> {
				Objects.requireNonNull(aplicacion.getRoles());
				aplicacion.getRoles().parallelStream().forEach(rol -> {
					Objects.requireNonNull(rol.getModulos());
					rol.setAplicacion(aplicacion);
					rol.getModulos().parallelStream().forEach(modulo -> {
						modulo.setFunciones(Funcion.jerarquizaFunciones(modulo, modulo.getFunciones()));
					});
				});
			});
		} catch (NullPointerException ignorar) {
		}
	}
}
