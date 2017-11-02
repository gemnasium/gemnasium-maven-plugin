package com.gemnasium.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Utils for authentication.
 */
public class AuthUtils {

    public static String getEncodedBasicToken(String apiKey) throws MojoExecutionException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new MojoExecutionException("please provide your Gemnasium apiKey");
        }
        return Base64
            .getEncoder()
            .encodeToString(("X:" + apiKey).getBytes(StandardCharsets.UTF_8));
    }
}
