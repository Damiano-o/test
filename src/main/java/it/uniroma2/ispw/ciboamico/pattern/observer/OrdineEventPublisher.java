package it.uniroma2.ispw.ciboamico.pattern.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subject Singleton del pattern Observer per la gestione degli eventi ordine.
 *
 * <p>Implementa il ruolo di <b>Subject</b> nel pattern Observer (GoF):
 * mantiene una lista di observer ({@link OrdineEventListener}) e li notifica
 * quando un ordine viene confermato. È il punto di coordinamento tra il layer
 * Control (dove avviene la conferma dell'ordine) e i componenti che consumano
 * la notifica (compratore e venditore), permettendo una <b>notifica attiva</b>
 * disaccoppiata dal dominio.</p>
 *
 * <p>Pattern applicati: <b>Singleton</b> (istanza unica thread-safe) e
 * <b>Observer/Subject</b> (registrazione, de-registrazione e notifica).</p>
 *
 */
public class OrdineEventPublisher {

    private static final Logger LOG = Logger.getLogger(OrdineEventPublisher.class.getName());

    /** Istanza Singleton. */
    private static OrdineEventPublisher instance;

    /** Lista degli observer registrati. */
    private final List<OrdineEventListener> listeners;

    /** Coda di eventi pendenti per listener che si registrano in ritardo. */
    private final Queue<OrdineEvent> pendingEvents;

    private OrdineEventPublisher() {
        this.listeners = new ArrayList<>();
        this.pendingEvents = new ConcurrentLinkedQueue<>();
        LOG.log(Level.INFO, "OrdineEventPublisher inizializzato");
    }

    /** Restituisce l'unica istanza del publisher (thread-safe). */
    public static synchronized OrdineEventPublisher getInstance() {
        if (instance == null) {
            instance = new OrdineEventPublisher();
        }
        return instance;
    }

    /**
     * Registra un nuovo observer. Idempotente: registrare lo stesso listener
     * più volte non causa notifiche duplicate. Gli eventi pendenti pubblicati
     * mentre non c'erano listener vengono consegnati al nuovo listener.
     *
     * @param listener l'observer da registrare
     * @throws IllegalArgumentException se listener è null
     */
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

    /** Rimuove un observer dalla lista di notifica. */
    public void removeListener(OrdineEventListener listener) {
        synchronized (listeners) {
            if (listeners.remove(listener)) {
                LOG.log(Level.FINE, () -> "Listener rimosso: " + listener.getClass().getSimpleName());
            }
        }
    }

    /**
     * Notifica tutti gli observer registrati che un ordine è stato confermato.
     * Iterazione su una copia (evita ConcurrentModificationException); fail-safe:
     * un errore in un listener non blocca gli altri. Se non ci sono listener,
     * l'evento viene accodato per una consegna successiva.
     *
     * @param event l'evento di conferma ordine
     * @throws IllegalArgumentException se event è null
     */
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

    /** @return il numero di observer registrati (utile per testing). */
    public int getListenerCount() {
        synchronized (listeners) {
            return listeners.size();
        }
    }

    /** Rimuove tutti i listener registrati e svuota la coda degli eventi pendenti
     *  (reset completo dello stato, utile per i test). */
    public void clearListeners() {
        synchronized (listeners) {
            listeners.clear();
            pendingEvents.clear();
            LOG.log(Level.INFO, "Listener rimossi ed eventi pendenti svuotati");
        }
    }
}
