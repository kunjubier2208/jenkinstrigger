package nrw.it.javacc.bube.validierung;

import com.google.common.base.Joiner;
import java.util.List;
import nrw.it.javacc.bube.Log;
import nrw.it.javacc.bube.model.Konto;
import nrw.it.javacc.bube.model.Menge;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Werkzeug;
import java.util.stream.Collectors;
import nrw.it.javacc.bube.Konfig;

/**
 *
 * @author schul13
 */
public class PreValidierer {

    public static void validiere() {
        validierePersonHatMaxEinBenutzerkonto();
        validierePersonIn343HatDefinierteAbrechnungsregelPI();
        validiereWerkzeugnutzungHatFinanzierung();
        validiereAbrechnungsregelPwHatWerkzeugnutzung();
        validiereAbrechnungViaPIHatWerkzeugnutzung();
        validiereAbrechnungsregelPiHatPersonalaufwand();
        obsoleteKonten();
    }

    private static void validierePersonHatMaxEinBenutzerkonto() {
        Log.logInfo("\n\n=== Prüfung, ob für jede Person max. ein Benutzerkonto vorliegt.\n");

        String format = "| %-20s | %-20s | %-8s | %-8s |";
        Log.logInfo("         " + String.format(format, "KontoID 1", "KontoID 2", "Gl. Name", "Gl. Mail"));

        List<Konto> konten = Repository.getKonten();
        for (int i = 0; i < konten.size(); i++) {
            Konto konto1 = konten.get(i);
            for (int j = i + 1; j < konten.size(); j++) {
                Konto konto2 = konten.get(j);
                boolean name = konto1.getName().equals(konto2.getName());
                boolean email = konto1.getEmail().equalsIgnoreCase(konto2.getEmail())
                        && konto1.getEmail().contains("@"); // Ignoriere ungültige E-Mail-Adressen wie "—" im Vergleich
                if (email || name) {
                    Log.logWarnung(String.format(format,
                            konto1.getId(), konto2.getId(),
                            name ? "x" : "", email ? "x" : ""));
                }

            }
        }

    }

    private static void validierePersonIn343HatDefinierteAbrechnungsregelPI() {
        Log.logInfo("\n\n=== Prüfung, ob für jede Person aus Ref. 343 eine Entscheidung\n"
                + "    bzgl. der Abrechnung via PI vorliegt.\n");

        for (Konto konto : Repository.getKonten()) {
            if (konto.isRef343()) {
                String monateOhneDefinierteAbrechungsregel = Monat.getAbrechnungsmonate()
                        .filter(monat -> !konto.hasAbrechnungsregelViaPI(monat))
                        .map(monat -> monat.toString())
                        .collect(Collectors.joining(", "));
                if (!monateOhneDefinierteAbrechungsregel.isEmpty()) {
                    Log.logOffenerPunkt("Definition Abrechnungregel PI offen für", konto.getId(), "in den Monat(en)", monateOhneDefinierteAbrechungsregel);
                }
            }
        }
    }

    private static void validiereWerkzeugnutzungHatFinanzierung() {
        Log.logInfo("\n\n=== Prüfung, ob für jede Werkzeugnutzung eine geeignete\n"
                + "    Finanzierung vorliegt.\n");

        for (Konto konto : Repository.getKonten()) {
            for (Werkzeug werkzeug : Werkzeug.values()) {
                for (Monat monat : Monat.values()) {
                    if (konto.hasWerkzeugnutzung(werkzeug, monat)) {
                        // Werkzeugleistung liegt vor
                        if (konto.isAbrechnungViaPI(monat)) {
                            // Abrechnung via Personalaufwand, alles okay
                        } else if (konto.getAbrechnungsgesamtmengeWerkzeug(werkzeug, monat).istGleich(Menge.from(1))) {
                            // Abrechnung in voller Höhe möglich, alles okay
                        } else if (konto.getAbrechnungsgesamtmengeWerkzeug(werkzeug, monat).größerNull()) {
                            // Fehler, weil Menge nicht 1 ist
                            Log.logWarnung(konto.getId() + " für " + werkzeug.toString() + "[" + monat.toString() + "] mit Menge " + konto.getAbrechnungsgesamtmengeWerkzeug(werkzeug, monat) + " statt 1");
                        } else {
                            // Fehler mangels Abrechnung
                            Log.logWarnung("| " + konto.getId() + " | " + konto.getName() + " (" + konto.getReferat() + ") | " + werkzeug.toString() + "[" + monat.toString() + "] ohne Abrechnung | | ");
                        }
                    }
                }
            }
        }
    }

