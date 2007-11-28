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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.sakaiproject.maven.plugin.component.stub.MavenProjectBasicStub;
import org.sakaiproject.maven.plugin.component.stub.ResourceStub;
import org.sakaiproject.maven.plugin.component.stub.SimpleWarArtifactStub;
import org.sakaiproject.maven.plugin.component.stub.SimpleConfigurationArtifactStub;

public class ComponentDeployMojoTest
    extends AbstractComponentMojoTest
{
    protected static final String pomFilePath =
        getBasedir() + "/target/test-classes/unit/wardeploymojo/plugin-config.xml";

    private ComponentDeployMojo mojo;
    
    /** 
     * @component
     */
    protected ArtifactFactory artifactFactory;
    /**
     * @component
     */
    protected ArtifactResolver artifactResolver;

    /**
     * @parameter expression="${localRepository}
     */
    protected ArtifactRepository artifactRepository;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    protected List remoteRepositories;


    protected File getTestDirectory()
        throws Exception
    {
        return new File( getBasedir(), "target/test-classes/unit/wardeploymojo/test-dir" );
    }

    public void setUp()
        throws Exception
    {
        super.setUp();

        mojo = (ComponentDeployMojo) lookupMojo( "deploy", pomFilePath );
        artifactFactory = (ArtifactFactory) lookup(ArtifactFactory.class.getName());
        assertNotNull( mojo );
        assertNotNull( artifactFactory );
    }

    /**
     * @throws Exception
     */
    public void testWebappDeployWar()
        throws Exception
    {
        // setup test data
        String testId = "SimpleDeployWebApp";
        MavenProjectBasicStub project = new MavenProjectBasicStub();
        project.setArtifact(new SimpleWarArtifactStub(getBasedir()));
        project.setPackaging("war");
        File webAppSource = createWebAppSource( testId );
        File classesDir = createClassesDir( testId, false );
        File webAppResource = new File( getTestDirectory(), testId + "-resources" );
        File webAppDirectory = new File( getTestDirectory(), testId );
        File deployDirectory = new File( getTestDirectory(), testId + "-tomcat" );
        File sampleResource = new File( webAppResource, "pix/panis_na.jpg" );
        ResourceStub[] resources = new ResourceStub[]{new ResourceStub()};

        createFile( sampleResource );

        assertTrue("sampeResource not found",sampleResource.exists());
      
        // configure mojo
        resources[0].setDirectory( webAppResource.getAbsolutePath() );
        this.configureMojo( mojo, new LinkedList(), classesDir, webAppSource, webAppDirectory, deployDirectory, project );
        setVariableValueToObject( mojo, "webResources", resources );
        mojo.execute();

        // validate operation
        File expectedWarFile = new File( deployDirectory, "webapps/"+project.getArtifactId()+".war" );

        assertTrue( "War File Not Found: " + expectedWarFile.toString(), expectedWarFile.exists() );
        
        // house keeping
        expectedWarFile.delete();
    }
    
    
    /**
     * @throws Exception
     */
    public void testComponentDeployWar()
        throws Exception
    {
        // setup test data
        String testId = "SimpleDeployComponent";
        MavenProjectBasicStub project = new MavenProjectBasicStub();
        project.setArtifact(new SimpleWarArtifactStub(getBasedir()));
        project.setPackaging("sakai-component");
        File webAppSource = createWebAppSource( testId );
        File classesDir = createClassesDir( testId, false );
        File webAppResource = new File( getTestDirectory(), testId + "-resources" );
        File webAppDirectory = new File( getTestDirectory(), testId );
        File deployDirectory = new File( getTestDirectory(), testId + "-tomcat" );
        File sampleResource = new File( webAppResource, "pix/panis_na.jpg" );
        ResourceStub[] resources = new ResourceStub[]{new ResourceStub()};

        createFile( sampleResource );

        assertTrue("sampeResource not found",sampleResource.exists());
      
        // configure mojo
        resources[0].setDirectory( webAppResource.getAbsolutePath() );
        this.configureMojo( mojo, new LinkedList(), classesDir, webAppSource, webAppDirectory, deployDirectory, project );
        setVariableValueToObject( mojo, "webResources", resources );
        mojo.execute();

        // validate operation
        File expectedWarFile = new File( deployDirectory, "components/"+project.getArtifactId()+"/org/sample/company/test.jsp" );

        assertTrue( "War File Not Found: " + expectedWarFile.toString(), expectedWarFile.exists() );
        
        // house keeping
        expectedWarFile.delete();
    }
    /**
     * @throws Exception
     */
    public void testConfigurationDeploy()
        throws Exception
    {
        // setup test data
        String testId = "SimpleDeployConfiguration";
        MavenProjectBasicStub project = new MavenProjectBasicStub();
        project.setArtifact(new SimpleConfigurationArtifactStub(getBasedir()));
        project.setPackaging("sakai-configuration");
        File webAppSource = createWebAppSource( testId );
        File classesDir = createClassesDir( testId, false );
        File webAppResource = new File( getTestDirectory(), testId + "-resources" );
        File webAppDirectory = new File( getTestDirectory(), testId );
        File deployDirectory = new File( getTestDirectory(), testId + "-tomcat" );
        File sampleResource = new File( webAppResource, "pix/panis_na.jpg" );
        ResourceStub[] resources = new ResourceStub[]{new ResourceStub()};

        createFile( sampleResource );

        assertTrue("sampeResource not found",sampleResource.exists());
      
        // configure mojo
        resources[0].setDirectory( webAppResource.getAbsolutePath() );
        this.configureMojo( mojo, new LinkedList(), classesDir, webAppSource, webAppDirectory, deployDirectory, project );
        setVariableValueToObject( mojo, "webResources", resources );
        mojo.execute();

        // validate operation
        File expectedWarFile = new File( deployDirectory, "/org/sample/company/test.jsp" );

        assertTrue( "Output File Not Found: " + expectedWarFile.toString(), expectedWarFile.exists() );
        
        // house keeping
       // expectedWarFile.delete();
    }
    /**
     * @throws Exception
     */
    public void xtestSharedDeployJar()
        throws Exception
    {
        // setup test data
        String testId = "SimpleDeployShared";
        MavenProjectBasicStub project = new MavenProjectBasicStub();
        project.setArtifact(artifactFactory.createArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(), null, "jar"));
        Artifact artifact  = artifactFactory.createArtifact("org.apache.maven","maven-core","2.0",null,"jar");
        HashSet<Artifact> dependencySet = new HashSet<Artifact>();        
        dependencySet.add(artifact);
        project.setDependencyArtifacts(dependencySet);
        project.setPackaging("jar");
        project.addProperty("deploy.target", "shared");
        File webAppSource = createWebAppSource( testId );
        File classesDir = createClassesDir( testId, false );
        File webAppResource = new File( getTestDirectory(), testId + "-resources" );
        File webAppDirectory = new File( getTestDirectory(), testId );
        File deployDirectory = new File( getTestDirectory(), testId + "-tomcat" );
        File sampleResource = new File( webAppResource, "pix/panis_na.jpg" );
        ResourceStub[] resources = new ResourceStub[]{new ResourceStub()};

        createFile( sampleResource );

        assertTrue("sampeResource not found",sampleResource.exists());
      
        // configure mojo
        resources[0].setDirectory( webAppResource.getAbsolutePath() );
        this.configureMojo( mojo, new LinkedList(), classesDir, webAppSource, webAppDirectory, deployDirectory, project );
        setVariableValueToObject( mojo, "webResources", resources );
        setVariableValueToObject(mojo, "remoteRepositories", new ArrayList());
        mojo.execute();

        // validate operation
        File expectedJarFile = new File( deployDirectory, "shared/lib/"+artifact.getArtifactId()+"-"+artifact.getVersion()+"."+artifact.getType() );

        assertTrue( "Jar File Not Found: " + expectedJarFile.toString(), expectedJarFile.exists() );
        
        // house keeping
        expectedJarFile.delete();
    }
    /**
     * @throws Exception
     */
    public void xtestCommonDeployJar()
        throws Exception
    {
        // setup test data
        String testId = "SimpleDeployCommon";
        MavenProjectBasicStub project = new MavenProjectBasicStub();
        project.setArtifact(artifactFactory.createArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(), null, "jar"));
        Artifact artifact  = artifactFactory.createArtifact("org.apache.maven","maven-core","2.0",null,"jar");
        HashSet<Artifact> dependencySet = new HashSet<Artifact>();        
        dependencySet.add(artifact);
        project.setDependencyArtifacts(dependencySet);
        project.setPackaging("jar");
        project.addProperty("deploy.target", "common");
        File webAppSource = createWebAppSource( testId );
        File classesDir = createClassesDir( testId, false );
        File webAppResource = new File( getTestDirectory(), testId + "-resources" );
        File webAppDirectory = new File( getTestDirectory(), testId );
        File deployDirectory = new File( getTestDirectory(), testId + "-tomcat" );
        File sampleResource = new File( webAppResource, "pix/panis_na.jpg" );
        ResourceStub[] resources = new ResourceStub[]{new ResourceStub()};

        createFile( sampleResource );

        assertTrue("sampeResource not found",sampleResource.exists());
      
        // configure mojo
        resources[0].setDirectory( webAppResource.getAbsolutePath() );
        this.configureMojo( mojo, new LinkedList(), classesDir, webAppSource, webAppDirectory, deployDirectory, project );
        setVariableValueToObject( mojo, "webResources", resources );
        setVariableValueToObject(mojo, "remoteRepositories", new ArrayList());
        mojo.execute();

        // validate operation
        File expectedJarFile = new File( deployDirectory, "common/lib/"+artifact.getArtifactId()+"-"+artifact.getVersion()+"."+artifact.getType() );

        assertTrue( "Jar File Not Found: " + expectedJarFile.toString(), expectedJarFile.exists() );
        
        // house keeping
        expectedJarFile.delete();
    }
    
    protected void configureMojo( ComponentDeployMojo mojo, List filters, File classesDir, File webAppSource,
            File webAppDir, File deployDir, MavenProjectBasicStub project )
    throws Exception
    {
    	super.configureMojo(mojo, filters, classesDir, webAppSource, webAppDir, deployDir, project);
        mojo.setDeployDirectory( deployDir );
    }

}
