package cn.tesseract.bes;

import cn.tesseract.bes.server.ServerConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ConfigIO {
    private static final String FILE_NAME = "config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigIO() {
    }

    public static ServerConfig loadOrCreate() {
        Path path = Paths.get(FILE_NAME);
        if (Files.exists(path)) {
            try {
                return read(path);
            } catch (IOException ignored) {
            }
        }

        ServerConfig config = new ServerConfig();
        write(config, path);
        return config;
    }

    private static ServerConfig read(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            ServerConfig config = GSON.fromJson(reader, ServerConfig.class);
            if (config == null) {
                config = new ServerConfig();
            }
            config.normalize();
            return config;
        }
    }

    private static void write(ServerConfig config, Path path) {
        config.normalize();
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        } catch (IOException ignored) {
        }
    }
}

