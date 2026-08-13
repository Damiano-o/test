package it.uniroma2.ispw.ciboamico.boundary;

import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEvent;
import it.uniroma2.ispw.ciboamico.pattern.observer.OrdineEventListener;
import it.uniroma2.ispw.ciboamico.pattern.singleton.SessionManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * ConcreteObserver di presentazione (boundary) dell'evento ordine confermato.
 *
 * <p>Implementa il contratto {@link OrdineEventListener} del pattern Observer:
 * è una delle reazioni possibili alla notifica del {@code OrdineEventPublisher},
 * qui declinata per l'interfaccia grafica (JavaFX). A differenza dei notifier
 * testuali ({@code UtenteNotifier}/{@code VenditoreNotifier}, che loggano e in
 * produzione delegherebbero a un servizio di email), questo observer mostra un
 * {@link Alert} all'utente finale: è il punto in cui la notifica attiva diventa
 * visibile nella View senza accoppiare il dominio alla presentazione.</p>
 *
 * <p>Riceve solo il DTO {@link OrdineEvent} (sola lettura), mai l'entità
 * {@code Ordine}: rispetta l'isolamento dei layer (BCE). L'aggiornamento della
 * UI è demandato al thread JavaFX tramite {@link Platform#runLater}.</p>
 *
 * <p>Il messaggio mostrato dipende dal ruolo dell'utente loggato in
 * {@link SessionManager}: chi ha effettuato l'ordine vede la conferma come
 * cliente; il venditore del prodotto ordinato vede la conferma come venditore.
 * Nessun alert se chi è loggato non è coinvolto nell'evento.</p>
 */
public class OrderNotificationAlert implements OrdineEventListener {

    @Override
    public void onOrdineConfermato(OrdineEvent event) {
        // La notifica può arrivare da un thread di dominio: l'aggiornamento
        // della UI JavaFX va schedulato sul thread grafico.
        Platform.runLater(() -> {
            UtenteBean logged = SessionManager.getInstance().getLoggedUser();
            if (logged == null) {
                return; // nessun utente loggato: nessuna notifica visibile
            }
            String email = logged.getEmail();
            boolean isCliente = email != null && email.equalsIgnoreCase(event.getClienteId());
            boolean isVenditore = email != null && event.getVenditoreId() != null
                    && email.equalsIgnoreCase(event.getVenditoreId());

            if (!isCliente && !isVenditore) {
                return; // l'utente loggato non è parte dell'ordine
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ordine confermato");
            if (isVenditore && !isCliente) {
                alert.setHeaderText("Nuovo ordine ricevuto");
                alert.setContentText(String.format(
                        "Il cliente %s ha confermato l'ordine #%d.%nTotale: %.2f EUR",
                        event.getClienteId(), event.getNumeroOrdine(), event.getTotale()));
            } else {
                alert.setHeaderText("Ordine confermato");
                alert.setContentText(String.format(
                        "Il tuo ordine #%d è stato confermato.%nTotale: %.2f EUR",
                        event.getNumeroOrdine(), event.getTotale()));
            }
            alert.showAndWait();
        });
    }
}
