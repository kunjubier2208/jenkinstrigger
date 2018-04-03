package nrw.it.javacc.bube.output;

import nrw.it.javacc.bube.Log;
import nrw.it.javacc.bube.model.Buchungsziel;
import nrw.it.javacc.bube.model.Konto;
import nrw.it.javacc.bube.model.Menge;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Werkzeug;
import java.util.Map;
import org.apache.poi.ss.usermodel.Sheet;

/**
 *
 * @author schul13
 */
public class StandardberichtWzDetails extends AbstrakteAuswertung {

    public StandardberichtWzDetails() {
        super("/Standardbericht WZ Details.xlsx");
    }

    public void erstelle() {
        Log.logFortschrittStart("Die Berichtserstellung dauert etwa 5 Stunden");

        // Vorbereitung
        Sheet sheet = erstelleTabellenblatt("Bericht Werkzeugabrechnung");
        Koordinate k = getErsteBerichtskoordinate();

        // Überschriften
        erstelleZellen(sheet, k, styleHeader, "ID", "Name", "Referat", "Werkzeug",
                "Abrechnung\n Jan.", "Abrechnung\n Feb.", "Abrechnung\n Mrz.",
                "Abrechnung\n Apr.", "Abrechnung\n Mai", "Abrechnung\n Jun.",
                "Abrechnung\n Jul.", "Abrechnung\n Aug.", "Abrechnung\n Sep.",
                "Abrechnung\n Okt.", "Abrechnung\n Nov.", "Abrechnung\n Dez.",
                "Abrechnung\n gesamt");

        // Nutzdaten
        for (Konto konto : Repository.getKonten()) {
            Log.logFortschritt();
            for (Werkzeug w : Werkzeug.values()) {
                if (konto.hasWerkzeugnutzung(w)) {
                    erstelleZellen(sheet, k.nZ(), styleZelleOhneUmbruch, konto.getId(), konto.getName(),
                            konto.getReferat(), w.name());
                    Menge summe = Menge.from("0");
                    for (Monat monat : Monat.values()) {
                        if (!konto.hasWerkzeugnutzung(w, monat)) {
                            erstelleZellen(sheet, k, styleZelleMitUmbruch, "—");
                            continue;
                        }
                        StringBuilder sb = new StringBuilder();
                        if (konto.isAbrechnungViaPI(monat)) {
                            sb.append("Nach Personalaufw.");
                        } else {
                            for (Map.Entry<Buchungsziel, Menge> e : konto.getAbrechnungsregelnPW(monat, w).entrySet()) {
                                if (sb.length() > 0) {
                                    sb.append("\n");
                                }
                                sb.append(e.getValue().toStringAlsDezimalzahl()).append(" x ").append(e.getKey().getId());
                                summe = summe.add(e.getValue());
                            }
                        }
                        if (sb.length() == 0) {
                            sb.append("FEHLER");
                        }
                        erstelleZellen(sheet, k, styleZelleMitUmbruch, sb.toString());
                    }
                    erstelleZellen(sheet, k, styleZelleOhneUmbruch, summe.asDouble());
                }

                // Nachbereitung
                formatiereTabellenblatt(sheet);
            }
        }

        Log.logFortschrittEnde();

        schreibeBericht();
    }
}
