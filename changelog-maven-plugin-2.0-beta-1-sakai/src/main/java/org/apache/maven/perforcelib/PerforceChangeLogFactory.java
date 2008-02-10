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

import org.apache.maven.changelog.ChangeLogFactory;
import org.apache.maven.changelog.ChangeLogGenerator;
import org.apache.maven.changelog.ChangeLogParser;

/**
 * Provides Perforce specific instances of the ChangeLogGenerator and
 * ChangeLogParser interfaces.
 *
 * It expects the repository connection element in the POM to be of
 * this form: <b><code>scm:perforce[:P4PORT]:FILESPEC</code></b>.  If
 * the P4PORT is omitted, the corresponding environment variable will
 * be used.  The FILESPEC should use "depot syntax" with the
 * appropriate Perforce wildcards to represent all of the project's
 * files, i.e. '...'
 *
 * The repository URL element should refer to a <a
 * href="http://www.perforce.com/perforce/products/p4web.html">p4web</a>
 * instance.  The portion of the URL following the host:port should
 * match the FILESPEC in the repository connection element, sans the
 * '...'.  It must end with a '/'.
 * 
 * For example,
 * <pre>
 * &lt;repository&gt;
 *   &lt;connection&gt;
 *     scm:perforce:some.host.com:1666://depot/projects/maven/...
 *   &lt;/connection&gt;
 *   &lt;url&gt;
 *     http://public.perforce.com:8080//depot/projects/maven/
 *   &lt;/url&gt;
 * &lt;/repository&gt;
 * </pre>
 *
 * @author <a href="mailto:jim@crossleys.org">Jim Crossley</a>
 * @version $Id: 
 */
public class PerforceChangeLogFactory implements ChangeLogFactory
{
    /**
     * Default no-arg constructor.
     */
    public PerforceChangeLogFactory()
    {
    }
    
    /**
     * Create a Perforce specific ChangeLogGenerator.
     *
     * @return a Perforce specific ChangeLogGenerator.
     */
    public ChangeLogGenerator createGenerator()
    {
        return new PerforceChangeLogGenerator();
    }

    /**
     * Create a Perforce specific ChangeLogParser.
     *
     * @return a Perforce specific ChangeLogParser.
     */
    public ChangeLogParser createParser()
    {
        return new PerforceChangeLogParser();
    }
}