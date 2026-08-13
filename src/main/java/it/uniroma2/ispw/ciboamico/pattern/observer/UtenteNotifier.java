package it.uniroma2.ispw.ciboamico.pattern.observer;

import java.util.logging.Level;
import java.util.logging.Logger;

// Observer concreto: notifica il compratore (utente) quando un

public class UtenteNotifier implements OrdineEventListener {

    private static final Logger LOG = Logger.getLogger(UtenteNotifier.class.getName());

    @Override
    public void onOrdineConfermato(OrdineEvent event) {
        // Funzionale: il compratore viene informato della conferma
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("[NOTIFICA COMPRATORE] Ordine #" + event.getNumeroOrdine()
                    + " confermato per " + event.getClienteId()
                    + " - totale " + event.getTotale());
        }
    }
}
