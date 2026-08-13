package it.uniroma2.ispw.ciboamico.pattern.strategy;

// ConcreteStrategy: sconto a importo fisso (es

public class ScontoImportoFissoStrategy implements ScontoStrategy {

    private final double importo;

    public ScontoImportoFissoStrategy(double importo) {
        if (importo < 0) {
            throw new IllegalArgumentException("Importo di sconto negativo: " + importo);
        }
        this.importo = importo;
    }

    @Override
    public double applicaSconto(double subtotale) {
        return Math.max(0.0, subtotale - importo);
    }

    @Override
    public String descrizione() {
        return String.format("-%.2f €", importo);
    }

    @Override
    public String getTipo() { return "FISSO"; }

    @Override
    public double getValore() { return importo; }
}
