package org.apache.maven.starteamlib;

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
// maven imports
import org.apache.maven.changelog.AbstractChangeLogGenerator;
import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.plugin.logging.Log;
// ant imports
import org.apache.tools.ant.types.Commandline;

/**
 * A Starteam implementation of the {@link org.apache.maven.changelog.ChangeLog}
 * interface.
 *
 * @author <a href="mailto:evenisse@ifrance.com">Emmanuel Venisse</a>
 * @version $Id$
 */
class StarteamChangeLogGenerator extends AbstractChangeLogGenerator
{
    /** Log */
    private static final Log LOG = ChangeLog.getLog();

    /**
     * Constructs the appropriate command line to execute the starteam
     * log command whose output is then later parsed.
     *
     * @return the starteam command line to be executed.
     */
    protected Commandline getScmLogCommand() 
    {
        Commandline command = new Commandline();
        command.setExecutable("stcmd");
        command.createArgument().setValue("hist");
        command.createArgument().setValue("-x");
        command.createArgument().setValue("-nologo");
        command.createArgument().setValue("-is");
        command.createArgument().setValue("-p");

        String repo = changeLogExecutor.getRepositoryConnection();

        if ("starteam".equals(repo.substring(4, 12)))
        {
            repo = repo.substring(13);
            command.createArgument().setValue(repo);
        }
        return command;
    }

    /** 
     * Construct the Starteam command-line argument that is used to specify
     * the appropriate date range.  This date option doesn't supported with
     * Starteam
     * 
     * @param before The starting point.
     * @param to The ending point.
     * @return An empty string
     */
    protected String getScmDateArgument(Date before, Date to)
    {
        return "";
    }

    /**
     * @see AbstractChangeLogGenerator#getScmTagArgument(String, String)
     */
    protected String getScmTagArgument(String tagStart, String tagEnd)
    {
        throw new UnsupportedOperationException("This plugin currently does not support generating logs from tags with Starteam.");
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
            || ioe.getMessage().indexOf("Starteam: not found") != -1)
        {
            // can't find Starteam on Win32 or Linux...
            LOG.error(
                "Unable to find Starteam executable. Changelog will be empty("+ioe+")");
        }
        else
        {
            throw ioe;
        }
    }
}
