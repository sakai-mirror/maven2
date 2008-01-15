package org.apache.maven.util;

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

public class RepositoryTest
    extends TestCase
{
    public void testSplitScmConnectionCvsPserver()
    {
        String con = "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:module";
        String[] tokens = RepositoryUtils.splitSCMConnection(con);
        assertEquals("Wrong number of tokens split", 6, tokens.length);
    }

    public void testSplitScmConnectionCvsLocal5Tokens()
    {
        String con = "scm:cvs:local:/cvs/root:module";
        String[] tokens = RepositoryUtils.splitSCMConnection(con);
        assertEquals("Wrong number of tokens split", 6, tokens.length);
    }

    public void testSplitScmConnectionCvsLocal6Tokens3rdEmpty()
    {
        String con = "scm:cvs:local::/cvs/root:module";
        String[] tokens = RepositoryUtils.splitSCMConnection(con);
        assertEquals("Wrong number of tokens split", 6, tokens.length);
    }

    public void testSplitScmConnectionCvsLocal6Tokens3rdLocal()
    {
        String con = "scm:cvs:local:local:/cvs/root:module";
        String[] tokens = RepositoryUtils.splitSCMConnection(con);
        assertEquals("Wrong number of tokens split", 6, tokens.length);
    }

    public void testSplitScmConnectionCvsLocal4Tokens()
    {
        String con = "scm:cvs:local:/cvs/root";
        try
        {
            String[] tokens = RepositoryUtils.splitSCMConnection(con);
            fail("Should throw an exception splitting " + con);
        }
        catch ( IllegalArgumentException expected )
        {
            assertTrue( true );
        }
    }

    public void testSplitScmConnectionCvsPserver5Tokens()
    {
        String con = "scm:cvs:pserver:user@host:/cvs/root";
        try
        {
            String[] tokens = RepositoryUtils.splitSCMConnection(con);
            fail("Should throw an exception splitting " + con);
        }
        catch ( IllegalArgumentException expected )
        {
            assertTrue( true );
        }
    }

    public void testSplitScmConnectionCvsLocal6TokensNonEmpty3rd()
    {
        String con = "scm:cvs:local:foo:/cvs/root:module";
        try
        {
            String[] tokens = RepositoryUtils.splitSCMConnection(con);
            fail("Should throw an exception splitting " + con);
        }
        catch ( IllegalArgumentException expected )
        {
            assertTrue( true );
        }
    }

    public void testSplitScmConnectionSvn()
    {
        String con = "scm|svn|http://svn.apache.org/repos";
        String[] tokens = RepositoryUtils.splitSCMConnection(con);
        assertEquals("Wrong number of tokens split", 3, tokens.length);
    }
}
