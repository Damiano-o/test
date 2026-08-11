package it.uniroma2.ispw.ciboamico.control.graphic;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.facade.OrdinaProdottoFacade;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

/**
 * Controller grafico della schermata di pagamento (passo 6 + estensione 6a
 * di UC-04 Ordina Prodotto).
 *
 * <p>Come da lezione sul controller grafico (MVC), coordina l'interazione tra
 * la {@code PaymentView} e il controller applicativo <b>disaccoppiato</b>:
 * raccoglie i dati carta (input utente), costruisce la mappa input → processi
 * di business (via {@link OrdinaProdottoFacade}) e restituisce il risultato
 * pronto per la presentazione. La view resta un puro layout.</p>
 *
 * <p>Non dipende da JavaFX: è testabile in isolamento. Se la view evolve,
 * si tocca solo questo controller grafico, non il controller applicativo.</p>
 */
public class PaymentGraphicController {

    private final OrdinaProdottoFacade facade;

    public PaymentGraphicController(DAOFactory factory) {
        this.facade = new OrdinaProdottoFacade(factory);
    }

    /** Costruttore di comodo per la boundary: factory attiva di runtime. */
    public PaymentGraphicController() {
        this.facade = new OrdinaProdottoFacade();
    }

    /**
     * Mappa l'azione "paga": autorizza l'addebito tramite il controller
     * applicativo (via Facade) e restituisce l'esito.
     *
     * @param ordine       ordine in checkout
     * @param utente       utente autenticato
     * @param numeroCarta  numero carta (input grezzo dalla view)
     * @param intestatario intestatario della carta
     * @param scadenza     scadenza della carta
     * @param cvv          codice di sicurezza
     * @return l'ordine risultante dopo l'autorizzazione
     */
    public OrdineBean paga(OrdineBean ordine, UtenteBean utente,
                           String numeroCarta, String intestatario,
                           String scadenza, String cvv)
            throws BusinessValidationException, it.uniroma2.ispw.ciboamico.exception.DAOException {
        if (ordine == null) {
            throw new BusinessValidationException("Nessun ordine in checkout.");
        }
        return facade.processaPagamento(
                ordine, utente, numeroCarta, intestatario, scadenza, cvv);
    }

    /**
     * Formatta l'esito del pagamento andato a buon fine.
     *
     * @param risultato ordine risultante dalla transazione
     * @return stringa di riepilogo da mostrare nella view
     */
    public static String formattaEsitoPagamento(OrdineBean risultato) {
        return "Pagamento riuscito ✓ — ordine " + risultato.getStato()
                + ", totale " + String.format("%.2f EUR", risultato.getTotale());
    }
}
