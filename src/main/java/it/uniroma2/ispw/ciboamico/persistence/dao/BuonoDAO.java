package it.uniroma2.ispw.ciboamico.persistence.dao;

import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.exception.DAOException;

import java.util.List;

/**
 * Interfaccia DAO per BuonoPromozionale.
 * Implementata dalle tre concrete factory (Demo in-memory, File System CSV/JSON, JDBC)
 * tramite il pattern Abstract Factory.
 */
public interface BuonoDAO {

    /** Recupera il buono tramite il codice (lookup usato dall'estensione 4a). */
    BuonoPromozionale findByCodice(String codice) throws DAOException;

    /** Restituisce i buoni emessi da un dato venditore (per la gestione). */
    List<BuonoPromozionale> findByVenditoreEmail(String venditoreEmail) throws DAOException;

    /** Salva un buono (creazione). */
    BuonoPromozionale save(BuonoPromozionale buono) throws DAOException;
}
