package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Store di presentazione degli ordini confermati (nessuna logica

public final class OrdiniRicevutiStore implements OrdineEventListener {

    @FunctionalInterface
    public interface OrdineArrivatoListener {
        void onOrdineArrivato(OrdineEvent event);
    }

    private static final OrdiniRicevutiStore INSTANCE = new OrdiniRicevutiStore();

    private final List<OrdineEvent> ordini = Collections.synchronizedList(new ArrayList<>());

    private final List<OrdineArrivatoListener> arrivatoListeners = new ArrayList<>();

    private OrdiniRicevutiStore() { }

    public static OrdiniRicevutiStore getInstance() {
        return INSTANCE;
    }

    @Override
    public void onOrdineConfermato(OrdineEvent event) {
        ordini.add(event);
        // Notifica attiva ai listener di presentazione (push, non
        List<OrdineArrivatoListener> copia;
        synchronized (arrivatoListeners) {
            copia = new ArrayList<>(arrivatoListeners);
        }
        for (OrdineArrivatoListener l : copia) {
            l.onOrdineArrivato(event);
        }
    }

    // Registra un listener di presentazione che viene avvisato (push)

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

    public List<OrdineEvent> getOrdini() {
        synchronized (ordini) {
            return new ArrayList<>(ordini);
        }
    }

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

    public void clear() {
        synchronized (ordini) {
            ordini.clear();
        }
    }
}
