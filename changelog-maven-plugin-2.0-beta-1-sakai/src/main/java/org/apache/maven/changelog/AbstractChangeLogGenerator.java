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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import org.apache.maven.plugin.logging.Log;

// maven imports
import org.apache.maven.util.AsyncStreamReader;
// ant imports
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;

/**
 * An abstract implementation of the {@link org.apache.maven.changelog.ChangeLog}
 * interface.
 *
 * @author Glenn McAllister
 * @author <a href="mailto:jeff.martin@synamic.co.uk">Jeff Martin</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:dion@multitask.com.au">dIon Gillard</a>
 * @author <a href="mailto:bodewig@apache.org">Stefan Bodewig</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @version 
 * $Id$
 */
public abstract class AbstractChangeLogGenerator implements ChangeLogGenerator, ExecuteStreamHandler
{
  /** 
   * The working directory.
   */
  protected File base;
  
  /** 
   * Reference to the enclosing ChangeLog instance - used to obtain
   * any necessary configuration information. 
   */
  protected ChangeLog changeLogExecutor;

  /**
   * The parser that takes the log output and transforms it into a
   * collection of ChangeLogEntry's.
   */
  protected ChangeLogParser clParser;

  /** The connection string from the project */
  private String connection;
  
  /** The log type (range, date, or tag). */
  protected String type;

  /**
   * The date range command line argument.
   */
  protected String dateRange;
  
  /** The tag command line argument. */
  protected String tag;
  
  /** Represents when this log starts (for the report). */
  protected String logStart = "";
  
  /** Represents when this log ends (for the report). */
  protected String logEnd = "";

  /**
   * The collection of ChangeLogEntry's returned from clParser.
   */
  protected Collection entries;

  /** 
   * Stderr stream eater.
   */
  protected AsyncStreamReader errorReader;

  /** 
   * The scm process input stream. 
   */
  protected InputStream in;

  /**
   * The comment format string used in interrogating the RCS.
   */
  protected String commentFormat;

  /** Log */
  private static Log LOG = ChangeLog.getLog();

  /**
   * Initialize the generator from the changelog controller.
   *
   * @param changeLog The invoking controller (useful for logging)
   * @see ChangeLogGenerator#init(ChangeLog)
   */
  public void init(ChangeLog changeLog)
  {
    changeLogExecutor = changeLog;

    base = changeLogExecutor.getBasedir();
    
    type = changeLogExecutor.getType();
    
    if (type.equalsIgnoreCase("tag"))
    {
        tag = getScmTagArgument(changeLogExecutor.getMarkerStart(), changeLogExecutor.getMarkerEnd());
        logStart = changeLogExecutor.getMarkerStart();
        logEnd = (changeLogExecutor.getMarkerEnd() == null) ? "" : changeLogExecutor.getMarkerEnd();
    }
    else if (type.equalsIgnoreCase("date"))
    {
        setDateRangeFromAbsoluteDate(changeLogExecutor.getMarkerStart(), changeLogExecutor.getMarkerEnd());
    }
    else // type == "range" (the default)
    {
        // This lets the user 'not' set a limit on the log command.  We
        // need this cuz Subversion doesn't currently support date
        // commands on web-based repositories, so it would be nice to
        // let the user still use the changelog plugin.
        if (changeLogExecutor.getRange() != null && changeLogExecutor.getRange().length() != 0)
        {
          setDateRange(changeLogExecutor.getMarkerStart(), changeLogExecutor.getMarkerEnd());
        }
    }

    setConnection(changeLogExecutor.getRepositoryConnection());

    // set the comment query string for the RCS.
    setCommentFormat(changeLogExecutor.getCommentFormat());
  }

  /**
   * Set the dateRange member based on the number of days obtained
   * from the ChangeLog.
   *
   * @param numDaysString The number of days of log output to
   * generate.
   */
  protected void setDateRange(String numDaysStartString, String numDaysEndString)
  {
    int daysStart = Integer.parseInt(numDaysStartString);
    int daysEnd = (numDaysEndString == null) ? -1 : Integer.parseInt(numDaysEndString);

    Date before = new Date(System.currentTimeMillis() - (long) daysStart * 24 * 60 * 60 * 1000);
    Date to = new Date(System.currentTimeMillis() - (long) daysEnd * 24 * 60 * 60 * 1000);

    dateRange = getScmDateArgument(before, to);
    setLogStart(before);
    setLogEnd(to);
  }
  
  /**
   * Set the dateRange member based on an absolute date.
   * @param startDate  The start date for the range.
   * @param endDate  The end date for the range, or <code>null</code> to use the present time.
   */
  protected void setDateRangeFromAbsoluteDate(String startDate, String endDate)
  {
    String dateFormat = changeLogExecutor.getDateFormat();
    SimpleDateFormat format = dateFormat == null ? new SimpleDateFormat("yyyy-MM-dd") : new SimpleDateFormat(dateFormat);
    
    Date before;
    try
    {
        before = format.parse(startDate);
    }
    catch (ParseException ex)
    {
        throw new IllegalArgumentException("Unable to parse start date " + startDate + ": " + ex.getLocalizedMessage());
    }
    Date to;
    try
    {
        to = (endDate != null) ? format.parse(endDate) : new Date(System.currentTimeMillis() + (long) 1 * 24 * 60 * 60 * 1000);
    }
    catch (ParseException ex)
    {
        throw new IllegalArgumentException("Unable to parse end date " + endDate + ": " + ex.getLocalizedMessage());
    }

    dateRange = getScmDateArgument(before, to);
    setLogStart(before);
    setLogEnd(to);
  }
  
