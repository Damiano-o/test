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

// Controller di presentazione (UI) della schermata di pagamento (passo 6 + estensione 6a...

public class PaymentUIController {

    private final OrdinaProdottoFacade facade;

    public PaymentUIController(DAOFactory factory) {
        this.facade = new OrdinaProdottoFacade(factory);
    }

    public PaymentUIController() {
        this.facade = new OrdinaProdottoFacade();
    }

    // Mappa l'azione "paga": converte i dati carta (formato esterno) nel PaymentInfoBean (for...

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

    // Formatta l'esito del pagamento andato a buon fine

    public static String formattaEsitoPagamento(OrdineBean risultato) {
        return "Pagamento riuscito ✓ — ordine " + risultato.getStato()
                + ", totale " + String.format(java.util.Locale.ROOT, "%.2f EUR", risultato.getTotale());
    }
}
