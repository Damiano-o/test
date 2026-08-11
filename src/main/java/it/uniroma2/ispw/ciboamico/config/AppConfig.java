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

    /** Default only se la risorsa configurazione manca (compatibilità test). */
    private static final String DEFAULT_SALT = "ciboamico-salt";

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

    /** Salt usato per l'hash SHA-256 delle password. */
    public String getSalt() {
        return props.getProperty(KEY_SALT, DEFAULT_SALT);
    }

    /** Proprietà generica con default. */
    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
