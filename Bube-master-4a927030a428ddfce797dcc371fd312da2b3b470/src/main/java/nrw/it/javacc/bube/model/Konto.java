package nrw.it.javacc.bube.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import nrw.it.javacc.bube.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nrw.it.javacc.bube.Konfig;

/**
 *
 * @author schul13
 */
public class Konto {

    // Stammdaten
    private final String id;
    private final String name;
    private final String land;
    private final String referat;
    private final String email;
    private final String anmerkung;
    private final Map<Monat, Abrechnungsdaten> abrechnungsdaten;

    
    private class Abrechnungsdaten {

        WWert abrechnungsregelPI = WWert.UNDEFINIERT;
        final Table<Werkzeug, Buchungsziel, Menge> abrechnungsregelPW = HashBasedTable.create();

        final Map<Buchungsziel, Menge> personalaufwand = new HashMap<>();
        final Map<Werkzeug, Boolean> werkzeugnutzung = new HashMap<>();
    }

    public Konto(String id, String name, String land, String referat, String email, String anmerkung) {
        this.id = id;
        this.name = name;
        this.land = land;
        this.referat = referat;
        this.email = email;
        this.anmerkung = anmerkung;

        this.abrechnungsdaten = new HashMap<>();
        for (Monat monat : Monat.values()) {
            this.abrechnungsdaten.put(monat, new Abrechnungsdaten());
        }
    }

    /*
     * Stammdaten
     */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean isRef343() {
        return referat.equals("Ref. 343");
    }

    public String getLand() {
        return land;
    }

    public String getReferat() {
        return referat;
    }

    public String getEmail() {
        return email;
    }    

    /*
     * Abrechnungsregeln
     */
    public void setAbrechnungViaPI(Monat monat, Boolean pauschal) {
        Abrechnungsdaten daten = abrechnungsdaten.get(monat);
        if (daten.abrechnungsregelPI.equals(WWert.UNDEFINIERT)) {
            if (pauschal) {
                daten.abrechnungsregelPI = WWert.ABRECHNUNG_VIA_PI;
            } else {
                daten.abrechnungsregelPI = WWert.KEINE_ABRECHNUNG_VIA_PI;
            }
        } else {
            throw new IllegalStateException("Abrechnung nach Personalaufwand wiederholt gesetzt für Konto " + id + " im Monat " + monat.toString());
        }
    }

    public Boolean isAbrechnungViaPI(Monat monat) {
        return WWert.ABRECHNUNG_VIA_PI.equals(abrechnungsdaten.get(monat).abrechnungsregelPI) ? Boolean.TRUE : Boolean.FALSE;
    }

    // Prüft, ob eine Regel hinterlegt wurde
    public Boolean hasAbrechnungsregelViaPI(Monat monat) {
        return !abrechnungsdaten.get(monat).abrechnungsregelPI.equals(WWert.UNDEFINIERT);
    }

    public void addAbrechnungsregelPW(Werkzeug werkzeug, Monat monat, Menge menge, Buchungsziel buchungsziel) {
        Abrechnungsdaten daten = abrechnungsdaten.get(monat);

        // Abrechnung nur bei vorliegender Leistung
        if (Boolean.FALSE.equals(daten.werkzeugnutzung.get(werkzeug))) {
            throw new IllegalStateException("Abrechnungsregel für Konto " + this.getId() + " im Monat " + monat + " für Werkzeug " + werkzeug.toString() + ", obwohl keine Leistung vorliegt.");
        }

        // Doppelte Abrechnungsregel für ein Buchungsziel ausschließen
        if (daten.abrechnungsregelPW.contains(werkzeug, buchungsziel)) {
            throw new IllegalStateException("Mehrere Abrechnungsregel für ein Buchungsziel für Konto " + this.getId() + " im Monat " + monat + " für Werkzeug " + werkzeug.toString());
        }

        // Gesamtmenge prüfen
        Menge gesamtmenge = getAbrechnungsgesamtmengeWerkzeug(werkzeug, monat).add(menge);
        if (gesamtmenge.sub(Menge.from(1)).größerNull()) {
            throw new IllegalStateException("Abrechnungsregel für Gesamtmenge größer als 1 für Konto " + this.getId() + " im Monat " + monat + " für Werkzeug " + werkzeug.toString());
        }

        // Neue Abrechnungsregel hinzufügen
        daten.abrechnungsregelPW.put(werkzeug, buchungsziel, menge);
    }

