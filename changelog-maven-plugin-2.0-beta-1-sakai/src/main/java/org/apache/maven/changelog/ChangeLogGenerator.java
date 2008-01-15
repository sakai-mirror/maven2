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

import java.io.IOException;
import java.util.Collection;

/**
 * Instances of <code>ChangeLogGenerator</code> are intended to provide an
 * {@link java.io.InputStream} for a {@link ChangeLogParser} to parse into 
 * individual {@link ChangeLogEntry} objects.
 * 
 * @author Glenn McAllister
 * @author dion
 * @version $Id$
 */
public interface ChangeLogGenerator
{
    /**
     * Initialize the ChangeLogGenerator instance with in the controlling
     * {@link ChangeLog} instance.  Any configuration required for the generator
     * should be obtained from the <code>changeLog</code>.  This method is 
     * guaranteed to be called before {@link #getEntries}.
     *
     * @param changeLog the controlling ChangeLog instance
     */
    void init(ChangeLog changeLog);

    /**
     * Return a Collection of ChangeLogEntry objects.  This method should
     * create an {@link java.io.InputStream} that contains the change log
     * insformation which is then passed to the <code>parser</code> to create
     * the individual {@link ChangeLogEntry} objects.
     *
     * <p>This method is guaranteed to be called after {@link #init} and before
     * {@link #cleanup}.  This method must invoke <em>only</em> the {@link
     * ChangeLogParser#parse} method.</p>
     *
     * @param parser the parser that will create the individual ChangeLogEntry
     * objects.
     * @return a Collection of ChangeLogEntry objects
     * @throws IOException if there is an error while creating the
     * ChangeLogEntry objects
     */
    Collection getEntries(ChangeLogParser parser) throws IOException;

    /**
     * Return a string indicating the start of the entries.
     * This will usually be a date or a tag.
     * 
     * @return  a string indicating the start of the entries.
     */
    String getLogStart();
    
    /**
     * Return a string indicating the end of the entries.
     * This will usually be a date or a tag.
     * 
     * @return  a string indicating the end of the entries.
     */
    String getLogEnd();
    
    /**
     * Provides the opportunity for the generator to do any required cleanup.
     * This method is guaranteed to be called after the getEntries method even
     * if an exception is thrown from getEntries.
     */
    void cleanup();
}
