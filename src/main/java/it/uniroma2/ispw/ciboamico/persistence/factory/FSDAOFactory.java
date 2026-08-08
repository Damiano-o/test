package it.uniroma2.ispw.ciboamico.persistence.factory;

import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSBuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSOrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSUtenteDAO;

/**
 * Factory FS: persistenza su file JSON (Gson) — milestone M2, NFR-01.
 */
public class FSDAOFactory extends DAOFactory {

    @Override
    public UtenteDAO getUtenteDAO() { return new FSUtenteDAO(); }

    @Override
    public ProdottoDAO getProdottoDAO() { return new FSProdottoDAO(); }

    @Override
    public OrdineDAO getOrdineDAO() { return new FSOrdineDAO(); }

    @Override
    public BuonoDAO getBuonoDAO() { return new FSBuonoDAO(getUtenteDAO()); }
}
