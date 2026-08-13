package it.uniroma2.ispw.ciboamico.persistence.dao;

import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.exception.DAOException;

import java.util.List;

// Interfaccia DAO per BuonoPromozionale

public interface BuonoDAO {

    BuonoPromozionale findByCodice(String codice) throws DAOException;

    List<BuonoPromozionale> findByVenditoreEmail(String venditoreEmail) throws DAOException;

    BuonoPromozionale save(BuonoPromozionale buono) throws DAOException;
}
