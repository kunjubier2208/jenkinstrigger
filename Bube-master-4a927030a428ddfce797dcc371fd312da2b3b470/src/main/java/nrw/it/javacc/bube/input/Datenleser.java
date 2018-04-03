package nrw.it.javacc.bube.input;

import com.google.common.base.Splitter;
import nrw.it.javacc.bube.Log;
import nrw.it.javacc.bube.model.Buchungsziel;
import nrw.it.javacc.bube.model.Konto;
import nrw.it.javacc.bube.model.Menge;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Werkzeug;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author schul13
 */
public class Datenleser {

    public static void lese() {
        leseStammdatenKonten();
        leseStammdatenBuchungsziele();
        leseAbrechnungsregelnPI();
        leseAbrechnungsregelnPW();
        leseWerkzeugnutzung();
        lesePersonalaufwand();
        leseIstBuchungen();
    }

    static void leseStammdatenKonten() {
        final String dateiname = "Stammdaten Konten.xlsx";
        final String kId = "id";
        final String kName = "name";
        final String kLand = "land";
        final String kReferat = "referat";
        final String kEmail = "email";
        final String kWvl = "wvl";
        final String kAnmerkung = "anmerkung";

        Tabellenblattleser d = new Tabellenblattleser(dateiname, 0, 4,
                kId, kName, kLand, kReferat, kEmail, kWvl, kAnmerkung);
        try {
            List<Wertetupel> werte = d.lese();
            Log.logLeseInfo("Stammdaten", "Konten", null, werte.size());
            for (Wertetupel w : werte) {
                Konto k = new Konto(
                        w.getText(kId),
                        w.getText(kName),
                        w.getText(kLand),
                        w.getText(kReferat),
                        w.getText(kEmail),
                        w.getText(kAnmerkung));
                Repository.add(k);
                setzeWvl(w, "Konto", kWvl, kId, kAnmerkung);
            }
        } catch (FileNotFoundException | ParseException ex) {
            Log.logFehler(Datenleser.class, ex);
            Log.logOffenerPunkt("Keine Konten gelesen, daher Abbruch");
            System.exit(1);
        }

    }

    static void leseStammdatenBuchungsziele() {
        final String dateiname = "Stammdaten Buchungsziele.xlsx";
        final String kId = "ID des jahresübergreifenden Auftrags";
        final String kName = "Name des jahresübergreifenden Auftrags";
        final String kVerantwortlich = "Auftragsverantwortliche Person";
        final String kAnmerkung = "Anmerkungen";

        Tabellenblattleser d = new Tabellenblattleser(dateiname, 0, 4, kId, kName, kVerantwortlich, kAnmerkung);
        try {
            List<Wertetupel> werte = d.lese();
            Log.logLeseInfo("Stammdaten", "Buchungsziele", null, werte.size());
            for (Wertetupel w : werte) {
                Optional<Konto> konto = Repository.getKonto(w.getText(kVerantwortlich));
                if (!konto.isPresent()) {
                    überspringe(w, "Kein Konto mit der ID", w.getText(kVerantwortlich), "gefunden");
                    continue;
                }
                Repository.add(new Buchungsziel(
                        w.getText(kId),
                        w.getText(kName),
                        konto.get(),
                        w.getText(kAnmerkung)));
            }
        } catch (FileNotFoundException | ParseException ex) {
            Log.logFehler(Datenleser.class, ex);
            Log.logOffenerPunkt("Keine Buchungsziele gelesen, daher Abbruch");
            System.exit(1);
        }

        // Abbildung iA auf Buchungsziele
        final String dateiname2 = "Stammdaten Buchungsziele.xlsx";
        final String kIA = "Interner Auftrag (Jahresscheibe)";
        final String KJüA = "Zugehöriger jahresübergreifender Auftrag (Buchungsziel)";
        final String kJüPI = "Optional Abweichendes Ziel für PI";
        final String kEntscheidung = "Entscheidung zum optionalen Ziel";
        final String kWvl = "Wiedervorlage";
        final String kAnmerkung2 = "Anmerkungen";

        d = new Tabellenblattleser(dateiname2, 1, 4, kIA, KJüA, kJüPI, kEntscheidung, kWvl, kAnmerkung2);

        try {
            List<Wertetupel> werte = d.lese();
            Log.logLeseInfo("Stammdaten", "Abbildung iAs", null, werte.size());
            for (Wertetupel w : werte) {
                Repository.addZuordnungInternerAuftragZuBuchungszielFürPW(w.getText(kIA), w.getText(KJüA));
                if (w.getText(kJüPI).isEmpty()) {
                    // Zugehöriger jahresübergreifender Auftrag als Defaultwert
                    Repository.addZuordnungInternerAuftragZuBuchungszielFürPI(w.getText(kIA), w.getText(KJüA));
                } else {
                    // Spezieller Auftrag bei abweichender Angabe
                    Repository.addZuordnungInternerAuftragZuBuchungszielFürPI(w.getText(kIA), w.getText(kJüPI));
                }
                setzeWvl(w, "Buchungsziel", kWvl, kIA, kAnmerkung2);
            }

        } catch (FileNotFoundException | ParseException ex) {
            Log.logFehler(Datenleser.class, ex);
            Log.logOffenerPunkt("Abbildung iA auf Buchungsziel fehlgeschlagen, daher Abbruch");
            System.exit(1);
        }
    }

