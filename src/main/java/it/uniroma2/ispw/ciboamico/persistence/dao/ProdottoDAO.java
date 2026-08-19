package it.uniroma2.ispw.ciboamico.persistence.dao;

import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.ProdottoInventario;

import java.util.List;

/**
 * Interfaccia DAO per Prodotto (marketplace) e ProdottoInventario (frigo).
 */
public interface ProdottoDAO {

    List<Prodotto> findAll();
    Prodotto findById(Long id);
    Prodotto save(Prodotto prodotto);
    Prodotto update(Prodotto prodotto);

    List<ProdottoInventario> findInventario(String utenteEmail);
    ProdottoInventario saveInventario(String utenteEmail, ProdottoInventario prodotto);
}
