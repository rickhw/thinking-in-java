package com.example.messageboard.docker;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify Docker-related configurations and files.
 */
class DockerBuildTest {

    @Test
    void testDockerfileExists() {
        File dockerfile = new File("Dockerfile");
        assertTrue(dockerfile.exists(), "Dockerfile should exist in the project root");
        assertTrue(dockerfile.isFile(), "Dockerfile should be a file");
    }

    @Test
    void testDockerfileProductionExists() {
        File dockerfileProd = new File("Dockerfile.prod");
        assertTrue(dockerfileProd.exists(), "Dockerfile.prod should exist in the project root");
        assertTrue(dockerfileProd.isFile(), "Dockerfile.prod should be a file");
    }

    @Test
    void testDockerIgnoreExists() {
        File dockerignore = new File(".dockerignore");
        assertTrue(dockerignore.exists(), ".dockerignore should exist in the project root");
        assertTrue(dockerignore.isFile(), ".dockerignore should be a file");
    }

    @Test
    void testDockerComposeExists() {
        File dockerCompose = new File("docker-compose.yml");
        assertTrue(dockerCompose.exists(), "docker-compose.yml should exist in the project root");
        assertTrue(dockerCompose.isFile(), "docker-compose.yml should be a file");
    }

    @Test
    void testBuildScriptExists() {
        File buildScript = new File("build-docker.sh");
        assertTrue(buildScript.exists(), "build-docker.sh should exist in the project root");
        assertTrue(buildScript.isFile(), "build-docker.sh should be a file");
        assertTrue(buildScript.canExecute(), "build-docker.sh should be executable");
    }

    @Test
    void testJavaVersionCompatibility() {
        // Verify that we're running on Java 17 or compatible version
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion, "Java version should be available");
        
        // Extract major version number
        String majorVersion = javaVersion.split("\\.")[0];
        int majorVersionInt = Integer.parseInt(majorVersion);
        
        assertTrue(majorVersionInt >= 17, "Java version should be 17 or higher for Docker compatibility");
    }
}