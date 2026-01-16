package com.reciclaje.config;

public class Config {
    public static String getOpenRouterKey() {
        return System.getenv("OPENROUTER_API_KEY");
    }

    public static String getReferer() {
        String ref = System.getenv("OPENROUTER_REFERER");
        return ref != null ? ref : "http://localhost";
    }

    public static String getTitle() {
        String title = System.getenv("OPENROUTER_TITLE");
        return title != null ? title : "Chatbot Educativo sobre Reciclaje";
    }

    // Persist simple app settings under user home: ~/.reciclaje/config.properties
    private static final String APP_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + ".reciclaje";
    private static final String CONFIG_FILE = APP_DIR + System.getProperty("file.separator") + "config.properties";

    public static String getSavedProvider() {
        java.util.Properties p = new java.util.Properties();
        java.io.File f = new java.io.File(CONFIG_FILE);
        if (!f.exists()) return null;
        try (java.io.FileInputStream in = new java.io.FileInputStream(f)) {
            p.load(in);
            return p.getProperty("provider");
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveProvider(String provider) {
        try {
            java.io.File dir = new java.io.File(APP_DIR);
            if (!dir.exists()) dir.mkdirs();
            java.util.Properties p = new java.util.Properties();
            java.io.File f = new java.io.File(CONFIG_FILE);
            if (f.exists()) {
                try (java.io.FileInputStream in = new java.io.FileInputStream(f)) { p.load(in); }
            }
            p.setProperty("provider", provider == null ? "" : provider);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(f)) { p.store(out, "reciclaje config"); }
        } catch (Exception ignored) {}
    }

    public static String getOpenAIKey() {
        java.util.Properties p = new java.util.Properties();
        java.io.File f = new java.io.File(CONFIG_FILE);
        if (!f.exists()) return null;
        try (java.io.FileInputStream in = new java.io.FileInputStream(f)) {
            p.load(in);
            String v = p.getProperty("OPENAI_API_KEY");
            return (v != null && !v.isBlank()) ? v : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveOpenAIKey(String key) {
        try {
            java.io.File dir = new java.io.File(APP_DIR);
            if (!dir.exists()) dir.mkdirs();
            java.util.Properties p = new java.util.Properties();
            java.io.File f = new java.io.File(CONFIG_FILE);
            if (f.exists()) {
                try (java.io.FileInputStream in = new java.io.FileInputStream(f)) { p.load(in); }
            }
            p.setProperty("OPENAI_API_KEY", key == null ? "" : key);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(f)) { p.store(out, "reciclaje config"); }
        } catch (Exception ignored) {}
    }
}