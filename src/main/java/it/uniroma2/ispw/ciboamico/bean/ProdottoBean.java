package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

import java.time.LocalDate;

/**
 * Bean/DTO: unico canale Boundary→Control per i dati di inventario.
 * Segue il pattern BCE: incapsula la validazione sintattica dei dati
 * obbligatori in {@code validate()} (e la presenza del prodotto).
 */
public class ProdottoBean {

    private String nome;
    private Double quantita;
    private Double prezzo;
    private LocalDate scadenza;
    private String posizione;
    private String unitaMisura;

    public Double getPrezzo() { return prezzo; }
    public void setPrezzo(Double prezzo) { this.prezzo = prezzo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Double getQuantita() { return quantita; }
    public void setQuantita(Double quantita) { this.quantita = quantita; }
    public LocalDate getScadenza() { return scadenza; }
    public void setScadenza(LocalDate scadenza) { this.scadenza = scadenza; }
    public String getPosizione() { return posizione; }
    public void setPosizione(String posizione) { this.posizione = posizione; }
    public String getUnitaMisura() { return unitaMisura; }
    public void setUnitaMisura(String unitaMisura) { this.unitaMisura = unitaMisura; }

    /** Valida che tutti i dati obbligatori del prodotto siano presenti. */
    public void validate() throws BusinessValidationException {
        if (nome == null || nome.isBlank()
                || quantita == null
                || prezzo == null
                || scadenza == null
                || posizione == null
                || unitaMisura == null) {
            throw new BusinessValidationException(
                    "Tutti i dati del prodotto sono obbligatori.",
                    "ProdottoBean con dati incompleti.",
                    "ERR-PRODOTTO-DATI");
        }
    }
}
