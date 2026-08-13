package it.uniroma2.ispw.ciboamico.pattern.strategy;

// ConcreteStrategy: sconto percentuale sull'importo lordo

public class ScontoPercentualeStrategy implements ScontoStrategy {

    private final double percentuale;

    public ScontoPercentualeStrategy(double percentuale) {
        if (percentuale < 0 || percentuale > 1) {
            throw new IllegalArgumentException("Percentuale di sconto fuori intervallo: " + percentuale);
        }
        this.percentuale = percentuale;
    }

    @Override
    public double applicaSconto(double subtotale) {
        double scontato = subtotale * (1 - percentuale);
        return Math.max(0.0, scontato);
    }

    @Override
    public String descrizione() {
        return "-" + Math.round(percentuale * 100) + "%";
    }

    @Override
    public String getTipo() { return "PERCENTUALE"; }

    @Override
    public double getValore() { return percentuale; }
}
