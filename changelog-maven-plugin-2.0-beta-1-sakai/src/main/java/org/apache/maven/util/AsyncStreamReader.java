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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * A class to asynchronously read the provided InputStream and
 * provide the output as a String
 *
 * @author <a href="mailto:dion@multitask.com.au">dIon Gillard</a>
 * @version $Id$
 */
public class AsyncStreamReader extends Thread
{
    /** buffer to dump output to */
    private StringBuffer streamBuffer = new StringBuffer();
    
    /** stream to read from */
    private BufferedReader stream;

    /**
     * Create a reader to process the provided stream
     * @param anInputStream any input stream
     */
    public AsyncStreamReader(InputStream anInputStream)
    {
        if (anInputStream == null)
        {
            throw new NullPointerException("input stream parameter is null");
        }
        stream = new BufferedReader(new InputStreamReader(anInputStream));
    }

    /**
     * Read lines from the input stream provided, appending to a buffer
     * for retrieval via the toString() method 
     */
    public void run()
    {
        String line;
        try
        {
            while ((line = stream.readLine()) != null)
            {
                if (okToConsume(line))
                {
                    streamBuffer.append(line);
                    streamBuffer.append('\n');
                }
            }
        }
        catch (IOException ioe) 
        {
            ioe.printStackTrace();
        }
    }

    /**
     * @return the contents of the buffer
     */
    public String toString()
    {
        return streamBuffer.toString();
    }

    /**
     * Indicate if its ok to consume (add to the buffer) this line.  This
     * implementation always returns <code>true</code>.
     *
     * @param line the line to check.
     * @return if its ok to add this line to the buffer.
     */
    protected boolean okToConsume(String line)
    {
        return true;
    }
    
} // end of AsyncStreamReader
