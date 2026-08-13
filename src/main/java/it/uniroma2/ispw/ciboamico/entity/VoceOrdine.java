package it.uniroma2.ispw.ciboamico.entity;

// Voce d'ordine: prodotto, quantità e prezzo snapshot al momento

public class VoceOrdine {

    private final Prodotto prodotto;
    private final int quantita;
    private final double prezzoAcquisto;

    public VoceOrdine(Prodotto prodotto, int quantita) {
        this.prodotto = prodotto;
        this.quantita = quantita;
        this.prezzoAcquisto = prodotto.getPrezzo();
    }

    public double getParziale() {
        return prezzoAcquisto * quantita;
    }

    public Prodotto getProdotto() { return prodotto; }
    public int getQuantita() { return quantita; }
    public double getPrezzoAcquisto() { return prezzoAcquisto; }
}
