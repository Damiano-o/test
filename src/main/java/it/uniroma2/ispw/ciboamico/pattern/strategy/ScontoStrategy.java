package it.uniroma2.ispw.ciboamico.pattern.strategy;

// Strategy (GoF - Behavioral) per il calcolo dello sconto di un buono promozionale

public interface ScontoStrategy {

    // Restituisce l'importo totale da pagare dopo l'applicazione dello sconto sull'importo di...

    double applicaSconto(double subtotale);

    String descrizione();

    String getTipo();

    double getValore();
}
