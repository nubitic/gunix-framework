/*
 * Created on 9-nov-2004
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
package nl.fountain.xelem;

import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.fountain.xelem.excel.Workbook;

import org.w3c.dom.Document;

/**
 * A conveniance class for serializing Workbooks.
 * 
 */
public class XSerializer {
    
    private Transformer xformer;
    private String encoding;
    
    public static final String US_ASCII = "US-ASCII";
    
    public XSerializer() {}
    
    public XSerializer(String encoding) {
        this.encoding = encoding;
    }
    
    /**
     * 
     */
    public String serializeToString(Workbook wb) throws XelemException {
        StringWriter out = new StringWriter();
        serialize(wb, out);
        return out.toString();
    }
    
    public String serializeToString(Document doc) throws XelemException {
        StringWriter out = new StringWriter();
        serialize(doc, out);
        return out.toString();
    }
    
    /**
     * Serializes the Workbook to the file specified with the Workbook's
     * {@link nl.fountain.xelem.excel.Workbook#getFileName() getFileName}-method.
     * 
     */
    public void serialize(Workbook wb) throws XelemException {
        File out = new File(wb.getFileName());
        serialize(wb, out);
    }
    
    public void serialize(Workbook wb, File out) throws XelemException {
        Result result = new StreamResult(out);
        transform(wb, result); 
    }
    
    public void serialize(Document doc, File out) throws XelemException {
        Result result = new StreamResult(out);
        transform(doc, result); 
    }
    
    public void serialize(Workbook wb, OutputStream out) throws XelemException {
        Result result = new StreamResult(out);
        transform(wb, result);
    }
    
    public void serialize(Document doc, OutputStream out) throws XelemException {
        Result result = new StreamResult(out);
        transform(doc, result);
    }
    
    public void serialize(Workbook wb, Writer out) throws XelemException {
        Result result = new StreamResult(out);
        transform(wb, result);
    }
    
    public void serialize(Document doc, Writer out) throws XelemException {
        Result result = new StreamResult(out);
        transform(doc, result);
    }
    
    private void transform(Workbook wb, Result result) throws XelemException {
        try {
            Document doc = wb.createDocument();
            transform(doc, result);
        } catch (ParserConfigurationException e) {
            throw new XelemException(e.fillInStackTrace());
        }
    }
    
    private void transform(Document doc, Result result) throws XelemException {
        try {
            Transformer xformer = getTransformer();          
            Source source = new DOMSource(doc);
            xformer.transform(source, result);
        } catch (TransformerException e) {
            throw new XelemException(e.fillInStackTrace());
        }
    }

    private Transformer getTransformer() throws XelemException {
        if (xformer == null) {
	        TransformerFactory tFactory = TransformerFactory.newInstance();
	        try {
	            xformer = tFactory.newTransformer();
	        } catch (TransformerConfigurationException e) {
	            throw new XelemException(e.fillInStackTrace());
	        }
	        xformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        xformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        if (encoding != null) {
	            xformer.setOutputProperty(OutputKeys.ENCODING, encoding);
	        }
        }
        return xformer;
    }

}
