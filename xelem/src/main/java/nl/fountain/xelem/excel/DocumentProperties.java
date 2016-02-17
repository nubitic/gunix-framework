/*
 * Created on 8-nov-2004
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

import java.util.Date;

/**
 * Represents the DocumentProperties element.
 */
public interface DocumentProperties extends XLElement {
    
    void setTitle(String title);
    String getTitle();
    
    void setSubject(String subject);
    String getSubject();
    
    void setKeywords(String keywords);
    String getKeywords();
    
    void setDescription(String description);
    String getDescription();
    
    void setCategory(String category);
    String getCategory();
    
    void setAuthor(String author);
    String getAuthor();
    
    void setLastAuthor(String lastAuthor);
    String getLastAuthor();
    
    void setManager(String manager);
    String getManager();
    
    void setCompany(String company);
    String getCompany();
    
    /**
     * Sets the hyperlinkbase for this workbook. The hyperlinkbase -when set-
     * is prepended before link-values in cells.
     * For instance:
     * a hyperlinkbase of <code>"file://C:/foo/"</code> and a cell where upon
     * the HRef is set as <code>"bar.txt"</code> will result in the link encountered
     * in that cell pointing to <code>"file://C:/foo/bar.txt"</code>.
     */
    void setHyperlinkBase(String hyperlinkbase);
    String getHyperlinkBase();
    
    void setAppName(String appname);
    String getAppName();
    
    void setCreated(Date created);
    Date getCreated();
    
    void setLastSaved(Date lastsaved);
    Date getLastSaved();
    
    Date getLastPrinted();
    String getVersion();
}
