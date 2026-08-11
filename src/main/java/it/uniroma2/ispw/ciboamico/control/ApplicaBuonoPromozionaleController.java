package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.entity.VoceOrdine;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.pattern.factory.OrdineLazyFactory;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

/**
 * Controller dello Use Case estensione "Applica Buono Promozionale" (4a
 * di UC-04 Ordina un Prodotto).
 *
 * <p>Separa la logica di validazione/ applicazione di un buono promozionale
 * dal controller principale di UC-04, rendendo esplicita l'estensione nel
 * modello dei casi d'uso. Il controller principale lo istanzia on-demand
 * (GRASP: Low Coupling), seguendo la stessa strategia del pattern di
 * riferimento. Conversione Bean &lt;-&gt; Entity interna, scambio con la
 * boundary limitato ai Bean (BCE).</p>
 */
public class ApplicaBuonoPromozionaleController {

    private final BuonoDAO buonoDAO;
    private final ProdottoDAO prodottoDAO;
    private final UtenteDAO utenteDAO;

    /**
     * Costruttore principale: riceve la {@link DAOFactory} attiva (testabile).
     *
     * @param factory factory di persistenza da cui risolvere i DAO
     */
    public ApplicaBuonoPromozionaleController(DAOFactory factory) {
        this.buonoDAO = factory.getBuonoDAO();
        this.prodottoDAO = factory.getProdottoDAO();
        this.utenteDAO = factory.getUtenteDAO();
    }

    /** Costruttore no-arg: factory risolta dal gestore della modalità. */
    public ApplicaBuonoPromozionaleController() {
        this(it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager.getInstance().getDAOFactory());
    }

    /**
     * Applica un buono promozionale all'ordine (estensione 4a) e aggiorna
     * l'ordine in corso in {@link SessionManager} con il totale scontato.
     *
     * @param codiceBuono  codice inserito dall'utente
     * @param nomeProdotto prodotto selezionato
     * @param utente       utente autenticato
     * @return OrdineBean con totale aggiornato dopo lo sconto
     */
    public OrdineBean applicaBuono(String codiceBuono, String nomeProdotto, UtenteBean utente)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        if (codiceBuono == null || codiceBuono.isBlank()) {
            throw new BusinessValidationException("Inserisci un codice buono.");
        }
        OrdineBean bean = new OrdineBean();
        bean.setNomeProdotto(nomeProdotto);
        OrdineBean ris = applicaBuonoPromozionale(codiceBuono, bean, utente);
        SessionManager.getInstance().setOrdineInCorso(ris);
        return ris;
    }

    /**
     * Estensione 4a: applica un buono valido all'ordine corrente.
     *
     * @param codiceBuono codice del buono da applicare
     * @param bean        ordine in costruzione su cui applicare lo sconto
     * @param utente      utente autenticato
     * @return OrdineBean con il totale ricalcolato dopo lo sconto
     */
    public OrdineBean applicaBuonoPromozionale(String codiceBuono, OrdineBean bean, UtenteBean utente)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        if (codiceBuono == null || codiceBuono.isBlank()) {
            throw new BusinessValidationException("Codice buono mancante");
        }
        if (utente == null) {
            throw new IllegalStateException("Utente non autenticato");
        }
        bean.validate();

        BuonoPromozionale buono = buonoDAO.findByCodice(codiceBuono);
        if (buono == null) {
            throw new BusinessValidationException("Buono promozionale non valido");
        }
        if (!buono.isValido()) {
            throw new BusinessValidationException("Buono promozionale scaduto o non ancora attivo");
        }
        if (utente.getEmail() != null && haGiaUsatoBuono(utente.getEmail(), codiceBuono)) {
            throw new BusinessValidationException("Buono già utilizzato da questo utente");
        }

        Prodotto prodotto = prodottoDAO.findByNome(bean.getNomeProdotto());
        if (prodotto == null || prodotto.getVenditore() == null
                || prodotto.getVenditore().getUtente() == null) {
            throw new IllegalStateException("Prodotto o venditore non risolvibile");
        }
        String emailVenditoreBuono = buono.getVenditore().getUtente() != null
                ? buono.getVenditore().getUtente().getEmail()
                : buono.getVenditore().getRecapito();
        String emailVenditoreOrdine = prodotto.getVenditore().getUtente().getEmail();
        if (!emailVenditoreBuono.equals(emailVenditoreOrdine)) {
            throw new BusinessValidationException(
                    "Il buono promozionale non appartiene al venditore di questo prodotto");
        }

        Utente compratore = new Utente(utente.getUsername(), utente.getEmail(), "");
        Utente venditore = prodotto.getVenditore().getUtente();
        Ordine ordine = OrdineLazyFactory.getInstance().newOrdine(compratore, venditore);
        ordine.aggiungiVoce(new VoceOrdine(prodotto, 1));
        ordine.applicaBuono(buono);

        if (utente.getEmail() != null) {
            Utente persona = utenteDAO.findByEmail(utente.getEmail());
            if (persona != null) {
                persona.registraBuonoUtilizzato(buono.getCodice());
                utenteDAO.save(persona);
            }
        }

        OrdineBean risultato = new OrdineBean();
        risultato.setIdOrdine(bean.getIdOrdine());
        risultato.setNomeProdotto(bean.getNomeProdotto());
        risultato.setTotale(ordine.getTotale());
        risultato.setCodiceBuono(buono.getCodice());
        return risultato;
    }

    private boolean haGiaUsatoBuono(String email, String codiceBuono)
            throws it.uniroma2.ispw.ciboamico.exception.DAOException {
        Utente u = utenteDAO.findByEmail(email);
        return u != null && u.haUsatoBuono(codiceBuono);
    }
}
