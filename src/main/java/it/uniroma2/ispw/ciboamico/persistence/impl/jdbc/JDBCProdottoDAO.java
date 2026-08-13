package it.uniroma2.ispw.ciboamico.persistence.impl.jdbc;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.UnitaEnum;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO JDBC per Prodotto — PreparedStatement anti SQL-injection (NFR-02)

public class JDBCProdottoDAO implements ProdottoDAO {

    @Override
    public List<Prodotto> findAll() throws DAOException {
        String sql = "SELECT id, nome, prezzo, quantita_disponibile, scadenza, unita, "
                + "venditore_zona, venditore_recapito FROM prodotti "
                + "WHERE scadenza >= CURDATE()"; // BR-01
        List<Prodotto> risultati = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                risultati.add(mappa(rs));
            }
            return risultati;
        } catch (SQLException e) {
            throw new DAOException("Errore lettura prodotti", e);
        }
    }

    @Override
    public Prodotto findByNome(String nome) throws DAOException {
        String sql = "SELECT id, nome, prezzo, quantita_disponibile, scadenza, unita, "
                + "venditore_zona, venditore_recapito FROM prodotti WHERE nome = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mappa(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Errore ricerca prodotto per nome", e);
        }
    }

    @Override
    public Prodotto save(Prodotto prodotto) throws DAOException {
        String sql = "INSERT INTO prodotti (nome, prezzo, quantita_disponibile, scadenza, unita, "
                + "venditore_zona, venditore_recapito) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false);
            try {
                ps.setString(1, prodotto.getNome());
                ps.setDouble(2, prodotto.getPrezzo());
                ps.setInt(3, prodotto.getQuantitaDisponibile());
                ps.setDate(4, Date.valueOf(prodotto.getScadenza()));
                ps.setString(5, prodotto.getUnita().name());
                ps.setString(6, prodotto.getVenditore() != null ? prodotto.getVenditore().getZona() : null);
                ps.setString(7, prodotto.getVenditore() != null ? prodotto.getVenditore().getRecapito() : null);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException tex) {
                conn.rollback();
                throw tex;
            } finally {
                conn.setAutoCommit(true);
            }
            return prodotto;
        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio prodotto", e);
        }
    }

    @Override
    public Prodotto update(Prodotto prodotto) throws DAOException {
        String sql = "UPDATE prodotti SET prezzo = ?, quantita_disponibile = ?, "
                + "venditore_zona = ?, venditore_recapito = ? WHERE nome = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, prodotto.getPrezzo());
            ps.setInt(2, prodotto.getQuantitaDisponibile());
            ps.setString(3, prodotto.getVenditore() != null ? prodotto.getVenditore().getZona() : null);
            ps.setString(4, prodotto.getVenditore() != null ? prodotto.getVenditore().getRecapito() : null);
            ps.setString(5, prodotto.getNome());
            ps.executeUpdate();
            return prodotto;
        } catch (SQLException e) {
            throw new DAOException("Errore aggiornamento prodotto", e);
        }
    }

    
    private Prodotto mappa(ResultSet rs) throws SQLException, DAOException {
        RuoloVenditore venditore = new RuoloVenditore(
                rs.getString("venditore_zona"), rs.getString("venditore_recapito"));
        try {
            return new Prodotto(
                    rs.getString("nome"),
                    rs.getDouble("prezzo"),
                    rs.getInt("quantita_disponibile"),
                    rs.getDate("scadenza").toLocalDate(),
                    UnitaEnum.valueOf(rs.getString("unita")),
                    venditore);
        } catch (BusinessValidationException e) {
            throw new DAOException("Dati prodotto corrotti in persistenza: " + e.getMessage(), e);
        }
    }
}
