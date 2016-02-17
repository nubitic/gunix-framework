/*
 * Created on 5-apr-2005
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

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.XLElement;
import nl.fountain.xelem.excel.ss.SSRow;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 */
class SSRowBuilder extends AnonymousBuilder {
    
    private Row currentRow;
    private int currentCellIndex;
    
    SSRowBuilder(Director director) {
        super(director);
    }
    
    public void build(XMLReader reader, ContentHandler parent, XLElement xle) {
        setUpBuilder(reader, parent);
        currentRow = (SSRow) xle;
        currentCellIndex = 0;
    }
    
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (XLElement.XMLNS_SS.equals(uri)) {
            if ("Cell".equals(localName)) {
                String index = atts.getValue(XLElement.XMLNS_SS, "Index");
                if (index != null) {
                    currentCellIndex = Integer.parseInt(index);
                } else {
                    currentCellIndex++;
                }
                if (director.getBuildArea().isColumnPartOfArea(currentCellIndex)) {
	                Cell cell = currentRow.addCellAt(currentCellIndex);
	                cell.setIndex(currentCellIndex);
	                cell.setAttributes(atts);
	                Builder builder = director.getSSCellBuilder();
	                builder.build(reader, this, cell);
                }
            }
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (currentRow.getTagName().equals(localName)) {
            if (currentRow.getNameSpace().equals(uri)) {
            	for (ExcelReaderListener listener : director.getListeners()) {
                    listener.setRow(director.getCurrentSheetIndex(),
                            director.getCurrentSheetName(), currentRow);
                }
                reader.setContentHandler(parent);
                return;
            }
        }
        // no child elements?
    }

}
