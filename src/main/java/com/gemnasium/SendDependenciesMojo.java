package com.gemnasium;

import com.gemnasium.utils.AuthUtils;
import com.gemnasium.utils.ProjectsUtils;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.net.ssl.HttpsURLConnection;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Sends project's dependencies to Gemnasium
 */
@Mojo(name = "send-dependencies", requiresDependencyResolution = ResolutionScope.TEST)
public class SendDependenciesMojo extends AbstractMainMojo {

    public static final String DEPENDENCY_FILE_NAME = "gemnasium-maven-plugin.json";

    public void execute() throws MojoExecutionException {
        super.execute();
        sendDependencies();
    }

    public void sendDependencies() throws MojoExecutionException {

        String content = new String();
        try {
            ArrayNode jsonDependencies = ProjectsUtils.getJsonDependencies(getProjectDependencies());
            content = ProjectsUtils.getDependencyFileContent(jsonDependencies);
        } catch (JsonProcessingException e) {
            throw new MojoExecutionException("send-dependencies failed, can't get project dependencies", e);
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("path", DEPENDENCY_FILE_NAME);
        jsonNode.put("content", content);
        arrayNode.add(jsonNode);
        String requestBody = arrayNode.toString();

        URL url;

        try {
            url = new URL(config.getApiBaseUrl() + "/projects/" + config.getProjectSlug() + "/dependency_files");
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(
                    "send-dependencies failed, invalid parameters baseUrl or teamSlug, can't forge URL");
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

            // TODO: Try to fetch branch and revision headers from git if not provided
            if (config.getProjectBranch() != null && !config.getProjectBranch().isEmpty()) {
                conn.setRequestProperty("X-Gms-Branch", config.getProjectBranch());
            }
            if (config.getProjectRevision() != null && !config.getProjectRevision().isEmpty()) {
                conn.setRequestProperty("X-Gms-Revision", config.getProjectRevision());
            }

            os = conn.getOutputStream();

            os.write(requestBody.getBytes("UTF-8"));
            os.close();
        } catch (IOException e) {
            throw new MojoExecutionException("send-dependencies failed, can't connect to Gemnasium API", e);
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
                            "send-dependencies failed, API Error: " + node.get("message").asText());
                }
            } catch (IOException e) {
                throw new MojoExecutionException("send-dependencies failed, API Error", e);
            }
        }

        // Parses JSON response to find the created commit's sha
        JsonNode node;
        try {
            node = mapper.readTree(is);
        } catch (IOException ioe) {
            throw new MojoExecutionException("send-dependencies failed, malformed API response");
        }

        if (node.get("commit_sha") == null || node.get("commit_sha").asText().isEmpty()) {
            throw new MojoExecutionException("send-dependencies failed, no new commit was returned by the API");
        }

        String commitSha = node.get("commit_sha").asText();
        String projectUrl = config.getUIBaseUrl() + "/projects/" + config.getProjectSlug() + "/commits/" + commitSha;
        getLog().info(
                "Your project's dependencies have been successfully sent to Gemnasium and a new revision has been created:");
        getLog().info(projectUrl);
    }
}
