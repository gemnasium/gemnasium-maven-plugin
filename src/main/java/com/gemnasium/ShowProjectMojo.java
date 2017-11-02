package com.gemnasium;

import com.gemnasium.utils.AuthUtils;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.net.ssl.HttpsURLConnection;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Shows project info
 */
@Mojo(name = "show-project", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class ShowProjectMojo extends AbstractMainMojo {

    public void execute() throws MojoExecutionException {
        super.execute();
        showProject();
    }

    private void showProject() throws MojoExecutionException {
        String projectSlug = config.getProjectSlug();
        if (projectSlug == null || projectSlug.isEmpty()) {
            throw new MojoExecutionException("show-project failed, please provide the projectSlug option.");
        }

        URL url;
        try {
            url = new URL(config.getApiBaseUrl() + "/projects/" + projectSlug);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(
                    "create-project failed, invalid parameters baseUrl or teamSlug, can't forge URL");
        }

        HttpsURLConnection conn;
        try {
            conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestProperty("Authorization", "Basic " + AuthUtils.getEncodedBasicToken(config.getApiKey()));
        } catch (IOException e) {
            throw new MojoExecutionException("show-project failed, can't connect to Gemnasium API.", e);
        }

        InputStream is = null;
        try {
            is = conn.getInputStream();
        } catch (IOException ioe) {
            try {
                if (conn.getResponseCode() != 200) {
                    is = conn.getErrorStream();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(is);
                    throw new MojoExecutionException("show-project failed, API Error: " + node.get("message").asText());
                }
                throw new MojoExecutionException("show-project failed, API Error");
            } catch (IOException e) {
                throw new MojoExecutionException("show-project failed, API Error");
            }
        }

        // Parses JSON response to find project attributes
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node;
        try {
            node = mapper.readTree(is);
            getLog().info("Project Info for: " + node.get("name").asText());
            getLog().info("");
            getLog().info("\t slug: " + node.get("slug").asText());
            getLog().info("\t branch: " + node.get("branch").asText());
            getLog().info("\t color: " + node.get("color").asText());
        } catch (Exception e) {
            throw new MojoExecutionException("show-project failed, malformed API response");
        }
    }
}
