/*
 * Created on Oct 15, 2004
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
 * Represents the WorksheetOptions element.
 */
public interface WorksheetOptions extends XLElement {
    
    public static final String SHEET_VISIBLE = "SheetVisible";
    public static final String SHEET_HIDDEN = "SheetHidden";
    public static final String SHEET_VERY_HIDDEN = "SheetVeryHidden";
    
    void setTopRowVisible(int tr);
    int getTopRowVisible();
    void setLeftColumnVisible(int lc);
    int getLeftColumnVisible();
    
    void setZoom(int z);
    int getZoom();
    
    void setGridlineColor(int r, int g, int b);
    String getGridlineColor();
    
    void setTabColorIndex(int ci);
    int getTabColorIndex();
    
    void setSelected(boolean s);
    boolean isSelected();
    
    void doNotDisplayHeadings(boolean b);
    boolean displaysNoHeadings();
    
    void doNotDisplayGridlines(boolean b);
    boolean displaysNoGridlines();
    
    void doDisplayFormulas(boolean f);
    boolean displaysFormulas();
    
    void setVisible(String wsoValue);
    String getVisible();
    
    void setActiveCell(int r, int c);
    void setActiveCell(int paneNumber, int r, int c);
    
    /**
     * Sets a range selection. The active cell must also be set within
     * the range in order for this method to have effect.
     * 
     * @param rcRange A String in R1C1 reference style.
     * @see #setActiveCell(int, int)
     */
    void setRangeSelection(String rcRange);
    void setRangeSelection(Area area);
    
    void setRangeSelection(int paneNumber, String rcRange);
    
    void splitHorizontal(int points, int topRow);
    void splitVertical(int points, int leftColumn);
    
    boolean hasHorizontalSplit();
    boolean hasVerticalSplit();
    boolean hasSplit();
    
    void freezePanesAt(int r, int c);
    boolean hasFrozenPanes();
    
}
