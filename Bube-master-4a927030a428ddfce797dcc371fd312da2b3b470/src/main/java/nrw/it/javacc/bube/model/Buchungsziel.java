package nrw.it.javacc.bube.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import nrw.it.javacc.bube.Konfig;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Stamm- und Verbrauchsdaten zu einem jahresübergreifenden SAfIR-Auftrag, der
 * als Ziel von Buchungssätzen benutzt wird.
 *
 * Alle Jahresscheiben sowie alle jahresübergreifende Aufträge, die nicht selbst
 * benucht werden sondern einen anderen Auftrag belasten, werden nur in
 * Abrechnungsvorschriften ohne Stammdaten verwaltet.
 *
 * @author schul13
 */
public class Buchungsziel {

    // Stammdaten
    private final String id;
    private final String titel;
    private final Konto verantwortlich;
    private final String anmerkung;
    private final Map<Monat, Buchungsdaten> buchungsdaten;

    private class Buchungsdaten {

        // Soll-Buchungen (Erbrachte und hier zugeordnete Leistungen)
        Menge sollBuchungPersonal = new Menge(0);
        Table<Werkzeug, Konto, Menge> sollBuchungWerkzeug = HashBasedTable.create();

        // Ist-Buchungen
        Menge istBuchungPersonal = new Menge(0);
        Table<Werkzeug, Konto, Menge> istBuchungWerkzeug = HashBasedTable.create();
    }

    public Buchungsziel(String id, String titel, Konto verantwortlich, String anmerkung) {
        this.id = id;
        this.titel = titel;
        this.verantwortlich = verantwortlich;
        this.anmerkung = anmerkung;

        this.buchungsdaten = new HashMap<>();
        for (Monat monat : Monat.values()) {
            this.buchungsdaten.put(monat, new Buchungsdaten());
        }
    }

    public String getId() {
        return id;
    }

    public String getTitel() {
        return titel;
    }

    public Konto getVerantwortlich() {
        return verantwortlich;
    }

    public String getAnmerkung() {
        return anmerkung;
    }

    public void addSollBuchungPersonal(Monat monat, Menge menge) {
        buchungsdaten.get(monat).sollBuchungPersonal = buchungsdaten.get(monat).sollBuchungPersonal.add(menge);
    }

    public Menge getSollBuchungPersonal(Monat monat) {
        return buchungsdaten.get(monat).sollBuchungPersonal;
    }

    public void addSollBuchungWerkzeug(Monat monat, Werkzeug werkzeug, Konto konto, Menge menge) {
        if (buchungsdaten.get(monat).sollBuchungWerkzeug.contains(werkzeug, konto)) {
            throw new IllegalArgumentException("Wiederholte Soll-Buchungen für " + werkzeug.toString() + " und Konto " + konto.getId());
        }
        buchungsdaten.get(monat).sollBuchungWerkzeug.put(werkzeug, konto, menge);
    }

    public Collection<Table.Cell<Werkzeug, Konto, Menge>> getSollBuchungWerkzeug(Monat monat) {
        return buchungsdaten.get(monat).sollBuchungWerkzeug.cellSet();
    }
    
    public Map<Konto, Menge> getSollBuchungWerkzeug(Monat monat, Werkzeug werkzeug){
        return buchungsdaten.get(monat).sollBuchungWerkzeug.row(werkzeug);
    }

    public Menge getMengeSollbuchungWerkzeug(Monat monat, Werkzeug werkzeug) {
        Menge ergebnis = new Menge(0);
        for (Menge m : buchungsdaten.get(monat).sollBuchungWerkzeug.row(werkzeug).values()) {
            ergebnis = ergebnis.add(m);
        }
        return ergebnis;
    }

    public void addIstBuchungPersonal(Monat monat, Menge menge) {
        buchungsdaten.get(monat).istBuchungPersonal = buchungsdaten.get(monat).istBuchungPersonal.add(menge);
    }

    public void addIstBuchungWerkzeug(Monat monat, Werkzeug werkzeug, Konto konto, Menge menge) {
        Menge alt = buchungsdaten.get(monat).istBuchungWerkzeug.contains(werkzeug, konto)
                ? buchungsdaten.get(monat).istBuchungWerkzeug.get(werkzeug, konto) : Menge.from(0);
        buchungsdaten.get(monat).istBuchungWerkzeug.put(werkzeug, konto, menge.add(alt));
    }

    public Menge getIstBuchungPersonal(Monat monat) {
        return buchungsdaten.get(monat).istBuchungPersonal;
    }

    public Menge getMengeIstbuchungWerkzeug(Monat monat, Werkzeug werkzeug) {
        Menge ergebnis = new Menge(0);
        for (Menge m : buchungsdaten.get(monat).istBuchungWerkzeug.row(werkzeug).values()) {
            ergebnis = ergebnis.add(m);
        }
        return ergebnis;
    }

    public boolean hasDatenZurAbrechnung() {
        for (Buchungsdaten daten : buchungsdaten.values()) {
            if (daten.sollBuchungPersonal.größerNull()
                    || daten.istBuchungPersonal.größerNull()
                    || !daten.sollBuchungWerkzeug.isEmpty()
                    || !daten.istBuchungWerkzeug.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("null")
    public void erstelleVerbrauchsbuchungen() {
        if (!Konfig.FREIGESTELLTE_AUFTRAEGE.contains(this.id)) {
            for (Monat monat : Monat.values()) {
                Buchungsdaten daten = buchungsdaten.get(monat);
                // 1. Personalleistungen
                Menge offeneBuchungPersonal = daten.sollBuchungPersonal.sub(daten.istBuchungPersonal);
                if (!offeneBuchungPersonal.istNull() && !isMindermengePersonal(offeneBuchungPersonal)) {
                    Repository.add(Verbrauchsbuchung.fürPI(monat, this, offeneBuchungPersonal));
                }
                // 2. Werkzeugleistungen
                for (Table.Cell<Werkzeug, Konto, Menge> sollBuchung : daten.sollBuchungWerkzeug.cellSet()) {
                    Menge offeneBuchungWerkzeug
                            = sollBuchung.getValue().sub(daten.istBuchungWerkzeug.get(sollBuchung.getRowKey(), sollBuchung.getColumnKey()));
                    if (!offeneBuchungWerkzeug.istNull()) {
                        Repository.add(Verbrauchsbuchung.fürPW(
                                sollBuchung.getRowKey(), sollBuchung.getColumnKey(), monat, this, offeneBuchungWerkzeug));
                    }
                }
                // Vollständige Stornierung (wird in 1. for-Schleife nicht gefunden)
                for (Table.Cell<Werkzeug, Konto, Menge> c : daten.istBuchungWerkzeug.cellSet()) {
                    if (!daten.sollBuchungWerkzeug.contains(c.getRowKey(), c.getColumnKey())) {
                        // Ist-Buchung, obwohl keine Soll-Stellung mehr vorliegt -> vollständig stornieren
                        // sofern Summe nicht "0" ist, also schon eine vollständige Storno erfolgte
                        if (!c.getValue().istNull()) {
                            Repository.add(Verbrauchsbuchung.fürPW(
                                    c.getRowKey(), c.getColumnKey(), monat, this, c.getValue().mult(-1)));
                        }
                    }
                }

            }
        }
    }
    
    private boolean isMindermengePersonal(Menge menge){
        return menge.größerGleichNull() && menge.sub(Menge.from(Konfig.MINDERMENGENGRENZE_PERSONAL)).kleinerNull();
    }
    
    @Override
    public String toString(){
        return id;
    }
}
