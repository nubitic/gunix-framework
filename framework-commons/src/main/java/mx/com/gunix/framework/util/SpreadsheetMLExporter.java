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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SpreadsheetMLExporter {
	private static final int MAX_REG_EXCEL_DEFAULT = 1048576;
	private static final String EXTENSION = ".xls";
	private static Logger log = Logger.getLogger(SpreadsheetMLExporter.class);
	
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

			stream=new ZipOutputStream(out);
            stream.setLevel(Deflater.BEST_COMPRESSION);
            stream.putNextEntry(new ZipEntry(new StringBuilder(StringUtils.stripAccents(nombreArchivo)).append(EXTENSION).toString()));
			progreso.despliegaMensaje("Extrayendo información");
			while (resultados.siguiente()) {
				if (numHoja == 0 && cont == 0) {
					MetaDatos md = resultados.getMetaDatos();
					for (int i = 1; i <= md.getCuentaColumnas(); i++) {
						String str_enc_nom = "";
						str_enc_nom = md.getNombreColumna(i);
						cabecera.add(str_enc_nom);
						tipos.add(new Integer(md.getTipoColumna(i)));
					}
                    byte[] bytes=CABECERA_WOORKBOOK.getBytes("ISO-8859-1");
                    if(bytes!=null&&bytes.length!=0){
                        stream.write(bytes, 0, bytes.length);
                    }
                    creaNuevaHoja(numHoja, cabecera, maxRegsHoja, stream, progreso);
				}
				if (cont == maxRegsHoja) {
					cont = 1;
					numHoja=cierraHoja(numHoja, stream);
                    creaNuevaHoja(numHoja, cabecera, maxRegsHoja, stream, progreso);
				}

				StringBuilder nuevoRegistro=new StringBuilder();
                nuevoRegistro.append("<Row>");
				for (int i = 0; i < cabecera.size(); i++) {
					String tipo=null;
                    Object valor=null;
					switch (((Integer) tipos.get(i)).intValue()) {
					case MetaDatos.NUMERICO:
						try {
							valor = resultados.getDouble(i + 1);
							tipo="Number";
						} catch (NumberFormatException ignorar) {
							valor = resultados.getString(i + 1);
							tipo="String";
						}
						break;
					default:
						valor = resultados.getString(i + 1);
						if (valor == null || (valor != null && ((String) valor).equalsIgnoreCase("null"))) {
							valor = "";
						}
						valor=((String)valor).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                        tipo="String";
					}
					if (valor != null && !"".equals(valor)) {
						nuevoRegistro.append("\n<Cell ss:Index=\"");
                        nuevoRegistro.append(i+1);
                        nuevoRegistro.append("\"><Data ss:Type=\"");
                        nuevoRegistro.append(tipo);
                        nuevoRegistro.append("\">");
                        nuevoRegistro.append(valor);
                        nuevoRegistro.append("</Data></Cell>");
					}
				}
				nuevoRegistro.append("\n</Row>\n");

                byte[] bytes=nuevoRegistro.toString().getBytes("ISO-8859-1");
                if(bytes!=null&&bytes.length!=0){
                    stream.write(bytes, 0, bytes.length);
                }
                stream.flush();
                cont++;
                conTotal++;
                progreso.registrosProcesados(conTotal);;
			}
            progreso.despliegaMensaje("Liberando recursos");
            if(conTotal==0){
                MetaDatos md=resultados.getMetaDatos();
                for(int i=1;i<=md.getCuentaColumnas();i++){
                    String str_enc_nom="";
                    str_enc_nom=md.getNombreColumna(i);
                    cabecera.add(str_enc_nom);
                    tipos.add(new Integer(md.getTipoColumna(i)));
                }
                byte[] bytes=CABECERA_WOORKBOOK.getBytes("ISO-8859-1");
                if(bytes!=null&&bytes.length!=0){
                    stream.write(bytes, 0, bytes.length);
                }
                numHoja=cierraHoja(numHoja, stream);
            }

            if(cont!=0){
                numHoja=cierraHoja(numHoja, stream);
            }

            byte[] bytes=PIE_WOORKBOOK.getBytes("ISO-8859-1");
            if(bytes!=null&&bytes.length!=0){
                stream.write(bytes, 0, bytes.length);
            }
            stream.flush();
            stream.closeEntry();
            stream.close();
		} catch (Throwable thr) {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}

			progreso.despliegaMensaje("Se presentó un error durante el proceso de exportación: " + thr.getMessage());
			progreso.terminarProceso(Progreso.Estatus.ERROR);
			log.error(thr);
			throw new RuntimeException(thr);
		} finally {
			resultados.liberar();
		}
		progreso.terminarProceso(Progreso.Estatus.OK);
	}

	private static int cierraHoja(int nH, ZipOutputStream stream) throws IOException{
        int numHoja=nH+1;

        byte[] bytes=PIE_HOJA.getBytes("ISO-8859-1");
        if(bytes!=null&&bytes.length!=0){
            stream.write(bytes, 0, bytes.length);
        }
        return numHoja;
    }

    private static String creaNuevaHoja(String nombreHoja, int numRegs, List<String> columnasCabecera){
        String cabeceraHoja="";
        cabeceraHoja+=CABECERA_HOJA;
        cabeceraHoja=cabeceraHoja.replaceAll("\\{0\\}", nombreHoja);
        if(columnasCabecera!=null&&!columnasCabecera.isEmpty()){
            cabeceraHoja=cabeceraHoja.replaceAll("\\{1\\}", String.valueOf(columnasCabecera.size()));
            cabeceraHoja=cabeceraHoja.replaceAll("\\{2\\}", String.valueOf(numRegs));
            for(int i=0;i<columnasCabecera.size();i++){
                cabeceraHoja+="\n<Column "+ESTILO_COLUMNA_WRAP+" ss:Width=\"106.5\"/>";
            }
            cabeceraHoja+="\n<Row>\n";
            for(int i=0;i<columnasCabecera.size();i++){
                cabeceraHoja+=" <Cell "+ESTILO_CABECERA+"><Data ss:Type=\"String\">"+columnasCabecera.get(i)+"</Data></Cell>\n";
            }
            cabeceraHoja+="</Row>\n";
        }
        return cabeceraHoja;
    }

    private static final String ESTILO_CABECERA="ss:StyleID=\"s25\"";
    private static final String ESTILO_COLUMNA_WRAP="ss:StyleID=\"s23\"";
    private static final String CABECERA_WOORKBOOK="<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n"+"<?mso-application progid=\"Excel.Sheet\"?>\n"+"<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n"+" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n"+" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n"+" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n"+" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n"+" <DocumentProperties xmlns=\"urn:schemas-microsoft-com:office:office\">\n"+"  <Author>gunix</Author>\n"+"  <LastAuthor>gunix</LastAuthor>\n"+"  <LastPrinted>2008-08-13T16:06:43Z</LastPrinted>\n"+"  <Created>2008-08-13T16:05:12Z</Created>\n"+"  <LastSaved>2008-08-13T16:07:00Z</LastSaved>\n"+"  <Company>EMPATIC</Company>\n"+"  <Version>11.9999</Version>\n"+" </DocumentProperties>\n"+" <ExcelWorkbook xmlns=\"urn:schemas-microsoft-com:office:excel\">\n"+"  <WindowHeight>13035</WindowHeight>\n"+"  <WindowWidth>15195</WindowWidth>\n"+"  <WindowTopX>120</WindowTopX>\n"+"  <WindowTopY>90</WindowTopY>\n"+"  <ProtectStructure>False</ProtectStructure>\n"+"  <ProtectWindows>False</ProtectWindows>\n"+" </ExcelWorkbook>\n"+" <Styles>\n"+"  <Style ss:ID=\"Default\" ss:Name=\"Normal\">\n"+"   <Alignment ss:Vertical=\"Bottom\"/>\n"+"   <Borders/>\n"+"   <Font/>\n"+"   <Interior/>\n"+"   <NumberFormat/>\n"+"   <Protection/>\n"+"  </Style>\n"+"  <Style ss:ID=\"s23\">\n"+"    <Alignment ss:Vertical=\"Bottom\" ss:WrapText=\"1\"/>\n"+"  </Style>\n"+"  <Style ss:ID=\"s25\">\n"+"   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Bottom\"/>\n"+"   <Borders>\n"+"    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n"+"    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n"+"    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n"+"    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n"+"   </Borders>\n"+"   <Font x:Family=\"Swiss\" ss:Bold=\"1\"/>\n"+"   <Interior ss:Color=\"#C0C0C0\" ss:Pattern=\"Solid\"/>\n"+"  </Style>\n"+" </Styles>\n";
    private static final String PIE_WOORKBOOK="</Workbook>";
    private static final String CABECERA_HOJA="<Worksheet ss:Name=\"{0}\">\n"+" <Table ss:ExpandedColumnCount=\"{1}\" ss:ExpandedRowCount=\"{2}\" x:FullColumns=\"1\" x:FullRows=\"1\" ss:DefaultColumnWidth=\"60\">";
    private static final String PIE_HOJA="</Table>\n"+" <WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">\n"+"  <PageSetup>\n"+"   <Header x:Margin=\"0\"/>\n"+"   <Footer x:Margin=\"0\"/>\n"+"   <PageMargins x:Bottom=\"0.984251969\" x:Left=\"0.78740157499999996\"\n"+"    x:Right=\"0.78740157499999996\" x:Top=\"0.984251969\"/>\n"+"  </PageSetup>\n"+"  <Print>\n"+"   <ValidPrinterInfo/>\n"+"   <PaperSizeIndex>9</PaperSizeIndex>\n"+"   <HorizontalResolution>600</HorizontalResolution>\n"+"   <VerticalResolution>600</VerticalResolution>\n"+"  </Print>\n"+"  <Selected/>\n"+"  <FreezePanes/>\n"+"  <FrozenNoSplit/>\n"+"  <SplitHorizontal>1</SplitHorizontal>\n"+"  <TopRowBottomPane>1</TopRowBottomPane>\n"+"  <ActivePane>2</ActivePane>\n"+"  <Panes>\n"+"   <Pane>\n"+"    <Number>3</Number>\n"+"   </Pane>\n"+"   <Pane>\n"+"    <Number>2</Number>\n"+"    <ActiveRow>1</ActiveRow>\n"+"   </Pane>\n"+"  </Panes>\n"+"  <ProtectObjects>False</ProtectObjects>\n"+"  <ProtectScenarios>False</ProtectScenarios>\n"+" </WorksheetOptions>\n"+"</Worksheet>\n";

    private static void creaNuevaHoja(int numHoja, List<String> cabecera, int maxRegsHoja, ZipOutputStream stream, Progreso progreso) throws IOException{
    	progreso.despliegaMensaje("Creando Hoja "+(numHoja+1));
        if(cabecera!=null&&!cabecera.isEmpty()){
            byte[] bytes=creaNuevaHoja("Hoja "+(numHoja+1), maxRegsHoja, cabecera).getBytes("ISO-8859-1");
            if(bytes!=null&&bytes.length!=0){
                stream.write(bytes, 0, bytes.length);
            }
        } else {
            throw new IllegalArgumentException("No se pudo establecer la cabecera del Archivo porque no se encontraron los MetaDatos de la Consulta");
        }
    }
}
