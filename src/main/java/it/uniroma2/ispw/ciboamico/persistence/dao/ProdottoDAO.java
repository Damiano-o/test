package it.uniroma2.ispw.ciboamico.persistence.dao;

import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.exception.DAOException;

import java.util.List;

/**
 * Interfaccia DAO per Prodotto (marketplace locale, UC-04).
 */
public interface ProdottoDAO {

    List<Prodotto> findAll() throws DAOException;
    /** Lookup per nome (UC-04: la boundary seleziona il prodotto dal catalogo).
     *  Nel dominio CiboAmico il Prodotto si identifica per nome (univoco nel
     *  catalogo del venditore): il nome è la chiave identitaria, non un id
     *  surrogato. */
    Prodotto findByNome(String nome) throws DAOException;
    Prodotto save(Prodotto prodotto) throws DAOException;
    Prodotto update(Prodotto prodotto) throws DAOException;
}