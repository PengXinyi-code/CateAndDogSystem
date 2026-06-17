package com.fast.system.general.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ProjectPathUtils {
    private ProjectPathUtils() {
    }

    public static Path resolve(String path) {
        if (path == null || path.trim().isEmpty()) {
            return projectRoot();
        }

        Path configuredPath = Paths.get(path);
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize();
        }

        return projectRoot().resolve(configuredPath).normalize();
    }

    public static String resourceLocation(String path) {
        String uri = resolve(path).toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    private static Path projectRoot() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        Path candidate = current;
        while (candidate != null) {
            if (isProjectRoot(candidate)) {
                return candidate;
            }
            candidate = candidate.getParent();
        }

        if (current.getFileName() != null && "springboot".equalsIgnoreCase(current.getFileName().toString())) {
            return current.getParent();
        }

        return current;
    }

    private static boolean isProjectRoot(Path path) {
        return Files.exists(path.resolve("springboot").resolve("pom.xml"))
                && Files.exists(path.resolve("vue").resolve("package.json"));
    }
}
