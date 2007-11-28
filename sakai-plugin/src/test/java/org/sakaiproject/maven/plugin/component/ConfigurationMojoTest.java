package org.sakaiproject.maven.plugin.component;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.sakaiproject.maven.plugin.component.stub.MavenProject4CopyConstructor;
import org.sakaiproject.maven.plugin.component.stub.ProjectHelperStub;
import org.sakaiproject.maven.plugin.component.stub.SimpleConfigurationArtifact4CCStub;

/**
 * comprehensive test on buildExplodedWebApp is done on ComponentExplodedMojoTest
 */
public class ConfigurationMojoTest
    extends AbstractComponentMojoTest
{
    ConfigurationMojo mojo;

    private static File pomFile =
        new File( getBasedir(), "target/test-classes/unit/configurationmojotest/plugin-config.xml" );

    protected File getTestDirectory()
    {
        return new File( getBasedir(), "target/test-classes/unit/configurationmojotest" );
    }

    public void setUp()
        throws Exception
    {
        super.setUp();
        mojo = (ConfigurationMojo) lookupMojo( "configuration", pomFile );
    }

    public void testEnvironment()
        throws Exception
    {
        // see setup
    }

    public void testSimpleWar()
        throws Exception
    {
        String testId = "SimpleConfig";
        MavenProject4CopyConstructor project = new MavenProject4CopyConstructor();
        String outputDir = getTestDirectory().getAbsolutePath() + "/" + testId + "-output";
        File webAppDirectory = new File( getTestDirectory(), testId );
        ProjectHelperStub projectHelper = new ProjectHelperStub();
        SimpleConfigurationArtifact4CCStub warArtifact = new SimpleConfigurationArtifact4CCStub( getBasedir() );
        String zipName = "simple";
        File webAppSource = createWebAppSource( testId );
        File classesDir = createClassesDir( testId, true );

        project.setArtifact( warArtifact );
        mojo.setProject(project);
        setVariableValueToObject( mojo, "projectHelper", projectHelper );
        setVariableValueToObject( mojo, "outputDirectory", outputDir );
        setVariableValueToObject( mojo, "configurationDirectory",getBaseSampleDir(testId).getAbsolutePath() );
        setVariableValueToObject( mojo, "configurationName", zipName );
  
        mojo.execute();

        //validate jar file
        File expectedZipFile = new File( outputDir, "simple.configuration" );
        Map zipContent = new HashMap();

        assertTrue( "configuration file not created: " + expectedZipFile.toString(), expectedZipFile.exists() );

        ZipFile zipFile = new ZipFile( expectedZipFile );
        ZipEntry entry;
        Enumeration enumeration = zipFile.entries();

        while ( enumeration.hasMoreElements() )
        {
            entry = (ZipEntry) enumeration.nextElement();
            zipContent.put( entry.getName(), entry );
        }

        assertTrue( "Expected file content not found",
        		zipContent.containsKey( "source/org/web/app/last-exile.jsp" ) );
    }

    public void testClassifier()
        throws Exception
    {
        String testId = "Classifier";
        MavenProject4CopyConstructor project = new MavenProject4CopyConstructor();
        String outputDir = getTestDirectory().getAbsolutePath() + "/" + testId + "-output";
        File webAppDirectory = new File( getTestDirectory(), testId );
        SimpleConfigurationArtifact4CCStub warArtifact = new SimpleConfigurationArtifact4CCStub( getBasedir() );
        ProjectHelperStub projectHelper = new ProjectHelperStub();

        String zipName = "simple";
        File webAppSource = createWebAppSource( testId );
        File classesDir = createClassesDir( testId, true );
        File xmlSource = createXMLConfigDir( testId, new String[]{"web.xml"} );

        project.setArtifact( warArtifact );
        mojo.setProject(project);
        setVariableValueToObject( mojo, "projectHelper", projectHelper );
        setVariableValueToObject( mojo, "classifier", "test-classifier" );
        setVariableValueToObject( mojo, "outputDirectory", outputDir );
        setVariableValueToObject( mojo, "configurationDirectory",getBaseSampleDir(testId).getAbsolutePath() );
        setVariableValueToObject( mojo, "configurationName", zipName );

        mojo.execute();

        //validate jar file
        File expectedZipFile = new File( outputDir, "simple-test-classifier.configuration" );
        HashSet zipContent = new HashSet();

        assertTrue( "configuration file not created: " + expectedZipFile.toString(), expectedZipFile.exists() );

        ZipFile ZipFile = new ZipFile( expectedZipFile );
        ZipEntry entry;
        Enumeration enumeration = ZipFile.entries();

        while ( enumeration.hasMoreElements() )
        {
            entry = (ZipEntry) enumeration.nextElement();
            zipContent.add( entry.getName() );
        }

        assertTrue( "Expected File Not Found",
        		zipContent.contains( "source/org/web/app/last-exile.jsp" ) );
    }

}
