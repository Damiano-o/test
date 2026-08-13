package it.uniroma2.ispw.ciboamico.persistence.impl.fs;

import com.google.gson.Gson;
import it.uniroma2.ispw.ciboamico.entity.Utente;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.persistence.dao.UtenteDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// DAO FS: persistenza JSON (Gson) su file — NFR-01 (persistenza intercambiabile)

public class FSUtenteDAO implements UtenteDAO {

    private static final Path FILE = Path.of("data", "utenti.json");
    private static final Gson GSON = GsonConfig.gson();

    private List<Utente> carica() throws DAOException {
        try {
            if (!Files.exists(FILE)) {
                return new ArrayList<>();
            }
            String json = Files.readString(FILE);
            return GSON.fromJson(json, new com.google.gson.reflect.TypeToken<List<Utente>>() {}.getType());
        } catch (IOException e) {
            throw new DAOException("Errore lettura utenti.json", e);
        }
    }

    private void salva(List<Utente> utenti) throws DAOException {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(utenti));
        } catch (IOException e) {
            throw new DAOException("Errore scrittura utenti.json", e);
        }
    }

    @Override
    public Utente findByEmail(String email) throws DAOException {
        return carica().stream().filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);
    }

    @Override
    public Utente save(Utente utente) throws DAOException {
        List<Utente> utenti = carica();
        utenti.removeIf(u -> u.getEmail().equals(utente.getEmail()));
        utenti.add(utente);
        salva(utenti);
        return utente;
    }
}