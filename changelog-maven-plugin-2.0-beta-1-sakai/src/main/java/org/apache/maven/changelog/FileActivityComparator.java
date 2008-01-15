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

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Object used to sort the file-activity report into descending order
 *
 */
public class FileActivityComparator implements Comparator
{
    /**
     * @see java.util.Comparator#compare(Object,Object)
     */
    public int compare(Object o1, Object o2) throws ClassCastException
    {
        int returnValue = 0;
        
        LinkedList list1 = (LinkedList) o1;
        
        LinkedList list2 = (LinkedList) o2;
        
        returnValue = sortByCommits( list1, list2 );
        
        if ( returnValue != 0 ) return returnValue;
        
        returnValue = sortByRevision( list1, list2 );
        
        if ( returnValue != 0 ) return returnValue;
        
        returnValue = sortByName( list1, list2 );
        
        return returnValue;
    }
    
    /**
     * compares list1 and list2 by the number of commits
     *
     * @return an integer describing the order comparison of list1 and list2
     * @param list1 the first object in a compare statement
     * @param list2 the object to compare list1 against
     */
    private int sortByCommits( List list1, List list2 )
    {
        if ( list1.size() > list2.size() ) return -1;
        
        if ( list1.size() < list2.size() ) return 1;
        
        return 0;
    }
    
    /**
     * compares list1 and list2 by comparing their revision code
     *
     * @return an integer describing the order comparison of list1 and list2
     * @param list1 the first object in a compare statement
     * @param list2 the object to compare list1 against
     */
    private int sortByRevision( List list1, List list2 )
    {
        String revision1 = getLatestRevision( list1 );
        
        String revision2 = getLatestRevision( list2 );
        
        return revision1.compareTo( revision2 );
    }
    
    /**
     * retrieves the latest revision from the commits made from the SCM
     *
     * @return the latest revision code
     * @param the list of revisions from the file
     */
    private String getLatestRevision( List list )
    {
        String latest = "";
        
        for( Iterator i=list.iterator(); i.hasNext(); )
        {
            ChangeLogFile file = (ChangeLogFile) i.next();
            
            if ( latest.length() == 0 ) 
                latest = file.getRevision();
            else if ( latest.compareTo( file.getRevision() ) > 0 )
                latest = file.getRevision();
        }
        
        return latest;
    }
    
    /**
     * compares list1 and list2 by comparing their filenames. Least priority sorting when both number of commits and
     *      and revision are the same
     *
     * @return an integer describing the order comparison of list1 and list2
     * @param list1 the first object in a compare statement
     * @param list2 the object to compare list1 against
     */
    private int sortByName( List list1, List list2 )
    {
        ChangeLogFile file1 = (ChangeLogFile) list1.get( 0 );
        
        ChangeLogFile file2 = (ChangeLogFile) list2.get( 0 );
        
        return file1.getName().compareTo( file2.getName() );
    }
}