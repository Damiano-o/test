package it.uniroma2.ispw.ciboamico.pattern.observer;

/**
 * Interfaccia Observer del pattern Observer applicato agli eventi ordine.
 *
 * <p>Definisce il contratto per i componenti che vogliono essere notificati
 * quando un ordine viene confermato. Il metodo di callback riceve il DTO
 * {@link OrdineEvent} (sola lettura), mai l'entità di dominio {@code Ordine}:
 * così i layer di presentazione restano completamente disaccoppiati dal dominio.</p>
 *
 * <p>Ruoli GoF: Subject = {@link OrdineEventPublisher}, Observer = questa
 * interfaccia, ConcreteObserver = {@link VenditoreNotifier} / {@link UtenteNotifier}.</p>
 *
 * <p>Principi GRASP: <b>Low Coupling</b> (il publisher dipende solo da questa
 * interfaccia), <b>Polymorphism</b> (diverse implementazioni di notifica),
 * <b>Protected Variations</b> (le strategie di notifica possono cambiare senza
 * impattare il publisher).</p>
 *
 * @param event l'evento contenente i dettagli dell'ordine confermato
 * @throws NullPointerException se event è null
 */
@FunctionalInterface
public interface OrdineEventListener {

    /** Callback invocato dal publisher quando un ordine è stato confermato. */
    void onOrdineConfermato(OrdineEvent event);
}
