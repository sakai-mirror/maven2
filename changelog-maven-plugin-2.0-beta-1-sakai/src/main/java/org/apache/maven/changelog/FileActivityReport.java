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

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.codehaus.doxia.sink.Sink;

/**
 * @goal file-activity
 *
 * @description A Maven 2.0 file activity report plugin
 */
public class FileActivityReport extends ChangeLogReport
{
    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale)
    {
        return "Generate file activity report from SCM";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName(Locale locale)
    {
        return "file-activity";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return "file-activity";
    }
    
    /**
     * generates an empty report in case there are no sources to generate a report with
     *
     * @param bundle the resource bundle to retrieve report phrases from
     * @param sink the report formatting tool
     */
    protected void doGenerateEmptyReport( ResourceBundle bundle, Sink sink )
    {
        sink.head();
        sink.title();
        sink.text( bundle.getString( "report.file-activity.header" ) );
        sink.title_();
        sink.head_();

        sink.body();
        sink.section1();

        sink.sectionTitle1();
        sink.text( bundle.getString( "report.file-activity.mainTitle" ) );
        sink.sectionTitle1_();
        
        sink.paragraph();
        sink.text( "No sources found to create a report." );
        sink.paragraph_();
        
        sink.section1_();

        sink.body_();
        sink.flush();
        sink.close();
    }
    
    /**
     * method that generates the report for this mojo.
     *
     * @param changeSets changed sets to generate the report from
     * @param bundle the resource bundle to retrieve report phrases from
     * @param sink the report formatting tool
     */
    protected void doGenerateReport( Collection changeSets, ResourceBundle bundle, Sink sink )
    {
        sink.head();
        sink.title();
        sink.text( bundle.getString( "report.file-activity.header" ) );
        sink.title_();
        sink.head_();

        sink.body();
        sink.section1();
        sink.sectionTitle1();
        sink.text( bundle.getString( "report.file-activity.mainTitle" ) );
        sink.sectionTitle1_();
        
        for( Iterator i=changeSets.iterator(); i.hasNext(); )
        {
            ChangeLogSet set = (ChangeLogSet) i.next();
            
            doChangedSets( set, bundle, sink );
        }
        
        sink.section1_();
        sink.body_();
        
        sink.table_();
    }
    
    /**
     * generates a section of the report referring to a changeset
     *
     * @param set the current ChangeSet to generate this section of the report
     * @param bundle the resource bundle to retrieve report phrases from
     * @param sink the report formatting tool
     */
    private void doChangedSets( ChangeLogSet set, ResourceBundle bundle, Sink sink )
    {
        sink.section2();
        sink.sectionTitle2();
        if ( set.getStart() == null )
            sink.text( bundle.getString( "report.SetRangeUnknown" ) );
        else if ( set.getEnd() == null )
            sink.text( bundle.getString( "report.SetRangeSince" ) );
        else
        {
            sink.text( bundle.getString( "report.SetRangeBetween" ) );
            sink.text( " " + set.getStart() + " " + bundle.getString( "report.To" ) + " " + set.getEnd() );
        }
        sink.sectionTitle2_();
        
        doSummary( set, bundle, sink );
        
        sink.table();
        
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.file-activity.filename" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.file-activity.timesChanged" ) );
        sink.tableHeaderCell_();
        sink.tableRow_();
        
        doRows( set, sink );
        
        sink.table_();
        
        sink.section2_();
    }
    
    /**
     * generates the report summary section of the report
     *
     * @param set changed set to generate the report from
     * @param bundle the resource bundle to retrieve report phrases from
     * @param sink the report formatting tool
     */
    private void doSummary( ChangeLogSet set, ResourceBundle bundle, Sink sink )
    {
        sink.paragraph();
        
        sink.text( bundle.getString( "report.file-activity.range" ) );
        sink.text( ": " + set.getStart() + " " + bundle.getString( "report.To" ) + " " + set.getEnd() );

        sink.text( ", " + bundle.getString( "report.TotalCommits" ) );
        sink.text( ":" + set.getEntries().size() );
        
        sink.text( ", " + bundle.getString( "report.file-activity.filesChanged" ) );
        sink.text( ":" + countFilesChanged( set.getEntries() ) );
        
        sink.paragraph_();
    }
    
    /**
     * generates the row details for the file activity report
     *
     * @param set the changelog set to generate a report with
     * @param sink the report formatting tool
     */
    private void doRows( ChangeLogSet set, Sink sink )
    {
        List list = getOrderedFileList( set.getEntries() );
        
        initReportUrls();
        
        for( Iterator i=list.iterator(); i.hasNext(); )
        {
            List revision = (List) i.next();
            ChangeLogFile file = (ChangeLogFile) revision.get( 0 );
            
            sink.tableRow();
            sink.tableCell();
            
            try
            {
                generateLinks( getConnection(), file.getName(), sink );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                
                sink.text( file.getName() );
            }
            sink.tableCell_();
            
            sink.tableCell();
            sink.text( "" + revision.size() );
            
            sink.tableCell_();
            sink.tableRow_();
        }
    }
    
    /**
     * reads the change log entries and generates a list of files changed order by the number of times edited. This 
     *      used the FileActivityComparator Object to sort the list
     *
     * @return list of changed files within the SCM with the number of times changed in descending order 
     * @param entries the changelog entries to generate the report
     */
    private List getOrderedFileList( Collection entries )
    {
        List list = new LinkedList();
        
        Map map = new HashMap();
        
        for( Iterator i=entries.iterator(); i.hasNext(); )
        {
            ChangeLogEntry entry = (ChangeLogEntry) i.next();
            
            for( Enumeration e=entry.getFiles().elements(); e.hasMoreElements(); )
            {
                ChangeLogFile file = (ChangeLogFile) e.nextElement();
                
                List revisions;
                
                if ( map.containsKey( file.getName() ) )
                    revisions = (List) map.get( file.getName() );
                else
                    revisions = new LinkedList();
                
                revisions.add( file );
                
                map.put( file.getName(), revisions );
            }
        }
        
        list.addAll( map.values() );
        
        Collections.sort( list, new FileActivityComparator() );
        
        return list;
    }
}
