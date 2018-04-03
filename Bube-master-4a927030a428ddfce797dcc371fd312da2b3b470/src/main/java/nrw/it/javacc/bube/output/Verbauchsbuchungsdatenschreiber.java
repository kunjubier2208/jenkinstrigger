package nrw.it.javacc.bube.output;

import nrw.it.javacc.bube.Konfig;
import nrw.it.javacc.bube.Log;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Verbrauchsbuchung;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author schul13
 */
public class Verbauchsbuchungsdatenschreiber {

    private static final String TAGESZÄHLER = String.format("%1$03d", new GregorianCalendar().get(GregorianCalendar.DAY_OF_YEAR));

    public static void schreibe() {
        for (Monat monat : Monat.values()) {
            List<String> verbrauch = new ArrayList<>();
            List<String> stornierung = new ArrayList<>();
            for (Verbrauchsbuchung v : Repository.getVerbrauchsbuchungen(monat)) {
                if (v.isStorno()) {
                    stornierung.add(v.getSapRepresentation());
                } else {
                    verbrauch.add(v.getSapRepresentation());
                }
            }

            // Schreibe Buchungen
            if (!verbrauch.isEmpty()) {
                try {
                    // Dateiname: R343_JavaCC_2016_03_001.csv
                    String dateiname = "R343_JavaCC_" + Konfig.ABRECHNUNGSZAHR
                            + "_" + monat.toString() + "_" + TAGESZÄHLER
                            + ".csv";
                    Files.createDirectories(Paths.get(System.getProperty("user.dir"), Konfig.PFAD_OUTPUT));
                    Path p = Paths.get(System.getProperty("user.dir"), Konfig.PFAD_OUTPUT, dateiname);
                    Files.write(p, verbrauch, StandardCharsets.US_ASCII);

                    Log.logInfo("Schreibe " + dateiname + " mit Datensatzanzahl: " + verbrauch.size());

                } catch (IOException ex) {
                    Log.logFehler(Verbauchsbuchungsdatenschreiber.class, ex);
                }
            }

            // Schreibe Stornierungen
            if (!stornierung.isEmpty()) {
                try {
                    // Dateiname: R343_JavaCC_2016_03_001_Storno.csv
                    String dateiname = "R343_JavaCC_" + Konfig.ABRECHNUNGSZAHR
                            + "_" + monat.toString() + "_" + TAGESZÄHLER
                            + "_Storno.csv";
                    Files.createDirectories(Paths.get(System.getProperty("user.dir"), Konfig.PFAD_OUTPUT));
                    Path p = Paths.get(System.getProperty("user.dir"), Konfig.PFAD_OUTPUT, dateiname);
                    Files.write(p, stornierung, StandardCharsets.US_ASCII);

                    Log.logInfo("Schreibe " + dateiname + " mit Datensatzanzahl: " + stornierung.size());

                } catch (IOException ex) {
                    Log.logFehler(Verbauchsbuchungsdatenschreiber.class, ex);
                }
            }

        }

    }

}
