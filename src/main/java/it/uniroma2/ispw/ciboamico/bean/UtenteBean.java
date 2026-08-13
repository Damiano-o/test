package it.uniroma2.ispw.ciboamico.bean;

import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;
import it.uniroma2.ispw.ciboamico.enums.ExceptionMessagesEnum;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;

import java.util.regex.Pattern;

// Bean/DTO per la sessione utente — tenuto da SessionManager

public class UtenteBean {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$");

    private String username;
    private String email;
    private String ruoloAttivo;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

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

    public String getRuoloAttivo() { return ruoloAttivo; }
    public void setRuoloAttivo(String ruoloAttivo) { this.ruoloAttivo = ruoloAttivo; }
}
