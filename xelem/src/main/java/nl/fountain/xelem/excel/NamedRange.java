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
package nl.fountain.xelem.excel;

/**
 * Represents the NamedRange element.
 */
public interface NamedRange extends XLElement {
    
    String getName();
    
    String getRefersTo();
    
    /**
     * Sets whether this named range will be displayed in the
     * drop-down box of the Excel application. The default is false:
     * do not hide the name.
     */
    void setHidden(boolean hide);
    
    boolean isHidden();

}
