package it.uniroma2.ispw.ciboamico.persistence.impl.fs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.exception.DAOException;
import it.uniroma2.ispw.ciboamico.persistence.dao.ProdottoDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO FS per Prodotto e inventario — JSON persistente (NFR-01).
 */
public class FSProdottoDAO implements ProdottoDAO {

    private static final Path FILE_CATALOGO = Path.of("data", "prodotti.json");
    private static final Path FILE_INVENTARIO = Path.of("data", "inventario.json");
    private static final Gson GSON = GsonConfig.gson();

    private List<Prodotto> caricaCatalogo() throws DAOException {
        try {
            if (!Files.exists(FILE_CATALOGO)) {
                return new ArrayList<>();
            }
            return GSON.fromJson(Files.readString(FILE_CATALOGO),
                    new TypeToken<List<Prodotto>>() { }.getType());
        } catch (IOException e) {
            throw new DAOException("Errore lettura prodotti.json", e);
        }
    }

    private void salvaCatalogo(List<Prodotto> prodotti) throws DAOException {
        try {
            Files.createDirectories(FILE_CATALOGO.getParent());
            Files.writeString(FILE_CATALOGO, GSON.toJson(prodotti));
        } catch (IOException e) {
            throw new DAOException("Errore scrittura prodotti.json", e);
        }
    }

    @Override
    public List<Prodotto> findAll() throws DAOException { return caricaCatalogo(); }

    @Override
    public Prodotto findByNome(String nome) throws DAOException {
        return caricaCatalogo().stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst().orElse(null);
    }

    @Override
    public Prodotto save(Prodotto prodotto) throws DAOException {
        List<Prodotto> prodotti = caricaCatalogo();
        prodotti.add(prodotto);
        salvaCatalogo(prodotti);
        return prodotto;
    }

    @Override
    public Prodotto update(Prodotto prodotto) throws DAOException {
        List<Prodotto> prodotti = caricaCatalogo();
        String nome = prodotto.getNome();
        for (int i = 0; i < prodotti.size(); i++) {
            if (prodotti.get(i).getNome().equalsIgnoreCase(nome)) {
                prodotti.set(i, prodotto);
                salvaCatalogo(prodotti);
                return prodotto;
            }
        }
        throw new DAOException("Prodotto non trovato in catalogo per update");
    }
}