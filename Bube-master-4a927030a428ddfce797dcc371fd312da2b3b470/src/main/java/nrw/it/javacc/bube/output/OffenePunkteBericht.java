package nrw.it.javacc.bube.output;

import com.google.common.collect.Multimap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import nrw.it.javacc.bube.Konfig;
import nrw.it.javacc.bube.Log;
import nrw.it.javacc.bube.model.Buchungsziel;
import nrw.it.javacc.bube.model.Konto;
import nrw.it.javacc.bube.model.Menge;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Werkzeug;

/**
 *
 * @author schul13
 */
public class OffenePunkteBericht {

    public static void schreibe() {
        schreibeOffenePunkte();
        schreibeWiedervorlage();
    }

    private static void schreibeOffenePunkte() {
        Buchungsziel b = Repository.getBuchungszielFürPW(Konfig.BUCHUNGSZIEL_OFFENE_PUNKTE).get();
        List<Konto> konten = Repository.getKonten();
        for (Konto konto : konten) {
            for (Werkzeug werkzeug : Werkzeug.values()) {
                for (Monat monat : Monat.values()) {
                    Map<Buchungsziel, Menge> abrechnungsregelnPW = konto.getAbrechnungsregelnPW(monat, werkzeug);
                    if (abrechnungsregelnPW.containsKey(b)) {
                        Log.logOffenerPunkt(
                                "Offene Finanzierungsfrage für Konto: ", konto.getId(),
                                ", Werkzeug: ", werkzeug.name(),
                                ", (ab) Monat: ", monat.toString());
                        break;
                    }
                }
            }
        }
    }

    private static void schreibeWiedervorlage() {
        Multimap<LocalDate, String> wvlMM = Repository.getWiedervorlage();
        Map<LocalDate, Collection<String>> wvlMap = wvlMM.asMap();

        for (Map.Entry<LocalDate, Collection<String>> e : wvlMap.entrySet()) {
            if (e.getKey().isBefore(LocalDate.now())) {
                for (String s : e.getValue()) {
                    Log.logOffenerPunkt("WVL: ", s);
                }
            } else {
                Log.logInfo(e.getKey().format(DateTimeFormatter.ISO_DATE));
                for (String s : e.getValue()) {
                    Log.logInfo(" - WVL: ", s);
                }
            }
        }
    }
}
