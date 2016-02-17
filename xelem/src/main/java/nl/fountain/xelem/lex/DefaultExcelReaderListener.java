/*
 * Created on 8-apr-2005
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
 * Does effectively nothing. It's up to the subclasses of this class to do
 * effectively something.
 * 
 * @since xelem.2.0
 */
public class DefaultExcelReaderListener implements ExcelReaderListener {
    
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void startDocument() {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void processingInstruction(String target, String data) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void startWorkbook(String systemID) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void setDocumentProperties(DocumentProperties docProps) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void setExcelWorkbook(ExcelWorkbook excelWb) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void setNamedRange(NamedRange namedRange) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void startWorksheet(int sheetIndex, Worksheet sheet) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void setNamedRange(int sheetIndex, String sheetName,
            NamedRange namedRange) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void startTable(int sheetIndex, String sheetName, Table table) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void setColumn(int sheetIndex, String sheetName, Column column) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void setRow(int sheetIndex, String sheetName, Row row) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void setCell(int sheetIndex, String sheetName, int rowIndex,
            Cell cell) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void setWorksheetOptions(int sheetIndex, String sheetName,
            WorksheetOptions wsOptions) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void setAutoFilter(int sheetIndex, String sheetName,
            AutoFilter autoFilter) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void endWorksheet(int sheetIndex, String sheetName) {
    }
    /**
     * Does effectively nothing. Subclasses can override this method
     * to do effectively something.
     */
    public void endDocument(Map<String, String> prefixMap) {
    }

}
