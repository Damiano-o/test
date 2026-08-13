package it.uniroma2.ispw.ciboamico.persistence.impl.demo;

import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DAO Demo in-memory per ordini

public class DemoOrdineDAO implements OrdineDAO {

    private final Map<Long, Ordine> memoria = new HashMap<>();
    private long nextId = 1;

    @Override
    public long getNextId() { return nextId++; }

    @Override
    public Ordine save(Ordine ordine) {
        memoria.put(ordine.getIdOrdine(), ordine);
        return ordine;
    }

    @Override
    public Ordine findById(Long id) { return memoria.get(id); }

    @Override
    public List<Ordine> findByVenditore(String venditoreEmail) {
        return memoria.values().stream()
                .filter(o -> o.getVenditore().getEmail().equals(venditoreEmail))
                .toList();
    }

    @Override
    public List<Ordine> findByCompratore(String compratoreEmail) {
        return memoria.values().stream()
                .filter(o -> o.getCompratore().getEmail().equals(compratoreEmail))
                .toList();
    }
}
