package it.uniroma2.ispw.ciboamico.pattern.observer;

// Interfaccia Observer del pattern Observer applicato agli eventi ordine

@FunctionalInterface
public interface OrdineEventListener {

    void onOrdineConfermato(OrdineEvent event);
}
