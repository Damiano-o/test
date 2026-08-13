package it.uniroma2.ispw.ciboamico.pattern.payment;

import it.uniroma2.ispw.ciboamico.exception.PaymentRejectedException;

/**
 * Gateway di pagamento esterno (passo 6 UC-04 / estensione 6a).
 * Disaccoppia il controller dall'implementazione concreta del PSP:
 * l'autorizzazione all'addebito è modellata come operazione astratta.
 * Realizzazione sincrona (stub) con sollevamento di errore su esito negativo,
 * coerente con il flusso transazionale di {@code submitOrdine}.
 */
public interface PaymentGateway {

    /**
     * Richiede l'autorizzazione all'addebito per l'importo della transazione.
     *
     * @param importoInCent importo da addebitare in centesimi (long)
     * @return {@code true} se il pagamento è autorizzato
     * @throws PaymentRejectedException se l'autorizzazione è negata (estensione 6a)
     */
    boolean autorizza(long importoInCent) throws PaymentRejectedException;

    /** Descrittivo per log/diagrammi. */
    String fornitore();
}