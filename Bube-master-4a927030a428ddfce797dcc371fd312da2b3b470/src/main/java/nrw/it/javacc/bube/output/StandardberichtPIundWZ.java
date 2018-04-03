package nrw.it.javacc.bube.output;

import nrw.it.javacc.bube.Konfig;
import nrw.it.javacc.bube.Log;
import nrw.it.javacc.bube.model.Buchungsziel;
import nrw.it.javacc.bube.model.Menge;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Werkzeug;
import org.apache.poi.ss.usermodel.Sheet;

/**
 *
 * @author schul13
 */
public class StandardberichtPIundWZ extends AbstrakteAuswertung {

    public StandardberichtPIundWZ() {
        super("/Standardbericht PI + WZ.xlsx");
    }

    public void erstelle() {
        // Vorbereitung
        Sheet sheet = erstelleTabellenblatt("Bericht Stammdaten");
        Koordinate k = getErsteBerichtskoordinate();

        // Überschriften
        erstelleZellen(sheet, k, styleHeader, "ID", "Name", "Verantwortlich", "Leistung",
                "Verbrauch\n Jan.", "Verbrauch\n Feb.", "Verbrauch\n Mrz.",
                "Verbrauch\n Apr.", "Verbrauch\n Mai", "Verbrauch\n Jun.",
                "Verbrauch\n Jul.", "Verbrauch\n Aug.", "Verbrauch\n Sep.",
                "Verbrauch\n Okt.", "Verbrauch\n Nov.", "Verbrauch\n Dez.",
                "Verbrauch\n gesamt", "Offene Buchungen");

        // Nutzdaten
        for (Buchungsziel b : Repository.getBuchungsziele()) {
            // Personalaufwand
            erstelleZellen(sheet, k.nZ(), styleZelleOhneUmbruch, b.getId(), b.getTitel(),
                    b.getVerantwortlich().getName(), "Personalaufwand");
            Menge summe1 = Menge.from("0");
            Boolean offeneBuchungen1 = false;
            for (Monat monat : Monat.values()) {
                Menge verbrauch = b.getSollBuchungPersonal(monat);
                summe1 = summe1.add(verbrauch);
                erstelleZellen(sheet, k, styleZelleOhneUmbruch, verbrauch.asDouble());
                Menge offeneBuchungsmenge = verbrauch.sub(b.getIstBuchungPersonal(monat));
                if (offeneBuchungsmenge.sub(Menge.from(Konfig.MINDERMENGENGRENZE_PERSONAL)).größerNull()) {
                    markiereAlsOffenenPunkt(sheet, k, "Offene Abrechnungsmenge: " + offeneBuchungsmenge.toString());
                    offeneBuchungen1 = true;
                }
            }
            erstelleZellen(sheet, k, styleZelleOhneUmbruch, summe1.asDouble());
            if (offeneBuchungen1) {
                erstelleZellen(sheet, k, styleZelleOhneUmbruch, "x");
            }

            // Werkzeuge
            for (Werkzeug w : Werkzeug.values()) {
                erstelleZellen(sheet, k.nZ(), styleZelleOhneUmbruch, b.getId(), b.getTitel(),
                        b.getVerantwortlich().getName(), "Werkzeug " + w.toString());
                Menge summe2 = Menge.from("0");
                Boolean offeneBuchungen2 = false;
                for (Monat monat : Monat.values()) {
                    Menge verbrauch = b.getMengeSollbuchungWerkzeug(monat, w);
                    summe2 = summe2.add(verbrauch);
                    erstelleZellen(sheet, k, styleZelleOhneUmbruch, verbrauch.asDouble());
                    Menge offeneBuchungsmenge = verbrauch.sub(b.getMengeIstbuchungWerkzeug(monat, w));
                    if (offeneBuchungsmenge.größerNull()) {
                        markiereAlsOffenenPunkt(sheet, k, "Offene Abrechnungsmenge: " + offeneBuchungsmenge.toString());
                        offeneBuchungen2 = true;
                    }
                }
                erstelleZellen(sheet, k, styleZelleOhneUmbruch, summe2.asDouble());
                if (offeneBuchungen2) {
                    erstelleZellen(sheet, k, styleZelleOhneUmbruch, "x");
                }
            }
        }

        // Nachbereitung
        formatiereTabellenblatt(sheet);

        schreibeBericht();
    }
}
