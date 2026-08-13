package it.uniroma2.ispw.ciboamico.pattern.strategy;

// Simple Factory per la ricostruzione delle ScontoStrategy a

public final class ScontoStrategyFactory {

    public static final String TIPO_PERCENTUALE = "PERCENTUALE";
    public static final String TIPO_FISSO = "FISSO";

    private ScontoStrategyFactory() { }

    public static ScontoStrategy createStrategy(String tipo, double valore) {
        return switch (tipo) {
            case TIPO_PERCENTUALE -> new ScontoPercentualeStrategy(valore);
            case TIPO_FISSO -> new ScontoImportoFissoStrategy(valore);
            default -> throw new IllegalArgumentException("Tipo di sconto sconosciuto: " + tipo);
        };
    }
}
