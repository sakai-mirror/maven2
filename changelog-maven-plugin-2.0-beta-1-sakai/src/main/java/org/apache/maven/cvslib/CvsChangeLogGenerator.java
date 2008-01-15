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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.maven.changelog.AbstractChangeLogGenerator;
import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.changelog.ChangeLogParser;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.util.AsyncStreamReader;
import org.apache.maven.util.RepositoryUtils;
import org.apache.tools.ant.types.Commandline;

/**
 * A CVS implementation of the {@link org.apache.maven.changelog.ChangeLog}
 * interface.
 * 
 * @author Glenn McAllister
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:jeff.martin@synamic.co.uk">Jeff Martin</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:dion@multitask.com.au">dIon Gillard</a>
 * @author <a href="mailto:bodewig@apache.org">Stefan Bodewig</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @version $Id: CvsChangeLogGenerator.java,v 1.6 2003/04/11 18:53:19 bwalding
 *          Exp $
 */
class CvsChangeLogGenerator extends AbstractChangeLogGenerator
{
    private final Log LOG = ChangeLog.getLog();
    
    public static final int POS_SCM = 0;
    public static final int POS_SCM_TYPE = 1;
    public static final int POS_SCM_SUBTYPE = 2;
    public static final int POS_SCM_USERHOST = 3;
    public static final int POS_SCM_PATH = 4;
    public static final int POS_SCM_MODULE = 5;

    /**
	 * Execute cvslib client driving the given parser. @todo Currently the
	 * output from the logListener is a String, which is then converted to an
	 * InputStream. The output of logListener really should be an input stream.
	 * 
	 * @param parser A {@link ChangeLogParser parser}to process the scm
	 *            output.
	 * @return A collection of {@link ChangeLogEntry entries}parsed from the
	 *         scm output.
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
            throw new FileNotFoundException(
                "Cannot find base dir " + base.getAbsolutePath());
        }

        clParser = parser;

        String[] args = getScmLogCommand().getArguments();
        CvsLogListener logListener = new CvsLogListener();
        try
        {
            CvsConnection.processCommand(
                args,
                this.changeLogExecutor.getBasedir().toString(),
                logListener);
            entries =
                clParser.parse(
                    new ByteArrayInputStream(
                        logListener.getStdout().toString().getBytes()));
        }
        catch (IllegalArgumentException iae)
        {
            entries = super.getEntries(parser);
        }
        catch (Exception e){
            LOG.error("Error processing command", e);
        }
        

        return entries;
    }
    /**
	 * @return the cvs command line to be executed.
	 */
    protected Commandline getScmLogCommand()
    {
        String tokens[] = RepositoryUtils.splitSCMConnection(getConnection());

        if (!tokens[POS_SCM_TYPE].equals("cvs"))
        {
            throw new IllegalArgumentException(
                "repository connection string"
                    + " does not specify 'cvs' as the scm"
                    + System.getProperty("line.separator") 
                    + "If using another scm, maven.changelog.factory"
                    + " must be set." 
                    + System.getProperty("line.separator")
                    + "See the maven changelog plugin documentation" 
                    + " for correct settings."  );
        }

        Commandline command = new Commandline();

        command.setExecutable("cvs");
        command.createArgument().setValue("-d");
        // from format:
        // scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:jakarta-turbine-maven/src/plugins-build/changelog/
        // to format:
        // :pserver:anoncvs@cvs.apache.org:/home/cvspublic
        // use tokens 3+4+5
        String connectionBuffer = "";

        if (tokens[POS_SCM_SUBTYPE].equalsIgnoreCase("local"))
        {
            // use the local repository directory eg. '/home/cvspublic'
            connectionBuffer = tokens[POS_SCM_PATH];
        }
        else if (tokens[POS_SCM_SUBTYPE].equalsIgnoreCase("lserver"))
        {
            //create the cvsroot as the local socket cvsroot
            connectionBuffer =
                tokens[POS_SCM_USERHOST] + ":" + tokens[POS_SCM_PATH];
        }
        else
        {
            //create the cvsroot as the remote cvsroot
            connectionBuffer =
                ":"
                    + tokens[POS_SCM_SUBTYPE]
                    + ":"
                    + tokens[POS_SCM_USERHOST]
                    + ":"
                    + tokens[POS_SCM_PATH];
        }

        command.createArgument().setValue(connectionBuffer.toString());
        command.createArgument().setValue("log");

        if (dateRange != null)
        {
            command.createArgument().setValue(dateRange);
        }
        else if (tag != null)
        {
            command.createArgument().setValue(tag);
        }

        return command;
    }

    /**
	 * Construct the CVS command-line argument that is used to specify the
	 * appropriate date range.
	 * 
	 * @param before The starting point.
	 * @param to The ending point.
	 * @return A string that can be used to specify a date to a scm system.
	 */
    protected String getScmDateArgument(Date before, Date to)
    {
        SimpleDateFormat outputDate = new SimpleDateFormat("yyyy-MM-dd");
        String cmd = outputDate.format(before) + "<" + outputDate.format(to);
        if ( System.getProperty( "os.name" ).startsWith( "Windows" ) )
        {
            cmd = "\"" + cmd + "\"";
        }
        return "-d " + cmd;
    }
    
    /**
     * @see AbstractChangeLogGenerator#getScmTagArgument(String, String)
     */
    protected String getScmTagArgument(String tagStart, String tagEnd)
    {
        return "-r" + tagStart + "::" + (tagEnd != null ? tagEnd : "");
    }

    /**
	 * Handle ChangeLogParser IOExceptions.
	 * 
	 * @param ioe The IOException thrown.
	 * @throws IOException If the handler doesn't wish to handle the exception.
	 */
    protected void handleParserException(IOException ioe) throws IOException
    {
        if (ioe.getMessage().indexOf("CreateProcess") != -1
            || ioe.getMessage().indexOf("cvs: not found") != -1)
        {
            // can't find CVS on Win32 or Linux...
            if (LOG.isWarnEnabled())
            {
                LOG.warn(
                    "Unable to find cvs executable. Please check that it is in your path, and avoid using spaces in the path name. Changelog will be empty" );
            }
        }
        else
        {
            throw ioe;
        }
    }

    /**
	 * Set the error stream for reading from cvs log. This stream will be read
	 * on a separate thread.
	 * 
	 * @param is - an {@link java.io.InputStream}
	 */
    public void setProcessErrorStream(InputStream is)
    {
        errorReader = new CvsAsyncErrorReader(is);
    }

    /**
	 * A private AsyncStreamReader class that "swallows" the "cvs server:
	 * Logging" lines.
	 */
    private static class CvsAsyncErrorReader extends AsyncStreamReader
    {
        /**
		 * The obvious constructor.
		 * 
		 * @param anInputStream the input stream to consume
		 */
        public CvsAsyncErrorReader(InputStream anInputStream)
        {
            super(anInputStream);
        }

        /**
		 * If the line does not start with "cvs server: Logging", it's ok to
		 * consume it.
		 * 
		 * @param line the line to check
		 * @return <code>true</code> if the line does not start with "cvs
		 *         server: Logging"
		 */
        protected boolean okToConsume(String line)
        {
            return !line.startsWith("cvs server: Logging");
        }
    }
}
