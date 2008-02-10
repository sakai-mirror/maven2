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

import java.io.IOException;
import java.util.Date;
import org.apache.maven.changelog.AbstractChangeLogGenerator;
import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.types.Commandline;

/**
 * A Perforce implementation of the {@link org.apache.maven.changelog.ChangeLog}
 * interface.
 *
 * @author <a href="mailto:jim@crossleys.org">Jim Crossley</a>
 * @version $Id: 
 */
class PerforceChangeLogGenerator extends AbstractChangeLogGenerator
{
    /** The position after the connection prefix, 'scm:perforce:' */
    private static final int POS_PARAMS = 13;
    
    /** Log */ 
    private static final Log LOG = ChangeLog.getLog();

    /**
     * Constructs the appropriate command line to execute the perforce
     * log command whose output is then later parsed.
     *
     * Repository connection syntax:
     * scm:perforce[:host:port]://depot/projects/name/...
     *
     * @return the cvs command line to be executed.
     */
    protected Commandline getScmLogCommand() 
    {
        String conn = getConnection();
        if (!conn.startsWith("scm:perforce:"))
        {
            throw new IllegalArgumentException(
                "repository connection string"
                    + " does not specify 'perforce' as the scm");
        }
        int lastColon = conn.lastIndexOf(':');
        String fileSpec = conn.substring(lastColon+1);
        String p4port = (POS_PARAMS>lastColon) ? null : conn.substring(POS_PARAMS, lastColon);

        Commandline command = new Commandline();
        command.setExecutable("p4");
        if (p4port != null) {
            command.createArgument().setValue("-p");
            command.createArgument().setValue(p4port);
        }
        command.createArgument().setValue("filelog");
        command.createArgument().setValue("-tl");
        command.createArgument().setValue(fileSpec);
        
        return command;
    }

    /** 
     * The Perforce filelog doesn't support a revision, i.e. date,
     * range
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
            || ioe.getMessage().indexOf("p4: not found") != -1)
        {
            // can't find p4 on Win32 or Linux...
            LOG.error(
                      "Unable to find p4 executable. Changelog will be empty");
        }
        else
        {
            throw ioe;
        }
    }

    /**
     * @see AbstractChangeLogGenerator#getScmTagArgument(String, String)
     */
    protected String getScmTagArgument(String tagStart, String tagEnd)
    {
        throw new UnsupportedOperationException("This plugin currently does not support generating logs from tags with Perforce.");
    }
}
