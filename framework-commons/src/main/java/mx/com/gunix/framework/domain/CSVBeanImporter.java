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
						} else {
							break;
						}
					} catch (SuperCsvCellProcessorException errorInfo) {
						errorProcessor.error(header, errorInfo.getCsvContext(), errorInfo.getMessage());
					}
				} while (true);				
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

	public interface ImportedBeanProcessor<T extends Serializable> {
		public void process(T t, int linea);
	}

	public interface BeanErrorProcessor {
		public void error(String[] header, CsvContext context, String errorMessage);
	}
}
