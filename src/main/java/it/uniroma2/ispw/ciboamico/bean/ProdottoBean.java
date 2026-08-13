package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

import java.time.LocalDate;

// Bean/DTO: unico canale Boundary→Control per i dati di inventario

public class ProdottoBean {

    private String nome;
    private Double quantita;
    private Double prezzo;
    private LocalDate scadenza;
    private String posizione;
    private String unitaMisura;

    public Double getPrezzo() { return prezzo; }

    public void setPrezzo(Double prezzo) throws BusinessValidationException {
        this.prezzo = validaPrezzo(prezzo);
    }

    private static Double validaPrezzo(Double prezzo) throws BusinessValidationException {
        if (prezzo == null || prezzo <= 0) {
            throw new BusinessValidationException(
                    "Il prezzo del prodotto non è valido.",
                    "ProdottoBean con prezzo non valido.",
                    "ERR-PRODOTTO-PREZZO");
        }
        return prezzo;
    }

    public String getNome() { return nome; }

    public void setNome(String nome) throws BusinessValidationException {
        this.nome = validaNome(nome);
    }

    private static String validaNome(String nome) throws BusinessValidationException {
        if (nome == null || nome.isBlank()) {
            throw new BusinessValidationException(
                    "Il nome del prodotto è obbligatorio.",
                    "ProdottoBean senza nome.",
                    "ERR-PRODOTTO-NOME");
        }
        return nome.trim();
    }

    public Double getQuantita() { return quantita; }

    public void setQuantita(Double quantita) throws BusinessValidationException {
        this.quantita = validaQuantita(quantita);
    }

    private static Double validaQuantita(Double quantita) throws BusinessValidationException {
        if (quantita == null || quantita < 0) {
            throw new BusinessValidationException(
                    "La quantità del prodotto non è valida.",
                    "ProdottoBean con quantità non valida.",
                    "ERR-PRODOTTO-QUANTITA");
        }
        return quantita;
    }

    public LocalDate getScadenza() { return scadenza; }

    public void setScadenza(LocalDate scadenza) throws BusinessValidationException {
        this.scadenza = validaScadenza(scadenza);
    }

    private static LocalDate validaScadenza(LocalDate scadenza) throws BusinessValidationException {
        if (scadenza == null) {
            throw new BusinessValidationException(
                    "La scadenza del prodotto è obbligatoria.",
                    "ProdottoBean senza scadenza.",
                    "ERR-PRODOTTO-SCADENZA");
        }
        return scadenza;
    }

    public String getPosizione() { return posizione; }
    public void setPosizione(String posizione) { this.posizione = posizione; }
    public String getUnitaMisura() { return unitaMisura; }
    public void setUnitaMisura(String unitaMisura) { this.unitaMisura = unitaMisura; }

    public void validate() throws BusinessValidationException {
        validaNome(nome);
        validaQuantita(quantita);
        validaPrezzo(prezzo);
        validaScadenza(scadenza);
    }
}
