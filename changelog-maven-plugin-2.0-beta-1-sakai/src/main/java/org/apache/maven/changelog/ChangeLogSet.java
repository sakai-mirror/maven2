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

import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;


/**
 * Change Log Set - holds details about a set of change log entries.
 *
 * @author <a href="mailto:david.jackman@fastsearch.com">David Jackman</a>
 * @version $$
 */
public class ChangeLogSet
{
    private final Collection entries;
    private final String start;
    private final String end;
    
    
    /**
     * Initializes a new instance of this class.
     * 
     * @param entries  collection of {@link ChangeLogEntry} objects for this set.
     * @param start  the start date/tag for this set.
     * @param end  the end date/tag for this set, or <code>null</code> if this set goes to the present time.
     */
    public ChangeLogSet(Collection entries, String start, String end)
    {
        this.entries = entries;
        this.start = start;
        this.end = end;
    }


    /**
     * Returns the collection of entries for this set.
     * 
     * @return  the collection of {@link ChangeLogEntry} objects for this set.
     */
    public Collection getEntries()
    {
        return entries;
    }
    
    
    /**
     * Returns the start date/tag for this set.
     * 
     * @return  the start date/tag for this set.
     */
    public String getStart()
    {
        return start;
    }
    
    
    /**
     * Returns the end date/tag for this set.
     * 
     * @return  the end date/tag for this set, or <code>null</code> if this set goes to the present time.
     */
    public String getEnd()
    {
        return end;
    }
    
    
    /**
     * Creates an XML representation of this change log set.
     */
    public String toXML()
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("<changeset start=\"")
            .append(start)
            .append("\" end=\"")
            .append(end)
            .append("\">\n");
        
        //  Write out the entries
        for (Iterator i = getEntries().iterator(); i.hasNext();)
        {
            buffer.append(((ChangeLogEntry) i.next()).toXML());
        }
        
        buffer.append("</changeset>\n");

        return buffer.toString();
    }
}
