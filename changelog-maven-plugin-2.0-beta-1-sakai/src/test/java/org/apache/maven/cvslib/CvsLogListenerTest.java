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

import org.netbeans.lib.cvsclient.event.MessageEvent;


/**
 * Test cases for {@link CvsLogListener}
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @version $Id$
 */
public class CvsLogListenerTest extends TestCase
{


    /**
     * Create a test with the given name
     * @param testName the name of the test
     */
    public CvsLogListenerTest(String testName)
    {
        super(testName);
    }

    /**
     * Test of listening to a regular output
     * @throws Exception when there is an unexpected problem
     */
    public void testNormalEvent() throws Exception
    {
        String MESSAGE = "I am a message";
        CvsLogListener listener = new CvsLogListener();
		MessageEvent me = new MessageEvent("souce",MESSAGE,false);
        listener.messageSent(me);
        assertTrue(listener.getStdout().toString().indexOf(MESSAGE)>-1);

    }
    
	/**
	 * Test of listening to an error
	 * @throws Exception when there is an unexpected problem
	 */
	public void testErrorEvent() throws Exception
	{
		String MESSAGE = "I am a message";
		CvsLogListener listener = new CvsLogListener();
		MessageEvent me = new MessageEvent("souce",MESSAGE,true);
		listener.messageSent(me);
		assertTrue(listener.getStdout().toString().indexOf(MESSAGE)==-1);

	}    

   

}
