package it.uniroma2.ispw.ciboamico.persistence.impl.demo;

import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;

import java.util.HashMap;
import java.util.Map;

// DAO Demo in-memory — milestone M1 (senza salvataggio)

public class DemoUtenteDAO implements UtenteDAO {

    private final Map<String, Utente> memoria = new HashMap<>();

    @Override
    public Utente findByEmail(String email) {
        return memoria.get(email);
    }

    @Override
    public Utente save(Utente utente) {
        memoria.put(utente.getEmail(), utente);
        return utente;
    }
}
