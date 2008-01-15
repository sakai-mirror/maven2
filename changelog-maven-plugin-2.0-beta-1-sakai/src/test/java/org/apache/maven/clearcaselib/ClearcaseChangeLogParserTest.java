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

import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;

import org.apache.maven.changelog.ChangeLogEntry;

/**
 */
public class ClearcaseChangeLogParserTest extends TestCase {

    /** the {@link ClearcaseChangeLogParser}used for testing */
    private ClearcaseChangeLogParser instance;

    /** file with test results to check against */
    private String testFile;

    /**
     * Create a test with the given name
     * 
     * @param testName
     *            the name of the test
     */
    public ClearcaseChangeLogParserTest(String testName) {
        super(testName);
    }

    /**
     * Initialize per test data
     * 
     * @throws Exception
     *             when there is an unexpected problem
     */
    public void setUp() throws Exception {
        String baseDir = System.getProperty("basedir");
        assertNotNull("The system property basedir was not defined.", baseDir);
        testFile = baseDir
                + "/src/test-resources/clearcaselib/clearcaselog.txt";
        instance = new ClearcaseChangeLogParser();
    }

    /**
     * Test of parse method
     * 
     * @throws Exception
     *             when there is an unexpected problem
     */
    public void testParse() throws Exception {
        FileInputStream fis = new FileInputStream(testFile);
        Collection entries = instance.parse(fis);
        assertEquals("Wrong number of entries returned", 3, entries.size());
        ChangeLogEntry entry = null;
        for (Iterator i = entries.iterator(); i.hasNext();) {
            entry = (ChangeLogEntry) i.next();
            assertTrue("ChangeLogEntry erroneously picked up", entry.toString()
                    .indexOf("ChangeLogEntry.java") == -1);
        }

    }

    public void testParseCorrectUsername() throws Exception {

        // parse the test file
        FileInputStream fis = new FileInputStream(testFile);
        Collection entries = instance.parse(fis);

        // check 8 char usernames are parsed correctly
        Iterator i = entries.iterator();
        ChangeLogEntry entry = (ChangeLogEntry) i.next();
        assertEquals("exactly 8 chars expected", "88888888", entry.getAuthor());

        // check < 8 char usernames are parsed correctly
        entry = (ChangeLogEntry) i.next();
        assertEquals("exactly 5 chars expected", "55555", entry.getAuthor());


    }

    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}

}