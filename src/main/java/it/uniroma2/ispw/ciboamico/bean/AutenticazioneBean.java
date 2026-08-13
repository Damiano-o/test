package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;

import java.util.regex.Pattern;

/**
 * Bean/DTO per il login (UC-11). Segue il pattern BCE: il bean incapsula la
 * validazione sintattica delle credenziali (Fail Fast nei setter), separando
 * la verifica della forma da quella semantica (esistenza account) che resta
 * nel controller applicativo.
 */
public class AutenticazioneBean {

    /** Formato email di riferimento. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$");

    private String email;
    private String password;

    /**
     * Factory method di conversione esterno→interno: partendo da email e
     * password grezze della boundary (formato esterno) costruisce il bean
     * credenziali (formato interno). La costruzione del bean è responsabilità
     * della boundary/View (o del controller di presentazione), non del controller
     * applicativo. Vedi anche {@link #setEmail(String)} e
     * {@link #setPassword(String)}.
     *
     * @param email    email inserita nella view
     * @param password password inserita nella view
     * @return bean credenziali valorizzato
     * @throws AutenticazioneException se forma sintatticamente non valida
     */
    public static AutenticazioneBean fromCredenziali(String email, String password) throws AutenticazioneException {
        AutenticazioneBean bean = new AutenticazioneBean();
        bean.setEmail(email);
        bean.setPassword(password);
        return bean;
    }

    public String getEmail() { return email; }

    /**
     * Imposta l'email validandone la forma sintattica.
     *
     * @param email l'email dell'utente
     * @throws AutenticazioneException se l'email non ha un formato valido
     */
    public void setEmail(String email) throws AutenticazioneException {
        this.email = validaEmail(email);
    }

    /**
     * Controllo sintattico dell'email (Fail Fast).
     *
     * @return l'email normalizzata (trim)
     * @throws AutenticazioneException se l'email non ha un formato valido
     */
    private static String validaEmail(String email) throws AutenticazioneException {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new AutenticazioneException(
                    UserErrorMessagesEnum.MALFORMED_EMAIL_MSG.message,
                    ExceptionMessagesEnum.EMAIL_FORMAT.message + ": " + email,
                    "ERR-EMAIL-NON-VALIDA");
        }
        return email.trim();
    }

    public String getPassword() { return password; }

    /**
     * Imposta la password verificando che non sia vuota. La correttezza della
     * password rispetto all'account è verificata dal controller applicativo
     * (coinvolge il DAO, non una regola sintattica del bean).
     *
     * @param password la password dell'utente
     * @throws AutenticazioneException se la password è vuota
     */
    public void setPassword(String password) throws AutenticazioneException {
        this.password = validaPassword(password);
    }

    /** Controllo sintattico della password (non vuota). */
    private static String validaPassword(String password) throws AutenticazioneException {
        if (password == null || password.isEmpty()) {
            throw new AutenticazioneException(
                    "La password non può essere vuota.",
                    "Password vuota in fase di autenticazione.",
                    "ERR-PASSWORD-VUOTA");
        }
        return password;
    }
}
