package org.apache.maven.vsslib;

/*
 * ====================================================================
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ===================================================================
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.changelog.ChangeLogEntry;
import org.apache.maven.changelog.ChangeLogFile;
import org.apache.maven.plugin.logging.Log;


/**
 * Visual Source Safe specific implementation of ChangeLogParser interface.
 * 
 * @author Freddy Mallet
 */
public class VssChangeLogParser implements
        org.apache.maven.changelog.ChangeLogParser {

    /** * Log */
    private static final Log LOG = ChangeLog.getLog();

    /**
     * Custom date/time formatter. Rounds ChangeLogEntry times to the nearest
     * minute.
     */
    private static final SimpleDateFormat ENTRY_KEY_TIMESTAMP_FORMAT = new SimpleDateFormat(
            "yyyyMMddHHmm");

    /**
     * rcs entries, in reverse (date, time, author, comment) order
     */
    private Map entries = new TreeMap(Collections.reverseOrder());

    // state machine constants for reading Starteam output
    /** * expecting file information */
    private static final int GET_FILE = 1;

    /** * expecting file path information */
    private static final int GET_FILE_PATH = 2;

    /** * expecting date */
    private static final int GET_AUTHOR = 3;

    /** * expecting comments */
    private static final int GET_COMMENT = 4;

    /** * expecting revision */
    private static final int GET_REVISION = 5;

    /** * unknown vss history line status */
    private static final int GET_UNKNOWN = 6;

    /** * Marks start of file data */
    private static final String START_FILE = "*****  ";

    /** * Marks start of file data */
    private static String START_FILE_PATH = "$/";

    /** * Marks start of revision */
    private static final String START_REVISION = "Version";

    /** * Marks author data */
    private static final String START_AUTHOR = "User: ";

    /** * Marks comment data */
    private static final String START_COMMENT = "Comment: ";

    /** * last status of the parser */
    private int lastStatus = GET_FILE;

    /** * the current log entry being processed by the parser */
    private ChangeLogEntry currentLogEntry = null;

    /** * the current file being processed by the parser */
    private ChangeLogFile currentFile = null;

    /** * the bean representation of the vss connection string */
    private VssConnection vssConnection;

    /**
     * Create a new ChangeLogParser.
     */
    public VssChangeLogParser() {
    }

    /**
     * initialize the parser from the change log
     * 
     * @param changeLog
     *            the controlling task
     * @see ChangeLogParser#init(ChangeLog)
     */
    public void init(ChangeLog changeLog) {
        String connection = changeLog.getRepositoryConnection();
        try {
            vssConnection = new VssConnection(connection);
        } catch (Exception e) {
            String message = "Unable to parse vss connection string : "
                    + connection;
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Parse the input stream into a collection.
     * 
     * @param anInputStream
     *            an input stream containing clearcase log output
     * @return a collection of ChangeLogEntry's
     * @throws IOException
     *             when there are errors reading the provided stream
     */
    public Collection parse(InputStream inputStream) throws IOException {
        BufferedReader stream = new BufferedReader(new InputStreamReader(
                inputStream));

        String line;

        while ((line = stream.readLine()) != null) {
            switch (getLineStatus(line)) {
            case GET_FILE:
                processGetFile(line);
                break;
            case GET_REVISION:
                processGetRevision(line);
                break;
            case GET_AUTHOR:
                processGetAuthor(line);
                break;
            case GET_FILE_PATH:
                processGetFilePath(line);
                break;
            case GET_COMMENT:
                processGetComment(line);
                break;
            default:
                break;
            }
        }
        return entries.values();
    }

    /**
     * Process the current input line in the Get Comment state.
     * 
     * @param line
     *            a line of text from the VSS log output
     */
    private void processGetComment(String line) {
        String[] commentLine = line.split(":");
        if (commentLine.length == 2) {
            getCurrentLogEntry().setComment(commentLine[1]);
        }
        //Comment suite on a new line
        else {
            String comment = getCurrentLogEntry().getComment();
            comment = comment + " " + line;
            getCurrentLogEntry().setComment(comment);
        }
    }

    /**
     * Process the current input line in the Get Author state.
     * 
     * @param line
     *            a line of text from the VSS log output
     */
    private void processGetAuthor(String line) {
        String[] result = line.split("\\s");
        Vector vector = new Vector();
        for (int i = 0; i < result.length; i++) {
            if (!result[i].equals("")) {
                vector.add(result[i]);
            }
        }
        ChangeLogEntry entry = getCurrentLogEntry();
        entry.setAuthor((String) vector.get(1));
        entry.setDate(parseDate((String) vector.get(3) + " "
                + (String) vector.get(5)));
    }

    /**
     * Process the current input line in the Get File state.
     * 
     * @param line
     *            a line of text from the VSS log output
     */
    private void processGetFile(String line) {
        setCurrentLogEntry(new ChangeLogEntry());
        String[] fileLine = line.split(" ");
        setCurrentFile(new ChangeLogFile(fileLine[2]));
    }

    /**
     * Process the current input line in the Get File Path state.
     * 
     * @param line
     *            a line of text from the VSS log output
     */
    private void processGetFilePath(String line) {
        if (getCurrentFile() != null) {
            String fileName = getCurrentFile().getName();

            String path = line.substring(line.indexOf("$"), line.length());
            String longPath = path.substring(vssConnection.getVssProject()
                    .length() + 1, path.length())
                    + "/" + fileName;
            getCurrentFile().setName(longPath);
            addEntry(getCurrentLogEntry(), getCurrentFile());
        }
    }

    /**
     * Process the current input line in the Get Revision state.
     * 
     * @param line
     *            a line of text from the VSS log output
     */
    private void processGetRevision(String line) {
        String[] revisionLine = line.split(" ");
        getCurrentFile().setRevision(revisionLine[1]);
    }

    /**
     * Identify the status of a vss history line
     * 
     * @param the
     *            line to process
     * @return status
     */
    private int getLineStatus(String line) {
        int argument = GET_UNKNOWN;
        if (line.startsWith(START_FILE)) {
            argument = GET_FILE;
        } else if (line.startsWith(START_REVISION)) {
            argument = GET_REVISION;
        } else if (line.startsWith(START_AUTHOR)) {
            argument = GET_AUTHOR;
        } else if (line.indexOf(START_FILE_PATH) != -1) {
            argument = GET_FILE_PATH;
        } else if (line.startsWith(START_COMMENT)) {
            argument = GET_COMMENT;
        } else if (getLastStatus() == GET_COMMENT) {
            //Comment suite on a new line
            argument = getLastStatus();
        }
        setLastStatus(argument);
        return argument;
    }

    /**
     * Add a change log entry to the list (if it's not already there) with the
     * given file.
     * 
     * @param entry
     *            a {@link ChangeLogEntry}to be added to the list if another
     *            with the same key doesn't exist already. If the entry's author
     *            is null, the entry wont be added
     * @param file
     *            a {@link ChangeLogFile}to be added to the entry
     */
    private void addEntry(ChangeLogEntry entry, ChangeLogFile file) {
        // do not add if entry is not populated
        if (entry.getAuthor() == null) {
            return;
        }

        String key = ENTRY_KEY_TIMESTAMP_FORMAT.format(entry.getDate())
                + entry.getAuthor() + entry.getComment();

        if (!entries.containsKey(key)) {
            entry.addFile(file);
            entries.put(key, entry);
        } else {
            ChangeLogEntry existingEntry = (ChangeLogEntry) entries.get(key);
            existingEntry.addFile(file);
        }
    }

    /**
     * Converts the date timestamp from the svn output into a date object.
     * 
     * @return A date representing the timestamp of the log entry.
     */
    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm");
            Date date = format.parse(dateString);
            return date;
        } catch (ParseException e) {
            LOG.error("ParseException Caught", e);
            return null;
        }
    }

    /**
     * Getter for property currentFile.
     * 
     * @return Value of property currentFile.
     */
    private ChangeLogFile getCurrentFile() {
        return currentFile;
    }

    /**
     * Setter for property currentFile.
     * 
     * @param currentFile
     *            New value of property currentFile.
     */
    private void setCurrentFile(ChangeLogFile currentFile) {
        this.currentFile = currentFile;
    }

    /**
     * Getter for property currentLogEntry.
     * 
     * @return Value of property currentLogEntry.
     */
    private ChangeLogEntry getCurrentLogEntry() {
        return currentLogEntry;
    }

    /**
     * Setter for property currentLogEntry.
     * 
     * @param currentLogEntry
     *            New value of property currentLogEntry.
     */
    private void setCurrentLogEntry(ChangeLogEntry currentLogEntry) {
        this.currentLogEntry = currentLogEntry;
    }

    /**
     * Getter for property status.
     * 
     * @return Value of property status.
     */
    private int getLastStatus() {
        return lastStatus;
    }

    /**
     * Setter for property status.
     * 
     * @param status
     *            New value of property status.
     */
    private void setLastStatus(int status) {
        this.lastStatus = status;
    }

    /**
     * Clean up any parser resources
     * 
     * @see ChangeLogParser#cleanup()
     */
    public void cleanup() {
    }

    /**
     * Defined in ChangeLogParser interface
     * 
     * @see ChangeLogParser
     */
    public void setDateFormatInFile(String s) {
    }
}
