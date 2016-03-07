package mx.com.gunix.framework.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.io.dozer.CsvDozerBeanReader;
import org.supercsv.io.dozer.ICsvDozerBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

public abstract class CSVBeanImporter<T extends Serializable> {
	private final String[] requiredHeader;
	private final String[] fieldMappings;
	private final CellProcessor[] processors;
	private final String requiredHeadersString;
	private final Class<T> clazz;

	@SuppressWarnings("unchecked")
	public CSVBeanImporter(LinkedHashMap<String, String> fieldMappings, CellProcessor[] processors) {
		this.processors = processors;

		requiredHeader = fieldMappings.keySet().toArray(new String[] {});
		this.fieldMappings = fieldMappings.values().toArray(new String[] {});
		requiredHeadersString = Arrays.deepToString(requiredHeader);
		
		Type genSuperType = getClass().getGenericSuperclass();
		if (genSuperType instanceof ParameterizedType) {
			Type[] typeArguments = ((ParameterizedType) genSuperType).getActualTypeArguments();
			if (typeArguments.length == 1) {
				this.clazz = ((Class<T>) typeArguments[0]);
			} else {
				throw new IllegalArgumentException("No fue posible identificar la clase del bean que se importará");
			}
		} else {
			throw new IllegalArgumentException("No fue posible identificar la clase del bean que se importará");
		}
	}

	public void importBeans(InputStream csvSource, ImportedBeanProcessor<T> processor, BeanErrorProcessor errorProcessor) {
		importBeans(new InputStreamReader(csvSource), processor, errorProcessor);
	}

	public void importBeans(File csvSource, ImportedBeanProcessor<T> processor, BeanErrorProcessor errorProcessor) {
		try {
			importBeans(new FileReader(csvSource), processor, errorProcessor);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void importBeans(Reader csvSource, ImportedBeanProcessor<T> processor, BeanErrorProcessor errorProcessor) {
		ICsvDozerBeanReader beanReader = null;
		Boolean sinRegistros = true;
		try {		    
			beanReader = new CsvDozerBeanReader(csvSource, CsvPreference.STANDARD_PREFERENCE);

			String[] header = beanReader.getHeader(true); 
			 
			if (Arrays.equals(header, requiredHeader)) {
				beanReader.configureBeanMapping(clazz, fieldMappings);
				
				T t = null;
				do {
					try {
						t = beanReader.read(clazz, processors);
						if (t != null) {
							processor.process(t,beanReader.getLineNumber());
							sinRegistros = false;
						} else {
							break;
						}
					} catch (SuperCsvCellProcessorException errorInfo) {
						errorProcessor.error(header, errorInfo.getCsvContext(), traduce(errorInfo.getMessage()));
					}
				} while (true);
				if(sinRegistros){
					errorProcessor.error(header, new CsvContext(1, 0, 1), "El archivo esta vacío");
				}
			}else {
				errorProcessor.error(header, new CsvContext(1, 0, 1), String.format("El encabezado del archivo es incorrecto, debe ser: %s", requiredHeadersString));
			}
		} catch (IOException exc) {
		} finally {
			if (beanReader != null) {
				try {
					beanReader.close();
				} catch (IOException ignorar) {

				}
			}
		}
	}

	private String traduce(String message) {
		return message
					.replace("and max", "y máximo")
					.replace("contains the forbidden substring", "contiene el texto no permitido")
					.replace("could not be parsed as a BigDecimal", "no pudo leerse como un número")
					.replace("could not be parsed as a Date", "no pudo leerse como una fecha")
					.replace("could not be parsed as a Double", "no pudo leerse como un número")
					.replace("could not be parsed as a Long", "no pudo leerse como un número")
					.replace("could not be parsed as an Integer", "no pudo leerse como un número")
					.replace("could not be parsed as an Long", "no pudo leerse como un número")
					.replace("defined by the regular expression", "definido por el patrón")
					.replace("does not contain any of the required substrings", "no contiene ninguno de los textos requeridos")
					.replace("does not lie between the min", "no se encuentra entre el mínimo")
					.replace("does not match any of the required hashcodes", "no coincide con ninguno de los códigos de identificación requeridos")
					.replace("does not match the constraint", "no cumple con la restricción")
					.replace("does not match the regular expression", "no coincide con el patrón")
					.replace("duplicate value", "valor duplicado")
					.replace("encountered", "encontrado")
					.replace("encountered with hashcode", "encontrado con código de identificación")
					.replace("Failed to format value as a", "Error al dar formato de")
					.replace("Failed to parse value", "Error al leer valor")
					.replace("Failed to parse value as a Duration", "Error al leer valor como una Duración")
					.replace("Failed to parse value as a Period", "Error al leer valor como un Periodo")
					.replace("Failed to parse value as a ZoneId", "Error al leer valor como una Zona Horaria")
					.replace("for value", "para el valor")
					.replace("is not a valid date format", "no es un formato de fecha válido")
					.replace("is not a valid decimal format", "no es un formato de número decimal válido")
					.replace("is not an element of the supplied Collection", "no es un elemento de la colección proporcionada")
					.replace("is not equal to the previous value(s) of", "no es igual al(los) valor(es) previo(s) de")
					.replace("is not equal to the supplied constant", "no es igual al valor constante proporcionado")
					.replace("is not included in the allowed set of values", "no está incluido en el conjunto de valores permitidos")
					.replace("not any of the required lengths", "no tiene ninguna de las longitudes requeridas")
					.replace("null value encountered", "valor vacío encontrado")
					.replace("of value", "de valor")
					.replace("the hashcode of", "el código de identificación de")
					.replace("the length", "la longitud")
					.replace("the String should not be empty", "el valor no debe estar vacío")
					.replace("the String should not be null", "el valor no debe estar vacío")
					.replace("values (inclusive)", "valores (inclusivo)");
	}

	public interface ImportedBeanProcessor<T extends Serializable> {
		public void process(T t, int linea);
	}

	public interface BeanErrorProcessor {
		public void error(String[] header, CsvContext context, String errorMessage);
	}
}
