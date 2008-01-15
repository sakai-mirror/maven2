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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test cases for {@link ChangeLogFile}
 * @author dIon Gillard
 * @version $Id$
 */
public class ChangeLogFileTest extends TestCase
{

    /** the {@link ChangeLogFile} used for testing */
    private ChangeLogFile instance;
    
    /**
     * Create a test with the given name
     * @param testName the name of the test
     */
    public ChangeLogFileTest(String testName)
    {
        super(testName);
    }
    
    /**
     * Run the test using the {@link TestRunner}
     * @param args command line provided arguments
     */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
    /**
     * Create a test suite for this class
     * @return a {@link TestSuite} for all tests in this class
     */
    public static Test suite()
    {
        return new TestSuite(ChangeLogFileTest.class);
    }

    /**
     * Initialize per test data
     */
    public void setUp()
    {
        instance = new ChangeLogFile("maven:dummy", "maven:rev");
    }

    /**
     * Test of getName method
     */
    public void testGetName()
    {
        assertEquals("Name not being retrieved correctly", "maven:dummy", 
            instance.getName()); 
    }
    
    /**
     * Test of getRevision method
     */
    public void testGetRevision()
    {
        assertEquals("Revision not being retrieved correctly", "maven:rev", 
            instance.getRevision()); 
    }
    
    /** 
     * Test of setName method
     */
    public void testSetName()
    {
        instance.setName("maven:dummy:name");
        assertEquals("Name not set correctly", "maven:dummy:name", 
            instance.getName());
    }
    
    /**
     * Test of setRevision method
     */
    public void testSetRevision()
    {
        instance.setRevision("maven:rev:test");
        assertEquals("Revision not set correctly", "maven:rev:test", 
            instance.getRevision());
    }
    
    /** 
     * Test of toString method
     */
    public void testToString()
    {
        String value = instance.toString();
        assertTrue("Name not found in string", 
            value.indexOf(instance.getName()) != -1);
        assertTrue("Revision not found in string",
            value.indexOf(instance.getRevision()) != -1);
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {    
}
