package it.uniroma2.ispw.ciboamico.persistence;

import it.uniroma2.ispw.ciboamico.entity.Prodotto;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.UnitaEnum;
import it.uniroma2.ispw.ciboamico.persistence.factory.DAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.FSDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.factory.JDBCDAOFactory;
import it.uniroma2.ispw.ciboamico.persistence.impl.fs.FSProdottoDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Test di dettaglio della persistenza rilevante per UC-04. */
class PersistenceDetailTest {

    @BeforeEach
    void pulisci() {
        try {
            Path dir = Path.of("data");
            if (Files.exists(dir)) {
                try (var stream = Files.list(dir)) {
                    stream.forEach(file -> {
                        try {
                            Files.deleteIfExists(file);
                        } catch (IOException ignored) {
                            // La pulizia è best-effort; il test segnala eventuali dati residui.
                        }
                    });
                }
            }
        } catch (IOException ignored) {
            // La cartella può non esistere alla prima esecuzione.
        }
    }

    private RuoloVenditore venditore() {
        return new RuoloVenditore("RM", "tel");
    }

    @Test
    void testProdottoFindById() throws Exception {
        FSProdottoDAO dao = new FSProdottoDAO();
        Prodotto prodotto = new Prodotto("Pomodori", 2.0, 50,
                LocalDate.now().plusDays(7), UnitaEnum.GRAMMI, venditore());
        dao.save(prodotto);

        Prodotto trovato = dao.findById((long) prodotto.getNome().hashCode());

        assertNotNull(trovato);
        assertEquals("Pomodori", trovato.getNome());
    }

    @Test
    void testFactoryConcreteEsponeIDaoUc04() {
        DAOFactory fs = new FSDAOFactory();
        assertNotNull(fs.getUtenteDAO());
        assertNotNull(fs.getProdottoDAO());
        assertNotNull(fs.getOrdineDAO());
        assertNotNull(fs.getBuonoDAO());

        DAOFactory jdbc = new JDBCDAOFactory();
        assertNotNull(jdbc.getUtenteDAO());
        assertNotNull(jdbc.getProdottoDAO());
        assertNotNull(jdbc.getOrdineDAO());
        assertNotNull(jdbc.getBuonoDAO());
    }
}
