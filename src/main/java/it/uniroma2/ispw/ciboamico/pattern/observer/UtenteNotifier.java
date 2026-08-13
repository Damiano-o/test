package it.uniroma2.ispw.ciboamico.pattern.observer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Observer concreto: notifica il compratore (utente) quando un ordine viene
 * confermato.
 *
 * <p>Riceve il DTO {@link OrdineEvent} (sola lettura) dal
 * {@link OrdineEventPublisher} — mai l'entità di dominio — rispettando così
 * l'isolamento dei layer. In produzione delegherebbe a un servizio di
 * notifica/email; qui logica funzionale pura.</p>
 *
 */
public class UtenteNotifier implements OrdineEventListener {

    private static final Logger LOG = Logger.getLogger(UtenteNotifier.class.getName());

    @Override
    public void onOrdineConfermato(OrdineEvent event) {
        // Funzionale: il compratore viene informato della conferma dell'ordine.
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("[NOTIFICA COMPRATORE] Ordine #" + event.getNumeroOrdine()
                    + " confermato per " + event.getClienteId()
                    + " - totale " + event.getTotale());
        }
    }
}
