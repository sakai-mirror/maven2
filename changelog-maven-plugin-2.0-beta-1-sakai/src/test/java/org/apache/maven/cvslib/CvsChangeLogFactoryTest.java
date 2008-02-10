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

import junit.framework.TestCase;

/**
 * Unit Tests for {@link CvsChangeLogFactory}
 * @author dion
 * @version $Id$
 */
public class CvsChangeLogFactoryTest extends TestCase
{
    /** the instance being tested */
    private CvsChangeLogFactory instance;
    
    /**
     * Create a test with the given name
     * @param testName the name of the test
     */
    public CvsChangeLogFactoryTest(String testName)
    {
        super(testName);
    }
    
    /**
     * Initialize per test data
     * @throws Exception when there is an unexpected problem
     */
    public void setUp() throws Exception
    {
        instance = new CvsChangeLogFactory();
    }
    
    /** test the constructor */
    public void testConstructor()
    {
        assertNotNull("new instance wasn't created", instance);
    }
    
    /** test creating the generator */
    public void testCreateGenerator()
    {
        testConstructor();
        assertNotNull("Generator was not created", instance.createGenerator());
    }
    
    /** test creating the parser */
    public void testCreateParser()
    {
        testConstructor();
        assertNotNull("Parser was not created", instance.createParser());
    }

}
