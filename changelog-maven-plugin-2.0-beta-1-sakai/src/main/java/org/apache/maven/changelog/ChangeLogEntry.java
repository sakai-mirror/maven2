package org.apache.maven.changelog;

/* ====================================================================
 *   Copyright 2001-2004 The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * ====================================================================
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Change Log Entry - holds details about revisions to a file.
 *
 * @task add time of change to the entry
 * @task investigate betwixt for toXML method
 * @author <a href="mailto:dion@multitask.com.au">dIon Gillard</a>
 * @version $Id$
 */
public class ChangeLogEntry
{
    /**
     * Escaped <code>&lt;</code> entity
     */
    public static final String LESS_THAN_ENTITY = "&lt;";
    
    /**
     * Escaped <code>&gt;</code> entity
     */
    public static final String GREATER_THAN_ENTITY = "&gt;";
    
    /**
     * Escaped <code>&amp;</code> entity
     */
    public static final String AMPERSAND_ENTITY = "&amp;";
    
    /**
     * Escaped <code>'</code> entity
     */
    public static final String APOSTROPHE_ENTITY = "&apos;";
    
    /**
     * Escaped <code>"</code> entity
     */
    public static final String QUOTE_ENTITY = "&quot;";
    
    /**
     * Formatter used by the getDateFormatted method.
     */
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Formatter used by the getTimeFormatted method.
     */
    private static final SimpleDateFormat TIME_FORMAT = 
        new SimpleDateFormat("HH:mm:ss");

    
    /** Date the changes were committed */
    private Date date;
    
    /** User who made changes */
    private String author;
    
    /** comment provided at commit time */
    private String comment = "";
    
    /** ChangeLogFiles committed on the date, by the author, with comment*/
    private Vector files = new Vector();
    
    /**
     * Constructor used when attributes aren't available until later
     */
    public ChangeLogEntry() 
    {
    }
    
    /**
     * Adds a file to the list for this entry
     * @param file a {@link ChangeLogFile}
     */
    public void addFile(ChangeLogFile file)
    {
        files.addElement(file);
    }
    
    /** 
     * Adds a feature to the File attribute of the Entry object.
     * @param file the file name committed
     * @param revision the revision of the latest change
     */
    public void addFile(String file, String revision)
    {
        files.addElement(new ChangeLogFile(file, revision));
    }

    /** 
     * Get ChangeLogFiles associated with this entry
     *
     * @return a Vector collection of ChangeLogFile
     */
    public Vector getFiles()
    {
        return files;
    }

    /**
     * @return a string representation of the entry
     */
    public String toString()
    {
        return author + "\n" + date + "\n" + files + "\n" + comment;
    }

    /**
     * Provide the changelog entry as an XML snippet.
     * 
     * @task make sure comment doesn't contain CDATA tags - MAVEN114
     * @return a changelog-entry in xml format
     */
    public String toXML()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\t<changelog-entry>\n")
            .append("\t\t<date>")
            .append(getDateFormatted())
            .append("</date>\n")
            .append("\t\t<time>")
            .append(getTimeFormatted())
            .append("</time>\n")
            .append("\t\t<author><![CDATA[")
            .append(author)
            .append("]]></author>\n");
        
        for (Enumeration e = files.elements(); e.hasMoreElements();)
        {
            ChangeLogFile file = (ChangeLogFile) e.nextElement();
            buffer.append("\t\t<file>\n")
                .append("\t\t\t<name>")
                .append(escapeValue(file.getName()))
                .append("</name>\n")
                .append("\t\t\t<revision>")
                .append(file.getRevision())
                .append("</revision>\n");
            buffer.append("\t\t</file>\n");
        }
        buffer.append("\t\t<msg><![CDATA[")
            .append(removeCDataEnd(comment))
            .append("]]></msg>\n");
        buffer.append("\t</changelog-entry>\n");
        
        return buffer.toString();
    }
    
    /**
     * Getter for property author.
     * @return Value of property author.
     */
    public String getAuthor()
    {
        return author;
    }
    
    /**
     * Setter for property author.
     * @param author New value of property author.
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }
    
    /**
     * Getter for property comment.
     * @return Value of property comment.
     */
    public String getComment()
    {
        return comment;
    }
    
    /**
     * Setter for property comment.
     * @param comment New value of property comment.
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    
    /**
     * Getter for property date.
     * @return Value of property date.
     */
    public Date getDate()
    {
        return (Date) date.clone();
    }
    
    /**
     * Setter for property date.
     * @param date New value of property date.
     */
    public void setDate(Date date)
    {
        this.date = new Date(date.getTime());
    }

    /**
     * @return date in yyyy-mm-dd format
     */
    public String getDateFormatted()
    {
        return DATE_FORMAT.format(getDate());
    }
    
    /**
     * @return time in HH:mm:ss format
     */
    public String getTimeFormatted()
    {
        return TIME_FORMAT.format(getDate());
    }
    
    /**
     * remove a <code>]]></code> from comments (replace it with <code>] ] ></code>).
     * @param message
     * @return
     */
    private String removeCDataEnd(String message)
    {
        // check for invalid sequence ]]>
        int endCdata;
        while (message != null && (endCdata = message.indexOf("]]>")) > -1)
        {
            message = message.substring(0, endCdata) + "] ] >" + message.substring(endCdata + 3, message.length());
        }
        return message;
    }
    
    /** 
     * <p>Escape the <code>toString</code> of the given object. 
     * For use in an attribute value.</p> 
     * 
     * swiped from jakarta-commons/betwixt -- XMLUtils.java 
     * 
     * @param value escape <code>value.toString()</code> 
     * @return text with characters restricted (for use in attributes) escaped 
     */ 
    public static final String escapeValue(Object value) { 
        StringBuffer buffer = new StringBuffer(value.toString()); 
        for (int i=0, size = buffer.length(); i <size; i++) { 
            switch (buffer.charAt(i)) { 
                case '<': 
                    buffer.replace(i, i+1, LESS_THAN_ENTITY); 
                    size += 3; 
                    i+=3; 
                    break; 
                case '>': 
                    buffer.replace(i, i+1, GREATER_THAN_ENTITY); 
                    size += 3; 
                    i += 3; 
                    break; 
                case '&': 
                    buffer.replace(i, i+1, AMPERSAND_ENTITY); 
                    size += 4; 
                    i += 4; 
                    break; 
                case '\'': 
                    buffer.replace(i, i+1, APOSTROPHE_ENTITY); 
                    size += 4; 
                    i += 4; 
                    break; 
                case '\"': 
                    buffer.replace(i, i+1, QUOTE_ENTITY); 
                    size += 5; 
                    i += 5; 
                    break; 
            } 
        } 
        return buffer.toString(); 
    }
}
