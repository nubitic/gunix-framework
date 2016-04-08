package mx.com.gunix.framework.ui.vaadin;

import java.util.Arrays;
import java.util.Date;

import mx.com.gunix.framework.util.Utils;

public abstract class VaadinUtils {
	public static String getConversionError(Class<?> tipo) {
		if (Utils.isNumber(tipo)) {
			return "debe ser numérico";
		} else {
			if (Date.class.isAssignableFrom(tipo)) {
				return "Fecha inválida";
			} else {
				if (tipo.isEnum()) {
					return "debe ser uno de: " + Arrays.toString(tipo.getEnumConstants());
				} else {
					return "Valor inválido";
				}
			}

		}
	}
}
