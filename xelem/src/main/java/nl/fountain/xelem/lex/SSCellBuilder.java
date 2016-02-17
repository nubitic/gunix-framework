/*
 * Created on 22-mrt-2005
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

import nl.fountain.xelem.excel.Comment;
import nl.fountain.xelem.excel.XLElement;
import nl.fountain.xelem.excel.ss.SSCell;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 */
class SSCellBuilder extends AnonymousBuilder {
    
    private SSCell current;
    
    SSCellBuilder(Director director) {
        super(director);
    }
    
    public void build(XMLReader reader, ContentHandler parent, XLElement xle) {
        setUpBuilder(reader, parent);
        current = (SSCell) xle;
    }
    
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        contents.reset();
        if (XLElement.XMLNS_SS.equals(uri)) {
            if ("Data".equals(localName)) {
	            // set the atts of the data element
	            current.setAttributes(atts);
            } else if ("Comment".equals(localName)) {
                Comment comment = current.addComment();
                comment.setAttributes(atts);
                Builder builder = director.getAnonymousBuilder();
                builder.build(reader, this, comment);
            }
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (current.getNameSpace().equals(uri)) {
            if (current.getTagName().equals(localName)) {
            	for (ExcelReaderListener listener : director.getListeners()) {
                    listener.setCell(director.getCurrentSheetIndex(),
                            director.getCurrentSheetName(), 
                            director.getCurrentRowIndex(), current);
                }
                reader.setContentHandler(parent);
                return;
            }
        }
        current.setChildElement(localName, contents.toString());
    }

}
