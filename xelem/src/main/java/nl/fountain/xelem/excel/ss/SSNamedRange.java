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
package nl.fountain.xelem.excel.ss;

import java.lang.reflect.Method;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

import nl.fountain.xelem.GIO;
import nl.fountain.xelem.excel.AbstractXLElement;
import nl.fountain.xelem.excel.NamedRange;

/**
 * An implementation of the XLElement NamedRange.
 */
public class SSNamedRange extends AbstractXLElement implements NamedRange {
    
    private String name;
    private String refersTo;
    private boolean hidden;

    
    /**
     * Creates a new SSNamedRange.
     * 
     * @see nl.fountain.xelem.excel.Workbook#addNamedRange(String, String)
     */
    public SSNamedRange(String name, String refersTo) {
        this.name = name;
        this.refersTo = refersTo;
    }
    
    public String getName() {
        return name;
    }
    
    private void setRefersTo(String s) {
        refersTo = s;
    }
    
    public String getRefersTo() {
        return refersTo;
    }

    public void setHidden(boolean hide) {
        hidden = hide;
    }
    
    private void setHidden(String s) {
        hidden = s.equals("1");
    }
    
    public boolean isHidden() {
        return hidden;
    }

    // @see nl.fountain.xelem.excel.XLElement#getTagName()
    public String getTagName() {
        return "NamedRange";
    }

    // @see nl.fountain.xelem.excel.XLElement#getNameSpace()
    public String getNameSpace() {
        return XMLNS_SS;
    }

    // @see nl.fountain.xelem.excel.XLElement#getPrefix()
    public String getPrefix() {
        return PREFIX_SS;
    }

    public Element assemble(Element parent, GIO gio) {
        Document doc = parent.getOwnerDocument();
        Element nre = assemble(doc, gio);
        
        nre.setAttributeNodeNS(createAttributeNS(doc, "Name", name));
        if (refersTo != null) nre.setAttributeNodeNS(
                createAttributeNS(doc, "RefersTo", refersTo));
        if (hidden) nre.setAttributeNodeNS(
                createAttributeNS(doc, "Hidden", "1"));
        
        parent.appendChild(nre);
        return nre;
    }
    
    public void setAttributes(Attributes attrs) {
        for (int i = 0; i < attrs.getLength(); i++) {
            invokeMethod(attrs.getLocalName(i), attrs.getValue(i));
        }
    }
    
	private void invokeMethod(String name, Object value) {
        Class[] types = new Class[] { value.getClass() };
        Method method = null;
        try {
            method = this.getClass().getDeclaredMethod("set" + name, types);
            method.invoke(this, new Object[] { value });
        } catch (NoSuchMethodException e) {
            // no big deal
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
