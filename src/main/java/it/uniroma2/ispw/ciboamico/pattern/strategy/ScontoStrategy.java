package it.uniroma2.ispw.ciboamico.pattern.strategy;

// Strategy (GoF - Behavioral) per il calcolo dello sconto di un

public interface ScontoStrategy {

    // Restituisce l'importo totale da pagare dopo l'applicazione dello

    double applicaSconto(double subtotale);

    String descrizione();

    String getTipo();

    double getValore();
}
