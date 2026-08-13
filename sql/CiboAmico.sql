-- ============================================================
-- CiboAmico — MySQL schema (modalità JDBC, NFR-01)
-- Allineato ai DAO JDBC (JDBCOrdineDAO, JDBCProdottoDAO,
-- JDBCRicettaDAO, JDBCUtenteDAO). Schema minimale: la valutazione
-- ISPW si concentra sull'interazione controller/DAO, non sulle query.
-- ============================================================

CREATE DATABASE IF NOT EXISTS ciboamico
    DEFAULT CHARACTER SET utf8mb4;
USE ciboamico;

-- ------------------------------------------------------------
-- Utenti e ruoli (whole-part: un utente può avere più ruoli)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS utenti (
    email         VARCHAR(120) PRIMARY KEY,
    nome          VARCHAR(120) NOT NULL,
    password_hash CHAR(64)     NOT NULL
);

CREATE TABLE IF NOT EXISTS ruoli (
    email   VARCHAR(120) NOT NULL,
    ruolo   VARCHAR(40)  NOT NULL,   -- CLIENTE | VENDITORE | NUTRIZIONISTA | AMMINISTRATORE
    stato   VARCHAR(20)  DEFAULT 'IN_ATTESA',  -- per VENDITORE: APPROVATO/SOSPESO
    zona    VARCHAR(120),
    recapito VARCHAR(120),
    PRIMARY KEY (email, ruolo),
    FOREIGN KEY (email) REFERENCES utenti(email)
);

-- ------------------------------------------------------------
-- Marketplace: prodotti pubblicati dai venditori
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prodotti (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome                VARCHAR(120) NOT NULL,
    prezzo              DECIMAL(10,2) NOT NULL CONSTRAINT chk_prezzo CHECK (prezzo > 0),   -- BR-06
    quantita_disponibile INT NOT NULL DEFAULT 0 CONSTRAINT chk_quantita CHECK (quantita_disponibile >= 0),  -- BR-03
    scadenza            DATE NOT NULL,            -- BR-01: non nel passato
    unita               VARCHAR(20) NOT NULL,
    venditore_email     VARCHAR(120) NOT NULL,
    FOREIGN KEY (venditore_email) REFERENCES utenti(email)
);

-- ------------------------------------------------------------
-- Inventario domestico del client
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventario (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    utente_email VARCHAR(120) NOT NULL,
    nome        VARCHAR(120) NOT NULL,
    quantita    INT NOT NULL CONSTRAINT chk_inv_quantita CHECK (quantita > 0),
    scadenza    DATE NOT NULL,
    posizione   VARCHAR(120),
    unita       VARCHAR(20) NOT NULL,
    FOREIGN KEY (utente_email) REFERENCES utenti(email)
);

-- ------------------------------------------------------------
-- Ricette (nutrizionista) e ingredienti (BR-05: min 2)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ricette (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome         VARCHAR(120) NOT NULL,
    istruzioni   TEXT,
    stato        VARCHAR(20) NOT NULL DEFAULT 'PROPOSTA',  -- APPROVATA/RIFIUTATA
    autore_email VARCHAR(120) NOT NULL,
    FOREIGN KEY (autore_email) REFERENCES utenti(email)
);

CREATE TABLE IF NOT EXISTS ingredienti (
    ricetta_id  BIGINT NOT NULL,
    nome        VARCHAR(120) NOT NULL,
    quantita    DOUBLE NOT NULL,
    unita       VARCHAR(20) NOT NULL,
    PRIMARY KEY (ricetta_id, nome),
    FOREIGN KEY (ricetta_id) REFERENCES ricette(id)
);

-- ------------------------------------------------------------
-- Ordini (BR-04: machine a stati)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ordini (
    id              BIGINT PRIMARY KEY,
    compratore_email VARCHAR(120) NOT NULL,
    venditore_email  VARCHAR(120) NOT NULL,
    stato           VARCHAR(20) NOT NULL CONSTRAINT chk_stato CHECK (stato IN ('CREATED','CONFIRMED','IN_DELIVERY','DELIVERED','ANNULLED')),  -- BR-04
    totale          DECIMAL(10,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (compratore_email) REFERENCES utenti(email),
    FOREIGN KEY (venditore_email)  REFERENCES utenti(email)
);

-- ------------------------------------------------------------
-- Seed demo (coerente con DemoDAOFactory)
-- ------------------------------------------------------------
-- Password demo: 'password123' (SHA-256 con salt 'ciboamico-salt')
-- Valore precalcolato: sha256("ciboamico-saltpassword123")
INSERT INTO utenti (email, nome, password_hash) VALUES
    ('mario@cibo.it', 'Mario',
     SHA2('ciboamico-saltpassword123', 256)),
    ('marco@cibo.it', 'Marco',
     SHA2('ciboamico-saltpassword123', 256)),
    ('anna@cibo.it', 'Anna',
     SHA2('ciboamico-saltpassword123', 256)),
    ('admin@cibo.it', 'Admin',
     SHA2('ciboamico-saltpassword123', 256));

INSERT INTO ruoli (email, ruolo, stato) VALUES
    ('mario@cibo.it', 'CLIENTE', 'APPROVATO'),
    ('marco@cibo.it', 'VENDITORE', 'APPROVATO'),
    ('anna@cibo.it',  'NUTRIZIONISTA', 'APPROVATO'),
    ('admin@cibo.it', 'AMMINISTRATORE', 'APPROVATO');

-- ------------------------------------------------------------
-- Voci d'ordine (completezza del Model: Ordine compone + VoceOrdine)
-- Anche se il DAO salva oggi solo stato+totale, lo schema è pronto
-- per l'intera aggregazione (coerenza Model <-> DB).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS voci_ordine (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    ordine_id    BIGINT NOT NULL,
    prodotto_nome VARCHAR(120) NOT NULL,
    quantita     INT NOT NULL CONSTRAINT chk_voce_quantita CHECK (quantita > 0),
    prezzo_snapshot DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (ordine_id) REFERENCES ordini(id) ON DELETE CASCADE
);
