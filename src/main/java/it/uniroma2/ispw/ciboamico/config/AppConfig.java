package it.uniroma2.ispw.ciboamico.config;

import java.io.InputStream;
import java.util.Properties;

// Accesso centralizzato a config

public final class AppConfig {

    public static final String KEY_SALT = "ciboamico.salt";

    public static final String KEY_PERSISTENCE_TYPE = "persistence_type";

    public static final String KEY_DB_URL = "ciboamico.db.url";
    public static final String KEY_DB_USER = "ciboamico.db.user";
    public static final String KEY_DB_PASSWORD = "ciboamico.db.password";

    public static final String MODE_DEMO = "DEMO";
    public static final String MODE_FS = "FS";
    public static final String MODE_JDBC = "JDBC";

    private static final String DEFAULT_SALT = "ciboamico-salt";

    private static final String DEFAULT_PERSISTENCE_TYPE = MODE_DEMO;

    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/ciboamico";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "root";

    private final Properties props;

    private static AppConfig instance;

    private AppConfig() {
        this.props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            // risorsa assente o non valida: restano i default
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public String getSalt() {
        return props.getProperty(KEY_SALT, DEFAULT_SALT);
    }

    public String getPersistenceType() {
        return props.getProperty(KEY_PERSISTENCE_TYPE, DEFAULT_PERSISTENCE_TYPE);
    }

    public String getDbUrl() {
        return props.getProperty(KEY_DB_URL, DEFAULT_DB_URL);
    }

    public String getDbUser() {
        return props.getProperty(KEY_DB_USER, DEFAULT_DB_USER);
    }

    public String getDbPassword() {
        return props.getProperty(KEY_DB_PASSWORD, DEFAULT_DB_PASSWORD);
    }

    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
