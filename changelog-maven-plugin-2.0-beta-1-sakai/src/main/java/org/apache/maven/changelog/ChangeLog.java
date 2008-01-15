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

// java imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.maven.model.Developer;
import org.apache.maven.plugin.logging.Log;
import org.xml.sax.SAXException;

/**
 * Change log task. It uses a ChangeLogGenerator and ChangeLogParser to create
 * a Collection of ChangeLogEntry objects, which are used to produce an XML
 * output that represents the list of changes.
 *
 *
 * @author <a href="mailto:glenn@somanetworks.com">Glenn McAllister</a>
 * @author <a href="mailto:jeff.martin@synamic.co.uk">Jeff Martin</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:dion@multitask.com.au">dIon Gillard</a>
 * @author <a href="mailto:bodewig@apache.org">Stefan Bodewig</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Id$
 */
public class ChangeLog
{
    /** Used to specify whether to build the log from a range, absolute date, or tag. */
    private String type;
    
    /**
     * Used to specify the range of log entries to retrieve.
     */
    private String range;
    
    /** Used to specify the absolute date (or list of dates) to start log entries from. */
    private String date;
    
    /** Used to specify the tag (or list of tags) to start log entries from. */
    private String tag;
    
    /** This will contain the date/tag for the start of the current change set. */
    private String markerStart;
    
    /** This will contain the date/tag for the end of the current change set. */
    private String markerEnd;
    
    /**
     * Used to specify the date format of log entries to retrieve.
     */
    private String dateFormat;
    
    /**
     * Input dir. Working directory for running CVS executable
     */
    private File base;
    
    /**
     * The classname of our ChangeLogFactory, defaulting to Maven's built in
     * CVS factory.
     */
    private String clFactoryClass = null;
    
    private static final Map FACTORIES = new HashMap();
    
    /** the connection string used to access the SCM */
    private String connection;
    
    /** the list of developers on the project */
    private List developers;
    
    /** change log sets parsed (sets of entries) */
    private Collection sets;
    
    /** LOG */
    private static Log LOG = ChangeLog.getLog();
    
    /**
     * Output file for xml document
     */
    private File output;
    
    /** output encoding for the xml document */
    private String outputEncoding;
    
    /** initializes available factories for changelog */
    static
    {
        FACTORIES.put( "cvs", "org.apache.maven.cvslib.CvsChangeLogFactory" );
        FACTORIES.put( "svn", "org.apache.maven.svnlib.SvnChangeLogFactory" );
        FACTORIES.put( "clearcase", "org.apache.maven.clearcaselib.ClearcaseChangeLogFactory" );
        FACTORIES.put( "perforce", "org.apache.maven.perforcelib.PerforceChangeLogFactory" );
        FACTORIES.put( "starteam", "org.apache.maven.starteamlib.StarteamChangeLogFactory" );
        FACTORIES.put( "vss", "org.apache.maven.vsslib.VssChangeLogFactory" );
    }
    
    /**
     * Comment format string used for interrogating
     * the revision control system.
     * Currently only used by the ClearcaseChangeLogGenerator.
     */
    private String commentFormat;
    
    /**
     * Set the ChangeLogFactory class name.  If this isn't set, the factory
     * defaults to Maven's build in CVS factory.
     *
     * @param factoryClassName the fully qualified factory class name
     */
    public void setFactory(String factoryClassName)
    {
        clFactoryClass = factoryClassName;
    }
    
    /**
     * Set the type of log to generate (range, date, or tag).
     *
     * @param type  one of "range", "date", or "tag".
     */
    public void setType(String type)
    {
        this.type = type;
    }
    
    /**
     * Get the type of log to generate (range, date, or tag).
     *
     * @return  the basis for the log.
     */
    public String getType()
    {
        if ( type == null )
        {
            type = "range";
        }
        return type;
    }
    
    
    /**
     * Set the range of log entries to process; the interpretation of this
     * parameter depends on the generator.
     * This is only used if the type is "range".
     *
     * @param range the range of log entries.
     */
    public void setRange(String range)
    {
        this.range = range;
    }
    
