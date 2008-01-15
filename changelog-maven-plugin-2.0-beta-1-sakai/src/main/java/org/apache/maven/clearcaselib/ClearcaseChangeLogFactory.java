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

import org.apache.maven.changelog.ChangeLogFactory;
import org.apache.maven.changelog.ChangeLogGenerator;
import org.apache.maven.changelog.ChangeLogParser;

/**
 * Provides Clearcase specific instances of the ChangeLogGenerator and
 * ChangeLogParser interfaces.
 *
 * @author <a href="mailto:aldarion@virgilio.it">Simone Zorzetti</a>
 */
public class ClearcaseChangeLogFactory implements ChangeLogFactory {

    /* (non-Javadoc)
     * @see org.apache.maven.changelog.ChangeLogFactory#createGenerator()
     */
    public ChangeLogGenerator createGenerator()
    {
        return new ClearcaseChangeLogGenerator();
    }

    /* (non-Javadoc)
     * @see org.apache.maven.changelog.ChangeLogFactory#createParser()
     */
    public ChangeLogParser createParser()
    {
    return new ClearcaseChangeLogParser();
    }

}
