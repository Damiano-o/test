package it.uniroma2.ispw.ciboamico.persistence.factory;

import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;

/**
 * Abstract Factory (GoF): famiglia coerente di DAO.
 * Le concrete factory (JDBC/FS/Demo) sono scelte a runtime da ApplicationModeManager.
 */
public abstract class DAOFactory {

    public abstract UtenteDAO getUtenteDAO();
    public abstract ProdottoDAO getProdottoDAO();
    public abstract OrdineDAO getOrdineDAO();
    public abstract BuonoDAO getBuonoDAO();
}
