package com.gemnasium;

import com.gemnasium.utils.AuthUtils;
import com.gemnasium.utils.ProjectsUtils;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.net.ssl.HttpsURLConnection;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Creates a new project on Gemnasium with given parameters.
 * teamSlug and projectName are mandatory parameters
 */
@Mojo(name = "create-project", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CreateProjectMojo extends AbstractMainMojo {

    // Specific plugin parameter for that goal.
    @Parameter(property = "teamSlug", required = true)
    private String teamSlug;

    @Parameter(property = "projectName", required = true)
    private String projectName;

    @Parameter(property = "projectDescription", required = false)
    private String projectDescription;

    public void execute() throws MojoExecutionException {
        super.execute();
        createProject(teamSlug, projectName, projectDescription);
    }

    private void createProject(String teamSlug, String projectName, String projectDescription)
            throws MojoExecutionException {
        if (teamSlug == null || teamSlug.isEmpty()) {
            throw new MojoExecutionException("create-project failed, please provide the teamSlug option");
        }

        if (projectName == null || projectName.isEmpty()) {
            throw new MojoExecutionException("create-project failed, please provide the projectName option");
        }

        String baseName = ProjectsUtils.getBasename(projectName);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("name", projectName);
        jsonNode.put("basename", baseName);
        jsonNode.put("description", projectDescription);
        String requestBody = jsonNode.toString();

        URL url;
        try {
            url = new URL(config.getApiBaseUrl() + "/teams/" + teamSlug + "/projects");
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(
                    "create-project failed, invalid parameters baseUrl or teamSlug, can't forge URL");
        }

        HttpsURLConnection conn;
        OutputStream os;
        try {
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + AuthUtils.getEncodedBasicToken(config.getApiKey()));

            os = conn.getOutputStream();

            os.write(requestBody.getBytes("UTF-8"));
            os.close();
        } catch (IOException e) {
            throw new MojoExecutionException("create-project failed, can't connect to Gemnasium API", e);
        }

        InputStream is = null;
        try {
            is = conn.getInputStream();
        } catch (IOException ioe) {
            try {
                if (conn.getResponseCode() != 200) {
                    is = conn.getErrorStream();
                    JsonNode node = mapper.readTree(is);
                    throw new MojoExecutionException(
                            "create-project failed, API Error: " + node.get("message").asText());
                }
            } catch (IOException e) {
                throw new MojoExecutionException("create-project failed, API Error");
            }
        }

        // Parses JSON response to find the project slug
        JsonNode node;
        try {
            node = mapper.readTree(is);
        } catch (IOException ioe) {
            throw new MojoExecutionException("create-project failed, malformed API response");
        }

        String slug = node.get("slug").asText();
        if (slug == null || slug.isEmpty()) {
            throw new MojoExecutionException("create-project failed, no slug was returned by the API");
        }

        // Saves the project slug into the properties file
        Properties properties = new Properties();
        properties.setProperty("projectSlug", slug);
        try {
            config.updateConfigProperties(properties);
        } catch (Exception e) {
            throw new MojoExecutionException(
                    "Project was created but the configuration can't be stored in the properties file. Your project slug is: "
                            + slug,
                    e);
        }

        getLog().info(projectName + " project successfully created.");
        getLog().info("You can now send your dependencies using `mvn gemnasium:send-dependencies`");
        String projectUrl = config.getUIBaseUrl() + "/projects/" + slug;
        getLog().info("And your project is visible at:");
        getLog().info(projectUrl);

    }
}
