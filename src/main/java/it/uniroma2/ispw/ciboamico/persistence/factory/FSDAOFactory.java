package it.uniroma2.ispw.ciboamico.persistence.factory;

import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSBuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSOrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSUtenteDAO;

// Factory FS: persistenza su file JSON (Gson) — milestone M2,

public class FSDAOFactory implements DAOFactory {

    private final UtenteDAO utenteDAO = new FSUtenteDAO();
    private final ProdottoDAO prodottoDAO = new FSProdottoDAO();
    private final OrdineDAO ordineDAO = new FSOrdineDAO();
    private final BuonoDAO buonoDAO = new FSBuonoDAO(utenteDAO);

    @Override
    public UtenteDAO getUtenteDAO() { return utenteDAO; }

    @Override
    public ProdottoDAO getProdottoDAO() { return prodottoDAO; }

    @Override
    public OrdineDAO getOrdineDAO() { return ordineDAO; }

    @Override
    public BuonoDAO getBuonoDAO() { return buonoDAO; }
}
