package mx.com.gunix.framework.util.spreadsheetmlexporter;

import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import mx.com.gunix.framework.util.SpreadsheetMLExporter;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;

public class CollectionSSMLExporter<T extends List<S>, S extends Serializable> implements MetaDatos, DatosExportar {
	private T datos;
	private int processedIndex = -1;
	private int size;
	private String[] columnNames;
	private String[] columnPaths;
	private int[] columnTypes;
	private int columnCount;
	private BeanWrapperImpl bwi;
	private static final List<Class<?>> primitiveNumbers = Arrays.asList(new Class<?>[] { int.class, float.class, double.class, short.class, byte.class, long.class });
	private static final Object[] emptyArgs = new Object[] {};

	@SuppressWarnings("unchecked")
	public CollectionSSMLExporter(T datos, LinkedHashMap<String, String> columnMapping) {
		if (datos == null || datos.isEmpty()) {
			throw new IllegalArgumentException("Debe haber datos para exportar...");
		}
		this.datos = datos;
		size = datos.size();
		columnNames = columnMapping.keySet().toArray(new String[] {});
		columnPaths = columnMapping.values().toArray(new String[] {});
		columnCount = columnNames.length;
		columnTypes = new int[columnCount];

		Type genSuperType = getClass().getGenericSuperclass();
		if (genSuperType instanceof ParameterizedType) {
			Type[] typeArguments = ((ParameterizedType) genSuperType).getActualTypeArguments();
			if (typeArguments.length == 2) {
				Class<S> modelClass = ((Class<S>) typeArguments[1]);
				bwi = new BeanWrapperImpl(modelClass);
				for (int i = 0; i < columnPaths.length; i++) {
					Class<?> fieldType = bwi.getPropertyDescriptor(columnPaths[i]).getPropertyType();
					if (Number.class.isAssignableFrom(fieldType) || primitiveNumbers.contains(fieldType)) {
						columnTypes[i] = MetaDatos.NUMERICO;
					} else {
						columnTypes[i] = MetaDatos.TEXTO;
					}
				}
			}
		}
	}

	@Override
	public boolean siguiente() {
		return ++processedIndex < size;
	}

	@Override
	public MetaDatos getMetaDatos() {
		return this;
	}

	@Override
	public Double getDouble(int indiceColumna) {
		try {
			indiceColumna--;
			Number n = (Number) bwi.getPropertyDescriptor(columnPaths[indiceColumna]).getReadMethod().invoke(datos.get(processedIndex), emptyArgs);
			return n != null ? n.doubleValue() : null;
		} catch (InvalidPropertyException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("No fue posible leer la propiedad " + columnPaths[indiceColumna], e);
		}
	}

	@Override
	public String getString(int indiceColumna) {
		try {
			indiceColumna--;
			Object o = bwi.getPropertyDescriptor(columnPaths[indiceColumna]).getReadMethod().invoke(datos.get(processedIndex), emptyArgs);
			return o != null ? o.toString() : null;
		} catch (InvalidPropertyException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("No fue posible leer la propiedad " + columnPaths[indiceColumna], e);
		}
	}

	@Override
	public void liberar() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCuentaColumnas() {
		return columnCount;
	}

	@Override
	public String getNombreColumna(int indiceColumna) {
		indiceColumna--;
		return columnNames[indiceColumna];
	}

	@Override
	public int getTipoColumna(int indiceColumna) {
		indiceColumna--;
		return columnTypes[indiceColumna];
	}

	public void exporta(String nombreArcchivoExcel, OutputStream salida) {
		SpreadsheetMLExporter.exportaArchivo(nombreArcchivoExcel, salida, this, new Progreso() {

			@Override
			public void despliegaMensaje(String string) {
				// TODO Auto-generated method stub

			}

			@Override
			public void registrosProcesados(int conTotal) {
				// TODO Auto-generated method stub

			}

			@Override
			public void terminarProceso(Estatus error) {
				// TODO Auto-generated method stub

			}
		});
	}
}
