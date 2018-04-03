package nrw.it.javacc.bube.output;

import nrw.it.javacc.bube.Konfig;
import nrw.it.javacc.bube.model.Buchungsziel;
import nrw.it.javacc.bube.model.Menge;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Werkzeug;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Sheet;

/**
 *
 * @author schul13
 */
public class StandardberichtAbrechnung extends AbstrakteAuswertung {

    private static final String STATUS_SOLL = "soll";
    private static final String STATUS_IST = "ist";
    private static final String STATUS_OFFEN = "offen";
    private static final String STATUS_FREI = "freigestellt";
    private static final String STATUS_NA = "—";

    public StandardberichtAbrechnung() {
        super("/Standardbericht Abrechnung.xlsx");
    }

    public void erstelle() {
        // Vorbereitung
        Sheet sheet = erstelleTabellenblatt("Bericht Buchungen");
        Koordinate k = getErsteBerichtskoordinate();

        // Überschriften
        erstelleZellen(sheet, k, styleHeader, "Leistung", "Werkzeug", "Status",
                "Menge\n Jan.", "Menge\n Feb.", "Menge\n Mrz.",
                "Menge\n Apr.", "Menge\n Mai", "Menge\n Jun.",
                "Menge\n Jul.", "Menge\n Aug.", "Menge\n Sep.",
                "Menge\n Okt.", "Menge\n Nov.", "Menge\n Dez.",
                "Menge\n gesamt");

        // Nutzdaten
        // Personalaufwand
        Map<Monat, Menge> pi_soll = new HashMap<>();
        Map<Monat, Menge> pi_ist = new HashMap<>();
        Map<Monat, Menge> pi_offen = new HashMap<>();
        Map<Monat, Menge> pi_freigestellt = new HashMap<>();
        for (Buchungsziel b : Repository.getBuchungsziele()) {
            for (Monat m : Monat.values()) {
                if (Konfig.FREIGESTELLTE_AUFTRAEGE.contains(b.getId())) {
                    pi_freigestellt.put(m, b.getSollBuchungPersonal(m).add(pi_freigestellt.getOrDefault(m, Menge.from(0))));
                } else {
                    pi_soll.put(m, b.getSollBuchungPersonal(m).add(pi_soll.getOrDefault(m, Menge.from(0))));
                    pi_ist.put(m, b.getIstBuchungPersonal(m).add(pi_ist.getOrDefault(m, Menge.from(0))));
                    pi_offen.put(m, pi_soll.get(m).sub(pi_ist.get(m)));
                }
            }
        }
        schreibeBerichtszeile(sheet, k, "PI", STATUS_NA, STATUS_SOLL, pi_soll);
        schreibeBerichtszeile(sheet, k, "PI", STATUS_NA, STATUS_IST, pi_ist);
        schreibeBerichtszeile(sheet, k, "PI", STATUS_NA, STATUS_OFFEN, pi_offen);
        schreibeBerichtszeile(sheet, k, "PI", STATUS_NA, STATUS_FREI, pi_freigestellt);

        // Werkzeuge
        for (Werkzeug w : Werkzeug.values()) {
            Map<Monat, Menge> pw_soll = new HashMap<>();
            Map<Monat, Menge> pw_ist = new HashMap<>();
            Map<Monat, Menge> pw_offen = new HashMap<>();
            Map<Monat, Menge> pw_freigestellt = new HashMap<>();
            for (Buchungsziel b : Repository.getBuchungsziele()) {
                for (Monat m : Monat.values()) {
                    if (Konfig.FREIGESTELLTE_AUFTRAEGE.contains(b.getId())) {
                        pw_freigestellt.put(m, b.getMengeSollbuchungWerkzeug(m, w).add(pw_freigestellt.getOrDefault(m, Menge.from(0))));
                    } else {
                        pw_soll.put(m, b.getMengeSollbuchungWerkzeug(m, w).add(pw_soll.getOrDefault(m, Menge.from(0))));
                        pw_ist.put(m, b.getMengeIstbuchungWerkzeug(m, w).add(pw_ist.getOrDefault(m, Menge.from(0))));
                        pw_offen.put(m, pw_soll.get(m).sub(pw_ist.get(m)));
                    }
                }
            }
            schreibeBerichtszeile(sheet, k, "PW", w.name(), STATUS_SOLL, pw_soll);
            schreibeBerichtszeile(sheet, k, "PW", w.name(), STATUS_IST, pw_ist);
            schreibeBerichtszeile(sheet, k, "PW", w.name(), STATUS_OFFEN, pw_offen);
            schreibeBerichtszeile(sheet, k, "PW", w.name(), STATUS_FREI, pw_freigestellt);
        }

        // Nachbereitung
        formatiereTabellenblatt(sheet);

        schreibeBericht();
    }

    private void schreibeBerichtszeile(Sheet sheet, Koordinate k, String leistung, String werkzeug, String status, Map<Monat, Menge> werte) {
        erstelleZellen(sheet, k.nZ(), styleZelleOhneUmbruch, leistung, werkzeug, status);
        Menge summe = Menge.from("0");
        for (Monat monat : Monat.values()) {
            Menge verbrauch = Menge.from("0").add(werte.get(monat));
            summe = summe.add(verbrauch);
            erstelleZellen(sheet, k, styleZelleOhneUmbruch, verbrauch.asDouble());
        }
        erstelleZellen(sheet, k, styleZelleOhneUmbruch, summe.asDouble());

    }

}
