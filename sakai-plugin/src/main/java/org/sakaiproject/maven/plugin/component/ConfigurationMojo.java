package org.sakaiproject.maven.plugin.component;



import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * Package Configuration as a Zip for later deployment.
 *
 * @goal configuration
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class ConfigurationMojo
    extends AbstractMojo
{
	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

    /**
     * The Zip archiver.
     * @component role="org.codehaus.plexus.archiver.Archiver" role-hint="zip"
     */
    private ZipArchiver zipArchiver;

    /**
     * Directory containing the build files.
     * @parameter expression="${project.build.directory}/configuration"
     */
    private String configurationDirectory;
    /**
     * Directory containing the build files.
     * @parameter expression="${project.build.directory}"
     */
    private String outputDirectory;

    /**
     * The name of the generated Configuration.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
	private String configurationName;
	
    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * Whether this is the main artifact being built. Set to <code>false</code> if you don't want to install or
     * deploy it to the local repository instead of the default one in an execution.
     *
     * @parameter expression="${primaryArtifact}" default-value="true"
     */
    private boolean primaryArtifact;

    
    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     *
     * @parameter
     */
	private String classifier;

    protected static File getConfigurationFile( File basedir, String finalName, String classifier )
    {
        if ( classifier == null )
        {
            classifier = "";
        }
        else if ( classifier.trim().length() > 0 && !classifier.startsWith( "-" ) )
        {
            classifier = "-" + classifier;
        }

        return new File( basedir, finalName + classifier + ".configuration" );
    }

    public void execute()
        throws MojoExecutionException
    {
        try {
           	File outputDirectoryFile = new File(outputDirectory);
            File buildDirectoryFile = new File(configurationDirectory);
        	File outputFile = getConfigurationFile( outputDirectoryFile, configurationName, classifier);
            zipArchiver.addDirectory( buildDirectoryFile, new String[]{"**/**"}, new String[]{"**/"+outputFile.getName()} );
            zipArchiver.setDestFile( outputFile );
            zipArchiver.createArchive();
            
            String classifier = this.classifier;
            if ( classifier != null )
            {
                projectHelper.attachArtifact( getProject(), "configuration", classifier, outputFile );
            }
            else
            {
                Artifact artifact = getProject().getArtifact();
                if ( primaryArtifact )
                {
                    artifact.setFile( outputFile );
                }
                else if ( artifact.getFile() == null || artifact.getFile().isDirectory() )
                {
                    artifact.setFile( outputFile );
                }
            }

            
        } catch( Exception e ) {
            throw new MojoExecutionException( "Could not zip configuration settings", e );
        }
    }
    
	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

}