    private static void validiereAbrechnungsregelPwHatWerkzeugnutzung() {
        Log.logInfo("\n\n=== Prüfung, ob für jede Abrechnungsregel PW eine entsprechende \n"
                + "    Werkzeugnutzung vorliegt. (ggf. Abrechnugsvorschrift löschen!)\n");

        for (Konto konto : Repository.getKonten()) {
            for (Werkzeug werkzeug : Werkzeug.values()) {
                List<Monat> monateMitWzOhneWerkzeugnutzung
                        = Monat.getAbrechnungsmonate()
                        .filter(monat -> !konto.getAbrechnungsregelnPW(monat, werkzeug).isEmpty())
                        .filter(monat -> !konto.hasWerkzeugnutzung(werkzeug, monat))
                        .collect(Collectors.toList());
                if (!monateMitWzOhneWerkzeugnutzung.isEmpty()) {
                    Log.logOffenerPunkt(
                            konto.getId() + " für " + werkzeug.toString()
                            + "[" + Joiner.on(", ").join(monateMitWzOhneWerkzeugnutzung)
                            + "] mit Abrechnungsvorschrift, obwohl keine Nutzung vorliegt.");
                }
            }
        }
    }

    private static void validiereAbrechnungViaPIHatWerkzeugnutzung() {
        Log.logInfo("\n\n=== Prüfung, ob für ein Konto Abrechnung via PI erfolgt,\n"
                + "    obwohl keine Nutzung der Werkzeuge vorliegt (evtl. keine Java-Entwicklung?).\n"
                + "    (Die Werkzeugverwendung ist KEINE Voraussetung für die Abrechnung!)\n");

        for (Konto konto : Repository.getKonten()) {
            String monateMitPiOhneWerkzeugnutzung
                    = Monat.getAbrechnungsmonate()
                    .filter(monat -> konto.isAbrechnungViaPI(monat))
                    .filter(monat -> !konto.hasWerkzeugnutzung(monat))
                    .map(monat -> monat.toString())
                    .collect(Collectors.joining("; "));
            if (!monateMitPiOhneWerkzeugnutzung.isEmpty()) {
                Log.logInfo(konto.getId(), "mit Abrechnung via PI ohne Werkzeugnutzung in Monaten ",
                        monateMitPiOhneWerkzeugnutzung);
            }
        }
    }

    private static void validiereAbrechnungsregelPiHatPersonalaufwand() {
        Log.logInfo("\n\n=== Prüfung, ob für jede Abrechnungsregel PI auch\n"
                + "    Personalaufwände bekannt sind (sollte mit Ausnahme von \n"
                + "    langfristigen Abwesenheiten und fehlenden Buchungen im Januar \n"
                + "    i.d.R. gegeben sein, ggf. prüfen, ob Regel eine „Leiche“ ist!\n");

        for (Konto konto : Repository.getKonten()) {
            String monateMitPiOhnePersonalaufwand = Monat.getAbrechnungsmonate()
                    .filter(monat -> konto.hasAbrechnungsregelViaPI(monat))
                    .filter(monat -> konto.getPersonalaufwand(monat).istNull())
                    .map(monat -> monat.toString())
                    .collect(Collectors.joining(", "));
            if (!monateMitPiOhnePersonalaufwand.isEmpty()) {
                Log.logInfo(konto.toStringKontoidentifikation(), 
                        "mit Abrechnungsregel PI aber ohne Personalaufwand in den Monaten",
                        monateMitPiOhnePersonalaufwand);
            }
        }
    }

    private static void obsoleteKonten() {
        Log.logInfo("\n\n=== Identifiziere alle potentiell obsoleten Konten.\n"
                + "    (Keine Buchungen, Nutzungen, Verantwortungen, …)\n");

        for (Konto konto : Repository.getKonten()) {
            if (!konto.hasDatenZurAbrechnung()
                    && Repository.getBuchungszieleByVerantwortung(konto).isEmpty()) {
                // Keine Daten und keine Verantwortung -> ungenutzt
                Log.logInfo(konto.getId() + " wird womöglich nicht genutzt");
            }
        }
    }

}
