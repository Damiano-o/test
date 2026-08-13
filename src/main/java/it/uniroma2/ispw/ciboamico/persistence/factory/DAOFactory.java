package it.uniroma2.ispw.ciboamico.persistence.factory;

import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;

// Abstract Factory (GoF): famiglia coerente di DAO

public interface DAOFactory {

    UtenteDAO getUtenteDAO();
    ProdottoDAO getProdottoDAO();
    OrdineDAO getOrdineDAO();
    BuonoDAO getBuonoDAO();
}
