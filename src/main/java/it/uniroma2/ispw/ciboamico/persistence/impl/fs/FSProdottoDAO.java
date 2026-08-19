package it.uniroma2.ispw.ciboamico.persistence.impl.fs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.ProdottoInventario;
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

    private List<Prodotto> caricaCatalogo() {
        try {
            if (!Files.exists(FILE_CATALOGO)) return new ArrayList<>();
            return GSON.fromJson(Files.readString(FILE_CATALOGO),
                    new TypeToken<List<Prodotto>>() { }.getType());
        } catch (IOException e) {
            throw new RuntimeException("Errore lettura prodotti.json", e);
        }
    }

    private void salvaCatalogo(List<Prodotto> prodotti) {
        try {
            Files.createDirectories(FILE_CATALOGO.getParent());
            Files.writeString(FILE_CATALOGO, GSON.toJson(prodotti));
        } catch (IOException e) {
            throw new RuntimeException("Errore scrittura prodotti.json", e);
        }
    }

    private List<ProdottoInventario> caricaInventario() {
        try {
            if (!Files.exists(FILE_INVENTARIO)) return new ArrayList<>();
            return GSON.fromJson(Files.readString(FILE_INVENTARIO),
                    new TypeToken<List<ProdottoInventario>>() { }.getType());
        } catch (IOException e) {
            throw new RuntimeException("Errore lettura inventario.json", e);
        }
    }

    private void salvaInventario(List<ProdottoInventario> prodotti) {
        try {
            Files.createDirectories(FILE_INVENTARIO.getParent());
            Files.writeString(FILE_INVENTARIO, GSON.toJson(prodotti));
        } catch (IOException e) {
            throw new RuntimeException("Errore scrittura inventario.json", e);
        }
    }

    @Override
    public List<Prodotto> findAll() { return caricaCatalogo(); }

    @Override
    public Prodotto findById(Long id) {
        return caricaCatalogo().stream()
                .filter(p -> p.getNome().hashCode() == id)
                .findFirst().orElse(null);
    }

    @Override
    public Prodotto save(Prodotto prodotto) {
        List<Prodotto> prodotti = caricaCatalogo();
        prodotti.add(prodotto);
        salvaCatalogo(prodotti);
        return prodotto;
    }

    @Override
    public Prodotto update(Prodotto prodotto) {
        List<Prodotto> prodotti = caricaCatalogo();
        int id = prodotto.getNome().hashCode();
        for (int i = 0; i < prodotti.size(); i++) {
            if (prodotti.get(i).getNome().hashCode() == id) {
                prodotti.set(i, prodotto);
                salvaCatalogo(prodotti);
                return prodotto;
            }
        }
        throw new RuntimeException("Prodotto non trovato in catalogo per update");
    }

    @Override
    public List<ProdottoInventario> findInventario(String utenteEmail) {
        return caricaInventario();
    }

    @Override
    public ProdottoInventario saveInventario(String utenteEmail, ProdottoInventario prodotto) {
        List<ProdottoInventario> prodotti = caricaInventario();
        prodotti.add(prodotto);
        salvaInventario(prodotti);
        return prodotto;
    }
}
