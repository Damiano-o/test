package it.uniroma2.ispw.ciboamico.persistence.factory;

import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.RuoloCliente;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.UnitaEnum;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoBuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoOrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.demo.DemoUtenteDAO;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoPercentualeStrategy;

import java.time.LocalDate;

// Factory DEMO: DAO in-memory con dati seed (utenti, prodotti,

public class DemoDAOFactory implements DAOFactory {

    private final UtenteDAO utenteDAO = new DemoUtenteDAO();
    private final ProdottoDAO prodottoDAO = new DemoProdottoDAO();
    private final OrdineDAO ordineDAO = new DemoOrdineDAO();
    private final BuonoDAO buonoDAO = new DemoBuonoDAO();
    private boolean seeded;

    @Override
    public BuonoDAO getBuonoDAO() { return buonoDAO; }

    // Carica dati dimostrativi (chiamata dal bootstrap in modalità

    public synchronized void seedDemoData() throws DAOException {
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

    private void seed() throws BusinessValidationException, DAOException {
        // Cliente che ordina
        Utente mario = new Utente("Mario", "mario@cibo.it", Utente.hashPassword("password123"));
        mario.aggiungiRuolo(new RuoloCliente());
        utenteDAO.save(mario);

        // Venditore approvato (dal marketplace locale)
        Utente marco = new Utente("Marco", "marco@cibo.it", Utente.hashPassword("password123"));
        RuoloVenditore rv = new RuoloVenditore("RM", "marco@cibo.it");
        rv.approva(); // BR-02: solo un venditore approvato pubblica prodotti
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

    @Override
    public UtenteDAO getUtenteDAO() { return utenteDAO; }

    @Override
    public ProdottoDAO getProdottoDAO() { return prodottoDAO; }

    @Override
    public OrdineDAO getOrdineDAO() { return ordineDAO; }
}
