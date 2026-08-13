package it.uniroma2.ispw.ciboamico.persistence.impl.jdbc;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.StatoOrdineEnum;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO JDBC per Ordine — salvataggio stato e totale

public class JDBCOrdineDAO implements OrdineDAO {

    private static final String COL_COMPRATORE = "compratore_email";
    private static final String COL_VENDITORE = "venditore_email";

    @Override
    public long getNextId() throws DAOException {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM ordini";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 1L;
        } catch (SQLException e) {
            throw new DAOException("Errore generazione id ordine", e);
        }
    }

    @Override
    public Ordine save(Ordine ordine) throws DAOException {
        String sql = "INSERT INTO ordini (id, compratore_email, venditore_email, stato, totale) "
                + "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE stato = VALUES(stato), totale = VALUES(totale)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                ps.setLong(1, ordine.getIdOrdine());
                ps.setString(2, ordine.getCompratore().getEmail());
                ps.setString(3, ordine.getVenditore().getEmail());
                ps.setString(4, ordine.getStato().name());
                ps.setDouble(5, ordine.getTotale());
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return ordine;
        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio ordine", e);
        }
    }

    @Override
    public Ordine findById(Long id) throws DAOException {
        String sql = "SELECT id, compratore_email, venditore_email, stato, totale FROM ordini WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mappaOrdine(rs.getLong("id"),
                            rs.getString(COL_COMPRATORE),
                            rs.getString(COL_VENDITORE),
                            rs.getString("stato"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Errore ricerca ordine", e);
        }
    }

    @Override
    public List<Ordine> findByVenditore(String venditoreEmail) throws DAOException {
        String sql = "SELECT id, compratore_email, venditore_email, stato, totale "
                + "FROM ordini WHERE venditore_email = ?";
        List<Ordine> risultati = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, venditoreEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    risultati.add(mappaOrdine(rs.getLong("id"),
                            rs.getString(COL_COMPRATORE),
                            rs.getString(COL_VENDITORE),
                            rs.getString("stato")));
                }
            }
            return risultati;
        } catch (SQLException e) {
            throw new DAOException("Errore lettura ordini venditore", e);
        }
    }

    @Override
    public List<Ordine> findByCompratore(String compratoreEmail) throws DAOException {
        String sql = "SELECT id, compratore_email, venditore_email, stato, totale "
                + "FROM ordini WHERE compratore_email = ?";
        List<Ordine> risultati = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, compratoreEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    risultati.add(mappaOrdine(rs.getLong("id"),
                            rs.getString(COL_COMPRATORE),
                            rs.getString(COL_VENDITORE),
                            rs.getString("stato")));
                }
            }
            return risultati;
        } catch (SQLException e) {
            throw new DAOException("Errore lettura ordini compratore", e);
        }
    }

    private Ordine mappaOrdine(Long id, String compratoreEmail, String venditoreEmail, String stato)
            throws DAOException {
        try {
            Ordine o = new Ordine(id,
                    new Utente("c", compratoreEmail, ""),
                    new Utente("v", venditoreEmail, ""));
            o.ripristinaStato(StatoOrdineEnum.valueOf(stato));
            return o;
        } catch (BusinessValidationException e) {
            throw new DAOException("Dati ordine corrotti in persistenza: " + e.getMessage(), e);
        }
    }
}
