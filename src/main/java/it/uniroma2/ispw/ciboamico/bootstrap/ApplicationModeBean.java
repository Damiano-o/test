package it.uniroma2.ispw.ciboamico.bootstrap;

// Bean di avvio: incapsula la scelta dell'utente a runtime

public final class ApplicationModeBean {

    private String interfaccia;
    private String persistenza;

    public String getInterfaccia() {
        return interfaccia;
    }

    public void setInterfaccia(String interfaccia) {
        this.interfaccia = interfaccia;
    }

    public String getPersistenza() {
        return persistenza;
    }

    public void setPersistenza(String persistenza) {
        this.persistenza = persistenza;
    }

    public boolean gui() {
        return "gui".equalsIgnoreCase(interfaccia);
    }
}