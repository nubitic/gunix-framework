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

import java.util.List;

/**
 * Recieve notification of parsing events and the construction of 
 * {@link nl.fountain.xelem.excel.XLElement XLElements} and transmit these
 * events, values and instances to the listeners registered with this
 * ExcelReaderFilter.
 * 
 * @see <a href="package-summary.html#package_description">package overview</a>
 * @since xelem.2.0
 * 
 */
public interface ExcelReaderFilter extends ExcelReaderListener {
    
    /**
     * Gets a list of registered listeners.
     * 
     * @return a list of registered listeners
     */
    List<ExcelReaderListener> getListeners();
    
    /**
     * Registers the given listener.
     * @param listener the listener to be registered
     */
    void addExcelReaderListener(ExcelReaderListener listener);
    
    /**
     * Remove the specified listener.
     * @param listener the listener to be removed
     * @return <code>true</code> if the given listener was registered 
     * 		with this ExcelReaderFilter, <code>false</code> otherwise.
     */
    boolean removeExcelReaderListener(ExcelReaderListener listener);
    
    /**
     * Remove all registered listeners.
     *
     */
    void clearExcelReaderListeners();
}
