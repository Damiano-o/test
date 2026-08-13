package it.uniroma2.ispw.ciboamico.persistence.impl.fs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.uniroma2.ispw.ciboamico.entity.BuonoPromozionale;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.BusinessValidationException;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoStrategy;
import it.uniroma2.ispw.ciboamico.pattern.strategy.ScontoStrategyFactory;
import it.uniroma2.ispw.ciboamico.persistence.dao.BuonoDAO;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// DAO FS per BuonoPromozionale — persistenza JSON (Gson)

public class FSBuonoDAO implements BuonoDAO {

    private static final Path FILE = Path.of("data", "buoni.json");
    private static final Gson GSON = GsonConfig.gson();
    private final UtenteDAO utenteDAO;

    public FSBuonoDAO(UtenteDAO utenteDAO) {
        this.utenteDAO = utenteDAO;
    }

    static class BuonoJsonDTO {
        String codice;
        String venditoreEmail;
        String dataInizio;
        String dataScadenza;
        String tipoSconto;
        double valoreSconto;
    }

    private List<BuonoJsonDTO> carica() throws DAOException {
        try {
            if (!Files.exists(FILE)) {
                return new ArrayList<>();
            }
            return GSON.fromJson(Files.readString(FILE),
                    new TypeToken<List<BuonoJsonDTO>>() { }.getType());
        } catch (IOException e) {
            throw new DAOException("Errore lettura buoni.json", e);
        }
    }

    private void salva(List<BuonoJsonDTO> buoni) throws DAOException {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(buoni));
        } catch (IOException e) {
            throw new DAOException("Errore scrittura buoni.json", e);
        }
    }

    private Utente findByEmail(String email) throws DAOException {
        Utente u = utenteDAO.findByEmail(email);
        if (u == null) {
            throw new DAOException("Venditore non trovato: " + email);
        }
        return u;
    }

    private RuoloVenditore venditoreDa(String email) throws DAOException {
        RuoloVenditore rv = findByEmail(email).getRuolo(RuoloVenditore.class);
        if (rv == null) {
            throw new DAOException("L'utente " + email + " non è un venditore");
        }
        return rv;
    }

    private BuonoPromozionale aEntita(BuonoJsonDTO dto) throws DAOException {
        ScontoStrategy strategy = ScontoStrategyFactory.createStrategy(dto.tipoSconto, dto.valoreSconto);
        try {
            return new BuonoPromozionale(dto.codice, venditoreDa(dto.venditoreEmail),
                    LocalDate.parse(dto.dataInizio), LocalDate.parse(dto.dataScadenza), strategy);
        } catch (BusinessValidationException e) {
            throw new DAOException("Buono persistito non valido: " + dto.codice, e);
        }
    }

    private BuonoJsonDTO aDto(BuonoPromozionale b) {
        BuonoJsonDTO dto = new BuonoJsonDTO();
        dto.codice = b.getCodice();
        dto.venditoreEmail = b.getVenditore().getUtente() != null
                ? b.getVenditore().getUtente().getEmail()
                : b.getVenditore().getRecapito();
        dto.dataInizio = b.getDataInizio().toString();
        dto.dataScadenza = b.getDataScadenza().toString();
        dto.tipoSconto = b.getStrategiaSconto().getTipo();
        dto.valoreSconto = b.getStrategiaSconto().getValore();
        return dto;
    }

    @Override
    public BuonoPromozionale findByCodice(String codice) throws DAOException {
        for (BuonoJsonDTO dto : carica()) {
            if (dto.codice.equals(codice)) {
                return aEntita(dto);
            }
        }
        return null;
    }

    @Override
    public List<BuonoPromozionale> findByVenditoreEmail(String venditoreEmail) throws DAOException {
        List<BuonoJsonDTO> tutti = carica();
        List<BuonoPromozionale> result = new ArrayList<>();
        for (BuonoJsonDTO dto : tutti) {
            if (dto.venditoreEmail.equals(venditoreEmail)) {
                result.add(aEntita(dto));
            }
        }
        return result;
    }

    @Override
    public BuonoPromozionale save(BuonoPromozionale buono) throws DAOException {
        List<BuonoJsonDTO> buoni = carica();
        buoni.removeIf(dto -> dto.codice.equals(buono.getCodice()));
        buoni.add(aDto(buono));
        salva(buoni);
        return buono;
    }
}