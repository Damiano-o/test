package it.uniroma2.ispw.ciboamico.entity;

import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;

import java.time.ZoneId;
import java.time.LocalDate;

/**
 * Prodotto pubblicato nel marketplace da un venditore.
 * Regole: prezzo > 0 (BR-06), quantità >= 0 (BR-03), non scaduto (BR-01).
 */
public class Prodotto {

    private final String nome;
    private double prezzo;
    private int quantitaDisponibile;
    private final LocalDate scadenza;
    private final UnitaEnum unita;
    private final RuoloVenditore venditore;

    public Prodotto(String nome, double prezzo, int quantitaDisponibile,
                    LocalDate scadenza, UnitaEnum unita, RuoloVenditore venditore) {
        if (prezzo <= 0) {
            throw new BusinessValidationException("Il prezzo deve essere maggiore di 0 (BR-06)");
        }
        if (quantitaDisponibile < 0) {
            throw new BusinessValidationException("La quantità non può essere negativa (BR-03)");
        }
        if (scadenza.isBefore(LocalDate.now(ZoneId.systemDefault()))) {
            throw new BusinessValidationException("Un prodotto scaduto non può essere venduto (BR-01)");
        }
        this.nome = nome;
        this.prezzo = prezzo;
        this.quantitaDisponibile = quantitaDisponibile;
        this.scadenza = scadenza;
        this.unita = unita;
        this.venditore = venditore;
    }

    /**
     * Riduce la disponibilità verificando l'invariante di business (BR-03).
     * Information Expert: la Entity protegge le proprie invarianti; se la
     * quantità richiesta supera quella disponibile lancia una
     * BusinessValidationException (estensione 2a UC-04, out of stock).
     */
    public void riduciDisponibilita(int quantita) {
        if (quantita > quantitaDisponibile) {
            throw new BusinessValidationException(
                    "Quantità richiesta non disponibile: richieste " + quantita
                            + ", disponibili " + quantitaDisponibile + " (BR-03)");
        }
        quantitaDisponibile -= quantita;
    }

    public String getNome() { return nome; }
    public double getPrezzo() { return prezzo; }
    public int getQuantitaDisponibile() { return quantitaDisponibile; }
    public LocalDate getScadenza() { return scadenza; }
    public UnitaEnum getUnita() { return unita; }
    public RuoloVenditore getVenditore() { return venditore; }

    public void setPrezzo(double prezzo) {
        if (prezzo <= 0) throw new BusinessValidationException("Prezzo non valido (BR-06)");
        this.prezzo = prezzo;
    }
}
