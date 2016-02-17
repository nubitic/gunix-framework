/*
 * Created on 17-okt-2004
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
 * Indicates that a certain name allready exists in an otherwise unique name-sequence.
 */
public class DuplicateNameException extends RuntimeException {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DuplicateNameException() {
        super();
    }
    
    public DuplicateNameException(String message) {
        super(message);
    }

}