  /**
   * Sets the log start string based on the given date.
   * This uses the date format supplied in the plugin properties.
   * 
   * @param start  date the log started.
   */
  protected void setLogStart(Date start)
  {
    String dateFormat = changeLogExecutor.getDateFormat();
    SimpleDateFormat format = dateFormat == null ? new SimpleDateFormat("yyyy-MM-dd") : new SimpleDateFormat(dateFormat);
    
    logStart = format.format(start);
  }
  
  /**
   * Sets the log end string based on the given date.
   * This uses the date format supplied in the plugin properties.
   * 
   * @param end  date the log ended.
   */
  protected void setLogEnd(Date end)
  {
    String dateFormat = changeLogExecutor.getDateFormat();
    SimpleDateFormat format = dateFormat == null ? new SimpleDateFormat("yyyy-MM-dd") : new SimpleDateFormat(dateFormat);
      
    logEnd = format.format(end);
  }

  /**
   * Execute scm client driving the given parser.
   *
   * @param parser A {@link ChangeLogParser parser} to process the scm
   * output.
   * @return A collection of {@link ChangeLogEntry entries} parsed from
   * the scm output.
   * @throws IOException When there are issues executing scm.
   * @see ChangeLogGenerator#getEntries(ChangeLogParser)
   */
  public Collection getEntries(ChangeLogParser parser) throws IOException
  {
    if (parser == null)
    {
      throw new NullPointerException("parser cannot be null");
    }

    if (base == null)
    {
      throw new NullPointerException("basedir must be set");
    }

    if (!base.exists())
    {
      throw new FileNotFoundException("Cannot find base dir " + base.getAbsolutePath());
    }

    clParser = parser;
    try
    {
      Execute exe = new Execute(this);
      exe.setCommandline(getScmLogCommand().getCommandline());
      exe.setWorkingDirectory(base);
      logExecute(exe, base);

      exe.execute();

      // log messages from stderr
      String errors = errorReader.toString().trim();
      if (errors.length() > 0)
      {
        LOG.error(errors);
      }
    }
    catch (IOException ioe)
    {
      handleParserException(ioe);
    }

    return entries;
  }

  /** 
   * Handle ChangeLogParser IOExceptions.  The default implementation
   * just throws the exception again.
   * 
   * @param ioe The IOException thrown.
   * @throws IOException If the handler doesn't wish to handle the
   * exception (the default behavior).
   */
  protected void handleParserException(IOException ioe) throws IOException
  {
    throw ioe;
  }

  /**
   * @see ChangeLogGenerator#getLogStart()
   */
  public String getLogStart()
  {
    return logStart;
  }

  /**
   * @see ChangeLogGenerator#getLogEnd()
   */
  public String getLogEnd()
  {
    // TODO: Auto-generated method stub
    return logEnd;
  }

  /**
   * Clean up any generated resources for this run.
   *
   * @see ChangeLogGenerator#cleanup()
   */
  public void cleanup()
  {
  }

  /**
   * Constructs the appropriate command line to execute the scm's
   * log command.  This method must be implemented by subclasses.
   *
   * @return The command line to be executed.
   */
  protected abstract Commandline getScmLogCommand();

  /** 
   * Construct the command-line argument that is passed to the scm
   * client to specify the appropriate date range.
   * 
   * @param before The starting point.
   * @param to The ending point.
   * @return A string that can be used to specify a date to a scm
   * system.
   */
  protected abstract String getScmDateArgument(Date before, Date to);

  /** 
   * Construct the command-line argument that is passed to the scm
   * client to specify the appropriate tag.
   * 
   * @param tagStart  The tag name for the start of the log (log shouldn't actually contain the tag).
   * @param tagEnd  The tag name for the end of the log (the log can contain this tag), or <code>null</code> to
   *            log all changes since <code>tagStart</code>.
   * @return A string that can be used to specify the tag range to a scm system.
   */
  protected abstract String getScmTagArgument(String tagStart, String tagEnd);

  /**
   * Stop the process - currently unimplemented
   */
  public void stop()
  {
  }

  /**
   * Set the input stream for the scm process.
   * @param os An {@link java.io.OutputStream}
   */
  public void setProcessInputStream(OutputStream os)
  {
  }

  /**
   * Set the error stream for reading from scm log. This stream will
   * be read on a separate thread.
   *
   * @param is An {@link java.io.InputStream}
   */
  public void setProcessErrorStream(InputStream is)
  {
    errorReader = new AsyncStreamReader(is);
  }

  /**
   * Set the input stream used to read from scm log.
   *
   * @param is A stream of scm log output to be read from
   */
  public void setProcessOutputStream(InputStream is)
  {
    in = is;
  }

  /**
   * Start read from the scm log.
   *
   * @throws IOException When there are errors reading from the
   * streams previously provided
   */
  public void start() throws IOException
  {
    errorReader.start();
    entries = clParser.parse(in);
  }

  /**
   * Returns the connection.
   * @return String
   */
  public String getConnection()
  {
    return connection;
  }

  /**
   * Sets the connection.
   * @param connection The connection to set
   */
  public void setConnection(String connection)
  {
    this.connection = connection;
  }

  /**
   * Returns the commentFormat used to interrogate the RCS.
   * @return String
   */
  public String getCommentFormat()
  {
    return commentFormat;
  }

  /**
   * Sets the commentFormat.
   * @param commentFormat The commentFormat to set
   */
  public void setCommentFormat(String commentFormat)
  {
    this.commentFormat = commentFormat;
  }

  /**
   * Logs the pertinent details to the logging system (info level)
   * @param exe    The object to log
   * @param base   The working directory
   */
  public static void logExecute(Execute exe, File base)
  {
    String c[] = exe.getCommandline();
    LOG.info("SCM Working Directory: " + base);
    for (int i = 0; i < c.length; i++)
    {
      String string = c[i];
      LOG.info("SCM Command Line[" + i + "]: " + string);
    }
  }

}
