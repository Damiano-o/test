package it.uniroma2.ispw.ciboamico.persistence.impl.jdbc;

import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;

import java.sql.*;

// DAO JDBC per Utente — PreparedStatement anti SQL-injection

public class JDBCUtenteDAO implements UtenteDAO {

    private Connection getConnection() throws SQLException {
        // Connessione centralizzata (config da config.properties via
        // fallback locale) — le credenziali NON sono hardcoded qui
        // (vedi ConnectionManager).
        return ConnectionManager.getConnection();
    }

    @Override
    public Utente findByEmail(String email) throws DAOException {
        String sql = "SELECT nome, email, password_hash FROM utenti WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Utente(rs.getString("nome"), rs.getString("email"),
                            rs.getString("password_hash"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Errore ricerca utente", e);
        }
    }

    @Override
    public Utente save(Utente utente) throws DAOException {
        String sql = "INSERT INTO utenti (nome, email, password_hash) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE nome = VALUES(nome)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                ps.setString(1, utente.getNome());
                ps.setString(2, utente.getEmail());
                ps.setString(3, utente.getPasswordHash());
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return utente;
        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio utente", e);
        }
    }
}
