package it.uniroma2.ispw.ciboamico.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Accesso centralizzato a {@code config.properties} (risorsa di progetto).
 *
 * <p>Carica le proprietà una sola volta (lazy, thread-safe con
 * {@code synchronized}) ed espone getter tipizzati. Rimuove dal codice i valori
 * sensibili hardcoded (es. il salt delle password, NFR-03): il segreto vive
 * solo nel file di configurazione, non più nel bytecode.</p>
 */
public final class AppConfig {

    /** Chiave della proprietà del salt password. */
    public static final String KEY_SALT = "ciboamico.salt";

    /** Chiave della modalità di persistenza (NFR-01). */
    public static final String KEY_PERSISTENCE_TYPE = "persistence_type";

    /** Chiavi delle credenziali JDBC/MySQL (NFR-02). */
    public static final String KEY_DB_URL = "ciboamico.db.url";
    public static final String KEY_DB_USER = "ciboamico.db.user";
    public static final String KEY_DB_PASSWORD = "ciboamico.db.password";

    /** Valori ammessi per la modalità di persistenza (NFR-01). */
    public static final String MODE_DEMO = "DEMO";
    public static final String MODE_FS = "FS";
    public static final String MODE_JDBC = "JDBC";

    /** Default only se la risorsa configurazione manca (compatibilità test). */
    private static final String DEFAULT_SALT = "ciboamico-salt";

    /** Modalità di persistenza di default (DEMO in-memory, milestone M1). */
    private static final String DEFAULT_PERSISTENCE_TYPE = MODE_DEMO;

    /** Credenziali JDBC di sviluppo (fallback se assenti nel file). */
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

    /** Istanza unica (lazy, thread-safe via synchronized). */
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /** Salt usato per l'hash SHA-256 delle password (NFR-03). */
    public String getSalt() {
        return props.getProperty(KEY_SALT, DEFAULT_SALT);
    }

    /** Modalità di persistenza attiva (NFR-01): DEMO, FS o JDBC. */
    public String getPersistenceType() {
        return props.getProperty(KEY_PERSISTENCE_TYPE, DEFAULT_PERSISTENCE_TYPE);
    }

    /** URL JDBC del database (NFR-02). */
    public String getDbUrl() {
        return props.getProperty(KEY_DB_URL, DEFAULT_DB_URL);
    }

    /** Utente JDBC del database (NFR-02). */
    public String getDbUser() {
        return props.getProperty(KEY_DB_USER, DEFAULT_DB_USER);
    }

    /** Password JDBC del database (NFR-02). */
    public String getDbPassword() {
        return props.getProperty(KEY_DB_PASSWORD, DEFAULT_DB_PASSWORD);
    }

    /** Proprietà generica con default. */
    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
