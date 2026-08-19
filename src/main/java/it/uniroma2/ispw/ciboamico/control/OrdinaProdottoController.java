package it.uniroma2.ispw.ciboamico.control;

import it.uniroma2.ispw.ciboamico.bean.OrdineBean;
import it.uniroma2.ispw.ciboamico.bean.ProdottoBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.entity.*;
import java.util.List;
import it.uniroma2.ispw.ciboamico.pattern.observer.VenditoreNotifier;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;

/**
 * Control di UC-04 Ordina Prodotto (1:1 con l'use case).
 * Stateless: l'utente corrente è passato dalla View (Opzione A); scambia solo Bean con la Boundary.
 */
public class OrdinaProdottoController {

    private final OrdineDAO ordineDAO;
    private final ProdottoDAO prodottoDAO;

    public OrdinaProdottoController(DAOFactory factory) {
        this.ordineDAO = factory.getOrdineDAO();
        this.prodottoDAO = factory.getProdottoDAO();
    }

    /**
     * Catalogo del marketplace come Bean (BCE: la boundary non tocca i DAO).
     */
    public List<ProdottoBean> getProdottiDisponibili() {
        return prodottoDAO.findAll().stream()
                .map(p -> {
                    ProdottoBean bean = new ProdottoBean();
                    bean.setNome(p.getNome());
                    bean.setPrezzo(p.getPrezzo());
                    bean.setQuantita((double) p.getQuantitaDisponibile());
                    return bean;
                })
                .toList();
    }

    /**
     * Flusso UC-04: verifica disponibilità → riepilogo → conferma → ordine CREATED + notifica.
     */
    public OrdineBean submitOrdine(OrdineBean bean, UtenteBean utente) {
        if (utente == null) {
            throw new IllegalStateException("Utente non autenticato");
        }

        Prodotto prodotto = prodottoDAO.findById(bean.getIdOrdine());
        if (prodotto == null) {
            throw new IllegalStateException("Prodotto non disponibile (2.c)");
        }

        // Estensione 2a (out of stock): la Entity verifica la quantità richiesta
        // e riduce la disponibilità — BusinessValidationException se insufficiente.
        prodotto.riduciDisponibilita(1);
        prodottoDAO.update(prodotto);

        // Costruzione ordine singolo diretto (D-03)
        // Il venditore è risolto dal PRODOTTO acquistato: risalgo all'Utente
        // proprietario del ruolo Venditore (whole-part bidirezionale) — fix da
        // verifica NotebookLM 2026-08-02 (prima: ternary inutile compratore:compratore)
        Utente compratore = new Utente(utente.getUsername(), utente.getEmail(), "");
        Utente venditore = prodotto.getVenditore() != null && prodotto.getVenditore().getUtente() != null
                ? prodotto.getVenditore().getUtente()
                : compratore;

        Ordine ordine = new Ordine(bean.getIdOrdine(), compratore, venditore);
        ordine.subscribe(new VenditoreNotifier()); // Observer: notifica venditore

        VoceOrdine voce = new VoceOrdine(prodotto, 1);
        ordine.aggiungiVoce(voce);

        ordineDAO.save(ordine);

        OrdineBean risultato = new OrdineBean();
        risultato.setIdOrdine(ordine.getIdOrdine());
        risultato.setTotale(ordine.getTotale());
        risultato.setStato(ordine.getStato().name());
        return risultato;
    }
}
