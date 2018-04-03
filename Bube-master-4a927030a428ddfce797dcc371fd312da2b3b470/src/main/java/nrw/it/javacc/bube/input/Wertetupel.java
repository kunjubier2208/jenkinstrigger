package nrw.it.javacc.bube.input;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import nrw.it.javacc.bube.Konfig;
import nrw.it.javacc.bube.model.Menge;
import nrw.it.javacc.bube.model.Monat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import nrw.it.javacc.bube.Log;

/**
 *
 * @author schul13
 */
public class Wertetupel {

    Map<String, String> werte = new HashMap<>();

    public void add(String name, String wert) {
        werte.put(name, wert);
    }

    public String getText(String name) {
        return werte.getOrDefault(name, "");
    }

    public boolean isEmpty(String name) {
        return getText(name).isEmpty();
    }

    public Menge getMenge(String name) {
        // ggf. vorhandene Tausender-Trennzeichen entfernen
        String tMenge = getText(name).replace(".", "");
        return Menge.from(tMenge);
    }

    public boolean isMonat(String name) {
        try {
            getMonat(name);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }

    public Monat getMonat(String name) {
        // ggf. vorhandene Jahreszahlen und Punkte entfernen
        String tMonat = getText(name).replace(Konfig.ABRECHNUNGSZAHR, "").replace(".", "");
        // ggf. einstellige Angaben mit Null auffüllen
        tMonat = tMonat.length() == 1 ? "0" + tMonat : tMonat;

        return Monat.valueOf(tMonat);
    }

    public Optional<LocalDate> getDatum(String name) {
        String value = getText(name);
        if (value.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(LocalDate.parse(value));
            } catch (DateTimeParseException ex) {
                Log.logOffenerPunkt("DateTimeParseException für " + value);
                return Optional.empty();
            }
        }
    }

    @Override
    public String toString() {
        String gefilterteEinträge = werte.entrySet().stream()
                .filter(e -> !e.getKey().contains("Dummy"))
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("; "));
        return "Wertetupel{" + gefilterteEinträge + "}";
    }
}
