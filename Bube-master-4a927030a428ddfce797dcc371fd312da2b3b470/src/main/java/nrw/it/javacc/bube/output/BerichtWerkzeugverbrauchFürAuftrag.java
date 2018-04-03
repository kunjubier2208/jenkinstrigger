package nrw.it.javacc.bube.output;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.Optional;
import nrw.it.javacc.bube.model.Buchungsziel;
import nrw.it.javacc.bube.model.Konto;
import nrw.it.javacc.bube.model.Menge;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Werkzeug;
import org.apache.poi.ss.usermodel.Sheet;

/**
 *
 * @author schul13
 */
public class BerichtWerkzeugverbrauchFürAuftrag extends AbstrakteAuswertung {

    private final String auftragsnummer;
    private final Werkzeug werkzeug;
    private final Boolean intern;

    public BerichtWerkzeugverbrauchFürAuftrag(String name, Werkzeug werkzeug, String auftragsnummer, Boolean intern) {
        super("/" + name + ".xlsx");
        this.auftragsnummer = auftragsnummer;
        this.werkzeug = werkzeug;
        this.intern = intern;
    }

    public void erstelle() {
        Optional<Buchungsziel> buchungsziel = Repository.getBuchungszielFürPW(auftragsnummer);
        buchungsziel.ifPresent(b -> {
            erstelleÜbersicht(b);
            erstelleDetails(b);
        });
    }

    public void erstelleÜbersicht(Buchungsziel buchungsziel) {
        /*
         * Berechne Berichtsdaten
         */
        Table<String, Monat, Menge> detailauswertung = HashBasedTable.create();
        for (Monat monat : Monat.values()) {
            for (Map.Entry<Konto, Menge> verbrauch : buchungsziel.getSollBuchungWerkzeug(monat, werkzeug).entrySet()) {
                String land = verbrauch.getKey().getLand();
                // Lege ggf. leeren Datensatz an
                if (!detailauswertung.contains(land, monat)) {
                    detailauswertung.put(land, monat, Menge.from(0));
                }
                // Berechne neue Menge
                Menge bisher = detailauswertung.get(land, monat);
                Menge neu = verbrauch.getValue().add(bisher);
                // Schreibe Ergebnis
                detailauswertung.put(land, monat, neu);
            }
        }

        /*
         * Schreibe Bericht
         */
        // Vorbereitung
        Sheet sheet = erstelleTabellenblatt("Übersicht");
        Koordinate k = getErsteBerichtskoordinate();

        // Überschriften
        erstelleZellen(sheet, k, styleHeader, "", "Organisationseinheit", "Jan.",
                "Feb.", "Mrz.", "Apr.", "Mai", "Jun.", "Jul.", "Aug.", "Sep",
                "Okt.", "Nov.", "Dez.", "Summe");

        // Nutzdaten
        for (String land : detailauswertung.rowKeySet()) {
            erstelleZellen(sheet, k.nZ(), styleZelleOhneUmbruch, "");
            erstelleZellen(sheet, k, styleZelleOhneUmbruch, land);
            Menge summe = Menge.from(0);
            for (Monat monat : Monat.values()) {
                if (detailauswertung.contains(land, monat)) {
                    Menge wert = detailauswertung.get(land, monat);
                    erstelleZellen(sheet, k, styleZelleOhneUmbruch, wert.asDouble());
                    summe = summe.add(wert);
                } else {
                    erstelleZellen(sheet, k, styleZelleOhneUmbruch, 0.0);
                }
            }
            erstelleZellen(sheet, k, styleZelleOhneUmbruch, summe.asDouble());
        }

        // Nachbereitung
        formatiereTabellenblatt(sheet);
        schreibeBericht();
    }

    public void erstelleDetails(Buchungsziel buchungsziel) {
        /*
         * Berechne Berichtsdaten
         */

        // Erstelle Detailauswertung
        Table<Konto, Monat, Boolean> detailauswertung = HashBasedTable.create();
        for (Monat monat : Monat.values()) {
            for (Map.Entry<Konto, Menge> verbrauch : buchungsziel.getSollBuchungWerkzeug(monat, werkzeug).entrySet()) {
                detailauswertung.put(verbrauch.getKey(), monat, Boolean.TRUE);
            }
        }

        /*
         * Schreibe Berichtsblatt
         */
        // Vorbereitung
        Sheet sheet = erstelleTabellenblatt("Details");
        Koordinate k = getErsteBerichtskoordinate();

        // Überschriften
        if (intern) {
            erstelleZellen(sheet, k, styleHeader, "", "ID", "Name", "E-Mail",
                    "Land", "Ref.",
                    "Jan.", "Feb.", "Mrz.", "Apr.", "Mai", "Jun.", "Jul.", "Aug.",
                    "Sep", "Okt.", "Nov.", "Dez.");
        } else {
            erstelleZellen(sheet, k, styleHeader, "", "ID", "Name", "E-Mail", "Land",
                    "Jan.", "Feb.", "Mrz.", "Apr.", "Mai", "Jun.", "Jul.", "Aug.",
                    "Sep", "Okt.", "Nov.", "Dez.");
        }

        // Nutzdaten
        for (Konto konto : detailauswertung.rowKeySet()) {
            erstelleZellen(sheet, k.nZ(), styleZelleOhneUmbruch, "");
            erstelleZellen(sheet, k, styleZelleOhneUmbruch, konto.getId().replaceAll("^je_", ""));
            erstelleZellen(sheet, k, styleZelleOhneUmbruch, konto.getName());
            erstelleZellen(sheet, k, styleZelleOhneUmbruch, konto.getEmail());
            erstelleZellen(sheet, k, styleZelleOhneUmbruch, konto.getLand());
            if (intern) {
                erstelleZellen(sheet, k, styleZelleOhneUmbruch, konto.getReferat());

            }
            for (Monat monat : Monat.values()) {
                if (detailauswertung.contains(konto, monat)
                        && detailauswertung.get(konto, monat)) {
                    erstelleZellen(sheet, k, styleZelleOhneUmbruch, "x");
                } else {
                    erstelleZellen(sheet, k, styleZelleOhneUmbruch, "");
                }
            }
        }

        // Nachbereitung
        formatiereTabellenblatt(sheet);
        schreibeBericht();
    }
}
