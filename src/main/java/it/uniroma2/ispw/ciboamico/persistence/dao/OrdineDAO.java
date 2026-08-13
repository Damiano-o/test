package it.uniroma2.ispw.ciboamico.persistence.dao;

import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.exception.DAOException;

import java.util.List;

// Interfaccia DAO per Ordine

public interface OrdineDAO {

    Ordine save(Ordine ordine) throws DAOException;
    Ordine findById(Long id) throws DAOException;
    List<Ordine> findByVenditore(String venditoreEmail) throws DAOException;
    List<Ordine> findByCompratore(String compratoreEmail) throws DAOException;

    // possiede i dati di persistenza e genera gli identificativi)

    long getNextId() throws DAOException;
}
