package com.gemnasium.utils;

/**
 * Utils for projects.
 */
public class ProjectsUtils {

    public static String getBasename(String name) {
        return name.replaceAll("[^A-Za-z0-9._-]", "-");
    }
}
