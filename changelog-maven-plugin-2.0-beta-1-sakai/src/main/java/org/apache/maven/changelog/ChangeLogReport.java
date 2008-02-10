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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.doxia.site.renderer.SiteRenderer;


/**
 * @goal changelog
 *
 * @description A Maven 2.0 Changelog report plugin
 */
public class ChangeLogReport extends AbstractMavenReport
{
    /**
     * Used to specify whether to build the log from a range, absolute date, or tag.
     * 
     * @parameter default-value="range"
     * @required
     */
    private String type;
    
    /**
     * Used to specify the number of days (or list of days) of log entries to retrieve.
     *
     * @parameter 
     */
    private List ranges;
    
    /** 
     * Used to specify the absolute date (or list of dates) to start log entries from.
     *
     * @parameter
     */
    private List dates;
    
    /** 
     * Used to specify the tag (or list of tags) to start log entries from.
     *
     * @parameter
     */
    private List tags;
    
    /**
     * Used to specify the date format of log entries to retrieve.
     *
     * @parameter default-value="yyyy-MM-dd"
     */
    private String dateFormat;

    /**
     * Input dir.  Directory where the sources are located.
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private File basedir;

    /**
     * Output file for xml document
     *
     * @parameter expression="${project.build.directory}/changelog.xml"
     * @required
     */
    private File outputXML;
    
    /**
     * Comment format string used for interrogating
     * the revision control system.
     * Currently only used by the ClearcaseChangeLogGenerator.
     */
    private String commentFormat;
    
    /** 
     * Output encoding for the xml document
     *
     * @parameter default-value="ISO-8859-1"
     * @required
     */
    private String outputEncoding;
    
    /**
     * The URL to view the scm. Basis for external links from the generated report.
     *
     * @parameter expression="${project.scm.url}"
     *
     */
    private String scmUrl;

    /** 
     * The Maven Project Object
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * The directory where the report will be generated
     *
     * @parameter expression="${project.build.directory}/site"
     * @required
     * @readonly
     */
    private File outputDirectory;
    
    /**
     * @parameter expression="${component.org.codehaus.doxia.site.renderer.SiteRenderer}"
     * @required
     * @readonly
     */
    private SiteRenderer siteRenderer;
    
    // temporary field holder while generating the report
    private String rpt_Repository, rpt_OneRepoParam, rpt_MultiRepoParam;
    
    // field for SCM Connection URL
    private String connection;

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    public void executeReport(Locale locale)
        throws MavenReportException
    {
        //check if sources exists <-- required for parent poms
        if ( !basedir.exists() )
        {
            doGenerateEmptyReport( getBundle( locale ), getSink() );
            
            return;
        }
        
        verifySCMTypeParams();

        Collection changedSets;
        
        changedSets = getChangedSets();
        
        doGenerateReport( changedSets, getBundle( locale ), getSink() );
    }
    
    /**
     * populates the changedSets field by either connecting to the SCM or using an existing XML generated in a previous
     *      run of the report
     *
     * @throws MavenReportException 
     */
    protected Collection getChangedSets() throws MavenReportException
    {
        Collection changedSets;
        
        try
        {
            FileInputStream fIn = new FileInputStream( outputXML );
            
            changedSets = ChangeLog.loadChangedSets( fIn, dateFormat );
        }
        catch ( FileNotFoundException fnfe )
        {
            getLog().info( "Generating changed sets xml to: " + outputXML.getAbsolutePath() );
            
            changedSets = generateChangeSetsFromSCM();
        }
        catch ( Exception e )
        {
            throw new MavenReportException( "An error occurred while parsing " + outputXML.getAbsolutePath(), e );
        }
        
        return changedSets;
    }
    
    /**
     * creates a ChangeLog object and then connects to the SCM to generate the changed sets
     *
     * @return collection of changedsets generated from the SCM
     * @throws MavenReportException
     */
    protected Collection generateChangeSetsFromSCM() throws MavenReportException
    {
        try
        {
            ChangeLog changeLog = getChangeLog();
            
            return changeLog.getChangeSets();
        }
        catch ( IOException ioe )
        {
            throw new MavenReportException( "An error occurred while generating " + outputXML.getAbsolutePath(), ioe );
        }
    }
    
