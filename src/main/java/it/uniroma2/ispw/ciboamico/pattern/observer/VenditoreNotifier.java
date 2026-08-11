package it.uniroma2.ispw.ciboamico.pattern.observer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Observer concreto: notifica il venditore quando un ordine viene confermato.
 *
 * <p>Riceve il DTO {@link OrdineEvent} (sola lettura) dal
 * {@link OrdineEventPublisher} — mai l'entità di dominio — rispettando così
 * l'isolamento dei layer. In produzione delegherebbe a un servizio di
 * notifica/email; qui logica funzionale pura.</p>
 *
 * @author Michele Damiano
 */
public class VenditoreNotifier implements OrdineEventListener {

    private static final Logger LOG = Logger.getLogger(VenditoreNotifier.class.getName());

    @Override
    public void onOrdineConfermato(OrdineEvent event) {
        // Funzionale: il venditore riceve la notifica di vendita confermata.
        // (Implementazione reale: invio email/notifica al recapito del venditore)
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("[NOTIFICA VENDITORE] Ordine #" + event.getNumeroOrdine()
                    + " confermato dal cliente " + event.getClienteId()
                    + " - totale " + event.getTotale());
        }
    }
}
