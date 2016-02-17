/*
 * Created on 1-nov-2004
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

import java.util.Set;
import java.util.TreeSet;

/**
 * Gathers global information about a workbook during assembly.
 * <P>
 * A {@link nl.fountain.xelem.excel.ss.XLWorkbook} is a rather loosely organized
 * set of java class instances. In order to not only make valid xml, but to also
 * assure that the produced xml will open in Excel, this class 
 * collects global information about a workbook during assembly.
 * The Workbook uses the information to set additional elements or element-
 * attributes when producing a {@link org.w3c.dom.Document} with the method
 * {@link nl.fountain.xelem.excel.Workbook#createDocument() createDocument}.
 * 
 * 
 */
public class GIO {
    
    private Set<String> styleIDSet;
    private int selectedSheetsCount;
    private boolean printComments;
    
    /**
     * Adds the styleID to the set of styleID's of this GIO.
     * 
     * @param 	styleID	The styleID to be added.
     */
    public void addStyleID(String styleID) {
        getStyleIDSet().add(styleID);
    }
    
    /**
     * Gets the set of styleID's previously added with
     * {@link #addStyleID(String)}.
     * 
     * @return 	A set of styleID's.
     */
    public Set<String> getStyleIDSet() {
        if (styleIDSet == null) {
            styleIDSet = new TreeSet<String>();
        }
        return styleIDSet;
    }
    
    /**
     * Increases the number of selected sheets by one.
     */
    public void increaseSelectedSheets() {
        selectedSheetsCount++;
    }
    
    /**
     * Gets the number of selected sheets.
     * 
     * @return The number of selected sheets.
     */
    public int getSelectedSheetsCount() {
        return selectedSheetsCount;
    }
    
    /**
     * Sets whether the workbook's 
     * {@link nl.fountain.xelem.excel.Workbook#createDocument() createDocument}
     * -method will print comments.
     * 
     * @param 	print	<code>false</code> if comments must be ignored.
     * 
     * @see nl.fountain.xelem.excel.Workbook#setPrintElementComments(boolean print)
     */
    public void setPrintComments(boolean print) {
        printComments = print;
    }
    
    /**
     * Specifies whether the workbook's
     * {@link nl.fountain.xelem.excel.Workbook#createDocument() createDocument}
     * -method will print comments.
     *  
     * @return 	<code>true</code> if comments will be printed, 
     * 			<code>false</code> otherwise.
     */
    public boolean isPrintingComments() {
        return printComments;
    }

}
