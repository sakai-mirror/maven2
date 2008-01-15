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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Change log generated xml parser.  SAXParser listener for processing a previously generated xml into several
 *      change log sets.
 */
public class ChangeLogHandler extends DefaultHandler
{    
    private Collection changeSets;
    
    private SimpleDateFormat sdf;
    
    private String bufData;
    private ChangeLogFile bufFile;
    private ChangeLogEntry bufEntry;
    private LinkedList bufEntries;
    private ChangeLogSet bufSet;
    
    /**
     * contructor
     *
     * @param changeSets collection object to store all change sets found within the xml document
     * @param dateFormat string date format used to parse the date as saved into the xml document
     */
    public ChangeLogHandler( Collection changeSets, String dateFormat )
    {
        this.changeSets = changeSets;
        
        sdf = new SimpleDateFormat( dateFormat );
    }
    
    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[],int,int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        bufData += new String( ch, start, length );
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(String,String,String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
            if ( "changeset".equals( qName ) )
            {
                changeSets.add( bufSet );
            }

            if ( "changelog-entry".equals( qName ) )
            {
                bufEntries.add( bufEntry );
            }

            if ( "file".equals( qName ) )
            {
                bufEntry.addFile( bufFile );
            }
            else if ( "date".equals( qName ) )
            {
                try
                {
                    bufEntry.setDate( sdf.parse( bufData ) );
                }
                catch ( ParseException e )
                {
                    throw new SAXException( e );
                }
            }
            else if ( "author".equals( qName ) )
            {
                bufEntry.setAuthor( bufData );
            }
            else if ( "msg".equals( qName ) )
            {
                bufEntry.setComment( bufData );
            }

            if ( "revision".equals( qName ) )
            {
                bufFile.setRevision( bufData );
            }
            else if ( "name".equals( qName ) )
            {
                bufFile.setName( bufData );
            }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(String,String,String,Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) 
        throws SAXException
    {
        bufData = "";
        
        if ( "file".equals( qName ) )
        {
            bufFile = new ChangeLogFile( "" );
        }
        else if ( "changelog-entry".equals( qName ) )
        {
            bufEntry = new ChangeLogEntry();
        }
        else if ( "changeset".equals( qName ) )
        {
            bufEntries = new LinkedList();
            
            String start = attributes.getValue( "start" );
            
            String end = attributes.getValue( "end" );
            
            bufSet = new ChangeLogSet( bufEntries, start, end );
        }
    }
}