    static void leseAbrechnungsregelnPI() {
        final String dateiname = "Abrechnungsregeln PI.xlsx";
        final String kId = "ID";
        final String kWert = "Wert";
        final String kVon = "von";
        final String kBis = "bis";
        final String kEntscheidung = "Entscheidung";
        final String kWvl = "WVL";
        final String kAnmerkung = "Anmerkung";

        Tabellenblattleser d = new Tabellenblattleser(dateiname, 0, 4, kId, kWert, kVon, kBis, kEntscheidung, kWvl, kAnmerkung);

        try {
            List<Wertetupel> werte = d.lese();
            Log.logLeseInfo("Abrechnungsregeln", "PI", null, werte.size());
            for (Wertetupel w : werte) {
                Optional<Konto> konto = Repository.getKonto(w.getText(kId));

                if (!konto.isPresent() || !w.isMonat(kVon) || !w.isMonat(kBis)) {
                    überspringe(w,
                            !konto.isPresent() ? "Kein gültiges Konto angegeben." : "",
                            !w.isMonat(kVon) ? "Kein gültiger Von-Wert angegeben" : "",
                            !w.isMonat(kBis) ? "Kein gültiger Bis-Wert angegeben" : "");
                    continue;
                }

                // Abrechnungsregeln behandeln
                Monat.getMonate(w.getMonat(kVon), w.getMonat(kBis)).forEach(
                        m -> konto.get().setAbrechnungViaPI(m, w.getText(kWert).equals("ja")));
                // WVL setzen
                setzeWvl(w, "Abrechnungsregeln PI", kWvl, kId, kAnmerkung);
            }
        } catch (FileNotFoundException | ParseException ex) {
            Log.logLeseInfo("Abrechnungsregeln", null, null, null);
        }
    }

