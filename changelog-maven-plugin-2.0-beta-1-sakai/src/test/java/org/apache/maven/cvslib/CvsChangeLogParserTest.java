package org.apache.maven.cvslib;

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
 * Test cases for {@link CvsChangeLogParser}
 * @author dion
 * @version $Id$
 */
public class CvsChangeLogParserTest extends TestCase
{

    /** the {@link CvsChangeLogParser} used for testing */
    private CvsChangeLogParser instance;
    /** file with test results to check against */
    private String testFile;
    /** file with test results to check against */
    private String testFile2;

    /**
     * Create a test with the given name
     * @param testName the name of the test
     */
    public CvsChangeLogParserTest(String testName)
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
        testFile = baseDir + "/src/test-resources/cvslib/cvslog.txt";
        testFile2 = baseDir + "/src/test-resources/cvslib/cvslog_new.txt";
        instance = new CvsChangeLogParser();
    }

    /**
     * Test of parse method
     * @throws Exception when there is an unexpected problem
     */
    public void testParse() throws Exception
    {
        parse(testFile);
    }
    
    /**
     * Test of parse method
     * @throws Exception when there is an unexpected problem
     */
    public void testParse2() throws Exception
    {
        parse(testFile2);
    }
    
    /**
     * Test of parse method
     * @throws Exception when there is an unexpected problem
     */
    public void parse(String file) throws Exception
    {
        FileInputStream fis = new FileInputStream(file);
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

    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}


}
