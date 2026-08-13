package it.uniroma2.ispw.ciboamico.pattern.observer;

// Interfaccia Observer del pattern Observer applicato agli eventi

@FunctionalInterface
public interface OrdineEventListener {

    void onOrdineConfermato(OrdineEvent event);
}
