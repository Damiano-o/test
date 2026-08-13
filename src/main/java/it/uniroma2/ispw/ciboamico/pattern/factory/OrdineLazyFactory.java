package it.uniroma2.ispw.ciboamico.pattern.factory;

import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory con inizializzazione lazy e cache degli ordini creati nella sessione.
 * L'identificativo viene richiesto al DAO, che conosce la propria strategia
 * di generazione degli id. L'istanza unica è gestita tramite un container
 * statico accesso in modo {@code synchronized} (thread-safe), con una
 * {configure(DAOFactory)} esplicita per poterla resettare tra le modalità
 * e nei test.
 */
public final class OrdineLazyFactory {

    private final OrdineDAO ordineDAO;
    private final List<Ordine> cacheOrdini = new ArrayList<>();

    private OrdineLazyFactory(DAOFactory factory) {
        this.ordineDAO = factory.getOrdineDAO();
    }

    private static class Container {
        private static OrdineLazyFactory INSTANCE;
    }

    /**
     * Configura la factory con la DAOFactory attiva (chiamata al bootstrap).
     * Il punto di configurazione è unico nel compositore {@code Runner}:
     * al riavvio (o cambio modalità) viene ricreata l'istanza con la factory
     * corrente. Separata da getInstance() per evitare doppia responsabilità
     * (PMD SingleMethodSingleton).
     */
    public static synchronized void configure(DAOFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("La DAOFactory di configurazione non può essere nulla.");
        }
        Container.INSTANCE = new OrdineLazyFactory(factory);
    }

    /** Istanza configurata (da usare dopo il bootstrap); fallisce se non è stata chiamata configure(). */
    public static synchronized OrdineLazyFactory getInstance() {
        if (Container.INSTANCE == null) {
            throw new IllegalStateException("OrdineLazyFactory non configurata: chiamare configure(DAOFactory) prima (bootstrap).");
        }
        return Container.INSTANCE;
    }

    /**
     * Crea un nuovo ordine in stato CREATED assegnando l'id dal DAO.
     * L'ordine viene aggiunto alla cache in RAM (non ancora persistito).
     */
    public Ordine newOrdine(Utente compratore, Utente venditore)
            throws BusinessValidationException, DAOException {
        long id = ordineDAO.getNextId();
        Ordine ordine = new Ordine(id, compratore, venditore);
        cacheOrdini.add(ordine);
        return ordine;
    }

    /** Ordini creati nella sessione corrente (cache in RAM). */
    public List<Ordine> getCacheOrdini() {
        return new ArrayList<>(cacheOrdini);
    }

    /** Resetta la cache e l'istanza (per test e cambio modalità). */
    public static synchronized void reset() {
        Container.INSTANCE = null;
    }
}
