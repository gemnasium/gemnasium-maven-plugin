package com.gemnasium;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gemnasium.utils.ProjectsUtils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * List project's dependencies
 */
@Mojo(name = "dump-dependencies", requiresDependencyResolution = ResolutionScope.TEST)
public class DumpDependenciesMojo extends AbstractMainMojo {

    public static final String DEPENDENCY_FILE_NAME = "gemnasium-maven-plugin.json";

    public void execute() throws MojoExecutionException {
        super.execute();
        listDependencies();
    }

    public void listDependencies() throws MojoExecutionException {
        ArrayNode jsonDependencies = ProjectsUtils.getJsonDependencies(getAllDependencies(),
                    getDirectDependencies());

        String output = jsonDependencies.toString();
        String filePath = config.getBaseDir() + "/" + DEPENDENCY_FILE_NAME;

        try {
            Files.write(Paths.get(filePath), output.getBytes(), StandardOpenOption.CREATE);
            getLog().info("Project's dependencies have been succesfully dumped into: " + filePath);
        } catch (IOException e) {
            getLog().info("Can't write project's dependencies into: " + filePath);
            e.printStackTrace();
        }
    }
}
