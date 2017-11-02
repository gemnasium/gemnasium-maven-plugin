package com.gemnasium;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Gemnasium Maven Plugin configuration
 */
public class Config {

    private static final String DEFAULT_BASE_URL = "https://gemnasium.com";
    private static final String API_PREFIX = "/api/v2";
    private static final String GEMNASIUM_PROPERTIES_FILE_PATH = "/src/main/resources/gemnasium.properties";

    private File baseDir;

    private String apiKey;
    private String baseUrl;
    private String projectBranch;
    private String projectSlug;
    private String projectRevision;
    private String ignoredScopes;

    /**
     * Initializes a the plugin configuration with the following ascending priority:
     *  - properties file (gemnasium.properties)
     *  - plugin configuration (within pom.xml)
     *  - env variables
     * @param baseDir The maven project baseDir.
     * @param baseUrl The base URL of the Gemnasium instance (for Gemnasium Enteprise usage).
     * @param apiKey Your Gemanisum API key
     * @param projectBranch Current branch
     * @param projectSlug The project identifier on Gemnasium.
     * @param projectRevision Current revision
     * @param ignoredScopes Comma separated list of Maven dependency scopes to ignore.
     * @throws MojoExecutionException if properties configuration can't be loaded.
     */
    public Config(File baseDir, String baseUrl, String apiKey, String projectBranch, String projectSlug,
            String projectRevision, String ignoredScopes) throws MojoExecutionException {
        this.baseDir = baseDir;
        Properties configProperties;
        try {
            configProperties = loadConfigProperties();
        } catch (Exception e) {
            throw new MojoExecutionException("Can't load configuration file.", e);
        }

        this.baseUrl = getFirstNotEmpty(System.getenv().get("GEMNASIUM_BASE_URL"), baseUrl,
                configProperties.getProperty("baseUrl"));
        // Set default baseUrl if none provided
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            this.baseUrl = DEFAULT_BASE_URL;
        }

        this.apiKey = getFirstNotEmpty(System.getenv().get("GEMNASIUM_API_KEY"), apiKey,
                configProperties.getProperty("apiKey"));
        this.projectBranch = getFirstNotEmpty(System.getenv().get("GEMNASIUM_PROJECT_BRANCH"), projectBranch,
                configProperties.getProperty("projectBranch"));
        this.projectSlug = getFirstNotEmpty(System.getenv().get("GEMNASIUM_PROJECT_SLUG"), projectSlug,
                configProperties.getProperty("projectSlug"));
        this.projectRevision = getFirstNotEmpty(System.getenv().get("GEMNASIUM_PROJECT_REVISION"), projectRevision,
                configProperties.getProperty("projectRevision"));
        this.ignoredScopes = getFirstNotEmpty(System.getenv().get("GEMNASIUM_IGNORED_SCOPES"), ignoredScopes,
                configProperties.getProperty("ignoredScopes"));
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
        InputStream is = new FileInputStream(file);
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
        return "baseUrl:" + baseUrl + "\n" + "apiKey:" + apiKey + "\n" + "projectBranch:" + projectBranch + "\n"
                + "projectSlug:" + projectSlug + "\n" + "projectRevision:" + projectRevision + "\n" + "ignoredScopes:"
                + ignoredScopes + "\n";
    }

    private void storeConfigProperties(Properties properties) throws Exception {
        File file = new File(baseDir + GEMNASIUM_PROPERTIES_FILE_PATH);
        OutputStream os = new FileOutputStream(file);
        properties.store(os, "Gemnasium configuration");
    }

    /**
    * @return the API base url
    */
    public String getApiBaseUrl() {
        return baseUrl + API_PREFIX;
    }

    /**
    * @return the UI url
    */
    public String getUIBaseUrl() {
        // TODO: hardcoded value for Beta
        if (baseUrl == DEFAULT_BASE_URL) {
            return "https://beta.gemnasium.com";
        }
        return baseUrl;
    }

    // Getters and setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setbaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public String getIgnoredScopes() {
        return ignoredScopes;
    }

    public void setIgnoredScopes(String ignoredScopes) {
        this.ignoredScopes = ignoredScopes;
    }

}
