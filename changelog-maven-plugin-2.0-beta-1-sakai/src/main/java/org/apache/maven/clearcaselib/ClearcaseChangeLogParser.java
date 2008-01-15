package org.apache.maven.clearcaselib;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;


import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.changelog.ChangeLogParser;
import org.apache.maven.changelog.ChangeLogEntry;
import org.apache.maven.changelog.ChangeLogFile;

/**
 * Clearcase specific implementation of ChangeLogParser interface. 
 * 
 * @author <a href="mailto:aldarion@virgilio.it">Simone Zorzetti</a>
 */
public class ClearcaseChangeLogParser implements ChangeLogParser {

    /**
     * Formatter used to parse Clearcase date/timestamp.
     */
    private static final SimpleDateFormat CLEARCASE_TIMESTAMP_FORMAT =
        new SimpleDateFormat("yyyyMMdd.HHmmss");

    private static final String NAME_TAG = "NAME:";
    private static final String USER_TAG = "USER:";
    private static final String DATE_TAG = "DATE:";
    private static final String COMMENT_TAG = "COMM:";
    
    /**
     * Custom date/time formatter.  Rounds ChangeLogEntry times to the nearest
     * minute.
     */
    private static final SimpleDateFormat ENTRY_KEY_TIMESTAMP_FORMAT = 
        new SimpleDateFormat("yyyyMMddHHmm");

    /**
     * Custom date/time formatter.  Rounds ChangeLogEntry to date
     */
    private static final SimpleDateFormat GROUPED_ENTRY_KEY_TIMESTAMP_FORMAT = 
        new SimpleDateFormat("yyyyMMdd");
    
    /**
     * rcs entries, in reverse (date, time, author, comment) order
     */
    private Map entries = new TreeMap(Collections.reverseOrder());

    // state machine constants for reading clearcase lshistory command output
    /** expecting file information */
    private static final int GET_FILE = 1;
    /** expecting date */
    private static final int GET_DATE = 2;
    /** expecting comments */
    private static final int GET_COMMENT = 3;

    /** current status of the parser */
    private int status = GET_FILE;
    
    /** the current log entry being processed by the parser*/
    private ChangeLogEntry currentLogEntry = null;
    
    /** the current file being processed by the parser */
    private ChangeLogFile currentFile = null;

    /**
     * Create a new ChangeLogParser.
     */
    public ClearcaseChangeLogParser()
    {
    }

    /**
     * initialize the parser from the change log
     * @param changeLog the controlling task
     * @see ChangeLogParser#init(ChangeLog)
     */ 
    public void init(ChangeLog changeLog)
    {
    }

    /**
     * Clean up any parser resources
     * @see ChangeLogParser#cleanup()
     */ 
    public void cleanup()
    {
    }
    
    /**
     * Parse the input stream into a collection.
     * @param anInputStream an input stream containing clearcase log output
     * @return a collection of ChangeLogEntry's
     * @throws IOException when there are errors reading the provided stream
     */
    public Collection parse(InputStream anInputStream) throws IOException
    {
        BufferedReader stream = new BufferedReader(
            new InputStreamReader(anInputStream));

        // Read the tags in the order they are produced by the command setup
        // by ClearcaseChangeLogGenerator
        //      File name
        //      Date                
        //      Comment
        //      User Name (author)        
        String line = null;
        
        while ((line = stream.readLine())  != null)
        {
            switch (getStatus())
            {
                case GET_FILE:
                    processGetFile(line);
                    break;
                case GET_DATE:
                    processGetDate(line);
                    break;
                case GET_COMMENT:
                    processGetCommentAndUser(line);
                    break;
                default:
                    throw new IllegalStateException("Unknown state: " + status);
            }
        }
        
        return entries.values();
     }
    
