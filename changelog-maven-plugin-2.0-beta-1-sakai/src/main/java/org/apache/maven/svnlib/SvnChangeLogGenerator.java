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

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
// maven imports
import org.apache.maven.changelog.AbstractChangeLogGenerator;
import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.plugin.logging.Log;
// ant imports
import org.apache.tools.ant.types.Commandline;

/**
 * A Subversion implementation of the {@link org.apache.maven.changelog.ChangeLog}
 * interface.
 *
 * @author Glenn McAllister
 * @author <a href="mailto:jeff.martin@synamic.co.uk">Jeff Martin</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:dion@multitask.com.au">dIon Gillard</a>
 * @author <a href="mailto:bodewig@apache.org">Stefan Bodewig</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @author Daniel Rall
 * @version 
 * $Id$
 */
class SvnChangeLogGenerator extends AbstractChangeLogGenerator
{
    /** Log */ 
    private static final Log LOG = ChangeLog.getLog();
    /**
     * Constructs the appropriate command line to execute the subversion
     * log command whose output is then later parsed.
     *
     * @return the cvs command line to be executed.
     */
    protected Commandline getScmLogCommand() 
    {
        Commandline command = new Commandline();

        command.setExecutable("svn");
        command.createArgument().setValue("log");
        command.createArgument().setValue("-v");

        if (dateRange != null)
        {
            command.createArgument().setValue(dateRange);
        }
        
        return command;
    }

    /** 
     * Construct the svn command-line argument that is used to specify
     * the appropriate date range.  This date option takes the format
     * of <code>-r{start}:{end}</code> in the case of Subversion.
     * 
     * @param before The starting point.
     * @param to The ending point.
     * @return A string that can be used to specify a date to a scm
     * system.
     */
    protected String getScmDateArgument(Date before, Date to)
    {
        SimpleDateFormat outputDate = new SimpleDateFormat("yyyy-MM-dd");
        // Tell SVN to sort log entries from newest to oldest.
        String cmd = "-r{" + outputDate.format(to) + "}:{" +
            outputDate.format(before) + "}";
        if ( System.getProperty( "os.name" ).startsWith( "Windows" ) )
        {
            cmd = "\"" + cmd + "\"";
        }
        return cmd;
    }

    /**
     * @see AbstractChangeLogGenerator#getScmTagArgument(String, String)
     */
    protected String getScmTagArgument(String tagStart, String tagEnd)
    {
        throw new UnsupportedOperationException("This plugin currently does not support generating logs from tags with Subversion.");
    }

    /** 
     * Handle ChangeLogParser IOExceptions.  
     * 
     * @param ioe The IOException thrown.
     * @throws IOException If the handler doesn't wish to handle the
     * exception.
     */
    protected void handleParserException(IOException ioe)
        throws IOException
    {
        if (ioe.getMessage().indexOf("CreateProcess")  != -1
            || ioe.getMessage().indexOf("svn: not found") != -1)
        {
            // can't find svn on Win32 or Linux...
            LOG.error(
                "Unable to find svn executable. Changelog will be empty");
        }
        else
        {
            throw ioe;
        }
    }
}
