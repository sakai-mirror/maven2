package org.apache.maven.cvslib;

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
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.changelog.ChangeLogParser;
import org.apache.maven.changelog.ChangeLogEntry;
import org.apache.maven.changelog.ChangeLogFile;

/**
 * A class to parse cvs log output
 *
 * @author <a href="mailto:dion@multitask.com.au">dIon Gillard</a>
 * @version $Id$
 */
class CvsChangeLogParser implements ChangeLogParser
{
    
    /**
     * Old formatter used to parse CVS date/timestamp.
     */
    private static final SimpleDateFormat OLD_CVS_TIMESTAMP_FORMAT =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    /**
     * New formatter used to parse CVS date/timestamp.
     */
    private static final SimpleDateFormat NEW_CVS_TIMESTAMP_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    
    /**
     * Custom date/time formatter.  Rounds ChangeLogEntry times to the nearest
     * minute.
     */
    private static final SimpleDateFormat ENTRY_KEY_TIMESTAMP_FORMAT = 
        new SimpleDateFormat("yyyyMMddHHmm");
    
    /**
     * rcs entries, in reverse (date, time, author, comment) order
     */
    private Map entries = new TreeMap(Collections.reverseOrder());

    // state machine constants for reading cvs output
    /** expecting file information */
    private static final int GET_FILE = 1;
    /** expecting date */
    private static final int GET_DATE = 2;
    /** expecting comments */
    private static final int GET_COMMENT = 3;
    /** expecting revision */
    private static final int GET_REVISION = 4;
    /** Marks start of file data*/
    private static final String START_FILE = "Working file: ";
    /** Marks end of file */
    private static final String END_FILE = "==================================="
        + "==========================================";
    /** Marks start of revision */
    private static final String START_REVISION = "----------------------------";
    /** Marks revision data */
    private static final String REVISION_TAG = "revision ";
    /** Marks date data */
    private static final String DATE_TAG = "date: ";

    /** current status of the parser */
    private int status = GET_FILE;
    
    /** the current log entry being processed by the parser*/
    private ChangeLogEntry currentLogEntry = null;
    
    /** the current file being processed by the parser */
    private ChangeLogFile currentFile = null;

    /**
     * Create a new ChangeLogParser.
     */
    public CvsChangeLogParser()
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
     * @param anInputStream an input stream containing cvs log output
     * @return a collection of ChangeLogEntry's
     * @throws IOException when there are errors reading the provided stream
     */
    public Collection parse(InputStream anInputStream) throws IOException
    {
        BufferedReader stream = new BufferedReader(
            new InputStreamReader(anInputStream));

        // current state transitions in the state machine - starts with Get File
        //      Get File                -> Get Revision
        //      Get Revision            -> Get Date or Get File
        //      Get Date                -> Get Comment
        //      Get Comment             -> Get Comment or Get Revision
        String line = null;
        while ((line = stream.readLine()) != null)
        {
            switch (getStatus())
            {
                case GET_FILE:
                    processGetFile(line);
                    break;
                case GET_REVISION:
                    processGetRevision(line);
                    break;
                case GET_DATE:
                    processGetDate(line);
                    break;
                case GET_COMMENT:
                    processGetComment(line);
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
        
        String key = ENTRY_KEY_TIMESTAMP_FORMAT.format(entry.getDate())
            + entry.getAuthor() + entry.getComment();
        
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
     * @param line a line of text from the cvs log output
     */
    private void processGetFile(String line) 
    {
        if (line.startsWith(START_FILE))
        {
            setCurrentLogEntry(new ChangeLogEntry());
            setCurrentFile(new ChangeLogFile(line.substring(START_FILE.length(),
                line.length())));
            setStatus(GET_REVISION);
        }
    }

    /**
     * Process the current input line in the Get Revision state.
     * @param line a line of text from the cvs log output
     */
    private void processGetRevision(String line) 
    {
        if (line.startsWith(REVISION_TAG))
        {
            getCurrentFile().setRevision(line.substring(REVISION_TAG.length()));
            setStatus(GET_DATE);
        }
        else if (line.startsWith(END_FILE))
        {
            // If we encounter an end of file line, it means there 
            // are no more revisions for the current file.
            // there could also be a file still being processed.
            setStatus(GET_FILE);
            addEntry(getCurrentLogEntry(), getCurrentFile());
        }
    }

    /**
     * Process the current input line in the Get Date state.
     * @param line a line of text from the cvs log output
     */
    private void processGetDate(String line)
    {
        if (line.startsWith(DATE_TAG))
        {
            //date: YYYY/mm/dd HH:mm:ss; author: name
            //or date: YYYY-mm-dd HH:mm:ss Z; author: name
            StringTokenizer tokenizer = new StringTokenizer(line, ";");
            String dateToken = tokenizer.nextToken();
            String dateString = 
                dateToken.trim().substring("date: ".length()).trim();
            getCurrentLogEntry().setDate(parseDate(dateString));

            String authorToken = tokenizer.nextToken();
            String author = 
                authorToken.trim().substring("author: ".length()).trim();
            getCurrentLogEntry().setAuthor(author);
            setStatus(GET_COMMENT);
        }
    }

    /**
     * Tries to parse the given String according to all known CVS timeformats.
     * 
     * @param dateString String to parse
     * @return <code>java.util.Date</code> representing the time.
     * @throws IllegalArgumentException if it's not possible to parse the date.
     */
    private Date parseDate(String dateString)
    {
        Date date;
        try
        {
            date = OLD_CVS_TIMESTAMP_FORMAT.parse(dateString);
        }
        catch (ParseException e)
        {
            //try other format
            try {
                date = NEW_CVS_TIMESTAMP_FORMAT.parse(dateString);
            } catch (ParseException e1) {
                throw new IllegalArgumentException("I don't understand this date: "
                        + dateString);
            }
        }
        return date;
    }

    /**
     * Process the current input line in the Get Comment state.
     * @param line a line of text from the cvs log output
     */
    private void processGetComment(String line)
    {
        if (line.startsWith(START_REVISION))
        {
            // add entry, and set state to get revision
            addEntry(getCurrentLogEntry(), getCurrentFile());
            // new change log entry
            setCurrentLogEntry(new ChangeLogEntry());
            // same file name, but different rev
            setCurrentFile(new ChangeLogFile(getCurrentFile().getName()));
            setStatus(GET_REVISION);
        }
        else if (line.startsWith(END_FILE))
        {
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
