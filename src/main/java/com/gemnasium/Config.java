package com.gemnasium;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Gemnasium Maven Plugin configuration
 */
public class Config {

    private static final String DEFAULT_API_BASE_URL = "https://gemnasium.com/api/v2";
    private static final String GEMNASIUM_PROPERTIES_FILE_PATH = "/src/main/resources/gemnasium.properties";
    
    private File baseDir;

    private String apiBaseUrl;
    private String apiKey;
    private String projectBranch;
    private String projectSlug;
    private String projectRevision;

    /**
     * Initializes a the plugin configuration with the following ascending priority:
     *  - properties file (gemnasium.properties)
     *  - plugin configuration (within pom.xml)
     *  - env variables
     */

    public Config(File baseDir, String apiBaseUrl, String apiKey, String projectBranch, String projectSlug, String projectRevision) throws MojoExecutionException {
        this.baseDir = baseDir;
        Properties configProperties;
        try {
            configProperties = loadConfigProperties();
        } catch(Exception e) {
            throw new MojoExecutionException("Can't load configuration file.", e);
        }

        this.apiBaseUrl = getFirstNotEmpty(
            System.getenv().get("GEMNASIUM_API_BASE_URL"),
            apiBaseUrl,
            configProperties.getProperty("apiBaseUrl")
        );
        // Set default apiBaseUrl if none provided
        if (this.apiBaseUrl == null || this.apiBaseUrl.isEmpty()) {
            this.apiBaseUrl = DEFAULT_API_BASE_URL;
        }

        this.apiKey = getFirstNotEmpty(
            System.getenv().get("GEMNASIUM_API_KEY"),
            apiKey,
            configProperties.getProperty("apiKey")
        );
        this.projectBranch = getFirstNotEmpty(
            System.getenv().get("GEMNASIUM_PROJECT_BRANCH"),
            projectBranch,
            configProperties.getProperty("projectBranch")
        );
        this.projectSlug = getFirstNotEmpty(
            System.getenv().get("GEMNASIUM_PROJECT_SLUG"),
            projectSlug,
            configProperties.getProperty("projectSlug")
        );
        this.projectRevision = getFirstNotEmpty(
            System.getenv().get("GEMNASIUM_PROJECT_REVISION"),
            projectRevision,
            configProperties.getProperty("projectRevision")
        );
    }

    private String getFirstNotEmpty(String envVarConfig, String pluginConfig, String propertyConfig) {
        if (envVarConfig != null && !envVarConfig.isEmpty()) {
            return envVarConfig;
        }

        if (pluginConfig != null && !pluginConfig.isEmpty()) {
            return pluginConfig;
        }

        return propertyConfig;
    }

    public Properties loadConfigProperties() throws Exception {
        Properties properties = new Properties();
        File file = new File(baseDir + GEMNASIUM_PROPERTIES_FILE_PATH);
        if (!file.exists()) {
            return properties;
        }
        InputStream is = new FileInputStream( file );
        properties.load(is);
        return properties;
    }

    public void updateConfigProperties(Properties updatedProperties) throws Exception {
        Properties oldProperties = loadConfigProperties();
        Properties newProperties = new Properties();
        newProperties.putAll(oldProperties);
        newProperties.putAll(updatedProperties);
        storeConfigProperties(newProperties);
    }
 
    public String toString() {
        return "apiBaseUrl:" + apiBaseUrl + "\n" +
               "apiKey:" + apiKey + "\n" +
               "projectBranch:" + projectBranch + "\n" +
               "projectSlug:" + projectSlug + "\n" +
               "projectRevision:" + projectRevision + "\n";
    }

    private void storeConfigProperties(Properties properties) throws Exception {
        File file = new File(baseDir + GEMNASIUM_PROPERTIES_FILE_PATH);
        OutputStream os = new FileOutputStream( file );
        properties.store(os, "Gemnasium configuration");
    }

    // Getters and setters
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getProjectBranch() {
        return projectBranch;
    }
    public void setProjectBranch(String projectBranch) {
        this.projectBranch = projectBranch;
    }

    public String getProjectSlug() {
        return projectSlug;
    }
    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public String getProjectRevision() {
        return projectRevision;
    }
    public void setProjectRevision(String projectRevision) {
        this.projectRevision = projectRevision;
    }
}
