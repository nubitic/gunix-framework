/*
 * Created on 16-apr-2005
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.fountain.xelem.excel.AutoFilter;
import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Column;
import nl.fountain.xelem.excel.DocumentProperties;
import nl.fountain.xelem.excel.ExcelWorkbook;
import nl.fountain.xelem.excel.NamedRange;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Table;
import nl.fountain.xelem.excel.Worksheet;
import nl.fountain.xelem.excel.WorksheetOptions;

/**
 * Passes all events, values and instances unfiltered. It's up to the subclasses
 * of this class to take appropriate filtering action.
 * 
 * @since xelem.2.0
 */
public class DefaultExcelReaderFilter implements ExcelReaderFilter {
    
    private List<ExcelReaderListener> listeners;

    public List<ExcelReaderListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<ExcelReaderListener>();
        }
        return listeners;
    }

    public void addExcelReaderListener(ExcelReaderListener listener) {
        if (!getListeners().contains(listener)) {
            getListeners().add(listener);
        }
    }

    public boolean removeExcelReaderListener(ExcelReaderListener listener) {
        return getListeners().remove(listener);
    }

    public void clearExcelReaderListeners() {
        getListeners().clear();
    }
    
    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void startDocument() {
        for (ExcelReaderListener l : getListeners()) {
            l.startDocument();
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void processingInstruction(String target, String data) {
    	for (ExcelReaderListener l : getListeners()) {
            l.processingInstruction(target, data);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void startWorkbook(String systemID) {
    	for (ExcelReaderListener l : getListeners()) {
            l.startWorkbook(systemID);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void setDocumentProperties(DocumentProperties docProps) {
        for (ExcelReaderListener l : getListeners()) {
            l.setDocumentProperties(docProps);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void setExcelWorkbook(ExcelWorkbook excelWb) {
        for (ExcelReaderListener l : getListeners()) {
            l.setExcelWorkbook(excelWb);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void setNamedRange(NamedRange namedRange) {
        for (ExcelReaderListener l : getListeners()) {
            l.setNamedRange(namedRange);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void startWorksheet(int sheetIndex, Worksheet sheet) {
        for (ExcelReaderListener l : getListeners()) {
            l.startWorksheet(sheetIndex, sheet);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void setNamedRange(int sheetIndex, String sheetName,
            NamedRange namedRange) {
        for (ExcelReaderListener l : getListeners()) {
            l.setNamedRange(sheetIndex, sheetName, namedRange);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void startTable(int sheetIndex, String sheetName, Table table) {
        for (ExcelReaderListener l : getListeners()) {
            l.startTable(sheetIndex, sheetName, table);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void setColumn(int sheetIndex, String sheetName, Column column) {
        for (ExcelReaderListener l : getListeners()) {
            l.setColumn(sheetIndex, sheetName, column);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void setRow(int sheetIndex, String sheetName, Row row) {
        for (ExcelReaderListener l : getListeners()) {
            l.setRow(sheetIndex, sheetName, row);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void setCell(int sheetIndex, String sheetName, int rowIndex,
            Cell cell) {
        for (ExcelReaderListener l : getListeners()) {
            l.setCell(sheetIndex, sheetName, rowIndex, cell);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void setWorksheetOptions(int sheetIndex, String sheetName,
            WorksheetOptions wsOptions) {
        for (ExcelReaderListener l : getListeners()) {
            l.setWorksheetOptions(sheetIndex, sheetName, wsOptions);
        }
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void setAutoFilter(int sheetIndex, String sheetName,
            AutoFilter autoFilter) {
        for (ExcelReaderListener l : getListeners()) {
            l.setAutoFilter(sheetIndex, sheetName, autoFilter);
        }
    }
    
    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void endWorksheet(int sheetIndex, String sheetName) {
        for (ExcelReaderListener l : getListeners()) {
            l.endWorksheet(sheetIndex, sheetName);
        } 
    }

    /**
     * Passes the event unfiltered to it's listeners. 
     * Subclasses can override this method and take appropriate action.
     */
    public void endDocument(Map<String, String> prefixMap) {
        for (ExcelReaderListener l : getListeners()) {
            l.endDocument(prefixMap);
        }
    }

}