    /**
     * Add a change log entry to the list (if it's not already there)
     * with the given file.
     * @param entry a {@link ChangeLogEntry} to be added to the list if another
     *      with the same key doesn't exist already. If the entry's author
     *      is null, the entry wont be added
     * @param file a {@link ChangeLogFile} to be added to the entry
     */
    private void addEntry(ChangeLogEntry entry, ChangeLogFile file)
    {
            
        // do not add if entry is not populated
        if (entry.getAuthor() == null)
        {
            return;
        }
        // do not add if the operation is not checkin
        if (entry.getComment().indexOf("checkin ") == -1)
        {
            return;
        }
        
        String key = ENTRY_KEY_TIMESTAMP_FORMAT.format(entry.getDate())
            + entry.getAuthor() + entry.getComment();
        
                
        file.setRevision("");

        if (!entries.containsKey(key))
        {
            entry.addFile(file);
            entries.put(key, entry);
        }
        else
        {
            ChangeLogEntry existingEntry = (ChangeLogEntry) entries.get(key);
            existingEntry.addFile(file);
        }
    }
 
    /**
     * Process the current input line in the Get File state.
     * @param line a line of text from the clearcase log output
     */
    private void processGetFile(String line) 
    {
        if (line.startsWith(NAME_TAG))
        {
            setCurrentLogEntry(new ChangeLogEntry());
            setCurrentFile(new ChangeLogFile(line.substring(NAME_TAG.length(),
                line.length())));
            setStatus(GET_DATE);
        }
    }


    /**
     * Process the current input line in the Get Date state.
     * @param line a line of text from the clearcase log output
     */
    private void processGetDate(String line)
    {
        if (line.startsWith(DATE_TAG))
        {
            try
            {
                getCurrentLogEntry().setDate(CLEARCASE_TIMESTAMP_FORMAT.parse(line.substring(DATE_TAG.length())));
            }
            catch (ParseException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            setStatus(GET_COMMENT);
        }
    }

    
    /**
     * Process the current input line in the Get Comment state.
     * @param line a line of text from the clearcase log output
     */
    private void processGetCommentAndUser(String line)
    {
        if (line.startsWith(COMMENT_TAG))
        {
            String comm = line.substring(COMMENT_TAG.length());
            getCurrentLogEntry().setComment(
                getCurrentLogEntry().getComment() + comm + "\n");
            
        }
        else if (line.startsWith(USER_TAG))
        {
            getCurrentLogEntry().setAuthor(line.substring(USER_TAG.length()).trim());
            // add entry, and set state to get file
            addEntry(getCurrentLogEntry(), getCurrentFile());
            setStatus(GET_FILE);            
        }
        else
        {
            // keep gathering comments
            getCurrentLogEntry().setComment(
                getCurrentLogEntry().getComment() + line + "\n");
        }
    }

    /**
     * Getter for property currentFile.
     * @return Value of property currentFile.
     */
    private ChangeLogFile getCurrentFile()
    {
        return currentFile;
    }
    
    /**
     * Setter for property currentFile.
     * @param currentFile New value of property currentFile.
     */
    private void setCurrentFile(ChangeLogFile currentFile)
    {
        this.currentFile = currentFile;
    }
    
    /**
     * Getter for property currentLogEntry.
     * @return Value of property currentLogEntry.
     */
    private ChangeLogEntry getCurrentLogEntry()
    {
        return currentLogEntry;
    }
    
    /**
     * Setter for property currentLogEntry.
     * @param currentLogEntry New value of property currentLogEntry.
     */
    private void setCurrentLogEntry(ChangeLogEntry currentLogEntry)
    {
        this.currentLogEntry = currentLogEntry;
    }
    
    /**
     * Getter for property status.
     * @return Value of property status.
     */
    private int getStatus()
    {
        return status;
    }
    
    /**
     * Setter for property status.
     * @param status New value of property status.
     */
    private void setStatus(int status)
    {
        this.status = status;
    }

    public void setDateFormatInFile( String dateFormat )
    {
    }
}