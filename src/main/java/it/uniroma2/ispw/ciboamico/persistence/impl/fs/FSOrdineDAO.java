package it.uniroma2.ispw.ciboamico.persistence.impl.fs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.uniroma2.ispw.ciboamico.entity.Ordine;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.persistence.dao.OrdineDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// DAO FS per Ordine — JSON persistente

public class FSOrdineDAO implements OrdineDAO {

    private static final Path FILE = Path.of("data", "ordini.json");
    private static final Gson GSON = GsonConfig.gson();

    private List<Ordine> carica() throws DAOException {
        try {
            if (!Files.exists(FILE)) {
                return new ArrayList<>();
            }
            return GSON.fromJson(Files.readString(FILE), new TypeToken<List<Ordine>>() { }.getType());
        } catch (IOException e) {
            throw new DAOException("Errore lettura ordini.json", e);
        }
    }

    private void salva(List<Ordine> ordini) throws DAOException {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(ordini));
        } catch (IOException e) {
            throw new DAOException("Errore scrittura ordini.json", e);
        }
    }

    @Override
    public Ordine save(Ordine ordine) throws DAOException {
        List<Ordine> ordini = carica();
        ordini.removeIf(o -> o.getIdOrdine().equals(ordine.getIdOrdine()));
        ordini.add(ordine);
        salva(ordini);
        return ordine;
    }

    @Override
    public long getNextId() throws DAOException {
        return carica().stream()
                .mapToLong(Ordine::getIdOrdine)
                .max()
                .orElse(0L) + 1L;
    }

    @Override
    public Ordine findById(Long id) throws DAOException {
        return carica().stream().filter(o -> o.getIdOrdine().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Ordine> findByVenditore(String venditoreEmail) throws DAOException {
        return carica().stream()
                .filter(o -> o.getVenditore().getEmail().equals(venditoreEmail)).toList();
    }

    @Override
    public List<Ordine> findByCompratore(String compratoreEmail) throws DAOException {
        return carica().stream()
                .filter(o -> o.getCompratore().getEmail().equals(compratoreEmail)).toList();
    }
}