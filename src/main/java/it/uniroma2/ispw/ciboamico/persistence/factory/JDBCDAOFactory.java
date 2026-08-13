package it.uniroma2.ispw.ciboamico.persistence.factory;

import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.jdbc.JDBCBuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.jdbc.JDBCOrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.jdbc.JDBCProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.jdbc.JDBCUtenteDAO;

// Factory JDBC: persistenza su MySQL con PreparedStatement (anti

public class JDBCDAOFactory implements DAOFactory {

    private final UtenteDAO utenteDAO = new JDBCUtenteDAO();
    private final ProdottoDAO prodottoDAO = new JDBCProdottoDAO();
    private final OrdineDAO ordineDAO = new JDBCOrdineDAO();
    private final BuonoDAO buonoDAO = new JDBCBuonoDAO(utenteDAO);

    @Override
    public UtenteDAO getUtenteDAO() { return utenteDAO; }

    @Override
    public ProdottoDAO getProdottoDAO() { return prodottoDAO; }

    @Override
    public OrdineDAO getOrdineDAO() { return ordineDAO; }

    @Override
    public BuonoDAO getBuonoDAO() { return buonoDAO; }
}
