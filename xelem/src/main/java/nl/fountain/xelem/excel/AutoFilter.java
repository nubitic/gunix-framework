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
 * Represents the AutoFilter element. 
 * <P>
 * There's a lot more to autofiltering
 * than this interface provides. However, if it suffices to just set the
 * autofiltering-mode to 'on' for a certain range, here's all you need.
 * <P>
 * Usually you don't have to deal with implementations of this interface.
 * You just call the Worksheet-method 
 * {@link nl.fountain.xelem.excel.Worksheet#setAutoFilter(String)}.
 */
public interface AutoFilter extends XLElement {
    
    
    void setRange(String rcString);
    String getRange();

}
