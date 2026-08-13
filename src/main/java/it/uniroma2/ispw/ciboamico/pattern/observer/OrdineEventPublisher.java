package it.uniroma2.ispw.ciboamico.pattern.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

// Subject Singleton del pattern Observer per la gestione degli

public class OrdineEventPublisher {

    private static final Logger LOG = Logger.getLogger(OrdineEventPublisher.class.getName());

    private static OrdineEventPublisher instance;

    private final List<OrdineEventListener> listeners;

    private final Queue<OrdineEvent> pendingEvents;

    private OrdineEventPublisher() {
        this.listeners = new ArrayList<>();
        this.pendingEvents = new ConcurrentLinkedQueue<>();
        LOG.log(Level.INFO, "OrdineEventPublisher inizializzato");
    }

    public static synchronized OrdineEventPublisher getInstance() {
        if (instance == null) {
            instance = new OrdineEventPublisher();
        }
        return instance;
    }

    // Registra un nuovo observer

    public void addListener(OrdineEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Il listener non può essere null");
        }
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
                LOG.log(Level.FINE, () -> "Listener aggiunto: " + listener.getClass().getSimpleName());
                if (!pendingEvents.isEmpty()) {
                    for (OrdineEvent event : pendingEvents) {
                        try {
                            listener.onOrdineConfermato(event);
                        } catch (Exception e) {
                            LOG.log(Level.WARNING, "Errore durante la consegna evento pendente", e);
                        }
                    }
                }
            }
        }
    }

    public void removeListener(OrdineEventListener listener) {
        synchronized (listeners) {
            if (listeners.remove(listener)) {
                LOG.log(Level.FINE, () -> "Listener rimosso: " + listener.getClass().getSimpleName());
            }
        }
    }

    // Notifica tutti gli observer registrati che un ordine è stato

    public void notifyOrdineConfermato(OrdineEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("L'evento non può essere null");
        }
        LOG.log(Level.INFO, () -> "Pubblicazione evento: " + event);

        List<OrdineEventListener> listenersCopy;
        synchronized (listeners) {
            listenersCopy = new ArrayList<>(listeners);
        }
        if (listenersCopy.isEmpty()) {
            pendingEvents.add(event);
            LOG.log(Level.INFO, () -> "Nessun listener registrato. Evento accodato. Pendenti: " + pendingEvents.size());
            return;
        }
        for (OrdineEventListener listener : listenersCopy) {
            try {
                listener.onOrdineConfermato(event);
            } catch (Exception e) {
                LOG.log(Level.WARNING, e,
                        () -> "Errore durante la notifica al listener " + listener.getClass().getSimpleName());
            }
        }
        LOG.log(Level.INFO, () -> "Evento notificato a " + listenersCopy.size() + " listener");
    }

    public int getListenerCount() {
        synchronized (listeners) {
            return listeners.size();
        }
    }

    // (reset completo dello stato, utile per i test)

    public void clearListeners() {
        synchronized (listeners) {
            listeners.clear();
            pendingEvents.clear();
            LOG.log(Level.INFO, "Listener rimossi ed eventi pendenti svuotati");
        }
    }
}