    /**
     * Initializes the changelog object to generate the changedsets and the equivalent XML file which can be used for
     *      as cache on succeeding runs
     *
     * @throws IOException when an error occurred while executing the ChangeLog object
     * @throws MavenReportException when an error occurred while retrieving the SCM connection
     */
    protected ChangeLog getChangeLog() throws MavenReportException, IOException
    {
        ChangeLog changeLog = new ChangeLog();
        
        changeLog.setLog( getLog() );
        
        changeLog.setBasedir( basedir );

        changeLog.setDevelopers( getProject().getDevelopers() );

        changeLog.setOutput( outputXML );

        changeLog.setOutputEncoding( outputEncoding );

        changeLog.setType( type );

        changeLog.setRange( getDelimitedString(ranges) );

        changeLog.setDate( getDelimitedString(dates) );
        
        changeLog.setTag( getDelimitedString(tags) );

        changeLog.setRepositoryConnection( getConnection() );

        changeLog.setDateFormat( dateFormat );

        changeLog.setCommentFormat( commentFormat );
        
        changeLog.doExecute();
        
        return changeLog;
    }
    
    private String getDelimitedString( List list )
    {
        if ( list == null ) return null;
        
        if ( list.size() == 0 ) return "";
        
        String retValue = "";
        
        for( Iterator i=list.iterator(); i.hasNext(); )
        {
            retValue += "," + (String) i.next();
        }
        
        return retValue.substring( 1 );
    }
    
    /**
     * used to retrieve the SCM connection string
     *
     * @return the url string used to connect to the SCM
     * @throws MavenReportException when there is insufficient information to retrieve the SCM connection string
     */
    protected String getConnection() throws MavenReportException
    {
        if ( this.connection != null ) return connection;
        
        if ( project.getScm() == null ) throw new MavenReportException( "SCM Connection is not set." );
        
        this.connection = project.getScm().getConnection();
        
        if ( this.connection != null )
            if ( this.connection.length() > 0 ) return connection;
        
        this.connection = project.getScm().getDeveloperConnection();
        
        if ( this.connection == null ) throw new MavenReportException( "SCM Connection is not set." );
        
        if ( this.connection.length() == 0 ) throw new MavenReportException( "SCM Connection is not set." );
        
        return this.connection;
    }
    
