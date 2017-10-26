package com.gemnasium;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Parent Mojo holding common stuff
 */
public class MainMojo extends AbstractMojo {

    protected Config config;

    @Parameter( property = "basedir", defaultValue = "${basedir}", required = true)
    private File baseDir;

    // Plugin Configuration from pom.xml
    @Parameter( property = "apiBaseUrl" )
    private String apiBaseUrl;

    @Parameter( property = "apiKey" )
    private String apiKey;

    @Parameter( property = "projectBranch" )
    private String projectBranch;

    @Parameter( property = "projectSlug" )
    private String projectSlug;

    @Parameter( property = "projectRevision" )
    private String projectRevision;

    public void execute() throws MojoExecutionException {
        printHeader();
        loadConfig();
    }

    /**
     * Loads configuration :
     */
    protected void loadConfig() throws MojoExecutionException {
        this.config = new Config(baseDir, apiBaseUrl, apiKey, projectBranch, projectSlug, projectRevision);
    }

    protected void printHeader() {
      getLog().info( "Gemnasium Maven Plugin" );
      getLog().info( "" );
    }
}
