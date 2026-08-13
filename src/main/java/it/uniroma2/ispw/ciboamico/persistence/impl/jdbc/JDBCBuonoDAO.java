package it.uniroma2.ispw.ciboamico.persistence.impl.jdbc;

import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoStrategy;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoStrategyFactory;
import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO JDBC per BuonoPromozionale — tabella piatta (codice,

public class JDBCBuonoDAO implements BuonoDAO {

    private final UtenteDAO utenteDAO;

    public JDBCBuonoDAO(UtenteDAO utenteDAO) {
        this.utenteDAO = utenteDAO;
    }

    private RuoloVenditore venditoreDa(String email) throws DAOException {
        Utente u = utenteDAO.findByEmail(email);
        if (u == null) {
            throw new DAOException("Venditore non trovato: " + email);
        }
        RuoloVenditore rv = u.getRuolo(RuoloVenditore.class);
        if (rv == null) {
            throw new DAOException("L'utente " + email + " non è un venditore");
        }
        return rv;
    }

    private BuonoPromozionale aEntita(ResultSet rs) throws SQLException, DAOException {
        ScontoStrategy strategy = ScontoStrategyFactory.createStrategy(
                rs.getString("tipo_sconto"), rs.getDouble("valore_sconto"));
        try {
            return new BuonoPromozionale(
                    rs.getString("codice"),
                    venditoreDa(rs.getString("venditore_email")),
                    rs.getDate("data_inizio").toLocalDate(),
                    rs.getDate("data_scadenza").toLocalDate(),
                    strategy);
        } catch (BusinessValidationException e) {
            throw new DAOException("Buono persistito non valido", e);
        }
    }

    @Override
    public BuonoPromozionale findByCodice(String codice) throws DAOException {
        String sql = "SELECT codice, venditore_email, data_inizio, data_scadenza, "
                + "tipo_sconto, valore_sconto FROM buoni_promozionali WHERE codice = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codice);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? aEntita(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Errore lettura buono: " + codice, e);
        }
    }

    @Override
    public List<BuonoPromozionale> findByVenditoreEmail(String venditoreEmail) throws DAOException {
        String sql = "SELECT codice, venditore_email, data_inizio, data_scadenza, "
                + "tipo_sconto, valore_sconto FROM buoni_promozionali WHERE venditore_email = ?";
        List<BuonoPromozionale> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, venditoreEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(aEntita(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DAOException("Errore lettura buoni del venditore " + venditoreEmail, e);
        }
    }

    @Override
    public BuonoPromozionale save(BuonoPromozionale buono) throws DAOException {
        String venditoreEmail = buono.getVenditore().getUtente() != null
                ? buono.getVenditore().getUtente().getEmail()
                : buono.getVenditore().getRecapito();
        String sql = "INSERT INTO buoni_promozionali "
                + "(codice, venditore_email, data_inizio, data_scadenza, tipo_sconto, valore_sconto) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE data_inizio = VALUES(data_inizio), "
                + "data_scadenza = VALUES(data_scadenza), tipo_sconto = VALUES(tipo_sconto), "
                + "valore_sconto = VALUES(valore_sconto)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, buono.getCodice());
            ps.setString(2, venditoreEmail);
            ps.setDate(3, java.sql.Date.valueOf(buono.getDataInizio()));
            ps.setDate(4, java.sql.Date.valueOf(buono.getDataScadenza()));
            ps.setString(5, buono.getStrategiaSconto().getTipo());
            ps.setDouble(6, buono.getStrategiaSconto().getValore());
            ps.executeUpdate();
            return buono;
        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio buono: " + buono.getCodice(), e);
        }
    }
}
