package it.uniroma2.ispw.ciboamico.persistence.impl.fs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import it.uniroma2.ispw.ciboamico.entity.Ruolo;
import it.uniroma2.ispw.ciboamico.entity.RuoloCliente;
import it.uniroma2.ispw.ciboamico.entity.RuoloVenditore;
import it.uniroma2.ispw.ciboamico.entity.StatoVenditoreEnum;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Configurazione Gson condivisa: TypeAdapter per java.time (LocalDate/LocalDateTime)
 * — necessari su Java 17+ dove la reflection su java.time è bloccata dal module system.
 */
public final class GsonConfig {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATA_ORA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private GsonConfig() { }

    public static Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    @Override
                    public void write(JsonWriter out, LocalDate value) throws IOException {
                        if (value == null) { out.nullValue(); return; }
                        out.value(value.format(DATA));
                    }
                    @Override
                    public LocalDate read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
                        return LocalDate.parse(in.nextString(), DATA);
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter out, LocalDateTime value) throws IOException {
                        if (value == null) { out.nullValue(); return; }
                        out.value(value.format(DATA_ORA));
                    }
                    @Override
                    public LocalDateTime read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
                        return LocalDateTime.parse(in.nextString(), DATA_ORA);
                    }
                })
                // Polimorfismo Ruolo: Ruolo è astratta (non istanziabile da Gson);
                // serializza un discriminante (nome ruolo) e i campi del sotto-tipo.
                .registerTypeAdapter(Ruolo.class, new TypeAdapter<Ruolo>() {
                    @Override
                    public void write(JsonWriter out, Ruolo ruolo) throws IOException {
                        if (ruolo == null) { out.nullValue(); return; }
                        out.beginObject();
                        out.name("tipo").value(ruolo.getNomeRuolo());
                        if (ruolo instanceof RuoloVenditore rv) {
                            out.name("zona").value(rv.getZona());
                            out.name("recapito").value(rv.getRecapito());
                            if (rv.getStato() != null) {
                                out.name("stato").value(rv.getStato().name());
                            }
                        }
                        out.endObject();
                    }
                    @Override
                    public Ruolo read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
                        JsonObject obj = JsonParser.parseReader(in).getAsJsonObject();
                        String tipo = obj.has("tipo") ? obj.getAsJsonPrimitive("tipo").getAsString() : null;
                        if ("VENDITORE".equals(tipo)) {
                            String zona = obj.has("zona") ? obj.get("zona").getAsString() : null;
                            String recapito = obj.has("recapito") ? obj.get("recapito").getAsString() : null;
                            RuoloVenditore rv = new RuoloVenditore(zona, recapito);
                            JsonElement st = obj.get("stato");
                            if (st != null && !st.isJsonNull()) {
                                rv.setStato(StatoVenditoreEnum.valueOf(st.getAsString()));
                            }
                            return rv;
                        }
                        // default: CLIENTE (o valore non riconosciuto -> ruolo cliente neutro)
                        return new RuoloCliente();
                    }
                })
                .create();
    }
}
