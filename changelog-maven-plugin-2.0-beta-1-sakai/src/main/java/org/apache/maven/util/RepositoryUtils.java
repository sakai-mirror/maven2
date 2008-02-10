package org.apache.maven.util;

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

import java.util.ArrayList;
import java.util.List;

/**
 * NOTE: This is very CVS specific, but I would like to try additional SCM
 * package like subversion ASAP.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public final class RepositoryUtils
{
    /**
     * Splits an SCM string into parts 
     * @param connection
     * @return
     */
    public static String[] splitSCMConnection(String connection)
    {
        if (connection == null)
        {
            throw new NullPointerException("repository connection is null");
        }

        if (connection.length() < 4)
        {
            throw new IllegalArgumentException("repository connection is too short");
        }

        if (!connection.startsWith("scm"))
        {
            throw new IllegalArgumentException("repository connection must start with scm[delim]");
        }
        
        String delimiter = "" + connection.charAt(3);

        EnhancedStringTokenizer tok = new EnhancedStringTokenizer(connection, delimiter);

        String[] tokens = tokenizerToArray(tok);

        // for a valid repository, it should be scm:<provider> at least
        if (tokens.length >= 1 && tokens[1].equals("cvs"))
        {
            if (tokens.length >= 2 && tokens[2].equals("local"))
            {
                if (tokens.length == 6)
                {
                    if (tokens[3].length() > 0 && !tokens[3].equals("local"))
                    {
                        throw new IllegalArgumentException("cvs local repository connection string must specify 5 tokens, or an empty 3rd token if 6");
                    }
                }
                else if (tokens.length == 5)
                {
                    String[] newTokens = new String[6];
                    newTokens[0] = tokens[0];
                    newTokens[1] = tokens[1];
                    newTokens[2] = tokens[2];
                    newTokens[3] = "";
                    newTokens[4] = tokens[3];
                    newTokens[5] = tokens[4];
                    tokens = newTokens;
                }
                else
                {
                    throw new IllegalArgumentException("cvs local repository connection string doesn't contain five tokens");
                }
            }
            if (tokens.length != 6)
            {
                throw new IllegalArgumentException("cvs repository connection string doesn't contain six tokens");
            }
        }
        return tokens;
    }

    /**
     * Converts a tokenizer to an array of strings
     * FIXME: This should be in a string util class
     * @param tok
     * @return String[]
     */
    public static String[] tokenizerToArray(EnhancedStringTokenizer tok)
    {
        List l = new ArrayList();
        while (tok.hasMoreTokens())
        {
            l.add(tok.nextToken());
        }
        return (String[]) l.toArray(new String[l.size()]);
    }

}
