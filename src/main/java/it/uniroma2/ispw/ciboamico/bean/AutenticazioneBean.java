package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;

import java.util.regex.Pattern;

// Bean/DTO per il login (UC-11)

public class AutenticazioneBean {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$");

    private String email;
    private String password;

    // Factory method di conversione esterno→interno

    public static AutenticazioneBean fromCredenziali(String email, String password) throws AutenticazioneException {
        AutenticazioneBean bean = new AutenticazioneBean();
        bean.setEmail(email);
        bean.setPassword(password);
        return bean;
    }

    public String getEmail() { return email; }

    // Imposta l'email validandone la forma sintattica

    public void setEmail(String email) throws AutenticazioneException {
        this.email = validaEmail(email);
    }

    // Controllo sintattico dell'email (Fail Fast)

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

    // Imposta la password verificando che non sia vuota

    public void setPassword(String password) throws AutenticazioneException {
        this.password = validaPassword(password);
    }

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