    /**
     * Get the range of log entries to process; the interpretation of the range
     * depends on the generator implementation.
     *
     * @return the range of log entries.
     */
    public String getRange()
    {
        return range;
    }
    
    
    /**
     * Set the date to start the log from.
     * This is only used if the type is "date".
     * The format is that given by the dateFormat property, if present. Otherwise, the format is "yyyy-MM-dd".
     *
     * @param date  the date to use.
     */
    public void setDate(String date)
    {
        this.date = date;
    }
    
    /**
     * Get the date to start the log from.
     *
     * @return  the start date.
     */
    public String getDate()
    {
        return date;
    }
    
    
    /**
     * Set the tag to start the log from.
     * This is only used if the type is "tag".
     *
     * @param tag  the tag to use.
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    
    /**
     * Get the tag to start the log from.
     *
     * @return  the tag.
     */
    public String getTag()
    {
        return tag;
    }
    
    
    /**
     * Sets the marker (date or tag) for the start of the current change set.
     * (This is only set internally, but also by test code.)
     *
     * @param marker  start marker to use.
     */
    public void setMarkerStart(String marker)
    {
        markerStart = marker;
    }
    
    /**
     * Returns the marker (date or tag) for the start of the current change set.
     * Whether it's a date or tag depends on {@link #getType}.
     *
     * @return  the marker (date or tag) for the start of the current change set.
     */
    public String getMarkerStart()
    {
        return markerStart;
    }
    
    /**
     * Sets the marker (date or tag) for the end of the current change set.
     * (This is only set internally, but also by test code.)
     *
     * @param marker  end marker to use, or <code>null</code> to specify all changes since the start.
     */
    public void setMarkerEnd(String marker)
    {
        markerEnd = marker;
    }
    
    /**
     * Returns the marker (date or tag) for the end of the current change set.
     * Whether it's a date or tag depends on {@link #getType}.
     *
     * @return  the marker (date or tag) for the end of the current change set, or <code>null</code> if there is no
     *          end (meaning the change set should show all changes from the start to the present time).
     */
    public String getMarkerEnd()
    {
        return markerEnd;
    }
    
    
    /**
     * Set the date format of log entries to process; the
     * interpretation of this parameter depends on the generator.
     *
     * @param dateFormat the dateFormat of log entries.
     */
    public void setDateFormat(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }
    
    /**
     * Get the date format of log entries to process; the
     * interpretation of this parameter depends on the generator.
     *
     * @return the dateFormat of log entries.
     */
    public String getDateFormat()
    {
        return dateFormat;
    }
    
    /**
     * Set the base directory for the change log generator.
     * @param base the base directory
     */
    public void setBasedir(File base)
    {
        this.base = base;
    }
    
    /**
     * Get the base directory for the change log generator.
     *
     * @return the base directory
     */
    public File getBasedir()
    {
        return base;
    }
    
    /**
     * Set the output file for the log.
     * @param output the output file
     */
    public void setOutput(File output)
    {
        this.output = output;
    }
    
    /**
     * Return connection string declared in project.xml
     * @return connection string
     */
    public String getRepositoryConnection()
    {
        return connection;
    }
    
    /**
     * Change SCM connection string
     * @param aString a string containing the project's repository
     *      connection
     */
    public void setRepositoryConnection(String aString)
    {
        connection = aString;
    }
    
    /**
     * Execute task.
     * @throws FileNotFoundException if {@link ChangeLog#base} doesn't exist
     * @throws IOException if there are problems running CVS
     * @throws UnsupportedEncodingException if the underlying platform doesn't
     *      support ISO-8859-1 encoding
     */
    public void doExecute() throws FileNotFoundException, IOException,
            UnsupportedEncodingException
    {
        generateSets();
        replaceAuthorIdWithName();
        if ( output == null ) return;
        createDocument();
    }
    
