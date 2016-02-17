/*
 * Created on Sep 7, 2004
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
package nl.fountain.xelem.excel;


/**
 * Represents the ExcelWorkbook element.
 */
public interface ExcelWorkbook extends XLElement {
    
    void setWindowHeight(int height);
    int getWindowHeight();
    
    void setWindowWidth(int width);
    int getWindowWidth();
    
    void setWindowTopX(int x);
    int getWindowTopX();
    
    void setWindowTopY(int y);
    int getWindowTopY();
    
    /**
     * Set the active sheet. 0-based.
     * @param nr the index of the active sheet.
     */
    void setActiveSheet(int nr);
    
    /**
     * Gets the active sheet. 0 based.
     * @return an int >= 0
     */
    int getActiveSheet();
    
    void setProtectStructure(boolean protect);
    boolean getProtectStructure();
    
    void setProtectWindows(boolean protect);
    boolean getProtectWindows();
    
}
