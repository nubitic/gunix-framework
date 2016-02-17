/*
 * Created on Nov 3, 2004
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

import nl.fountain.xelem.Area;

/**
 * Represents the Pane element. 
 * <P>
 * Excel can split a window into a maximum of 4 panes. Panes come into vision
 * when you apply the 
 * {@link nl.fountain.xelem.excel.WorksheetOptions#splitHorizontal(int, int) splitHorizontal}-,
 * {@link nl.fountain.xelem.excel.WorksheetOptions#splitVertical(int, int) splitVertical}- or
 * {@link nl.fountain.xelem.excel.WorksheetOptions#freezePanesAt(int, int) freezePanesAt}-methods
 * of the {@link nl.fountain.xelem.excel.WorksheetOptions}.
 * 
 * 
 * @see nl.fountain.xelem.excel.WorksheetOptions#setActiveCell(int, int, int)
 * @see nl.fountain.xelem.excel.WorksheetOptions#setActiveCell(int, int)
 * @see nl.fountain.xelem.excel.WorksheetOptions#freezePanesAt(int, int)
 * @see nl.fountain.xelem.excel.WorksheetOptions#splitHorizontal(int, int)
 * @see nl.fountain.xelem.excel.WorksheetOptions#splitVertical(int, int)
 * @see nl.fountain.xelem.excel.WorksheetOptions#setRangeSelection(int, String)
 * @see nl.fountain.xelem.excel.WorksheetOptions#setRangeSelection(String)
 */
public interface Pane extends XLElement {
    
    /**
     * Variable indicating the top left pane. The top left pane is the standard
     * pane which is allways visible.
     */
    public static final int TOP_LEFT = 3;
    
    /**
     * Variable indicating the bottom left pane. The bottom left pane can
     * only be addressed if a window is split horizontally, either by splitting
     * or freezing panes.
     */
    public static final int BOTTOM_LEFT = 2;
    
    /**
     * Variable indicating the top right pane. The top right pane can 
     * only be addressed if a window is split vertically, either by splitting 
     * or freezing panes.
     */
    public static final int TOP_RIGHT = 1;
    
    /**
     * Variable indicating the bottom right pane. The bottom right pane can
     * only be addressed if a window is split vertically <em>and</em> horizontally,
     * either by spltting or freezing panes.
     */
    public static final int BOTTOM_RIGHT = 0;
    
    int getNumber();
    void setActiveCell(int row, int col);
    void setActiveCol(int col);
    int getActiveCol();
    void setActiveRow(int row);
    int getActiveRow();
    void setRangeSelection(String rc);
    void setRangeSelection(Area area);
    String getRangeSelection();

}
