/*
 * Created on 17-mrt-2005
 * Copyright 2013 Henk van den Berg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * see license.txt
 */
package nl.fountain.xelem.lex;

import nl.fountain.xelem.excel.AutoFilter;
import nl.fountain.xelem.excel.Column;
import nl.fountain.xelem.excel.NamedRange;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Table;
import nl.fountain.xelem.excel.WorksheetOptions;
import nl.fountain.xelem.excel.XLElement;
import nl.fountain.xelem.excel.ss.SSColumn;
import nl.fountain.xelem.excel.ss.SSNamedRange;
import nl.fountain.xelem.excel.ss.SSRow;
import nl.fountain.xelem.excel.ss.SSTable;
import nl.fountain.xelem.excel.x.XAutoFilter;
import nl.fountain.xelem.excel.x.XWorksheetOptions;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *  
 */
class SSWorksheetBuilder extends AnonymousBuilder {

    private int currentRowIndex;
    private int currentColumnIndex;
    
    SSWorksheetBuilder(Director director) {
        super(director);
    }

    public void build(XMLReader reader, ContentHandler parent) {
        setUpBuilder(reader, parent);
        currentRowIndex = 0;
        currentColumnIndex = 0;
    }
    
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (XLElement.XMLNS_SS.equals(uri)) {
	        startSSElement(localName, atts);
        } else if (XLElement.XMLNS_X.equals(uri)) {
            if ("WorksheetOptions".equals(localName)) {
                WorksheetOptions wso = new XWorksheetOptions();
                Builder builder = director.getAnonymousBuilder();
                builder.build(reader, this, wso);
            } else if ("AutoFilter".equals(localName)) {
                AutoFilter autoF = new XAutoFilter();
                autoF.setRange(atts.getValue(XLElement.XMLNS_X, "Range"));
                for (ExcelReaderListener listener : director.getListeners()) {
                    listener.setAutoFilter(director.getCurrentSheetIndex(),
                            director.getCurrentSheetName(), autoF);
                }
            }
        }        
    }

    private void startSSElement(String localName, Attributes atts) {
        if ("Row".equals(localName)) {
            String index = atts.getValue(XLElement.XMLNS_SS, "Index");
            if (index != null) {
                currentRowIndex = Integer.parseInt(index);
            } else {
                currentRowIndex++;
            }
            if (director.getBuildArea().isRowPartOfArea(currentRowIndex)) {
	            Row row = new SSRow();
	            row.setIndex(currentRowIndex);
	            row.setAttributes(atts);
	            director.setCurrentRowIndex(currentRowIndex);
	            Builder builder = director.getSSRowBuilder();
	            builder.build(reader, this, row);
            }
        } else if ("Column".equals(localName)) {
            String index = atts.getValue(XLElement.XMLNS_SS, "Index");
            if (index != null) {
                currentColumnIndex = Integer.parseInt(index);
            } else {
                currentColumnIndex++;
            }
            if (director.getBuildArea().isColumnPartOfArea(currentColumnIndex)) {
                Column column = new SSColumn();
                column.setIndex(currentColumnIndex);
	            column.setAttributes(atts);
	            for (ExcelReaderListener listener : director.getListeners()) {
	                listener.setColumn(director.getCurrentSheetIndex(), 
	                        director.getCurrentSheetName(), column);
	            }
            }
        } else if ("Table".equals(localName)) {
            Table table = new SSTable();
            table.setAttributes(atts);
            for (ExcelReaderListener listener : director.getListeners()) {
                listener.startTable(director.getCurrentSheetIndex(),
                        director.getCurrentSheetName(), table);
            }
        } else if ("NamedRange". equals(localName)) {
            String name = atts.getValue(XLElement.XMLNS_SS, "Name");
            NamedRange nr = new SSNamedRange(name, null);
            nr.setAttributes(atts);
            for (ExcelReaderListener listener : director.getListeners()) {
                listener.setNamedRange(director.getCurrentSheetIndex(),
                        director.getCurrentSheetName(), nr);
            }
        } 
    }
    

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("Worksheet".equals(localName)) {
            if (XLElement.XMLNS_SS.equals(uri)) {
            	for (ExcelReaderListener listener : director.getListeners()) {
                    listener.endWorksheet(director.getCurrentSheetIndex(),
                            director.getCurrentSheetName());
                }
                reader.setContentHandler(parent);
                return;
            }
        }
        // no child elements?
    }

}