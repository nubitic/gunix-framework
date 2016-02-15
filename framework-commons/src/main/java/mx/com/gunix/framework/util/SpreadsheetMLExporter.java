package mx.com.gunix.framework.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mx.com.gunix.framework.util.spreadsheetmlexporter.DatosExportar;
import mx.com.gunix.framework.util.spreadsheetmlexporter.MetaDatos;
import mx.com.gunix.framework.util.spreadsheetmlexporter.Progreso;
import nl.fountain.xelem.XSerializer;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Workbook;
import nl.fountain.xelem.excel.Worksheet;
import nl.fountain.xelem.excel.ss.XLWorkbook;

import org.apache.commons.lang3.StringUtils;

public class SpreadsheetMLExporter {
	private static final int MAX_REG_EXCEL_DEFAULT = 1048576;
	private static final String EXTENSION = ".xml";

	public static void exportaArchivo(String nombreArchivo, OutputStream out, DatosExportar resultados, Progreso progreso) {
		// Bloque de consulta y construcción de excel.
		ZipOutputStream stream = null;
		int maxRegsHoja = MAX_REG_EXCEL_DEFAULT;
		try {
			List<String> cabecera = new ArrayList<String>();
			List<Integer> tipos = new ArrayList<Integer>();

			int cont = 0;
			int conTotal = 0;
			int numHoja = 0;

			progreso.despliegaMensaje("Extrayendo información");

			Workbook wb = null;
			Worksheet ws = null;

			while (resultados.siguiente()) {
				if (numHoja == 0 && cont == 0) {
					MetaDatos md = resultados.getMetaDatos();
					for (int i = 1; i <= md.getCuentaColumnas(); i++) {
						String str_enc_nom = "";
						str_enc_nom = md.getNombreColumna(i);
						cabecera.add(str_enc_nom);
						tipos.add(new Integer(md.getTipoColumna(i)));
					}
					wb = new XLWorkbook(nombreArchivo);
					ws = nuevaHoja(wb, numHoja, cabecera);
				}
				if (cont == maxRegsHoja) {
					cont = 1;
					numHoja++;
					ws = nuevaHoja(wb, numHoja, cabecera);
				}

				Row row = ws.addRow();
				for (int i = 0; i < cabecera.size(); i++) {
					Object valor = null;
					switch (((Integer) tipos.get(i)).intValue()) {
					case MetaDatos.NUMERICO:
						try {
							valor = resultados.getDouble(i + 1);
						} catch (NumberFormatException ignorar) {
							valor = resultados.getString(i + 1);
						}
						break;
					default:
						valor = resultados.getString(i + 1);
						if (valor == null || (valor != null && ((String) valor).equalsIgnoreCase("null"))) {
							valor = "";
						}
					}
					if (valor != null && !"".equals(valor)) {
						row.addCellAt(i + 1).setData(valor);
					}
				}
				cont++;
				conTotal++;
				progreso.registrosProcesados(conTotal);
			}
			
			XSerializer xs = new XSerializer("ISO-8859-1");
			stream = new ZipOutputStream(out);
			stream.setLevel(Deflater.BEST_COMPRESSION);
			stream.putNextEntry(new ZipEntry(new StringBuilder(StringUtils.stripAccents(nombreArchivo)).append(EXTENSION).toString()));
			xs.serialize(wb, stream);
			stream.flush();
			stream.closeEntry();
            stream.close();
			progreso.despliegaMensaje("Liberando recursos");
		} catch (Throwable thr) {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}

			progreso.despliegaMensaje("Se presentó un error durante el proceso de exportación: " + thr.getMessage());
			progreso.terminarProceso(Progreso.Estatus.ERROR);
			throw new RuntimeException(thr);
		} finally {
			resultados.liberar();
		}
		progreso.terminarProceso(Progreso.Estatus.OK);
	}

	private static Worksheet nuevaHoja(Workbook wb, int numHoja, List<String> cabecera) {
		Worksheet ws = wb.addSheet("Hoja " + (numHoja + 1));
		cabecera.forEach(colName -> {
			ws.addCell(colName, "s25");
		});
		return ws;
	}
}
