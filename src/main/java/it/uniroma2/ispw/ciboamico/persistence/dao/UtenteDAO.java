package it.uniroma2.ispw.ciboamico.persistence.dao;

import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.DAOException;

// Interfaccia DAO per Utente — implementata da JDBC, FS e Demo

public interface UtenteDAO {

    Utente findByEmail(String email) throws DAOException;
    Utente save(Utente utente) throws DAOException;
}
