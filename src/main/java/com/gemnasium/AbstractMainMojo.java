package com.gemnasium;

import com.gemnasium.utils.ProjectsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Main Mojo holding common stuff
 */
public abstract class AbstractMainMojo extends AbstractMojo {

    protected Config config;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "basedir", defaultValue = "${basedir}", required = true)
    private File baseDir;

    // Plugin Configuration from pom.xml
    @Parameter(property = "baseUrl")
    private String baseUrl;

    @Parameter(property = "apiKey")
    private String apiKey;

    @Parameter(property = "projectBranch")
    private String projectBranch;

    @Parameter(property = "projectSlug")
    private String projectSlug;

    @Parameter(property = "projectRevision")
    private String projectRevision;

    @Parameter(property = "ignoredScopes")
    private String ignoredScopes;

    public void execute() throws MojoExecutionException {
        printHeader();
        loadConfig();
    }

    /**
     * Loads configuration
     * @throws MojoExecutionException if config can't be loaded
     */
    protected void loadConfig() throws MojoExecutionException {
        this.config = new Config(baseDir, baseUrl, apiKey, projectBranch, projectSlug, projectRevision,
                ignoredScopes);
    }

    /**
    * Prints a header
    */
    protected void printHeader() {
        getLog().info("Gemnasium Maven Plugin");
        getLog().info("");
    }

    /**
    * Gets the project dependencies (except ignored scopes)
    * @return the project dependencies as List of Artifact
    */
    protected List<Artifact> getProjectDependencies() {
        return ProjectsUtils.getFilteredDependencies(new ArrayList<Artifact>(project.getArtifacts()),
                config.getIgnoredScopes());
    }
}
