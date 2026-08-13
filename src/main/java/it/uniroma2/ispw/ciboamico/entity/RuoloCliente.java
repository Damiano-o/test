package it.uniroma2.ispw.ciboamico.entity;

// Ruolo base dell'utente "casa": gestisce inventario e ricette

public class RuoloCliente extends Ruolo {
    @Override
    public String getNomeRuolo() { return "CLIENTE"; }
}
