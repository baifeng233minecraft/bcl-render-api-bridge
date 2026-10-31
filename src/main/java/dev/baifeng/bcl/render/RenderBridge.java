package dev.baifeng.bcl.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class RenderBridge implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        apply(FabricLoader.getInstance().getGameDir());
    }

    static void apply(Path game) {
        Path config = game.resolve("config").resolve("bcl-render-api.properties");
        if (!Files.isRegularFile(config)) return;
        try {
            Properties settings = new Properties();
            try (InputStream input = Files.newInputStream(config)) { settings.load(input); }
            String backend = settings.getProperty("backend", "").trim().toLowerCase(java.util.Locale.ROOT);
            if (!BackendOptions.valid(backend)) return;
            Path options = game.resolve("options.txt");
            if (!Files.isRegularFile(options)) {
                System.err.println("[BCL Render API Bridge] Waiting for Minecraft to create options.txt; retry next launch.");
                return;
            }
            String original = Files.readString(options, StandardCharsets.UTF_8);
            if (!BackendOptions.migrated(original)) {
                System.err.println("[BCL Render API Bridge] Waiting for Minecraft to migrate options.txt; retry next launch.");
                return;
            }
            String updated = BackendOptions.update(original, backend);
            if (updated.equals(original)) return;
            if (Files.isRegularFile(options)) {
                Path backup = game.resolve("options.txt.bcl-render-api.backup");
                if (!Files.exists(backup)) Files.copy(options, backup);
            }
            Path temporary = Files.createTempFile(game, ".bcl-render-api-", ".tmp");
            try {
                Files.writeString(temporary, updated, StandardCharsets.UTF_8);
                try { Files.move(temporary, options, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, options, StandardCopyOption.REPLACE_EXISTING); }
            } finally { Files.deleteIfExists(temporary); }
            System.out.println("[BCL Render API Bridge] Requested graphics backend: " + backend);
        } catch (IOException | SecurityException error) {
            // A settings write failure must never prevent Minecraft from starting.
            System.err.println("[BCL Render API Bridge] Could not apply graphics backend: " + error);
        }
    }
}
