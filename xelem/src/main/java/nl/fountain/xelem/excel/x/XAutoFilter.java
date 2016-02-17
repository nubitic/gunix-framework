/*
 * Created on Nov 4, 2004
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
 *
 */
package nl.fountain.xelem.excel.x;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nl.fountain.xelem.GIO;
import nl.fountain.xelem.excel.AbstractXLElement;
import nl.fountain.xelem.excel.AutoFilter;

/**
 * An implementation of the XLElement AutoFilter.
 * 
 * @see nl.fountain.xelem.excel.Worksheet#setAutoFilter(String)
 */
public class XAutoFilter extends AbstractXLElement implements AutoFilter {
    
    private String range;
    
    /**
     * Constructs a new XAutoFilter.
     * 
     * @see nl.fountain.xelem.excel.Worksheet#setAutoFilter(String)
     */
    public XAutoFilter() {}

    public void setRange(String rcString) {
        range = rcString;
    }
    
    public String getRange() {
        return range;
    }

    public String getTagName() {
        return "AutoFilter";
    }

    public String getNameSpace() {
        return XMLNS_X;
    }

    public String getPrefix() {
        return PREFIX_X;
    }

    public Element assemble(Element parent, GIO gio) {
        if (getRange() != null) {
            Document doc = parent.getOwnerDocument();
	        Element afe = assemble(doc, gio);
	        afe.setAttributeNodeNS(createAttributeNS(doc, "Range", getRange()));
	        parent.appendChild(afe);
	        return afe;
        } else {
            return null;
        }
    }

}
