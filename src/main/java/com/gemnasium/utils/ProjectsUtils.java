package com.gemnasium.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.artifact.Artifact;

/**
 * Utils for projects.
 */
public class ProjectsUtils {

    public static final String DEPENDENCY_FILE_FORMAT_VERSION = "1.0";

    public static String getBasename(String name) {
        return name.replaceAll("[^A-Za-z0-9._-]", "-");
    }

    public static String getDependencyFileContent(ArrayNode jsonDependencies) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("version", DEPENDENCY_FILE_FORMAT_VERSION);
        jsonNode.set("dependencies", jsonDependencies);

        String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        return Base64.getEncoder().encodeToString((prettyJson).getBytes(StandardCharsets.UTF_8));
    }

    public static List<Artifact> getFilteredDependencies(List<Artifact> artifacts, String ignoredScopes) {
        if (ignoredScopes == null || ignoredScopes.isEmpty()) {
            return artifacts;
        }
        List<String> ignoredScopesList = Arrays.asList(ignoredScopes.split("\\s*,\\s*"));

        List<Artifact> filteredDependencies = new ArrayList<Artifact>();
        for (Artifact art : artifacts) {
            if (!ignoredScopesList.contains(art.getScope())) {
                filteredDependencies.add(art);
            }
        }
        return filteredDependencies;
    }

    public static ArrayNode getJsonDependencies(List<Artifact> artifacts) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        for (Artifact art : artifacts) {
            arrayNode.add(depToJsonNode(mapper, art));
        }
        return arrayNode;
    }

    private static ObjectNode depToJsonNode(ObjectMapper mapper, Artifact art) {
        String parents = getDependencyParents(new ArrayList<String>(art.getDependencyTrail()));

        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("name", art.getGroupId() + ":" + art.getArtifactId());
        jsonNode.put("version", art.getVersion());
        jsonNode.put("scope", art.getScope());
        jsonNode.put("transitive", !parents.isEmpty());
        jsonNode.put("parents", parents);

        return jsonNode;
    }

    private static String getDependencyParents(List<String> trail) {
        List<String> ancestors;
        try {
            // Drop the project artifact (first element of the dependencyTrail)
            // Drop the current dependency artifact (last element of the dependencyTrail)
            // trail.remove(0);
            // trail.remove(trail.size() - 1);
            ancestors = trail.subList(1, trail.size() - 1);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }

        List<String> parents = new ArrayList<String>();
        for (String gav : ancestors) {
            String[] items = gav.split(":");
            parents.add(items[0] + ":" + items[1]);
        }
        return String.join(", ", parents);
    }

}
