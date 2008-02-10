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

import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Instance of <code>ChangeLogParser</code> are intended to parse an {@link
 * java.io.InputStream} created by a {@link ChangeLogGenerator} into individual
 * {@link ChangeLogEntry} objects.
 *
 * @author Glenn McAllister
 * @version $Id$
 */
public interface ChangeLogParser
{
    /**
     * Initialize the ChangeLogParser instance with the controlling {@link
     * ChangeLog} instance.  Any configuration required for the parser should
     * be obtained from the <code>changeLog</code>.  This method is guaranteed
     * to be called before {@link #parse}.
     *
     * @param changeLog the controlling ChangeLog instance
     */
    void init(ChangeLog changeLog);

    /**
     * Returns a {@link java.util.Collection} of ChangeLogEntry objects, parsed
     * from the {@link java.io.InputStream}.  This method is guaranteed to be
     * called after {@link #init} and before {@link #cleanup}.  However, it is
     * up to a {@link ChangeLogGenerator} instance to call this method, so no
     * guarantee can be made this this method will be called.
     *
     * @param in the input stream to parse
     * @return a Collection of ChangeLogEntry objects
     * @throws IOException if there is an error while parsing the input stream
     */
    Collection parse(InputStream in) throws IOException;

    /**
     * Provides the opportunity for the parser to do any required cleanup.
     * This method is guaranteed to be called after the {@link #init} (and
     * presumably the {@link #parse}) method.
     */
    void cleanup();

    /**
     * Set the date formatter for parse starteam stream
     * @param dateFormat a dateFormat for replace the local format
     */
    public void setDateFormatInFile( String dateFormat );
}
