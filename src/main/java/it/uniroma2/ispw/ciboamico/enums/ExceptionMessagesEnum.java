package it.uniroma2.ispw.ciboamico.enums;

// Messaggi tecnici (per il log) usati dalle eccezioni di dominio

public enum ExceptionMessagesEnum {

    EMAIL_FORMAT("Email address provided doesn't match with correct email format"),
    WRONG_PASSWORD("Inserted email or password is incorrect"),
    USER_NOT_FOUND("No user has been found with this credentials"),
    MISSING_AUTH("User doesn't have the authorization to execute the requested action"),
    DAO("An error occurred during data retrieval from the persistence layer"),
    OBJ_NOT_FOUND("Object requested is not present in the persistence layer"),
    NOT_INSTANTIABLE("This class cannot be instantiated"),
    /* === UC-04: buono promozionale === */
    BUONO_CODICE_OBBLIGATORIO("The coupon code is required"),
    BUONO_NON_VALIDO("No valid coupon has been found for the given code"),
    BUONO_NON_ATTIVO("The coupon is not active on the current date"),
    BUONO_GIÀ_USATO("The coupon has already been redeemed by this user"),
    BUONO_VENDITORE_ERRATO("The coupon does not belong to the product's seller"),

    /* === UC-04: ordine === */
    PRODOTTO_NON_SELEZIONATO("No product has been selected for checkout"),
    PRODOTTO_NON_DISPONIBILE("The selected product is no longer available"),

    /* === UC-04: pagamento === */
    ORDINE_MANCANTE("processaPagamento called without an order in checkout"),
    DATI_PAGAMENTO_NON_VALIDI("The payment details are not valid");

    public final String message;

    ExceptionMessagesEnum(String message) {
        this.message = message;
    }
}
