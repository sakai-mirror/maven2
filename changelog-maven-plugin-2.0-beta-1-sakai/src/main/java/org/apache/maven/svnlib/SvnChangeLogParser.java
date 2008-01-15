package org.apache.maven.svnlib;

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
import java.util.ArrayList;
// maven
import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.changelog.ChangeLogParser;
import org.apache.maven.changelog.ChangeLogEntry;
import org.apache.maven.changelog.ChangeLogFile;
import org.apache.maven.plugin.logging.Log;
// regexp
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * A class to parse the log output from Subversion.
 *
 * @author <a href="mailto:dion@multitask.com.au">dIon Gillard</a>
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @author Daniel Rall
 * @version $Id$
 */
class SvnChangeLogParser implements ChangeLogParser
{
    /** Date formatter for svn timestamp (after a little massaging) */
    private static final SimpleDateFormat SVN_TIMESTAMP =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzzzzzzzz");

    /** Log */ 
    private static final Log LOG = ChangeLog.getLog();
     
    /** State machine constant: expecting header */
    private static final int GET_HEADER = 1;

    /** State machine constant: expecting file information */
    private static final int GET_FILE = 2;

    /** State machine constant: expecting comments */
    private static final int GET_COMMENT = 3;

    /** A file line begins with a space character */
    private static final String FILE_BEGIN_TOKEN = " ";

    /** The file section ends with a blank line */
    private static final String FILE_END_TOKEN = "";

    /** The filename starts after 5 characters */
    private static final int FILE_START_INDEX = 5;

    /** The comment section ends with a dashed line */
    private static final String COMMENT_END_TOKEN = 
        "------------------------------------" +
        "------------------------------------";

    /** The pattern used to match svn header lines */
    private static final String pattern =
        "^r(\\d+)\\s+\\|\\s+" +          // revision number
        "(\\(\\S+\\s+\\S+\\)|\\S+)\\s+\\|\\s+" + // author username
        "(\\d+-\\d+-\\d+ " +             // date 2002-08-24
        "\\d+:\\d+:\\d+) " +             // time 16:01:00
        "([\\-+])(\\d\\d)(\\d\\d)";      // gmt offset -0400 

    /** Current status of the parser */
    private int status = GET_HEADER;
    
    /** List of change log entries */
    private Collection entries = new ArrayList();

    /** The current log entry being processed by the parser */
    private ChangeLogEntry currentLogEntry;

    /** The current revision of the entry being processed by the parser */
    private String currentRevision;

    /** The current comment of the entry being processed by the parser */
    private StringBuffer currentComment;

    /** The regular expression used to match header lines */
    private RE headerRegexp;

    /** The invoking changelog controller (useful to log messages) */
    private ChangeLog changeLog;

    /** 
     * Default constructor.
     */
    public SvnChangeLogParser()
    {
        try
        {
            headerRegexp = new RE(pattern);
        }
        catch (RESyntaxException ignored)
        {
            LOG.error("Could not create regexp to parse svn log file", ignored);
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
     * Parse the input stream into a collection.
     *
     * @param anInputStream An input stream containing svn log output
     * @return A collection of ChangeLogEntry's
     * @throws IOException When there are errors reading the provided stream
     */
    public Collection parse(InputStream anInputStream) throws IOException
    {
        BufferedReader stream = new BufferedReader(
            new InputStreamReader(anInputStream));

        // Current state transitions in the parser's state machine:
        //      Get Header     -> Get File
        //      Get File       -> Get Comment or Get (another) File
        //      Get Comment    -> Get (another) Comment
        String line = null;
        while ((line = stream.readLine()) != null)
        {
            switch (status)
            {
                case GET_HEADER:
                    processGetHeader(line);
                    break;
                case GET_FILE:
                    processGetFile(line);
                    break;
                case GET_COMMENT:
                    processGetComment(line);
                    break;
                default:
                    throw new IllegalStateException("Unknown state: " + status);
            }
        }
        return entries;
     }
 
    /**
     * Process the current input line in the GET_HEADER state.  The
     * author, date, and the revision of the entry are gathered.  Note,
     * Subversion does not have per-file revisions, instead, the entire
     * repository is given a single revision number, which is used for
     * the revision number of each file.  
     *
     * @param line A line of text from the svn log output
     */
    private void processGetHeader(String line) 
    {
        if (!headerRegexp.match(line))
        {
            return;
        }

        currentRevision = headerRegexp.getParen(1);
        currentLogEntry = new ChangeLogEntry();
        currentLogEntry.setAuthor(headerRegexp.getParen(2));
        currentLogEntry.setDate(parseDate());

        status = GET_FILE;
    }

    /**
     * Process the current input line in the GET_FILE state.  This state
     * adds each file entry line to the current change log entry.  Note,
     * the revision number for the entire entry is used for the revision
     * number of each file.
     *
     * @param line A line of text from the svn log output
     */
    private void processGetFile(String line) 
    {
        if (line.startsWith(FILE_BEGIN_TOKEN))
        {
            // Skip the status flags and just get the name of the file
            String name = line.substring(FILE_START_INDEX);
            currentLogEntry.addFile(new ChangeLogFile(name, currentRevision));

            status = GET_FILE;
        }
        else if (line.equals(FILE_END_TOKEN))
        {
            // Create a buffer for the collection of the comment now
            // that we are leaving the GET_FILE state.
            currentComment = new StringBuffer();

            status = GET_COMMENT;
        }
    }

    /**
     * Process the current input line in the GET_COMMENT state.  This
     * state gathers all of the comments that are part of a log entry.
     *
     * @param line a line of text from the svn log output
     */
    private void processGetComment(String line)
    {
        if (line.equals(COMMENT_END_TOKEN))
        {
            currentLogEntry.setComment(currentComment.toString());
            entries.add(currentLogEntry);

            status = GET_HEADER;
        }
        else
        {
            currentComment.append(line).append('\n');
        }
    }

    /** 
     * Converts the date timestamp from the svn output into a date
     * object.
     * 
     * @return A date representing the timestamp of the log entry.
     */
    private Date parseDate()
    {
        try
        {
            StringBuffer date = new StringBuffer()
                .append(headerRegexp.getParen(3))
                .append(" GMT")
                .append(headerRegexp.getParen(4))
                .append(headerRegexp.getParen(5))
                .append(':')
                .append(headerRegexp.getParen(6));

            return SVN_TIMESTAMP.parse(date.toString());
        }
        catch (ParseException e)
        {
            LOG.error("ParseException Caught", e);
            return null;        
        }
    }

    public void setDateFormatInFile( String dateFormat )
    {
    }
}