    /**
     * checks whether there are enough configuration parameters to generate the report
     *
     * @throws MavenReportException when there is insufficient paramters to generate the report
     */
    private void verifySCMTypeParams() throws MavenReportException
    {
        if ( "range".equals( type ) ) 
        {
            if ( ranges == null ) ranges = Collections.singletonList( "30" );
        }
        else if ( "date".equals( type ) )
        {
            if ( dates == null ) throw new MavenReportException( "The date parameter is required when type=\"date\". The value should be the absolute date for the start of the log." );
        }
        else if ( "tag".equals( type ) )
        {
            if ( tags == null ) throw new MavenReportException( "The tag parameter is required when type=\"tag\".  The value should be the value of the tag for the start of the log." );
        }
        else
        {
            throw new MavenReportException( "The type parameter has an invalid value: " + type + ".  The value should be \"range\", \"date\", or \"tag\"." );
        }
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
        sink.text( bundle.getString( "report.changelog.header" ) );
        sink.title_();
        sink.head_();

        sink.body();
        sink.section1();

        sink.sectionTitle1();
        sink.text( bundle.getString( "report.changelog.mainTitle" ) );
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
     * method that generates the report for this mojo. This method is overridden by dev-activity and file-activity mojo
     *
     * @param changeSets changed sets to generate the report from
     * @param bundle the resource bundle to retrieve report phrases from
     * @param sink the report formatting tool
     */
    protected void doGenerateReport(Collection changeSets, ResourceBundle bundle, Sink sink)
    {
        sink.head();
        sink.title();
        sink.text( bundle.getString( "report.changelog.header" ) );
        sink.title_();
        sink.head_();

        sink.body();
        sink.section1();

        sink.sectionTitle1();
        sink.text( bundle.getString( "report.changelog.mainTitle" ) );
        sink.sectionTitle1_();
        
        // Summary section
        doSummarySection( changeSets, bundle, sink );
        sink.section1_();

        //Iterate each set
        for ( Iterator i=changeSets.iterator(); i.hasNext(); )
        {
            ChangeLogSet set = (ChangeLogSet) i.next();
            
            doChangedSet( set, bundle, sink );
        }
        
        sink.body_();
        sink.flush();
        sink.close();
    }
    
    /**
     * generates the report summary section of the report
     *
     * @param changeSets changed sets to generate the report from
     * @param bundle the resource bundle to retrieve report phrases from
     * @param sink the report formatting tool
     */
    private void doSummarySection( Collection changeSets, ResourceBundle bundle, Sink sink )
    {
        sink.paragraph();
        
        sink.text( bundle.getString( "report.changelog.ChangedSetsTotal" ) );
        sink.text( ": " + changeSets.size() );
        
        sink.paragraph_();
    }
    
    /**
     * generates a section of the report referring to a changeset
     *
     * @param set the current ChangeSet to generate this section of the report
     * @param bundle the resource bundle to retrieve report phrases from
     * @param sink the report formatting tool
     */
    private void doChangedSet( ChangeLogSet set, ResourceBundle bundle, Sink sink )
    {
        sink.section1();
        
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
        
        sink.paragraph();
        sink.text( bundle.getString( "report.TotalCommits" ) );
        sink.text( ": " + set.getEntries().size() );
        sink.lineBreak();
        sink.text( bundle.getString( "report.changelog.FilesChanged" ) );
        sink.text( ": " + countFilesChanged( set.getEntries() ) );
        sink.paragraph_();
        
        doChangedSetTable( set.getEntries(), bundle, sink );
        
        sink.section1_();
    }
    
    /**
     * counts the number of files that were changed in the specified SCM
     *
     * @return number of files changed for the changedsets
     * @param entries a collection of SCM changes
     */
    protected long countFilesChanged( Collection entries )
    {
        if ( entries == null ) return 0;
        
        if ( entries.isEmpty() ) return 0;
        
        HashMap fileList = new HashMap();
        
        for( Iterator i=entries.iterator(); i.hasNext(); )
        {
            ChangeLogEntry entry = (ChangeLogEntry) i.next();
            
            Vector files = entry.getFiles();
            
            for( Enumeration e=files.elements(); e.hasMoreElements(); )
            {
                ChangeLogFile file = (ChangeLogFile) e.nextElement();
                
                String name = file.getName();
                
                if ( fileList.containsKey( name ) )
                {
                    LinkedList list = (LinkedList) fileList.get( name );
                    
                    list.add( file );
                }
                else 
                {
                    LinkedList list = new LinkedList();
                    
                    list.add( file );
                    
                    fileList.put( name, list );
                }
            }
        }
        
        return fileList.size();
    }

    /**
     * generates the report table showing the SCM log entries
     *
     * @param entries a list of change log entries to generate the report from
     * @param bundle the resource bundle to retrieve report phrases from
     * @param sink the report formatting tool
     */
    private void doChangedSetTable( Collection entries, ResourceBundle bundle, Sink sink )
    {
        sink.table();
        
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.changelog.timestamp" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.changelog.author" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.changelog.details" ) );
        sink.tableHeaderCell_();
        sink.tableRow_();
        
        for( Iterator i=entries.iterator(); i.hasNext(); )
        {
            ChangeLogEntry entry = (ChangeLogEntry) i.next();
            
            doChangedSetDetail( entry, bundle, sink );
        }
        
        sink.table_();
    }
    
    /**
     * reports on the details of an SCM entry log
     *
     * @param entry an SCM entry to generate the report from
     * @param bundle the resource bundle to retrieve report phrases from
     * @param sink the report formatting tool
     */
    private void doChangedSetDetail( ChangeLogEntry entry, ResourceBundle bundle, Sink sink )
    {
        sink.tableRow();
        
        sink.tableCell();
        sink.text( entry.getDateFormatted() + " " + entry.getTimeFormatted() );
        sink.tableCell_();
        
        sink.tableCell();
        sink.text( entry.getAuthor() );
        sink.tableCell_();
        
        sink.tableCell();
        initReportUrls();
        //doRevision( entry.getFiles(), bundle, sink );
        doChangedFiles( entry.getFiles(), sink );
        sink.lineBreak();
        sink.text( entry.getComment() );
        sink.tableCell_();
        
        sink.tableRow_();
    }
    
    /**
     * populates the report url used to create links from certain elements of the report
     */
    protected void initReportUrls()
    {
        if ( scmUrl != null )
        {
            int idx = scmUrl.indexOf( '?' );
        
            if ( idx > 0 )
            {
                rpt_Repository = scmUrl.substring( 0, idx );
                
                String rpt_TmpMultiRepoParam = scmUrl.substring( rpt_Repository.length() );
                
                rpt_OneRepoParam = "?" + rpt_TmpMultiRepoParam.substring( 1 );
                
                rpt_MultiRepoParam = "&" + rpt_TmpMultiRepoParam.substring( 1 );
            }
            else
            {
                rpt_Repository = scmUrl;
                
                rpt_OneRepoParam = "";
                
                rpt_MultiRepoParam = "";
            }
        }
    }
    
    /**
     * generates the section of the report listing all the files revisions
     *
     * @param files list of files to generate the reports from
     */
    private void doChangedFiles( Vector files, Sink sink )
    {
        for( Enumeration e=files.elements(); e.hasMoreElements(); )
        {
            ChangeLogFile file = (ChangeLogFile) e.nextElement();
            sinkLogFile( sink, file.getName(), file.getRevision() );
        }
    }
    
    /**
     * generates the section of the report detailing the revisions made and the files changed
     *
     * @param sink the report formatting tool
     * @param name filename of the changed file
     * @param revision the revision code for this file
     */
    private void sinkLogFile( Sink sink, String name, String revision )
    {
        sink.paragraph();
        
        try
        {
            String connection = getConnection();

            generateLinks( connection, name, revision, sink );
        }
        catch ( Exception e )
        {
            getLog().debug( e );
            
            sink.text( name + " v " + revision );
        }
        
        sink.paragraph_();
    }
    
    /**
     * attaches the http links from the changed files
     *
     * @param connection the string used to connect to the SCM
     * @param name filename of the file that was changed
     * @param sink the report formatting tool
     */
    protected void generateLinks( String connection, String name, Sink sink )
    {
        generateLinks( connection, name, null, sink );
    }
    
    /**
     * attaches the http links from the changed files
     *
     * @param connection the string used to connect to the SCM
     * @param name filename of the file that was changed
     * @param revision the revision code
     * @param sink the report formatting tool
     */
    protected void generateLinks( String connection, String name, String revision, Sink sink )
    {
        String linkFile = null;
        String linkRev  = null;
        
        if ( rpt_Repository != null )
        {
            if ( connection.startsWith( "scm:perforce" ) )
            {
                String path = getAbsolutePath( rpt_Repository, name );
                linkFile = path + "?ac=22";
                if ( revision != null) linkRev  = path + "?ac=64&rev=" + revision;
            }
            else if ( connection.startsWith( "scm:clearcase" ) )
            {
                String path = getAbsolutePath( rpt_Repository, name );
                linkFile = path + rpt_OneRepoParam;
            }
            else if ( connection.indexOf( "cvsmonitor.pl" ) > 0 )
            {
                String module = rpt_OneRepoParam.replaceAll("^.*(&amp;module=.*?(?:&amp;|$)).*$", "$1" );
                linkFile = rpt_Repository + "?cmd=viewBrowseFile" + module + "&file=" + name;
                if ( revision != null) linkRev  = rpt_Repository + "?cmd=viewBrowseVersion" + module + "&file=" + name + "&version=" + revision;
            }
            else
            {
                String path = getAbsolutePath( rpt_Repository, name );
                linkFile = path + rpt_OneRepoParam;
                if ( revision != null) linkRev  = path + "?rev=" + revision + 
                                            "&content-type=text/vnd.viewcvs-markup" + rpt_MultiRepoParam;
            }
        }
        
        if ( linkFile != null )
        {
            sink.link( linkFile );
            sink.text( name );
            sink.link_();
        }
        else
            sink.text( name );
        
        sink.text( " " );

        if ( linkRev != null )
        {
            sink.link( linkRev );
            sink.text( "v " + revision );
            sink.link_();
        }
        else if ( revision != null )
            sink.text( "v " + revision );
    }
    
    /**
     * calculates the path from a base directory to a target file
     *
     * @param base base directory to create the absolute path from
     * @param target target file to create the absolute path to
     */
    private String getAbsolutePath( final String base, final String target )
    {
        String absPath = "";
        
        StringTokenizer baseTokens = new StringTokenizer( base.replaceAll("\\\\", "/"), "/", true );
        
        StringTokenizer targetTokens = new StringTokenizer( target.replaceAll("\\\\", "/"), "/" );
        
        String targetRoot = targetTokens.nextToken();
        
        while ( baseTokens.hasMoreTokens() )
        {
            String baseToken = baseTokens.nextToken();
            
            if ( baseToken.equals( targetRoot ) ) break;
            
            absPath += baseToken;
        }
        
        return absPath + target.substring( 1 );
    }
    
    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    protected SiteRenderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale)
    {
        return "Generated Changelog report from SCM";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName(Locale locale)
    {
        return "changelog";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return "changelog";
    }
    
    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    protected ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "scm-activity", locale, this.getClass().getClassLoader() );
    }
}
