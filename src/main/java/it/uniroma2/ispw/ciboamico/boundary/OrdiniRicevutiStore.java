package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Store di presentazione degli ordini confermati (nessuna logica di business).
 *
 * <p>È un {@link OrdineEventListener} che accumula in memoria gli eventi di
 * conferma pubblicati dall'{@code OrdineEventPublisher} (Pattern Observer):
 * rappresenta il punto in cui la notifica attiva diventa una lista consultabile
 * dalla View, sia GUI sia CLI, senza introdurre business logic nel layer di
 * presentazione.</p>
 *
 * <p>Registrato in {@code Runner} insieme agli altri observer: riceve il DTO
 * {@link OrdineEvent} (sola lettura) e lo conserva ordinato per timestamp.
 * Le view del venditore filtrano per {@code venditoreId} per mostrare solo gli
 * ordini ricevuti dal venditore loggato. Non persiste nulla: è uno stato di
 * sessione, coerente con lo scope UC-04.</p>
 */
public final class OrdiniRicevutiStore implements OrdineEventListener {

    /** Interfaccia funzionale per osservare l'arrivo di un nuovo ordine. */
    @FunctionalInterface
    public interface OrdineArrivatoListener {
        void onOrdineArrivato(OrdineEvent event);
    }

    private static final OrdiniRicevutiStore INSTANCE = new OrdiniRicevutiStore();

    /** Lista thread-safe degli eventi ordine confermati (più recenti in coda). */
    private final List<OrdineEvent> ordini = Collections.synchronizedList(new ArrayList<>());

    /** Listener di presentazione notificati quando arriva un nuovo ordine. */
    private final List<OrdineArrivatoListener> arrivatoListeners = new ArrayList<>();

    private OrdiniRicevutiStore() { }

    public static OrdiniRicevutiStore getInstance() {
        return INSTANCE;
    }

    @Override
    public void onOrdineConfermato(OrdineEvent event) {
        ordini.add(event);
        // Notifica attiva ai listener di presentazione (push, non polling).
        List<OrdineArrivatoListener> copia;
        synchronized (arrivatoListeners) {
            copia = new ArrayList<>(arrivatoListeners);
        }
        for (OrdineArrivatoListener l : copia) {
            l.onOrdineArrivato(event);
        }
    }

    /**
     * Registra un listener di presentazione che viene avvisato (push) quando
     * un nuovo ordine è confermato. Consente alle view di aggiornarsi da sole,
     * senza dover premere un pulsante di aggiornamento.
     */
    public void addOrdineArrivatoListener(OrdineArrivatoListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (arrivatoListeners) {
            if (!arrivatoListeners.contains(listener)) {
                arrivatoListeners.add(listener);
            }
        }
    }

    public void removeOrdineArrivatoListener(OrdineArrivatoListener listener) {
        synchronized (arrivatoListeners) {
            arrivatoListeners.remove(listener);
        }
    }

    /** Tutti gli ordini confermati ricevuti (copia difensiva, sola lettura). */
    public List<OrdineEvent> getOrdini() {
        synchronized (ordini) {
            return new ArrayList<>(ordini);
        }
    }

    /** Ordini confermati ricevuti dal venditore indicato (per email). */
    public List<OrdineEvent> getOrdiniPerVenditore(String venditoreEmail) {
        if (venditoreEmail == null) {
            return List.of();
        }
        List<OrdineEvent> risultato = new ArrayList<>();
        synchronized (ordini) {
            for (OrdineEvent e : ordini) {
                if (venditoreEmail.equalsIgnoreCase(e.getVenditoreId())) {
                    risultato.add(e);
                }
            }
        }
        return risultato;
    }

    /** Svuota lo store (reset per i test e i riavvii). */
    public void clear() {
        synchronized (ordini) {
            ordini.clear();
        }
    }
}
