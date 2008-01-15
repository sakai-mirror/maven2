package org.apache.maven.starteamlib;

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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import junit.framework.TestCase;

import org.apache.maven.changelog.ChangeLogEntry;


/**
 * Test cases for {@link StarteamChangeLogParser}
 * @author <a href="mailto:evenisse@ifrance.com">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamChangeLogParser2Test extends TestCase
{

    /** the {@link StarteamChangeLogParser} used for testing */
    private StarteamChangeLogParser instance;
    /** file with test results to check against */
    private String testFile;

    /**
     * Create a test with the given name
     * @param testName the name of the test
     */
    public StarteamChangeLogParser2Test(String testName)
    {
        super(testName);
    }

    /**
     * Initialize per test data
     * @throws Exception when there is an unexpected problem
     */
    public void setUp() throws Exception
    {
        String baseDir = System.getProperty("basedir");
        assertNotNull("The system property basedir was not defined.", baseDir);
        testFile = baseDir + "/src/test-resources/starteamlib/starteamlog2.txt";
        instance = new StarteamChangeLogParser();
    }

    /**
     * Test of parse method
     * @throws Exception when there is an unexpected problem
     */
    public void testParse() throws Exception
    {
        FileInputStream fis = new FileInputStream(testFile);
        instance.setDateFormatInFile("yy-MM-dd HH:mm");
        Collection entries = instance.parse(fis);
        assertEquals("Wrong number of entries returned", 3, entries.size());
        ChangeLogEntry entry = null;
        for (Iterator i = entries.iterator(); i.hasNext(); )
        {
            entry = (ChangeLogEntry) i.next();
            assertTrue("ChangeLogEntry erroneously picked up",
                entry.toString().indexOf("ChangeLogEntry.java") == -1);
        }

    }

}
