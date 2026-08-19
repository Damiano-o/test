package it.uniroma2.ispw.ciboamico.persistence.impl.demo;

import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.ProdottoInventario;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO Demo in-memory per prodotti marketplace e inventario domestico.
 * Stato di istanza: ogni factory ha il proprio spazio dati.
 */
public class DemoProdottoDAO implements ProdottoDAO {

    private final Map<Long, Prodotto> catalogo = new HashMap<>();
    private final Map<String, List<ProdottoInventario>> inventari = new HashMap<>();
    private long nextId = 1;

    @Override
    public List<Prodotto> findAll() { return new ArrayList<>(catalogo.values()); }

    @Override
    public Prodotto findById(Long id) {
        return catalogo.values().stream()
                .filter(p -> p.getNome().hashCode() == id)
                .findFirst().orElse(null);
    }

    @Override
    public Prodotto save(Prodotto prodotto) {
        catalogo.put(nextId++, prodotto);
        return prodotto;
    }

    @Override
    public Prodotto update(Prodotto prodotto) {
        int id = prodotto.getNome().hashCode();
        catalogo.entrySet().stream()
                .filter(e -> e.getValue().getNome().hashCode() == id)
                .findFirst()
                .ifPresent(e -> catalogo.put(e.getKey(), prodotto));
        return prodotto;
    }

    @Override
    public List<ProdottoInventario> findInventario(String utenteEmail) {
        return new ArrayList<>(inventari.getOrDefault(utenteEmail, new ArrayList<>()));
    }

    @Override
    public ProdottoInventario saveInventario(String utenteEmail, ProdottoInventario prodotto) {
        inventari.computeIfAbsent(utenteEmail, k -> new ArrayList<>()).add(prodotto);
        return prodotto;
    }
}
