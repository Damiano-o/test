package it.uniroma2.ispw.ciboamico.persistence.impl.demo;

import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DAO Demo in-memory per prodotti marketplace e inventario

public class DemoProdottoDAO implements ProdottoDAO {

    private final Map<Long, Prodotto> catalogo = new HashMap<>();
    private long nextId = 1;

    @Override
    public List<Prodotto> findAll() { return new ArrayList<>(catalogo.values()); }

    @Override
    public Prodotto findByNome(String nome) {
        return catalogo.values().stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst().orElse(null);
    }

    @Override
    public Prodotto save(Prodotto prodotto) {
        catalogo.put(nextId++, prodotto);
        return prodotto;
    }

    @Override
    public Prodotto update(Prodotto prodotto) {
        String nome = prodotto.getNome();
        catalogo.entrySet().stream()
                .filter(e -> e.getValue().getNome().equalsIgnoreCase(nome))
                .findFirst()
                .ifPresent(e -> catalogo.put(e.getKey(), prodotto));
        return prodotto;
    }
}
