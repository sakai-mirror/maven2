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

/**
 * An AbstractFactory interface for creating the required ChangeLogGenerator
 * and ChangeLogParser pairs.
 *
 * @author Glenn McAllister
 * @version $Id$
 */
public interface ChangeLogFactory
{
    /**
     * Create the ChangeLogGenerator that extracts data from an SCM to be
     * parsed by an associated ChangeLogParser.
     *
     * @return The ChangeLogGenerator for a particular SCM.
     */
    ChangeLogGenerator createGenerator();

    /**
     * Create the ChangeLogParser that consumes the output from the
     * ChangeLogGenerator to produce the set of ChangeLogEntry objects.
     *
     * @return The ChangeLogParser for a particular SCM.
     */
    ChangeLogParser createParser();
}
