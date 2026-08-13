package it.uniroma2.ispw.ciboamico.entity;

// Ciclo di vita dell'ordine (BR-04): CREATED → CONFIRMED →

public enum StatoOrdineEnum {
    CREATED, CONFIRMED, IN_DELIVERY, DELIVERED, ANNULLED
}
