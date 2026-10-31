package dev.baifeng.bcl.render;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

public final class BackendOptionsTest {
    public static void main(String[] args) throws Exception {
        check("gamma:1.0\npreferredGraphicsBackend:\"default\"\n", "vulkan", "gamma:1.0\npreferredGraphicsBackend:\"vulkan\"\n");
        check("gamma:1.0\r\n", "opengl", "gamma:1.0\r\npreferredGraphicsBackend:\"opengl\"\r\n");
        check("", "default", "preferredGraphicsBackend:\"default\"\n");
        check("preferredGraphicsBackend:\"default\"", "opengl", "preferredGraphicsBackend:\"opengl\"");
        if (BackendOptions.valid("metal")) throw new AssertionError("Metal is not a Minecraft graphics backend");
        if (BackendOptions.migrated("version:4891\n")) throw new AssertionError("Must preserve old options for migration");
        if (!BackendOptions.migrated("version:4892\n")) throw new AssertionError("Current options should be editable");
        Path game = Files.createTempDirectory("bcl-render-test-");
        try {
            Files.createDirectories(game.resolve("config"));
            Files.writeString(game.resolve("options.txt"), "version:4892\ngamma:1.0\n", StandardCharsets.UTF_8);
            Files.writeString(game.resolve("config/bcl-render-api.properties"), "backend=vulkan\n", StandardCharsets.UTF_8);
            RenderBridge.apply(game);
            String updated = Files.readString(game.resolve("options.txt"), StandardCharsets.UTF_8);
            if (!updated.contains("gamma:1.0") || !updated.contains("preferredGraphicsBackend:\"vulkan\"")) throw new AssertionError("Backend was not applied");
            String backup = Files.readString(game.resolve("options.txt.bcl-render-api.backup"), StandardCharsets.UTF_8);
            if (!backup.equals("version:4892\ngamma:1.0\n")) throw new AssertionError("Original options were not backed up");
            Files.writeString(game.resolve("config/bcl-render-api.properties"), "backend=metal\n", StandardCharsets.UTF_8);
            RenderBridge.apply(game);
            if (!Files.readString(game.resolve("options.txt")).equals(updated)) throw new AssertionError("Invalid backend changed options");
            Files.delete(game.resolve("options.txt"));
            Files.writeString(game.resolve("config/bcl-render-api.properties"), "backend=opengl\n", StandardCharsets.UTF_8);
            RenderBridge.apply(game);
            if (Files.exists(game.resolve("options.txt"))) throw new AssertionError("Bridge should wait for Minecraft to create options");
        } finally {
            try (var paths = Files.walk(game)) {
                for (Path file : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
            }
        }
        System.out.println("BackendOptions tests passed");
    }

    private static void check(String before, String backend, String expected) {
        String actual = BackendOptions.update(before, backend);
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }
}
