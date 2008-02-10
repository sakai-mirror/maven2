package org.apache.maven.perforcelib;

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
import java.util.Date;
import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.changelog.ChangeLogParser;
import org.apache.maven.changelog.ChangeLogEntry;
import org.apache.maven.changelog.ChangeLogFile;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apache.maven.plugin.logging.Log;

/**
 * A class to parse the log output from the Perforce 'filelog'
 * command.
 *
 * @author <a href="mailto:jim@crossleys.org">Jim Crossley</a>
 * @version $Id: 
 */
class PerforceChangeLogParser implements ChangeLogParser
{
    /** Date formatter for perforce timestamp */
    private static final SimpleDateFormat PERFORCE_TIMESTAMP =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /** Log */ 
    private static final Log LOG = ChangeLog.getLog();
     
    /**
     * RCS entries, in reverse changelist number order
     */
    private Map entries = new TreeMap(Collections.reverseOrder());

    /** State machine constant: expecting revision and/or file information */
    private static final int GET_REVISION = 1;

    /** State machine constant: eat the first blank line */
    private static final int GET_COMMENT_BEGIN = 2;

    /** State machine constant: expecting comments */
    private static final int GET_COMMENT = 3;

    /** The comment section ends with a blank line */
    private static final String COMMENT_DELIMITER = "";

    /** A file line begins with two slashes */
    private static final String FILE_BEGIN_TOKEN = "//";

    /** Current status of the parser */
    private int status = GET_REVISION;
    
    /** The current log entry being processed by the parser */
    private ChangeLogEntry currentLogEntry;

    /** the current file being processed by the parser */
    private String currentFile;
    
    /** The regular expression used to match header lines */
    private RE revisionRegexp;

    /** The invoking changelog controller (useful to log messages) */
    private ChangeLog changeLog;

    /** the before date */
    private Date beforeDate;

    /** The depot filespec will be stripped from each filename */
    private int prefixLength;
    
    private static final String pattern =
        "^\\.\\.\\. #(\\d+) " +           // revision number
        "change (\\d+) .* " +             // changelist number
        "on (.*) " +                      // date 
        "by (.*)@";                       // author

    /** 
     * Default constructor.
     */
    public PerforceChangeLogParser()
    {
        try {
            revisionRegexp = new RE(pattern);
        } catch (RESyntaxException ignored) {
            LOG.error("Could not create regexp to parse perforce log file", ignored);
        }
    }

    /**
     * Initialize the parser from the change log.
     *
     * @param changeLog The controlling task
     * @see ChangeLogParser#init(ChangeLog)
     */ 
    public void init(ChangeLog changeLog)
    {
        this.changeLog = changeLog;
        setDateRange();
        String conn = changeLog.getRepositoryConnection();
        String filespec = conn.substring(conn.lastIndexOf(':')+1);
        this.prefixLength = 1 + filespec.lastIndexOf ('/');
    }

    /**
     * Clean up any parser resources.
     *
     * @see ChangeLogParser#cleanup()
     */ 
    public void cleanup()
    {
    }
    
    /**
     * Parse the input stream into a collection of entries
     *
     * @param anInputStream An input stream containing perforce log output
     * @return A collection of ChangeLogEntry's
     * @throws IOException When there are errors reading the provided stream
     */
    public Collection parse(InputStream anInputStream) throws IOException
    {
        BufferedReader stream = new BufferedReader(new InputStreamReader(anInputStream));

        String line = null;
        while ((line = stream.readLine()) != null)
        {
            switch (status)
            {
                case GET_REVISION:
                    processGetRevision(line);
                    break;
                case GET_COMMENT_BEGIN:
                    status = GET_COMMENT;
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
     *      with the same key (p4 change number) doesn't exist already.
     * @param file a {@link ChangeLogFile} to be added to the entry
     */
    private void addEntry(ChangeLogEntry entry, ChangeLogFile file)
    {
        if (beforeDate != null) {
            if (entry.getDate().before(beforeDate)) {
                return;
            }
        }
        Integer key = new Integer(revisionRegexp.getParen(2));
        if (!entries.containsKey(key)) {
            entry.addFile(file);
            entries.put(key, entry);
        } else {
            ChangeLogEntry existingEntry = (ChangeLogEntry) entries.get(key);
            existingEntry.addFile(file);
        }
    }
 
    /**
     * Most of the relevant info is on the revision line matching the
     * 'pattern' string.
     *
     * @param line A line of text from the perforce log output
     */
    private void processGetRevision(String line) 
    {
        if (line.startsWith(FILE_BEGIN_TOKEN)) {
            currentFile = line.substring (this.prefixLength);
            return;
        }
        
        if (!revisionRegexp.match(line)) {
            return;
        }

        currentLogEntry = new ChangeLogEntry();
        currentLogEntry.setDate(parseDate(revisionRegexp.getParen(3)));
        currentLogEntry.setAuthor(revisionRegexp.getParen(4));

        status = GET_COMMENT_BEGIN;
    }

    /**
     * Process the current input line in the GET_COMMENT state.  This
     * state gathers all of the comments that are part of a log entry.
     *
     * @param line a line of text from the perforce log output
     */
    private void processGetComment(String line)
    {
        if (line.equals(COMMENT_DELIMITER)) {
            addEntry(currentLogEntry, new ChangeLogFile(currentFile, revisionRegexp.getParen(1)));
            status = GET_REVISION;
        } else {
            currentLogEntry.setComment(currentLogEntry.getComment() + line + "\n");
        }
    }

    /** 
     * Converts the date timestamp from the perforce output into a date
     * object.
     * 
     * @return A date representing the timestamp of the log entry.
     */
    private Date parseDate(String date)
    {
        try {
            return PERFORCE_TIMESTAMP.parse(date);
        } catch (ParseException e) {
            LOG.error("ParseException Caught", e);
            return null;        
        }
    }

    /**
     * Set the beforeDate member based on the number of days obtained
     * from the ChangeLog.
     *
     * @param numDaysString The number of days of log output to
     * generate.
     */
    private void setDateRange()
    {
        if (this.changeLog == null ||
            this.changeLog.getRange() == null ||
            this.changeLog.getRange().length() == 0)
        {
            return;
        }
        int days = Integer.parseInt(this.changeLog.getRange());
        beforeDate = new Date(System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000);
    }

    public void setDateFormatInFile( String dateFormat )
    {
    }
}
