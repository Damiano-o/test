package it.uniroma2.ispw.ciboamico.exception;

// Sottoclasse di BusinessValidationException per le transizioni di stato non valide dell'...

public class InvalidStateTransitionException extends BusinessValidationException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}