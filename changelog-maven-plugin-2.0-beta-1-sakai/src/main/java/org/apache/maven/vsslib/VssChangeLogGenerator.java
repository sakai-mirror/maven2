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
 * ====================================================================
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.apache.maven.changelog.AbstractChangeLogGenerator;
import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.changelog.ChangeLogParser;
import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;

/*******************************************************************************
 * A Visual Source Safe implementation of the
 * {@link org.apache.maven.changelog.ChangeLogGenerator}interface realized
 * extending the {@link org.apache.maven.changelog.AbstractChangeLogGenerator}.
 * 
 * The command line build by this class uses the <code>ss History</code> VSS
 * command and formats the output in a way the VssChangeLogParser can
 * understand. Due to this fact this implementations works only if used within a
 * vss view.
 * 
 * The command looks like this:
 * <p>
 * set ssdir=vssRepositoryPath ss History vssProject -YvssUserId,vssUserPassword
 * -R -Vd01/12/04~23/11/04 -I-Y
 * 
 * @author Freddy Mallet
 */
public class VssChangeLogGenerator extends AbstractChangeLogGenerator {

    /**
     * Log
     */
    private static final Log LOG = ChangeLog.getLog();

    /***************************************************************************
     * Constructs the appropriate command line to execute the scm's log command.
     * For Clearcase it's lshistory.
     * 
     * @see org.apache.maven.changelog.AbstractChangeLogGenerator#getScmLogCommand()
     * @return The command line to be executed.
     */
    protected Commandline getScmLogCommand() {
        VssConnection vssConnection = new VssConnection(getConnection());
        Commandline command = new Commandline();
        command.setExecutable("ss");
        command.createArgument().setValue("History");
        command.createArgument().setValue("$" + vssConnection.getVssProject());
        //User identification to get access to vss repository 
        if(vssConnection.getVssUserInf() != null){
            command.createArgument().setValue("-Y" + vssConnection.getVssUserInf());
        }
        //Display the history of an entire project list
        command.createArgument().setValue("-R");
        //Ignore: Do not ask for input under any circumstances.
        command.createArgument().setValue("-I-");
        //Display only versions that fall within specified data range.
        if (dateRange != null) {
            command.createArgument().setValue("-Vd" + dateRange);
        }
        return command;
    }

    /***************************************************************************
     * Construct the command-line argument that is passed to the scm client to
     * specify the appropriate date range.
     * 
     * @param before
     *            The starting point.
     * @param to
     *            The ending point.
     * @return A string that can be used to specify a date to a scm system.
     * 
     * @see org.apache.maven.changelog.AbstractChangeLogGenerator#getScmDateArgument(java.util.Date,
     *      java.util.Date)
     */
    protected String getScmDateArgument(Date before, Date to) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",
                Locale.ENGLISH);
        String result = sdf.format(to) + "~" + sdf.format(before);
        return result;
    }

    protected String getScmTagArgument( String tagStart, String tagEnd )
    {
        throw new UnsupportedOperationException( "Cannot presently use tags for SCM operations on VSS" );
    }

    /**
     * Convert date range from 'number of days' format to 'date to date' format
     * 
     * @param numDaysString
     *            String
     */
    public void setDateRange(String numDaysString) {
        int days = Integer.parseInt(numDaysString);

        Date before = new Date(System.currentTimeMillis() - (long) days * 24
                * 60 * 60 * 1000);
        Date to = new Date(System.currentTimeMillis() + (long) 1 * 24 * 60 * 60
                * 1000);
    }

    /***************************************************************************
     * Execute vss client driving the given parser. This method has been
     * overwritten to be able to set 'SSDIR' environment variable. This variable
     * set the vss repository
     * 
     * @param parser
     *            A {@link ChangeLogParser parser}to process the vss output.
     * @return A collection of {@link ChangeLogEntry entries}parsed from the
     *         vss output.
     * @throws IOException
     *             When there are issues executing vss.
     * @see ChangeLogGenerator#getEntries(ChangeLogParser)
     */
    public Collection getEntries(ChangeLogParser parser) throws IOException {
        if (parser == null) {
            throw new NullPointerException("parser cannot be null");
        }

        if (base == null) {
            throw new NullPointerException("basedir must be set");
        }

        if (!base.exists()) {
            throw new FileNotFoundException("Cannot find base dir "
                    + base.getAbsolutePath());
        }

        clParser = parser;
        try {
            VssConnection vssConnection = new VssConnection(getConnection());

            Execute exe = new Execute(this);

            String[] env = exe.getEnvironment();
            if (env == null) {
                env = new String[0];
            }
            String[] newEnv = new String[env.length + 1];
            for (int i = 0; i < env.length; i++) {
                newEnv[i] = env[i];
            }
            newEnv[env.length] = "SSDIR=" + vssConnection.getVssDir();

            exe.setEnvironment(newEnv);
            exe.setCommandline(getScmLogCommand().getCommandline());
            //exe.setVMLauncher(false);
            exe.setWorkingDirectory(base);
            logExecute(exe, base);

            exe.execute();

            // log messages from stderr
            String errors = errorReader.toString().trim();
            if (errors.length() > 0) {
                LOG.error(errors);
            }
        } catch (IOException ioe) {
            handleParserException(ioe);
        }

        return entries;
    }
}