    /**
     * Create the change log entries.
     * @throws IOException if there is a problem creating the change log
     * entries.
     */
    private void generateSets() throws IOException
    {
        ChangeLogFactory factory = createFactory();
        
        String markers = "";
        if (getType().equalsIgnoreCase("tag"))
        {
            markers = getTag();
        }
        else if (getType().equalsIgnoreCase("date"))
        {
            markers = getDate();
        }
        else
        {
            markers = getRange();
        }
        
        try
        {
            StringTokenizer tokens = new StringTokenizer(markers, ",");
            int expectedSets = tokens.countTokens() - 1;
            if ( expectedSets < 0 )
                sets = new ArrayList( 1 );
            else
                sets = new ArrayList(expectedSets);
            String end = tokens.nextToken();
            do
            {
                String start = end;
                end = (tokens.hasMoreTokens()) ? tokens.nextToken() : null;
                
                setMarkerStart(start);
                setMarkerEnd(end);
                
                ChangeLogParser parser = factory.createParser();
                if (getDateFormat() != null)
                {
                    parser.setDateFormatInFile(getDateFormat());
                }
                parser.init(this);
                
                ChangeLogGenerator generator = factory.createGenerator();
                generator.init(this);
                
                Collection entries;
                String logStart;
                String logEnd;
                try
                {
                    entries = generator.getEntries(parser);
                    logStart = generator.getLogStart();
                    logEnd = generator.getLogEnd();
                }
                catch (IOException e)
                {
                    getLog().warn(e.getLocalizedMessage(), e);
                    throw e;
                }
                finally
                {
                    generator.cleanup();
                    parser.cleanup();
                }
                
                if ( entries == null )
                {
                    entries = Collections.EMPTY_LIST;
                }
                
                sets.add(new ChangeLogSet(entries, logStart, logEnd));
                if (getLog().isInfoEnabled())
                {
                    getLog().info("ChangeSet between " + logStart + " and " + logEnd + ": "
                            + entries.size() + " entries");
                }
            } while (sets.size() < expectedSets);
        }
        finally
        {
        }
    }
    
    /**
     * Create a new instance of the ChangeLogFactory specified by the
     * <code>clFactory</code> member.
     *
     * @return the new ChangeLogFactory instance
     * @throws IOException if there is a problem creating the instance.
     */
    private ChangeLogFactory createFactory() throws IOException
    {
        if ( clFactoryClass == null )
        {
            if ( connection == null || connection.length() < 7 || !connection.startsWith( "scm:" ) )
            {
                getLog().warn( "Connection does not appear valid" );
            }
            else
            {
                int idx = connection.indexOf( ':', 4 );
                
                if ( idx > 4 )
                    clFactoryClass = (String) FACTORIES.get( connection.substring( 4, idx ) );
                else
                    getLog().warn( "Connectiong does not appear to be valid" );
            }
            
            if ( clFactoryClass == null )
            {
                getLog().warn( "Could not derive factory from connection: using default CVS (valid factories are: " + FACTORIES.keySet() + ")" );
                clFactoryClass = "org.apache.maven.cvslib.CvsChangeLogFactory";
            }
        }
        
        try
        {
            Class clazz = Class.forName(clFactoryClass);
            return (ChangeLogFactory) clazz.newInstance();
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new IOException("Cannot find class " + clFactoryClass
                    + " " + cnfe.toString());
        }
        catch (IllegalAccessException iae)
        {
            throw new IOException("Cannot access class " + clFactoryClass
                    + " " + iae.toString());
        }
        catch (InstantiationException ie)
        {
            throw new IOException("Cannot instantiate class " + clFactoryClass
                    + " " + ie.toString());
        }
    }
    
    /**
     * Set up list of developers mapping id to name.
     * @task This should be a facility on the maven project itself
     * @return a list of developers ids and names
     */
    private Properties getUserList()
    {
        Properties userList = new Properties();
        
        Developer developer = null;
        for (Iterator i = getDevelopers().iterator(); i.hasNext();)
        {
            try {
              developer = (Developer) i.next();
              userList.put(developer.getId(), developer.getName());
	    } catch ( NullPointerException npe ) {
	    }
        }
        
        return userList;
    }
    
