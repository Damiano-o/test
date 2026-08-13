package it.uniroma2.ispw.ciboamico.entity;

import it.uniroma2.ispw.ciboamico.config.AppConfig;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

// Utente — classe radice del dominio

public class Utente {

    private String nome;
    private String email;
    private String passwordHash;
    private final List<Ruolo> ruoli = new ArrayList<>();

    private final List<String> buoniUtilizzati = new ArrayList<>();

    public Utente(String nome, String email, String passwordHash) {
        this.nome = nome;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String salt = AppConfig.getInstance().getSalt();
            byte[] digest = md.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean checkPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    public void aggiungiRuolo(Ruolo ruolo) {
        if (ruolo instanceof RuoloVenditore rv) {
            rv.setUtente(this);
        }
        ruoli.add(ruolo);
    }

    public boolean haRuolo(Class<? extends Ruolo> tipoRuolo) {
        return ruoli.stream().anyMatch(tipoRuolo::isInstance);
    }

    public <T extends Ruolo> T getRuolo(Class<T> tipoRuolo) {
        return ruoli.stream()
                .filter(tipoRuolo::isInstance)
                .map(tipoRuolo::cast)
                .findFirst()
                .orElse(null);
    }

    public boolean isVenditoreApprovato() {
        RuoloVenditore v = getRuolo(RuoloVenditore.class);
        return v != null && v.getStato() == StatoVenditoreEnum.APPROVATO;
    }

    public boolean haUsatoBuono(String codiceBuono) {
        return buoniUtilizzati.contains(codiceBuono);
    }

    public void registraBuonoUtilizzato(String codiceBuono) {
        if (!buoniUtilizzati.contains(codiceBuono)) {
            buoniUtilizzati.add(codiceBuono);
        }
    }

    public List<String> getBuoniUtilizzati() { return buoniUtilizzati; }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public List<Ruolo> getRuoli() { return ruoli; }
}
