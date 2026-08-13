package it.uniroma2.ispw.ciboamico.boundary.cli;

import it.uniroma2.ispw.ciboamico.bean.AutenticazioneBean;
import it.uniroma2.ispw.ciboamico.bean.UtenteBean;
import it.uniroma2.ispw.ciboamico.boundary.IView;
import it.uniroma2.ispw.ciboamico.control.facade.AutenticazioneFacade;
import it.uniroma2.ispw.ciboamico.enums.UserErrorMessagesEnum;
import it.uniroma2.ispw.ciboamico.exception.AutenticazioneException;

// Boundary CLI — Login (UC-11)

public class LoginCLIView implements IView {

    private final CLIContext ctx;
    private final AutenticazioneFacade facade;

    public LoginCLIView(CLIContext ctx) {
        this.ctx = ctx;
        this.facade = new AutenticazioneFacade();
    }

    @Override
    public void display() {
        System.out.println("\n=== CiboAmico — Accedi ===");
        String email = ctx.leggiStringa("Email: ");
        String password = ctx.leggiStringa("Password: ");
        try {
            // Conversione esterno→interno a carico della view: le
            // stringhe del form diventano il bean credenziali passato al Facade.
            UtenteBean utente = facade.login(
                    AutenticazioneBean.fromCredenziali(email, password));
            System.out.println("Benvenuto, " + utente.getUsername() + "! (ruolo: " + utente.getRuoloAttivo() + ")");
        } catch (AutenticazioneException e) {
            System.out.println(UserErrorMessagesEnum.WRONG_PASSWORD_MSG.message);
        } catch (Exception e) {
            System.out.println("Errore di sistema: riprovare più tardi.");
        }
    }
}
