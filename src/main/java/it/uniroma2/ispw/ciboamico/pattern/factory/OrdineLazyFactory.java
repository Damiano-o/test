package it.uniroma2.ispw.ciboamico.pattern.factory;

import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.util.ArrayList;
import java.util.List;

// Factory con inizializzazione lazy e cache degli ordini creati

public final class OrdineLazyFactory {

    private final OrdineDAO ordineDAO;
    private final List<Ordine> cacheOrdini = new ArrayList<>();

    private OrdineLazyFactory(DAOFactory factory) {
        this.ordineDAO = factory.getOrdineDAO();
    }

    private static class Container {
        private static OrdineLazyFactory INSTANCE;
    }

    // Configura la factory con la DAOFactory attiva (chiamata al

    public static synchronized void configure(DAOFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("La DAOFactory di configurazione non può essere nulla.");
        }
        Container.INSTANCE = new OrdineLazyFactory(factory);
    }

    public static synchronized OrdineLazyFactory getInstance() {
        if (Container.INSTANCE == null) {
            throw new IllegalStateException("OrdineLazyFactory non configurata: chiamare configure(DAOFactory) prima (bootstrap).");
        }
        return Container.INSTANCE;
    }

    // Crea un nuovo ordine in stato CREATED assegnando l'id dal DAO

    public Ordine newOrdine(Utente compratore, Utente venditore)
            throws BusinessValidationException, DAOException {
        long id = ordineDAO.getNextId();
        Ordine ordine = new Ordine(id, compratore, venditore);
        cacheOrdini.add(ordine);
        return ordine;
    }

    public List<Ordine> getCacheOrdini() {
        return new ArrayList<>(cacheOrdini);
    }

    public static synchronized void reset() {
        Container.INSTANCE = null;
    }
}
