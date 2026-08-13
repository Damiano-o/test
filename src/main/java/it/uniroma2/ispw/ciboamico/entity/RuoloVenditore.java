package it.uniroma2.ispw.ciboamico.entity;

// Ruolo Venditore (contadino/fornitore locale)

public class RuoloVenditore extends Ruolo {

    private final String zona;
    private final String recapito;
    private StatoVenditoreEnum stato;
    private transient Utente utente;

    public RuoloVenditore(String zona, String recapito) {
        this.zona = zona;
        this.recapito = recapito;
        this.stato = StatoVenditoreEnum.IN_ATTESA;
    }

    public String getZona() { return zona; }
    public String getRecapito() { return recapito; }
    public StatoVenditoreEnum getStato() { return stato; }

    @Override
    public String getNomeRuolo() { return "VENDITORE"; }

    public Utente getUtente() { return utente; }
    void setUtente(Utente utente) { this.utente = utente; }

    // Approvazione del venditore (BR-02): transizione IN_ATTESA →

    public void approva() {
        if (stato == StatoVenditoreEnum.IN_ATTESA) {
            this.stato = StatoVenditoreEnum.APPROVATO;
        }
    }

    // Imposta lo stato senza validazione di transizione

    public void setStato(StatoVenditoreEnum stato) { this.stato = stato; }
}
