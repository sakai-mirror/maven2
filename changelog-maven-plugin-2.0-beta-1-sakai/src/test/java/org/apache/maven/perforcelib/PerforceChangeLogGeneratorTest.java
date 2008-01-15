package org.apache.maven.perforcelib;
import org.apache.maven.util.RepositoryUtils;
import org.apache.tools.ant.types.Commandline;

import junit.framework.TestCase;
import org.apache.maven.util.EnhancedStringTokenizer;

/**
 * @author <a href="jim@crossleys.org">Jim Crossley</a>
 * @author <a href="bwalding@jakarta.org">Ben Walding</a>
 * @version $Id: 
 */
public class PerforceChangeLogGeneratorTest extends TestCase
{
    class Test
    {
        String conn;
        String args;
        Class throwable;

        public Test(String conn, String args, Class throwable)
        {
            this.conn = conn;
            this.args = args;
            this.throwable = throwable;
        }

    }

    Test[] tests =
        {
            new Test(null, "", NullPointerException.class),
            new Test("asd:asd", "", IllegalArgumentException.class),
            new Test("scm:cvs:pserver:anoncvs@cvs.apache.org:/home/cvspublic:jakarta-turbine-maven",
                     "",
                     IllegalArgumentException.class),
            new Test("scm:perforce:host:port://depot/projects/name/...",
                     "p4|-p|host:port|filelog|-tl|//depot/projects/name/...",
                     null),
            new Test("scm:perforce:port://depot/projects/name/...",
                     "p4|-p|port|filelog|-tl|//depot/projects/name/...",
                     null),
            new Test("scm:perforce://depot/projects/name/...",
                     "p4|filelog|-tl|//depot/projects/name/...",
                     null),
        };

    public void testParse() throws Throwable
    {
        for (int i = 0; i < tests.length; i++)
        {
            Test t = tests[i];
            testParse(t);
        }
    }

    public void testParse(Test test) throws Throwable
    {
        String[] expected = RepositoryUtils.tokenizerToArray(new EnhancedStringTokenizer(test.args, "|"));

        PerforceChangeLogGenerator eg = new PerforceChangeLogGenerator();
        try {
            eg.setConnection(test.conn);
            Commandline cl = eg.getScmLogCommand();
            String[] clArgs = cl.getCommandline();
            if (test.throwable == null)
            {
                assertEquals("clArgs.length", expected.length, clArgs.length);
                for (int i = 0; i < expected.length; i++)
                {
                    assertEquals("clArgs[" + i + "]", expected[i], clArgs[i]);
                }
            }
            else
            {
                fail("Failed to throw :" + test.throwable.getName());
            }

        }
        catch (Throwable t)
        {
            if (test.throwable != null && test.throwable.isAssignableFrom(t.getClass()))
            {
                //Success
            }
            else
            {
                throw new RuntimeException("Caught unexpected exception \"" + t.getLocalizedMessage() + "\" testing " + test.conn);
            }
        }
    }
}
