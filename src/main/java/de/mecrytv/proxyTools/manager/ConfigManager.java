package de.mecrytv.proxyTools.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigManager {
    private final Path filePath;
    private final String fileName;
    private final String resourcePath;
    private Map<String, Object> configData = new HashMap<>();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public ConfigManager(Path dataFolder, String fileName, String resourcePath) {
        this.filePath = dataFolder.resolve(fileName);
        this.fileName = fileName;
        this.resourcePath = resourcePath;
        init();
    }

    public ConfigManager(Path dataFolder, String fileName) {
        this(dataFolder, fileName, fileName);
    }

    private void init() {
        try {
            if (Files.notExists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            if (Files.notExists(filePath)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (in != null) {
                        Files.copy(in, filePath);
                    } else {
                        save();
                    }
                }
            }
            load();
        } catch (IOException e) {
            System.err.println("[ProxyTools] Fehler beim Initialisieren der Config: " + fileName);
            e.printStackTrace();
        }
    }

    public void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = configData;

        for (int i = 0; i < keys.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(keys[i], k -> new HashMap<String, Object>());
        }

        if (value == null) {
            current.remove(keys[keys.length - 1]);
        } else {
            current.put(keys[keys.length - 1], value);
        }
        save();
    }

    private Object get(String path) {
        String[] keys = path.split("\\.");
        Object current = configData;

        for (String key : keys) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(key);
        }
        return current;
    }

    public String getString(String path) {
        Object val = get(path);
        return (val != null) ? String.valueOf(val) : "";
    }

    public int getInt(String path) {
        Object val = get(path);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public List<Object> getList(String path) {
        Object val = get(path);
        if (val instanceof List) {
            return (List<Object>) val;
        }
        return new ArrayList<>();
    }

    public List<String> getStringList(String path) {
        List<Object> rawList = getList(path);
        List<String> stringList = new ArrayList<>();

        for (Object item : rawList) {
            stringList.add(String.valueOf(item));
        }
        return stringList;
    }

    public List<Integer> getIntList(String path) {
        List<Object> rawList = getList(path);
        List<Integer> intList = new ArrayList<>();

        for (Object item : rawList) {
            if (item instanceof Number) {
                intList.add(((Number) item).intValue());
            } else {
                try {
                    intList.add(Integer.parseInt(String.valueOf(item)));
                } catch (NumberFormatException ignored) {
                    // Ungültige Einträge werden übersprungen
                }
            }
        }
        return intList;
    }

    public boolean getBoolean(String path) {
        Object val = get(path);
        if (val instanceof Boolean) return (boolean) val;
        return false;
    }

    public boolean contains(String path) {
        return get(path) != null;
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            gson.toJson(configData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (Files.notExists(filePath)) return;

        java.nio.charset.Charset[] charsetsToTry = {
                StandardCharsets.UTF_8,
                java.nio.charset.Charset.forName("windows-1252"),
                StandardCharsets.ISO_8859_1,
                StandardCharsets.UTF_16
        };

        boolean success = false;
        Exception lastException = null;

        for (java.nio.charset.Charset charset : charsetsToTry) {
            try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
                Map<String, Object> loadedData = gson.fromJson(reader,
                        new TypeToken<Map<String, Object>>(){}.getType());

                if (loadedData != null) {
                    this.configData = loadedData;
                    success = true;
                    break;
                }
            } catch (Exception e) {
                lastException = e;
            }
        }

        if (!success) {
            System.err.println("[ProxyTools] KRITISCH: Die Datei " + fileName + " konnte mit keiner Kodierung geladen werden!");
            if (lastException != null) {
                System.err.println("[ProxyTools] Fehlermeldung: " + lastException.getMessage());
            }
        }
    }

    public void reload() {
        load();
    }
}