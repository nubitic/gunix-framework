package mx.com.gunix.framework.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

public abstract class Utils {
	/**
	 * <a href="http://stackoverflow.com/questions/12026885/common-util-to-break-a-list-into-batch#answer-30072617">http://stackoverflow.com/questions/12026885/common-util-to-break-a-list-into-batch#answer-30072617</a>
	 * */
	public static <T> Stream<List<T>> partition(List<T> source, int length) {
		if (length <= 0)
			throw new IllegalArgumentException("length = " + length);
		int size = source.size();
		if (size <= 0)
			return Stream.empty();
		int fullChunks = (size - 1) / length;
		return IntStream.range(0, fullChunks + 1).mapToObj(n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
	}

	private static final List<Class<?>> primitiveNumbers = Arrays.asList(new Class<?>[] { int.class, float.class, double.class, short.class, byte.class, long.class });
	public static boolean isNumber(Class<?> fieldType) {
		return Number.class.isAssignableFrom(fieldType) || primitiveNumbers.contains(fieldType);
	}
	
	public static MessageSource buildMessageSource() {
		ResourceBundleMessageSource messageSource = new GunixResourceBundleMessageSource();
		messageSource.setBasename("messages");
		Utils.ms = messageSource; 
		return messageSource;
	}

	private static final Pattern MESSAGE_TOKENS = Pattern.compile("\\$\\{.+?\\}");
	private static MessageSource ms;
	public static String procesaMensaje(MessageSource ms, Class<?> clase, String mKey, String defaultMessage, Object... mArgs) {
		String mCKey = (clase != null ? (clase.getSimpleName() + ".") : "") + mKey;
		String keyDef = null;
		Matcher m = null;
		try {
			keyDef = ((GunixResourceBundleMessageSource) ms).getKeyDefinition(mCKey, Locale.getDefault());
			m = MESSAGE_TOKENS.matcher(keyDef);
			if (m.find()) {
				m.reset();
				return procesaAnidaciones(ms, clase, defaultMessage, keyDef, m, mArgs);
			} else {
				return ms.getMessage(mCKey, mArgs, defaultMessage, Locale.getDefault());
			}
		} catch (IllegalArgumentException ilArgEx) {
			return procesaAnidaciones(ms, clase, defaultMessage, keyDef, m, mArgs );
		}
	}

	private static String procesaAnidaciones(MessageSource ms, Class<?> clase, String defaultMessage, String keyDef, Matcher m, Object... mArgs) {
		Map<String, String> prevEvs = new HashMap<String, String>();
		while(m.find()){
			String nestedKey = m.group();
			String orNestedKey = nestedKey;
			if (nestedKey != null && nestedKey.length() > 3 && prevEvs.get(orNestedKey) == null) {
				nestedKey = nestedKey.substring(2, nestedKey.length() - 1);
				prevEvs.put(orNestedKey, procesaMensaje(ms, null, nestedKey, defaultMessage, mArgs));
			}
		}
		AtomicReference<String> aRef = new AtomicReference<String>(keyDef); 
		prevEvs.keySet().forEach(nKey->{
			aRef.set(aRef.get().replace(nKey, prevEvs.get(nKey)));
		});
		return aRef.get();
	}

	public static String gMssg(Class<?> clase, String mKey, String defaultMessage, Object... mArgs) {
		if (clase == null) {
			throw new IllegalArgumentException("La clase no puede ser null");
		}
		return procesaMensaje(ms, clase, mKey, defaultMessage, mArgs);
	}
	
	static private class GunixResourceBundleMessageSource extends ResourceBundleMessageSource{

		public String getKeyDefinition(String code, Locale locale) {
			return resolveCodeWithoutArguments(code, locale);
		}
		
	} 
}
