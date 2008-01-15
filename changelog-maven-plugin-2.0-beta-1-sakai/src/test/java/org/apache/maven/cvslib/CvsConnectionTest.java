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

import java.io.BufferedReader;
import java.io.StringReader;

import junit.framework.TestCase;


/**
 * Test cases for {@link CvsConnection}
 * @author 
 * @version $Id$
 */
public class CvsConnectionTest extends TestCase
{

    /** the {@link CvsConnection} used for testing */
    private CvsConnection instance;
    /** test data */
    private String testData = ":pserver:user@server:/home/cvs";
    private String[] testTrueData = {
    		":pserver:user@server:/home/cvs",
			":pserver:user@server:2401/home/cvs"
    };
    private String[] testFalseData = {
    		":ext:user@server:/home/cvs",
			":pserver:admin@server:/home/cvs",
			":pserver:user@host:/home/cvs",
			":pserver:user@server:/home/cvsroot",
    		":ext:user@server:2401/home/cvs",
			":pserver:admin@server:2401/home/cvs",
			":pserver:user@host:2401/home/cvs",
			":pserver:user@server:2401/home/cvsroot"			
    };

    /**
     * Create a test with the given name
     * @param testName the name of the test
     */
    public CvsConnectionTest(String testName)
    {
        super(testName);
    }

    /**
     * Test of compareCvsRoot method
     * @throws Exception when there is an unexpected problem
     */
    public void testCompareTrue() throws Exception
    {
    	for (int i=0; i < testTrueData.length; i++) {
    		assertTrue(CvsConnection.compareCvsRoot(testData, testTrueData[i]));
    	}

    }

    /**
     * Test of compareCvsRoot method
     * @throws Exception when there is an unexpected problem
     */
    public void testCompareFalse() throws Exception
    {
    	for (int i=0; i < testFalseData.length; i++) {
    		assertFalse(CvsConnection.compareCvsRoot(testData, testFalseData[i]));
    	}

    }

    /**
     * Test of reading in .cvspass file processes different types of lines properly
     * @throws Exception when there is an unexpected problem
     */
    public void testProcessCvspass() throws Exception 
	{
        String[] expectedResult = {
        		"A ",
			null,
			"Axxx ",
			"Axxx xxx ",
    			"A ",
			null,
			"Axxx ",
			"Axxx xxx "			
        };
        String[] cvspassData = {
        		":pserver:user@server:/home/cvs A ",
				":ext:user@server:/home/cvs A ",
				":pserver:user@server:/home/cvs Axxx ",
				":pserver:user@server:/home/cvs Axxx xxx ",
        		"/1 :pserver:user@server:2401/home/cvs A ",
				"/1 :ext:user@server:2401/home/cvs A ",
				"/1 :pserver:user@server:2401/home/cvs Axxx ",
				"/1 :pserver:user@server:2401/home/cvs Axxx xxx ",				
        };
    		
    		for (int i = 4;i<expectedResult.length;i++){
    			BufferedReader reader = new BufferedReader(new StringReader(cvspassData[i]));

    			String password = CvsConnection.processCvspass(testData,reader);
    			assertEquals(expectedResult[i],password);
    		}
	}


}
