package it.uniroma2.ispw.ciboamico.enums;

/**
 * Messaggi tecnici (per il log) usati dalle eccezioni di dominio. Fonte
 * testuale centralizzata che mantiene coerente il layer di errore, senza
 * duplicare stringhe nei punti di lancio.
 */
public enum ExceptionMessagesEnum {

    EMAIL_FORMAT("Email address provided doesn't match with correct email format"),
    WRONG_PASSWORD("Inserted email or password is incorrect"),
    USER_NOT_FOUND("No user has been found with this credentials"),
    MISSING_AUTH("User doesn't have the authorization to execute the requested action"),
    DAO("An error occurred during data retrieval from the persistence layer"),
    OBJ_NOT_FOUND("Object requested is not present in the persistence layer"),
    NOT_INSTANTIABLE("This class cannot be instantiated"),
    UNEXPECTED_PROPERTY_NAME("Unexpected property name"),
    RESOURCE_NOT_FOUND("The requested resource has not been found");

    public final String message;

    ExceptionMessagesEnum(String message) {
        this.message = message;
    }
}