    static void leseAbrechnungsregelnPW() {
        final String dateiname = "Abrechnungsregeln PW.xlsx";
        final String kId = "ID";
        final String kLeistung = "Leistung";
        final String kMenge = "menge";
        final String kBuchungsziel = "buchungsziel";
        final String kVon = "von";
        final String kBis = "bis";
        final String kEntscheidung = "Entscheidung";
        final String kWvl = "WVL";
        final String kAnmerkung = "Anmerkung";

        Tabellenblattleser d = new Tabellenblattleser(dateiname, 0, 4, kId, kLeistung, kMenge, kBuchungsziel, kVon, kBis, kEntscheidung, kWvl, kAnmerkung);

        try {
            List<Wertetupel> werte = d.lese();
            Log.logLeseInfo("Abrechnungsregeln", "PW", null, werte.size());
            for (Wertetupel w : werte) {
                Optional<Konto> konto = Repository.getKonto(w.getText(kId));
                Optional<Buchungsziel> buchungsziel = Repository.getBuchungszielFürPW(w.getText(kBuchungsziel));

                if (!konto.isPresent() || !buchungsziel.isPresent() || !w.isMonat(kVon) || !w.isMonat(kBis)) {
                    überspringe(w,
                            !konto.isPresent() ? "Kein gültiges Konto angegeben." : "",
                            !buchungsziel.isPresent() ? "Kein gültiges Buchungsziel angegeben." : "",
                            !w.isMonat(kVon) ? "Kein gültiger Von-Wert angegeben" : "",
                            !w.isMonat(kBis) ? "Kein gültiger Bis-Wert angegeben" : "");
                    continue;
                }

                // Abrechnungsregeln behandeln
                Monat.getMonate(w.getMonat(kVon), w.getMonat(kBis)).forEach(
                        m -> konto.get().addAbrechnungsregelPW(
                                Werkzeug.valueOf(w.getText(kLeistung)), m, w.getMenge(kMenge), buchungsziel.get()));
                // WVL setzen
                setzeWvl(w, "Abrechnungsregeln PW", kWvl, kId, kAnmerkung);
            }
        } catch (FileNotFoundException | ParseException ex) {
            Log.logLeseInfo("Abrechnungsregeln", null, null, null);
        }
    }

    static void leseWerkzeugnutzung() {
        final String kId = "id";

        for (Werkzeug wz : Werkzeug.values()) {
            for (Monat monat : Monat.getAbrechnungsmonateAsList()) {
                Tabellenblattleser d = new Tabellenblattleser(
                        "Werkzeugnutzung " + wz.toString() + ".xlsx", monat.getIndex(), 0, kId);
                try {
                    List<Wertetupel> werte = d.lese();
                    Log.logLeseInfo("Werkzeugnutzung", wz.toString(), monat, werte.size());
                    for (Wertetupel w : werte) {
                        Optional<Konto> konto = Repository.getKonto(w.getText(kId));
                        if (!konto.isPresent()) {
                            überspringe(w, "Kein Konto mit der ID", w.getText(kId), "gefunden");
                            continue;
                        }
                        konto.get().setWerkzeugnutzung(wz, monat);
                    }
                } catch (FileNotFoundException | ParseException ex) {
                    Log.logLeseInfo("Nutzung", wz.toString(), monat, null);
                }
            }
        }
    }

    static void lesePersonalaufwand() {
        final String kMonat = "monat";
        final String kAuftragsnummer = "auftragsnummer";
        final String kName = "name";
        final String kZeit = "zeit";

        for (String s : Arrays.asList("343", "sonst")) {
            Tabellenblattleser d = new Tabellenblattleser(
                    "Personalaufwand " + s + ".xlsx", 0, 4, "DummyLeer", kMonat, kAuftragsnummer, "DummyKostenstelle", kName, "DummyAuftrag", "DummyVorgang", "DummyBemerkungen", kZeit);
            try {
                List<Wertetupel> werte = d.lese();
                Log.logLeseInfo("Personalaufwand", "Ref. " + s, null, werte.size());
                for (Wertetupel w : werte) {
                    Optional<Konto> konto = Repository.getKontoByName(w.getText(kName));
                    if (!konto.isPresent()) {
                        überspringe(w, "Stammdaten für Konto", w.getText(kName), "fehlen");
                        continue;
                    }
                    Optional<Buchungsziel> buchungsziel = Repository.getBuchungszielFürPI(w.getText(kAuftragsnummer));
                    if (!buchungsziel.isPresent()) {
                        überspringe(w, "Stammdaten für Buchungsziel", w.getText(kAuftragsnummer), "fehlen");
                        continue;
                    }
                    if (!w.isMonat(kMonat)) {
                        überspringe(w, "Fehlerhafte Monatsangabe");
                        continue;
                    }
                    Monat monat = w.getMonat(kMonat);
                    Menge menge = Menge.from(w.getText(kZeit));
                    konto.get().addPersonalaufwand(monat, buchungsziel.get(), menge);

                }
            } catch (FileNotFoundException | ParseException ex) {
                Log.logLeseInfo("Personalaufwand", "Ref. " + s, null, null);
            }
        }
    }

