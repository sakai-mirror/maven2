package org.apache.maven.svnlib;

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
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import junit.framework.TestCase;

import org.apache.maven.changelog.ChangeLogEntry;

/**
 * Test cases for {@link SvnChangeLogParser}.
 *
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @version $Id$
 */
public class SvnChangeLogParserTest extends TestCase
{
    /** Date formatter */
    private static final SimpleDateFormat DATE =
        new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);

    /** The {@link SvnChangeLogParser} used for testing */
    private SvnChangeLogParser parser;

    /** File with test results to check against */
    private String testFile;

    /**
     * Create a test with the given name.
     *
     * @param testName the name of the test.
     */
    public SvnChangeLogParserTest(String testName)
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
        testFile = baseDir + "/src/test-resources/svnlib/svnlog.txt";
        parser = new SvnChangeLogParser();
    }

    /**
     * Test the subversion parser.
     *
     * @throws Exception when there is an unexpected problem
     */
    public void testParse() throws Exception
    {
        FileInputStream fis = new FileInputStream(testFile);
        List entries = new ArrayList(parser.parse(fis));

        assertEquals("Wrong number of entries returned", 13, entries.size());

        ChangeLogEntry entry = (ChangeLogEntry) entries.get(0);
        assertEquals("Entry 0 was parsed incorrectly", 
                "kaz\n" +
                DATE.parse("Mon Aug 26 14:33:26 EDT 2002") + "\n" +
                "[/poolserver/trunk/build.xml, 15, " +
                "/poolserver/trunk/project.properties, 15]\n" +
                "Minor formatting changes.\n\n",
                entry.toString());

        entry = (ChangeLogEntry) entries.get(6);
        assertEquals("Entry 6 was parsed incorrectly", 
                "(no author)\n" +
                DATE.parse("Fri Aug 23 11:11:52 EDT 2002") + "\n" +
                "[/poolserver/trunk/build.xml, 9]\n" +
                "Testing script out again ...\n\n",
                entry.toString());

        entry = (ChangeLogEntry) entries.get(8);
        assertEquals("Entry 8 was parsed incorrectly",
                "pete\n" +
                DATE.parse("Fri Aug 23 11:03:39 EDT 2002") + "\n" +
                "[/poolserver/trunk/build.xml, 7]\n" +
                "Reformatted the indentation (really just an excuse to test out\n" +
                "subversion).\n\n",
                entry.toString());

        entry = (ChangeLogEntry) entries.get(12);
        assertEquals("Entry 12 was parsed incorrectly",
                "DOMAIN\\user\n" +
                DATE.parse("Wed Aug 21 00:20:25 EDT 2002") + "\n" +
                "[/poolserver/trunk/build.xml, 1]\n" +
                "Cleaned up some whitespace.\n\n",
                entry.toString());
    }
}
