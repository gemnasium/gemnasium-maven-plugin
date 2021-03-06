package com.gemnasium;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Sends a ping request to Gemnasium API.
 */
@Mojo(name = "ping", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class PingMojo extends AbstractMainMojo {

    public void execute() throws MojoExecutionException {
        super.execute();
        sendPing();
    }

    private void sendPing() throws MojoExecutionException {
        try {
            URL url = new URL(config.getApiBaseUrl() + "/ping");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String input;
            while ((input = br.readLine()) != null) {
                getLog().info(input);
            }

            br.close();

        } catch (Exception e) {
            throw new MojoExecutionException("Ping failed, please check network connection and configuration.", e);
        }
    }
}