    public Menge getAbrechnungsgesamtmengeWerkzeug(Werkzeug werkzeug, Monat monat) {
        Map<Buchungsziel, Menge> abrechungFürWerkzeug = abrechnungsdaten.get(monat).abrechnungsregelPW.row(werkzeug);
        Menge summe = Menge.from(0);
        if (null != abrechungFürWerkzeug) {
            for (Menge m : abrechungFürWerkzeug.values()) {
                summe = summe.add(m);
            }
        }
        return summe;
    }

    public Menge getPersonalaufwand(Monat monat){
        return abrechnungsdaten.get(monat).personalaufwand.values().stream()
                .reduce(Menge.from("0"), (m1,m2) -> m1.add(m2));
    }
    
    public Map<Buchungsziel, Menge> getAbrechnungsregelnPW(Monat monat, Werkzeug werkzeug) {
        return abrechnungsdaten.get(monat).abrechnungsregelPW.row(werkzeug);
    }

    public void setWerkzeugnutzung(Werkzeug werkzeug, Monat monat) {
        Abrechnungsdaten daten = abrechnungsdaten.get(monat);
        if (daten.werkzeugnutzung.containsKey(werkzeug)) {
            throw new IllegalArgumentException("Leistung für Werkzeug " + werkzeug + " im Monat " + monat + " wiederholt gesetzt");
        } else {
            daten.werkzeugnutzung.put(werkzeug, Boolean.TRUE);
        }
    }

    public Boolean hasWerkzeugnutzung(Werkzeug werkzeug) {
        for (Monat m : Monat.values()) {
            if (hasWerkzeugnutzung(werkzeug, m)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Boolean hasWerkzeugnutzung(Werkzeug werkzeug, Monat monat) {
        return abrechnungsdaten.get(monat).werkzeugnutzung.getOrDefault(werkzeug, Boolean.FALSE);
    }

    public Boolean hasWerkzeugnutzung(Monat monat) {
        for (Werkzeug werkzeug : Werkzeug.values()) {
            if (hasWerkzeugnutzung(werkzeug, monat)) {
                return true;
            }
        }
        return false;
    }

    public void addPersonalaufwand(Monat monat, Buchungsziel buchungsziel, Menge menge) {
            Abrechnungsdaten daten = abrechnungsdaten.get(monat);
            Menge buchungsmenge = menge;
            if (daten.personalaufwand.containsKey(buchungsziel)) {
                buchungsmenge = buchungsmenge.add(daten.personalaufwand.get(buchungsziel));
            }
            daten.personalaufwand.put(buchungsziel, buchungsmenge);
    }

    @SuppressWarnings("null")
    public void entlaste() {
        List<Monat> monate = Monat.getAbrechnungsmonate().collect(Collectors.toList());
        for (Monat monat : monate) {
            Abrechnungsdaten daten = abrechnungsdaten.get(monat);
            if (isAbrechnungViaPI(monat)) {
                for (Map.Entry<Buchungsziel, Menge> e : daten.personalaufwand.entrySet()) {
                    e.getKey().addSollBuchungPersonal(monat, e.getValue());
                }
            } else {
                for (Table.Cell<Werkzeug, Buchungsziel, Menge> c : daten.abrechnungsregelPW.cellSet()) {
                    if (daten.werkzeugnutzung.containsKey(c.getRowKey())) {
                        c.getColumnKey().addSollBuchungWerkzeug(monat, c.getRowKey(), this, c.getValue());
                    } else {
                        Log.logInfo("Ignoriere Abrechnungsvorschrift für " + c.getRowKey() + " in " + monat.toString() + " und Buchungsziel " + c.getColumnKey());
                    }
                }
            }
        }
    }

    public boolean hasDatenZurAbrechnung() {
        for (Abrechnungsdaten daten : abrechnungsdaten.values()) {
            if (!daten.personalaufwand.isEmpty()
                    || !daten.werkzeugnutzung.isEmpty()
                    || !daten.abrechnungsregelPW.isEmpty()
                    || isRef343()) // Ausgeschiedene 343er sind nicht mehr 343 und werden erkannt, wenn als ausgeschieden in Stammdaten markiert.
            {
                return true;
            }
        }
        return false;
    }
    
    public String toStringKontoidentifikation(){
        return this.name + " (" + this.referat + " / " + this.id + ")";
    }

    private enum WWert {

        ABRECHNUNG_VIA_PI, KEINE_ABRECHNUNG_VIA_PI, UNDEFINIERT
    }

}
