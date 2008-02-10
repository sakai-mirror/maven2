package org.apache.maven.clearcaselib;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// ant imports
import org.apache.tools.ant.types.Commandline;

import org.apache.maven.changelog.AbstractChangeLogGenerator;


/**
 * A Clearcase implementation of the {@link org.apache.maven.changelog.ChangeLogGenerator}
 * interface realized extending the {@link org.apache.maven.changelog.AbstractChangeLogGenerator}.
 * 
 * The command line build by this class uses the <code>lshistory</code> clearcase command
 * and formats the output in a way the ClearcaseChangeLogParser can understand. Due to this
 * fact this implementations works only if used within a clearcase view.
 * 
 * The command looks like this: <p>
 * cleartool lshistory -recurse -nco -since SAMEDATE 
 * -fmt "NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%-8.8u\\n"
 *
 * @author <a href="mailto:aldarion@virgilio.it">Simone Zorzetti</a>
 */
public class ClearcaseChangeLogGenerator extends AbstractChangeLogGenerator {

    /**
     * Constructs the appropriate command line to execute the scm's
     * log command.  For Clearcase it's lshistory.
     *     
     * @see org.apache.maven.changelog.AbstractChangeLogGenerator#getScmLogCommand()
     * @return The command line to be executed.
     */
    protected Commandline getScmLogCommand()
    {
        Commandline command = new Commandline();
        command.setExecutable("cleartool");
        command.createArgument().setValue("lshistory");

        StringBuffer format = new StringBuffer();
        format.append("NAME:%En\\n");
        format.append("DATE:%Nd\\n");    
        format.append("COMM:%-12.12o - ");

        String commentFormat = getCommentFormat();
        if (commentFormat == null)
        {
            format.append("%Sn - %c - Activity: %[activity]p\\n");
        }
        else
        {
            format.append(commentFormat);
        }
             
        format.append("USER:%-8.8u\\n");

        command.createArgument().setValue("-fmt");
        command.createArgument().setValue(format.toString());
        command.createArgument().setValue("-recurse");
        command.createArgument().setValue("-nco");
        command.createArgument().setValue("-since");
        command.createArgument().setValue(dateRange);

        return command; 
    }

    /** 
     * Construct the command-line argument that is passed to the scm
     * client to specify the appropriate date range.
     * 
     * @param before The starting point.
     * @param to The ending point.
     * @return A string that can be used to specify a date to a scm
     * system.
     *  
     * @see org.apache.maven.changelog.AbstractChangeLogGenerator#getScmDateArgument(java.util.Date, java.util.Date)  
     */
    protected String getScmDateArgument(Date before, Date to)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        String argument = sdf.format(before);        
        
        return argument;
    }

    /**
     * @see AbstractChangeLogGenerator#getScmTagArgument(String, String)
     */
    protected String getScmTagArgument(String tagStart, String tagEnd)
    {
        throw new UnsupportedOperationException("This plugin currently does not support generating logs from tags with Clearcase.");
    }
}
