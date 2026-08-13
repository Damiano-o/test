package it.uniroma2.ispw.ciboamico.control.ui;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.PaymentInfoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.control.facade.OrdinaProdottoFacade;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

/**
 * Controller di presentazione (UI) della schermata di pagamento (passo 6 +
 * estensione 6a di UC-04 Ordina Prodotto), condiviso da GUI e CLI.
 *
 * <p>Coordina l'interazione tra
 * la {@code PaymentView} e il controller applicativo <b>disaccoppiato</b>:
 * raccoglie i dati carta (input utente), costruisce la mappa input → processi
 * di business (via {@link OrdinaProdottoFacade}) e restituisce il risultato
 * pronto per la presentazione. La view resta un puro layout.</p>
 *
 * <p>Non dipende da JavaFX: è testabile in isolamento e riusabile sia dalla
 * vista grafica sia da quella testuale (CLI). Se la view evolve, si tocca solo
 * questo controller di presentazione, non il controller applicativo.</p>
 */
public class PaymentUIController {

    private final OrdinaProdottoFacade facade;

    public PaymentUIController(DAOFactory factory) {
        this.facade = new OrdinaProdottoFacade(factory);
    }

    /** Costruttore di comodo per la boundary: factory attiva di runtime. */
    public PaymentUIController() {
        this.facade = new OrdinaProdottoFacade();
    }

    /**
     * Mappa l'azione "paga": converte i dati carta (formato esterno) nel
     * {@link PaymentInfoBean} (formato interno), poi delega l'autorizzazione
     * al controller applicativo via Facade. La conversione esterno→interno è
     * responsabilità del controller di presentazione.
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
            throws BusinessValidationException, DAOException {
        if (ordine == null) {
            throw new BusinessValidationException(
                    UserErrorMessagesEnum.ORDINE_MANCANTE_MSG.message,
                    ExceptionMessagesEnum.ORDINE_MANCANTE.message,
                    "ERR-ORDINE-MANCANTE");
        }
        // Conversione formati esterno→interno incapsulata nel bean (DRY).
        PaymentInfoBean payment = PaymentInfoBean.fromCardData(
                numeroCarta, intestatario, scadenza, cvv, ordine.getTotale());
        return facade.processaPagamento(ordine, utente, payment);
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
