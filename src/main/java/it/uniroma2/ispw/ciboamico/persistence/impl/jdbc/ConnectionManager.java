package it.uniroma2.ispw.ciboamico.persistence.impl.jdbc;

import it.uniroma2.ispw.ciboamico.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestione centralizzata della connessione JDBC.
 * Le credenziali NON sono hardcoded nel bytecode: vengono lette dal file
 * {@code config.properties} tramite {@link AppConfig} (unico punto d'accesso
 * alla configurazione, DRY), con fallback a valori di sviluppo locali.
 * (L'intercambiabilità DEMO/FS/JDBC è già mediata da {@code DAOFactory}.)
 */
public final class ConnectionManager {

    private static final String URL = AppConfig.getInstance().getDbUrl();
    private static final String USER = AppConfig.getInstance().getDbUser();
    private static final String PASSWORD = AppConfig.getInstance().getDbPassword();

    private ConnectionManager() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
