package it.uniroma2.ispw.ciboamico.persistence;

import it.uniroma2.ispw.ciboamico.bootstrap.ApplicationModeManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test ApplicationModeManager: switch modalità e factory corretta.
 
 * @author Michele Damiano
*/
class ApplicationModeManagerTest {

    @org.junit.jupiter.api.AfterEach
    void resetModalita() {
        ApplicationModeManager.getInstance().setActiveMode(ApplicationModeManager.MODE_DEMO);
    }

    @Test
    void testDefaultDemo() {
        ApplicationModeManager manager = ApplicationModeManager.getInstance();
        assertEquals(ApplicationModeManager.MODE_DEMO, manager.getActiveMode());
    }

    @Test
    void testSwitchModalita() {

        ApplicationModeManager manager = ApplicationModeManager.getInstance();
        manager.setActiveMode(ApplicationModeManager.MODE_FS);
        assertEquals(ApplicationModeManager.MODE_FS, manager.getActiveMode());
    }
    @Test
    void testSwitchModalitaParte2() {
        ApplicationModeManager manager = ApplicationModeManager.getInstance();
        manager.setActiveMode(ApplicationModeManager.MODE_FS);
        assertEquals(ApplicationModeManager.MODE_FS, manager.getActiveMode());
        assertNotNull(manager.getDAOFactory());}

    @Test
    void testModalitaNonValida() {
        ApplicationModeManager manager = ApplicationModeManager.getInstance();
        assertThrows(IllegalArgumentException.class, () -> manager.setActiveMode("XYZ"));
    }

    @Test
    void testSingleton() {
        assertSame(ApplicationModeManager.getInstance(), ApplicationModeManager.getInstance());
    }

    @Test
    void testConfigPropertiesEsiste() {
        // NFR-01: config.properties nelle risorse, letto all'avvio senza ricompilare
        var in = getClass().getClassLoader().getResourceAsStream("config.properties");
        assertNotNull(in, "config.properties deve esistere nelle risorse (NFR-01)");
        var props = new java.util.Properties();
        try {
            props.load(in);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        String mode = props.getProperty("persistence_type", "DEMO");
        assertTrue(java.util.List.of("DEMO", "FS", "JDBC").contains(mode),
                "persistence_type non valido in config.properties");
        assertNotNull(ApplicationModeManager.getInstance().getDAOFactory());
    }
}
