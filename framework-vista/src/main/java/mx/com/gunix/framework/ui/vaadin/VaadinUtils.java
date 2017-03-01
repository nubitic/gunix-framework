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
	
	public static final String SELECTED_APP_TAB_REQUEST_PARAMETER = "_selectedAppTab";
	public static final String APLICACIONES_TABSHEET_COMPONENT_ID =  "gx_aplicaciones_tabsheet";
	public static final String MENU_BAR_COMPONENT_ID =  "gx_menuBar";
	public static final String ROL_COMBO_COMPONENT_ID =  "gx_rol_cbx";
	public static final String GUNIX_VAADIN_IFRAME_ID = "gnxVdnIF";
}
