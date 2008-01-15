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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.util.RepositoryUtils;
import org.apache.tools.ant.types.Commandline;

import junit.framework.TestCase;
import org.apache.maven.util.EnhancedStringTokenizer;

/**
 * @author <a href="bwalding@jakarta.org">Ben Walding</a>
 * @version $Id$
 */
class ExposeGenerator extends CvsChangeLogGenerator
{

    protected Commandline getScmLogCommand()
    {
        return super.getScmLogCommand();
    }
}

/**
 * @author <a href="bwalding@jakarta.org">Ben Walding</a>
 * @version $Id$
 */
public class CvsChangeLogGeneratorTest extends TestCase
{
    class Test
    {
        String conn;
        String args;
        Class throwable;
        Map map;
  
        public Test(String params, String conn, String args, Class throwable)
        {
            this.conn = conn;
            this.args = args;
            this.throwable = throwable;
            this.map = null;
            if (params != null)
            {
                map = new HashMap();
                StringTokenizer tokens = new StringTokenizer(params, "|");
                while (tokens.hasMoreTokens())
                {
                    String name = tokens.nextToken();
                    assertTrue("params must have an even number of values.", tokens.hasMoreTokens());
                    String value = tokens.nextToken();
                    map.put(name, value);
                }
            }
        }

    }

    static SimpleDateFormat standardFormat = new SimpleDateFormat("yyyy-MM-dd");
    static String now = standardFormat.format(new Date(System.currentTimeMillis() + (long) 1 * 24 * 60 * 60 * 1000));
    static String range30 = standardFormat.format(new Date(System.currentTimeMillis() - (long) 30 * 24 * 60 * 60 * 1000));
    static String range10 = standardFormat.format(new Date(System.currentTimeMillis() - (long) 10 * 24 * 60 * 60 * 1000));
    
    Test[] tests =
        {
            new Test(null, null, "", NullPointerException.class),
            new Test(null, "asd:asd", "", IllegalArgumentException.class),
            new Test(null, null, "", NullPointerException.class),
            new Test(null, "asd:asd", "", IllegalArgumentException.class),
            new Test(
                null,
                "scm:csvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "",
                IllegalArgumentException.class),
            new Test(
                null,
                "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:/home/cvspublic|log",
                null),
            new Test(
                null,
                "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven:anoncvs",
                "",
                IllegalArgumentException.class),
            new Test(
                null,
                "scm|cvs|pserver|anoncvs@cvs.apache.org|D:\\home\\cvspublic|maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:D:\\home\\cvspublic|log",
                null),
            new Test(
                null,
                "scm|cvs|pserver|anoncvs@cvs.apache.org|D:/home/cvspublic|maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:D:/home/cvspublic|log",
                null),
            new Test(
                null,
                "scm:cvs:lserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "cvs|-d|anoncvs@cvs.apache.org:/home/cvspublic|log",
                null) ,
            new Test(
                null,
                "scm|cvs|local|local|D:/home/cvspublic|maven",
                "cvs|-d|D:/home/cvspublic|log",
                null),
            new Test(
                null,
                "scm:cvs:extssh:someuser@cvs.apache.org:/home/cvs:maven",
                "cvs|-d|:extssh:someuser@cvs.apache.org:/home/cvs|log",
                null),
            new Test(
                "type|range|range|30|start|30",
                "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:/home/cvspublic|log|-d " + range30 + "<" + now,
                null),
            new Test(
                "type|range|range|10|start|10",
                "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:/home/cvspublic|log|-d " + range10 + "<" + now,
                null),
            new Test(
                "type|date|start|2004-04-01",
                "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:/home/cvspublic|log|-d 2004-04-01<" + now,
                null),
            new Test(
                "type|date|start|1996-06-12",
                "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:/home/cvspublic|log|-d 1996-06-12<" + now,
                null),
            new Test(
                "type|date|start|1996-06-12|end|1998-05-13",
                "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:/home/cvspublic|log|-d 1996-06-12<1998-05-13",
                null),
            new Test(
                "type|tag|start|my_tag_name",
                "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:/home/cvspublic|log|-rmy_tag_name::",
                null),
            new Test(
                "type|tag|start|my_tag_name|end|end_tag_name",
                "scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven",
                "cvs|-d|:pserver:anoncvs@cvs.apache.org:/home/cvspublic|log|-rmy_tag_name::end_tag_name",
                null),
            };

    public void testParse() throws Throwable
    {
        for (int i = 0; i < tests.length; i++)
        {

            Test t = tests[i];
            testParse(t, i);
        }
    }

    public void testParse(Test test, int index) throws Throwable
    {
        String[] expected = RepositoryUtils.tokenizerToArray(new EnhancedStringTokenizer(test.args, "|"));

        ExposeGenerator eg = new ExposeGenerator();
        try
        {
            ChangeLog changelog = new ChangeLog();
            if (test.map != null)
            {
                changelog.setType((String)test.map.get("type"));
                changelog.setRange((String)test.map.get("range"));
                changelog.setMarkerStart((String)test.map.get("start"));
                changelog.setMarkerEnd((String)test.map.get("end"));
                changelog.setDateFormat((String)test.map.get("dateformat"));
            }
            else
            {
                changelog.setType("range");
            }
            changelog.setRepositoryConnection(test.conn);
            eg.init(changelog);
            Commandline cl = eg.getScmLogCommand();
            String[] clArgs = cl.getCommandline();
            if (test.throwable == null)
            {
                assertEquals("index " + index + ": clArgs.length", expected.length, clArgs.length);
                for (int i = 0; i < expected.length; i++)
                {
                    if ( clArgs[i].startsWith( "-d \"" ) )
                    {
                        clArgs[i] = "-d " + clArgs[i].substring( 4, clArgs[i].length() - 1 );
                    }
                    assertEquals("index " + index + ": clArgs[" + i + "]", expected[i], clArgs[i]);
                }
            }
            else
            {
                fail("index " + index + ": Failed to throw :" + test.throwable.getName());
            }

        }
        catch (Exception t)
        {
            if (test.throwable != null && test.throwable.isAssignableFrom(t.getClass()))
            {
                //Success
            }
            else
            {
                throw new RuntimeException("Caught unexpected exception \"" + t.getLocalizedMessage() + "\" testing " + test.conn + " (index " + index + ")", t);
            }
        }

    }

    

    
}
