package dev.baifeng.bcl.render;

final class BackendOptions {
    private static final String KEY = "preferredGraphicsBackend:";

    private BackendOptions() {}

    static boolean valid(String backend) {
        return "default".equals(backend) || "opengl".equals(backend) || "vulkan".equals(backend);
    }

    static boolean migrated(String original) {
        if (original.isEmpty()) return true;
        for (String line : original.replace("\r\n", "\n").split("\n")) {
            if (line.startsWith("version:")) {
                try { return Integer.parseInt(line.substring(8).trim()) >= 4892; }
                catch (NumberFormatException ignored) { return false; }
            }
        }
        return false;
    }

    static String update(String original, String backend) {
        if (!valid(backend)) throw new IllegalArgumentException("Unsupported graphics backend");
        String newline = original.contains("\r\n") ? "\r\n" : "\n";
        String[] lines = original.replace("\r\n", "\n").split("\n", -1);
        String replacement = KEY + '"' + backend + '"';
        StringBuilder result = new StringBuilder();
        boolean replaced = false;
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            if (line.startsWith(KEY)) {
                if (replaced) continue;
                line = replacement;
                replaced = true;
            }
            if (index > 0 && result.length() > 0 && result.charAt(result.length() - 1) != '\n') result.append(newline);
            result.append(line);
        }
        if (!replaced) {
            if (result.length() > 0 && result.charAt(result.length() - 1) != '\n') result.append(newline);
            result.append(replacement).append(newline);
        }
        return result.toString();
    }
}
