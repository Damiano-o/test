package it.uniroma2.ispw.ciboamico.persistence.dao;

import it.uniroma2.ispw.ciboamico.entity.Prodotto;

import java.util.List;

/**
 * Interfaccia DAO per Prodotto (marketplace locale, UC-04).
 */
public interface ProdottoDAO {

    List<Prodotto> findAll();
    Prodotto findById(Long id);
    /** Lookup per nome (UC-04: la boundary seleziona il prodotto dal catalogo). */
    Prodotto findByNome(String nome);
    Prodotto save(Prodotto prodotto);
    Prodotto update(Prodotto prodotto);
}