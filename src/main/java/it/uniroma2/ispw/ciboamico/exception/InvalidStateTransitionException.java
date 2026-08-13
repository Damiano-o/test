package it.uniroma2.ispw.ciboamico.exception;

// Eccezione per transizioni di stato non valide

public class InvalidStateTransitionException extends BusinessValidationException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}