    static void leseIstBuchungen() {
        final String kMaterial = "Material";
        final String kBuchungstext = "Buchungstext";
        final String kBuchungsmenge = "Buchungsmenge";
        final String kBuchungsperiode = "Buchungsperiode";
        final String kAuftrag = "Auftrag";

        Tabellenblattleser d = new Tabellenblattleser(
                "Ist-Buchungen.xlsx", 0, 2, kMaterial, "[Dummy] Materialkurztext", kBuchungstext,
                kBuchungsmenge, "[Dummy] Buchungswert", "[Dummy] Buchungsdatum", kBuchungsperiode,
                kAuftrag, "[Dummy] Angelegt am", "[Dummy] Benutzername");
        try {
            List<Wertetupel> werte = d.lese();
            Log.logLeseInfo("Ist-Buchungen", null, null, werte.size());
            for (Wertetupel w : werte) {
                Optional<Buchungsziel> buchungsziel;
                switch (w.getText(kMaterial)) {
                    case "200000052":
                        // Verbrauchsdatensatz für PI
                        buchungsziel = Repository.getBuchungszielFürPI(w.getText(kAuftrag));
                        buchungsziel.ifPresent(b -> b.addIstBuchungPersonal(w.getMonat(kBuchungsperiode), w.getMenge(kBuchungsmenge)));
                        break;
                    case "200000053":
                        // Verbrauchsdatensatz für Werkzeug
                        buchungsziel = Repository.getBuchungszielFürPW(w.getText(kAuftrag));
                        String buchungstext = w.getText(kBuchungstext).replace("Projektwerkzeug", "").replace("fuer", "");
                        Iterator<String> itBuchungstext = Splitter.on(" ").omitEmptyStrings().trimResults().split(buchungstext).iterator();
                        String tWerkzeug = itBuchungstext.next();
                        String tKundenkonto = itBuchungstext.next();
                        Optional<Konto> konto = Repository.getKonto(tKundenkonto);
                        buchungsziel.ifPresent(b
                                -> konto.ifPresent(k
                                        -> b.addIstBuchungWerkzeug(
                                                w.getMonat(kBuchungsperiode),
                                                Werkzeug.valueOf(tWerkzeug),
                                                k,
                                                w.getMenge(kBuchungsmenge))));
                        break;
                }
            }
        } catch (FileNotFoundException | ParseException ex) {
            Log.logLeseInfo("Ist-Buchungen", null, null, null);
        }
    }

    static void setzeWvl(Wertetupel w, String bereichsbezeichnung, String kWvl, String kId, String kAnmerkung) {
        if (w.isEmpty(kWvl)) {
            // Keine WVL gesetzt
            return;
        }

        Optional<LocalDate> wvl = w.getDatum(kWvl);

        if (wvl.isPresent()) {
            Repository.addWvl(wvl.get(), bereichsbezeichnung + ": " + w.getText(kId) + " – " + w.getText(kAnmerkung));
        } else {
            Log.logOffenerPunkt("Fehlerhafte Wiedervorlage, irgnoriere WVL für: " + w.toString());

        }
    }

    private static void überspringe(Wertetupel übersprungenerDatensatz, String... fehlerbeschreibungen) {
        String message = Arrays.stream(fehlerbeschreibungen).filter(s -> s.length() > 0).collect(Collectors.joining(" "));
        Log.logOffenerPunkt(message + " => Überspringe " + übersprungenerDatensatz.toString());
    }
}
