package it.uniroma2.ispw.ciboamico.persistence.impl.demo;

import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DAO Demo in-memory per BuonoPromozionale

public class DemoBuonoDAO implements BuonoDAO {

    private final Map<String, BuonoPromozionale> perCodice = new HashMap<>();

    @Override
    public BuonoPromozionale findByCodice(String codice) {
        return perCodice.get(codice);
    }

    @Override
    public List<BuonoPromozionale> findByVenditoreEmail(String venditoreEmail) {
        return perCodice.values().stream()
                .filter(b -> b.getVenditore() != null
                        && b.getVenditore().getUtente() != null
                        && b.getVenditore().getUtente().getEmail().equals(venditoreEmail))
                .toList();
    }

    @Override
    public BuonoPromozionale save(BuonoPromozionale buono) {
        perCodice.put(buono.getCodice(), buono);
        return buono;
    }
}
