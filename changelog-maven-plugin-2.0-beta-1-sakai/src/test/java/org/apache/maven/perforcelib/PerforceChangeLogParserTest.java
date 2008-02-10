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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.apache.maven.changelog.ChangeLogEntry;
import org.apache.maven.changelog.ChangeLog;

/**
 * Test cases for {@link PerforceChangeLogParser}
 * @author <a href="mailto:jim@crossleys.org">Jim Crossley</a>
 * @version $Id: 
 */
public class PerforceChangeLogParserTest extends TestCase
{
    /** The {@link PerforceChangeLogParser} used for testing */
    private PerforceChangeLogParser parser;

    /** File with test results to check against */
    private String testFile;

    /**
     * Create a test with the given name.
     *
     * @param testName the name of the test.
     */
    public PerforceChangeLogParserTest(String testName)
    {
        super(testName);
    }

    /**
     * Initialize per test data.
     *
     * @throws Exception when there is an unexpected problem.
     */
    public void setUp() throws Exception
    {
        String baseDir = System.getProperty("basedir");
        assertNotNull("The system property basedir was not defined.", baseDir);
        testFile = baseDir + "/src/test-resources/perforcelib/perforcelog.txt";
        parser = new PerforceChangeLogParser();
        ChangeLog changeLog = new ChangeLog();
        changeLog.setRepositoryConnection ("scm:perforce://depot/test/...");
        parser.init (changeLog);
    }

    /**
     * Test the Perforce parser.
     *
     * @throws Exception when there is an unexpected problem
     */
    public void testParse() throws Exception
    {
        FileInputStream fis = new FileInputStream(testFile);
        List entries = new ArrayList(parser.parse(fis));

        assertEquals("Wrong number of entries returned", 7, entries.size());

        ChangeLogEntry entry = (ChangeLogEntry) entries.get(0);
        assertEquals("Entry 0 was parsed incorrectly", 
                     "\t<changelog-entry>\n" +
                     "\t\t<date>2003-10-15</date>\n" +
                     "\t\t<time>13:38:40</time>\n" +
                     "\t\t<author><![CDATA[jim]]></author>\n" +
                     "\t\t<file>\n" +
                     "\t\t\t<name>junk/linefeed.txt</name>\n" +
                     "\t\t\t<revision>3</revision>\n" +
                     "\t\t</file>\n" +
                     "\t\t<msg><![CDATA[	Where's my change #\n" +
                     "]]></msg>\n" +
                     "\t</changelog-entry>\n",
                     entry.toXML());

        entry = (ChangeLogEntry) entries.get(3);
        assertEquals("Entry 3 was parsed incorrectly", 
                     "\t<changelog-entry>\n" +
                     "\t\t<date>2003-10-01</date>\n" +
                     "\t\t<time>16:24:20</time>\n" +
                     "\t\t<author><![CDATA[jim]]></author>\n" +
                     "\t\t<file>\n" +
                     "\t\t\t<name>demo/demo.c</name>\n" +
                     "\t\t\t<revision>4</revision>\n" +
                     "\t\t</file>\n" +
                     "\t\t<msg><![CDATA[	Backing out my test changes\n" +
                     "\t\n" +
                     "\tUpdating a description\n" +
                     "]]></msg>\n" +
                     "\t</changelog-entry>\n",
                     entry.toXML());

        entry = (ChangeLogEntry) entries.get(6);
        assertEquals("Entry 6 was parsed incorrectly", 
                     "\t<changelog-entry>\n" +
                     "\t\t<date>2003-08-07</date>\n" +
                     "\t\t<time>17:21:57</time>\n" +
                     "\t\t<author><![CDATA[mcronin]]></author>\n" +
                     "\t\t<file>\n" +
                     "\t\t\t<name>demo/demo.c</name>\n" +
                     "\t\t\t<revision>1</revision>\n" +
                     "\t\t</file>\n" +
                     "\t\t<file>\n" +
                     "\t\t\t<name>demo/dictcalls.txt</name>\n" +
                     "\t\t\t<revision>1</revision>\n" +
                     "\t\t</file>\n" +
                     "\t\t<file>\n" +
                     "\t\t\t<name>demo/dictwords.txt</name>\n" +
                     "\t\t\t<revision>1</revision>\n" +
                     "\t\t</file>\n" +
                     "\t\t<msg><![CDATA[	demonstration of Perforce on Windows, Unix and VMS.\n" +
                     "]]></msg>\n" +
                     "\t</changelog-entry>\n",
                     entry.toXML());
    }
}
