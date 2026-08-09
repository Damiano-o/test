package it.uniroma2.ispw.ciboamico.persistence.factory;

import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.jdbc.JDBCBuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.jdbc.JDBCOrdineDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.jdbc.JDBCProdottoDAO;
import it.uniroma2.ispw.ciboamico.persistence.impl.jdbc.JDBCUtenteDAO;

/**
 * Factory JDBC: persistenza su MySQL con PreparedStatement (anti SQL-injection).
 */
public class JDBCDAOFactory implements DAOFactory {

    @Override
    public UtenteDAO getUtenteDAO() { return new JDBCUtenteDAO(); }

    @Override
    public ProdottoDAO getProdottoDAO() { return new JDBCProdottoDAO(); }

    @Override
    public OrdineDAO getOrdineDAO() { return new JDBCOrdineDAO(); }

    @Override
    public BuonoDAO getBuonoDAO() { return new JDBCBuonoDAO(getUtenteDAO()); }
}
