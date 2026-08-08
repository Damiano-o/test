package it.uniroma2.ispw.ciboamico.persistence.factory;

import it.uniroma2.ispw.ciboamico.entity.*;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoBuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoOrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoUtenteDAO;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoPercentualeStrategy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;

/**
 * Factory DEMO: DAO in-memory con dati seed (utenti, prodotti, ricette).
 * Riutilizza le stesse istanze DAO: lo spazio dati è condiviso tra chiamate
 * della stessa factory (ma isolato tra factory diverse — test indipendenti).
 * Il seed rende l'applicazione utilizzabile in modalità demo senza DB.
 */
public class DemoDAOFactory extends DAOFactory {

    private final UtenteDAO utenteDAO = new DemoUtenteDAO();
    private final ProdottoDAO prodottoDAO = new DemoProdottoDAO();
    private final OrdineDAO ordineDAO = new DemoOrdineDAO();
    private final BuonoDAO buonoDAO = new DemoBuonoDAO();
    private boolean seeded;

    @Override
    public BuonoDAO getBuonoDAO() { return buonoDAO; }

    /**
     * Carica dati dimostrativi (chiamata dal bootstrap in modalità DEMO).
     * Idempotente: una sola esecuzione anche se invocata più volte
     * (riavvio della scena, doppio start, test), così i dati non duplicano.
     */
    public synchronized void seedDemoData() {
        if (seeded) {
            return;
        }
        try {
            seed();
            seeded = true;
        } catch (BusinessValidationException e) {
            throw new IllegalStateException("Seed demo corrotto", e);
        }
    }

    /** Carica dati demo dello scope UC-04: 2 utenti (Cliente, Venditore), prodotti, buono. */
    private void seed() throws BusinessValidationException {
        // Cliente che ordina
        Utente mario = new Utente("Mario", "mario@cibo.it", hash("password123"));
        mario.aggiungiRuolo(new RuoloCliente());
        utenteDAO.save(mario);

        // Venditore approvato (dal marketplace locale)
        Utente marco = new Utente("Marco", "marco@cibo.it", hash("password123"));
        RuoloVenditore rv = new RuoloVenditore("RM", "marco@cibo.it");
        rv.setStato(StatoVenditoreEnum.APPROVATO);
        marco.aggiungiRuolo(rv);
        utenteDAO.save(marco);

        // Prodotti del venditore (marketplace)
        Prodotto miele = new Prodotto("Miele locale", 6.50, 20,
                LocalDate.now().plusMonths(6), UnitaEnum.PEZZI, rv);
        Prodotto pomodori = new Prodotto("Pomodori", 2.20, 50,
                LocalDate.now().plusDays(10), UnitaEnum.GRAMMI, rv);
        prodottoDAO.save(miele);
        prodottoDAO.save(pomodori);

        // Buono promozionale del venditore (-20% valido un mese, monouso)
        buonoDAO.save(new BuonoPromozionale("SALUTI20", rv,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
                new ScontoPercentualeStrategy(0.20)));
    }

    /** Hash SHA-256 con salt fisso — identico ad AutenticazioneController. */
    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(("ciboamico-salt" + password)
                    .getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public UtenteDAO getUtenteDAO() { return utenteDAO; }

    @Override
    public ProdottoDAO getProdottoDAO() { return prodottoDAO; }

    @Override
    public OrdineDAO getOrdineDAO() { return ordineDAO; }
}