    /**
     * replace all known author's id's with their maven specified names
     */
    private void replaceAuthorIdWithName()
    {
        Properties userList = getUserList();
        ChangeLogEntry entry = null;
        
        for (final Iterator iSets = getChangeSets().iterator() ; iSets.hasNext() ;)
        {
            final ChangeLogSet set = (ChangeLogSet)iSets.next();
            for (Iterator iEntries = set.getEntries().iterator(); iEntries.hasNext();)
            {
                entry = (ChangeLogEntry) iEntries.next();
                if (userList.containsKey(entry.getAuthor()))
                {
                    entry.setAuthor(userList.getProperty(entry.getAuthor()));
                }
            }
        }
    }
    
    /**
     * Create the XML document from the currently available details
     * @throws FileNotFoundException when the output file previously provided
     *      does not exist
     * @throws UnsupportedEncodingException when the platform doesn't support
     *      ISO-8859-1 encoding
     */
    private void createDocument() throws FileNotFoundException,
            UnsupportedEncodingException
    {
        File dir = output.getParentFile();
        if (dir != null)
        {
            dir.mkdirs();
        }
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(output), getOutputEncoding()));
        out.println(toXML());
        out.flush();
        out.close();
    }
    
    /**
     * @return an XML document representing this change log and it's entries
     */
    private String toXML()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"")
        .append(getOutputEncoding())
        .append("\" ?>\n")
        .append("<changelog>\n");
        
        for (Iterator i = getChangeSets().iterator(); i.hasNext();)
        {
            buffer.append(((ChangeLogSet) i.next()).toXML());
        }
        
        buffer.append("</changelog>\n");
        
        return buffer.toString();
    }
    
    /**
     * Gets the collection of change sets.
     *
     * @return collection of {@link ChangeLogSet} objects.
     */
    public Collection getChangeSets()
    {
        if (sets == null)
        {
            sets = Collections.EMPTY_LIST;
        }
        return sets;
    }
    
    /**
     * Sets the collection of change sets.
     * @param sets  New value of property sets.
     */
    public void setChangeSets(Collection sets)
    {
        this.sets = sets;
    }
    
    /**
     * Returns the developers.
     * @return List
     */
    public List getDevelopers()
    {
        return developers;
    }
    
    /**
     * Sets the developers.
     * @param developers The developers to set
     */
    public void setDevelopers(List developers)
    {
        this.developers = developers;
    }
    
    /**
     * Returns the outputEncoding.
     * @return String
     */
    public String getOutputEncoding()
    {
        return outputEncoding;
    }
    
    /**
     * Sets the outputEncoding.
     * @param outputEncoding The outputEncoding to set
     */
    public void setOutputEncoding(String outputEncoding)
    {
        this.outputEncoding = outputEncoding;
    }
    
    /**
     * Returns the commentFormat used to interrogate the RCS.
     * @return String
     */
    public String getCommentFormat()
    {
        return commentFormat;
    }
    
    /**
     * Sets the commentFormat.
     * @param commentFormat The commentFormat to set
     */
    public void setCommentFormat(String commentFormat)
    {
        this.commentFormat = commentFormat;
    }
    
    /**
     * parses a previously generated changelog xml document and return its changed sets
     *
     * @return changelog sets parsed from the xml document
     * @param stream the changelog xml document
     * @param dateFormat date format used to generate the changelog xml document
     *
     * @throws ParserConfigurationException when instantiation of the SAX parser failed
     * @throws SAXException when an error occurred while parsing the xml document
     * @throws IOException when an error occurred while accessing the xml document
     */
    public static Collection loadChangedSets( InputStream stream, String dateFormat )
    throws ParserConfigurationException, SAXException, IOException
    {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        
        Collection changeLogSets = new LinkedList();
        
        parser.parse( stream, new ChangeLogHandler( changeLogSets, dateFormat ) );
        
        return changeLogSets;
    }

    public static Log getLog()
    {
        return LOG;
    }
    
    public static void setLog( Log logger )
    {
        LOG = logger;
    }

} // end of ChangeLog
