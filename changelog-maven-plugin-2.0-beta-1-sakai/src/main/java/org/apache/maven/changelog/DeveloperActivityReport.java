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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.maven.model.Developer;
import org.codehaus.doxia.sink.Sink;


/**
 * @goal dev-activity
 *
 * @description A Maven 2.0 developer activity report plugin
 */
public class DeveloperActivityReport extends ChangeLogReport
{
    /**
     * List of developers to be shown on the report.
     * @parameter expression="${project.developers}"
     */
    private List developers;
    
    /**
     * Used to hold data while creating the report
     */
    private HashMap commits;
    private HashMap files;
    
    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale)
    {
        return "Generated developer activity report from SCM";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName(Locale locale)
    {
        return "dev-activity";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return "dev-activity";
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
        sink.text( bundle.getString( "report.dev-activity.header" ) );
        sink.title_();
        sink.head_();

        sink.body();
        sink.section1();

        sink.sectionTitle1();
        sink.text( bundle.getString( "report.dev-activity.mainTitle" ) );
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
    protected void doGenerateReport(Collection changeSets, ResourceBundle bundle, Sink sink)
    {
        sink.head();
        sink.title();
        sink.text( bundle.getString( "report.dev-activity.header" ) );
        sink.title_();
        sink.head_();

        sink.body();
        sink.section1();
        sink.sectionTitle1();
        sink.text( bundle.getString( "report.dev-activity.mainTitle" ) );
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
            sink.text( " " + set.getStart() + " " + bundle.getString( "report.To" ) + " " + set.getEnd() );
            sink.sectionTitle2_();
        }        
        doSummary( set, bundle, sink );
        
        sink.table();
        
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.dev-activity.developer" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.TotalCommits" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.dev-activity.filesChanged" ) );
        sink.tableHeaderCell_();
        sink.tableRow_();
        
        doDeveloperRows( set, sink );
        
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
        
        sink.text( bundle.getString( "report.dev-activity.range" ) );
        sink.text( ": " + set.getStart() + " " + bundle.getString( "report.To" ) + " " + set.getEnd() );

        sink.text( ", " + bundle.getString( "report.TotalCommits" ) );
        sink.text( ":" + set.getEntries().size() );
        
        sink.text( ", " + bundle.getString( "report.dev-activity.filesChanged" ) );
        sink.text( ":" + countFilesChanged( set.getEntries() ) );
        
        sink.paragraph_();
    }
    
    /**
     * counts the total commits made to the given sets 
     *
     * @return total number of commits for the given sets
     * @param sets collection of sets to count all the commits
     */
    private long getCommits( Collection sets )
    {
        long commits = 0;
        
        for( Iterator i=sets.iterator(); i.hasNext();)
        {
            ChangeLogSet set = (ChangeLogSet) i.next();
            
            commits += set.getEntries().size();
        }
        
        return commits;
    }
    
    /**
     * counts the total number of files changed
     *
     * @return total number of files changed
     * @param sets collection of sets to count all the files changed
     */
    private long getFilesChanged( Collection sets )
    {
        long count = 0;
        
        for( Iterator i=sets.iterator(); i.hasNext();)
        {
            ChangeLogSet set = (ChangeLogSet) i.next();
            
            count += countFilesChanged( set.getEntries() );
        }
        
        return count;
    }
    
    /**
     * generates the report section table of the developers
     *
     * @param set change log set generate the developer activity
     * @param sink the report formatting tool
     */
    private void doDeveloperRows( ChangeLogSet set, Sink sink )
    {
        getDeveloperDetails( set );
        
        //for( Iterator i=commits.keySet().iterator(); i.hasNext(); )
        for( Iterator i=developers.iterator(); i.hasNext(); )
        {
            Developer developer = (Developer) i.next();
            
            String name = developer.getName();
            
            if ( !commits.containsKey( name ) ) continue;
            
            LinkedList devCommits = (LinkedList) commits.get( name );
            
            HashMap devFiles = (HashMap) files.get( name );
            
            sink.tableRow();

            sink.tableCell();
            sink.link( "team-list.html#" + developer.getId() );
            sink.text( name );
            sink.link_();
            sink.tableCell_();
            
            sink.tableCell();
            sink.text( "" + devCommits.size() );
            sink.tableCell_();
            
            sink.tableCell();
            sink.text( "" + devFiles.size() );
            sink.tableCell_();
            
            sink.tableRow_();
        }
    }
    
    /**
     * counts the number of commits and files changed for each developer
     *
     * @param set the change log set to generate the developer details from
     */
    private void getDeveloperDetails( ChangeLogSet set )
    {
        commits = new HashMap();

        files = new HashMap();

        countDevCommits( set.getEntries() );
        
        countDevFiles( set.getEntries() );
    }
    
    /**
     * counts the number of commits of each developer
     *
     * @param entries the change log entries used to search and count developer commits
     */
    private void countDevCommits( Collection entries )
    {
        for( Iterator i2=entries.iterator(); i2.hasNext(); )
        {
            ChangeLogEntry entry = (ChangeLogEntry) i2.next();

            String developer = entry.getAuthor();
            
            LinkedList list;

            if ( commits.containsKey( developer ) )
            {
                list = (LinkedList) commits.get( developer );
            }
            else
            {
                list = new LinkedList();
            }

            list.add( entry );

            commits.put( developer, list );
        }
    }
    
    /**
     * counts the number of files changed by each developer
     *
     * @param entries the change log entries used to search and count file changes
     */
    private void countDevFiles( Collection entries )
    {
        for( Iterator i2=entries.iterator(); i2.hasNext(); )
        {
            ChangeLogEntry entry = (ChangeLogEntry) i2.next();

            String developer = entry.getAuthor();

            HashMap filesMap;

            if (files.containsKey( developer ) )
            {
                filesMap = (HashMap) files.get( developer );
            }
            else
            {
                filesMap = new HashMap();
            }

            for( Iterator i3=entry.getFiles().iterator(); i3.hasNext(); )
            {
                ChangeLogFile file = (ChangeLogFile) i3.next();

                filesMap.put( file.getName(), file );
            }

            files.put( developer, filesMap );
        }
    }
}
