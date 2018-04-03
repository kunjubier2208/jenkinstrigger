package nrw.it.javacc.bube.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.time.LocalDate;
import nrw.it.javacc.bube.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author schul13
 */
public class Repository {

    private static final HashMap<String, Konto> konten = new HashMap<>();
    private static final HashMap<String, Buchungsziel> buchungsziele = new HashMap<>();
    private static final HashMap<String, String> zuordnung_iA_nach_Buchungsziel_PI = new HashMap<>();
    private static final HashMap<String, String> zuordnung_iA_nach_Buchungsziel_PW = new HashMap<>();
    private static final Multimap<Monat, Verbrauchsbuchung> verbrauchsbuchungen = HashMultimap.create();
    private static final Multimap<LocalDate, String> wiedervorlage = TreeMultimap.create();

    // For test purposes only 
    // TODO: Bessere Lösung zum Zurücksetzen beim Testen finden
    public static void clear() {
        konten.clear();
        buchungsziele.clear();
        zuordnung_iA_nach_Buchungsziel_PI.clear();
        zuordnung_iA_nach_Buchungsziel_PW.clear();
        verbrauchsbuchungen.clear();
        wiedervorlage.clear();
    }

    public static void add(Konto konto) {
        String id = konto.getId();
        if (konten.containsKey(id)) {
            Log.logOffenerPunkt("Mehrere Konten mit der ID " + id);
            return;
        }
        konten.put(id, konto);
    }

    public static Optional<Konto> getKonto(String id) {
        if (konten.containsKey(id)) {
            return Optional.of(konten.get(id));
        }
        return Optional.empty();
    }

    public static Optional<Konto> getKontoByName(String name) {
        for (Konto konto : getKonten()) {
            if (konto.getName().equals(name)) {
                return Optional.of(konto);
            }
        }
        return Optional.empty();
    }

    public static List<Konto> getKonten() {
        List<Konto> ergebnis = new ArrayList<>(konten.values());
        Collections.<Konto>sort(ergebnis,
                (Konto o1, Konto o2) -> o1.getId().compareTo(o2.getId()));
        return ergebnis;
    }

    public static void add(Buchungsziel buchungsziel) {
        String id = buchungsziel.getId();
        if (buchungsziele.containsKey(id)) {
            // TODO: Code smell: Keine Log-Ausgaben an dieser Stelle, bolean als returnwert und beim setzen prüfen?
            Log.logOffenerPunkt("Mehrere Buchungsziele mit der ID " + id);
            return;
        }
        buchungsziele.put(id, buchungsziel);
    }

    private static Optional<Buchungsziel> getBuchungsziel(String id) {
        if (buchungsziele.containsKey(id)) {
            return Optional.of(buchungsziele.get(id));

        }
        return Optional.empty();
    }

    public static List<Buchungsziel> getBuchungsziele() {
        List<Buchungsziel> ergebnis = new ArrayList<>(buchungsziele.values());
        Collections.<Buchungsziel>sort(ergebnis,
                (Buchungsziel o1, Buchungsziel o2) -> o1.getId().compareTo(o2.getId()));
        return ergebnis;
    }

    public static List<Buchungsziel> getBuchungszieleByVerantwortung(Konto konto) {
        List<Buchungsziel> ergebnis = new ArrayList<>();
        for (Buchungsziel buchungsziel : buchungsziele.values()) {
            if (buchungsziel.getVerantwortlich().equals(konto)) {
                ergebnis.add(buchungsziel);
            }
        }
        Collections.<Buchungsziel>sort(ergebnis,
                (Buchungsziel o1, Buchungsziel o2) -> o1.getId().compareTo(o2.getId()));
        return ergebnis;
    }

    public static void addZuordnungInternerAuftragZuBuchungszielFürPI(String iA, String buchungsziel) {
        if (zuordnung_iA_nach_Buchungsziel_PI.containsKey(iA)) {
            // TODO: Code smell: Keine Log-Ausgaben an dieser Stelle, bolean als returnwert und beim setzen prüfen?
            Log.logOffenerPunkt("Mehrere Zuordnungen zu Buchungszielen für den iA " + iA);
        } else {
            zuordnung_iA_nach_Buchungsziel_PI.put(iA, buchungsziel);
        }
    }

    public static Optional<Buchungsziel> getBuchungszielFürPI(String id) {
        String idBuchungsziel = zuordnung_iA_nach_Buchungsziel_PI.getOrDefault(id, id);
        return getBuchungsziel(idBuchungsziel);
    }

    public static void addZuordnungInternerAuftragZuBuchungszielFürPW(String iA, String jüA) {
        if (zuordnung_iA_nach_Buchungsziel_PW.containsKey(iA)) {
            // TODO: Code smell: Keine Log-Ausgaben an dieser Stelle, bolean als returnwert und beim setzen prüfen?
            Log.logOffenerPunkt("Mehrere Zuordnungen zu jüA für den iA " + iA);
        } else {
            zuordnung_iA_nach_Buchungsziel_PW.put(iA, jüA);
        }
    }

    public static Optional<Buchungsziel> getBuchungszielFürPW(String id) {
        String idBuchungsziel = zuordnung_iA_nach_Buchungsziel_PW.getOrDefault(id, id);
        return getBuchungsziel(idBuchungsziel);
    }

    public static void add(Verbrauchsbuchung v) {
        verbrauchsbuchungen.put(v.getMonat(), v);
    }

    public static Collection<Verbrauchsbuchung> getVerbrauchsbuchungen(Monat monat) {
        return verbrauchsbuchungen.get(monat);
    }

    public static void addWvl(LocalDate date, String message) {
        wiedervorlage.put(date, message);
    }

    public static Multimap<LocalDate, String> getWiedervorlage() {
        return wiedervorlage;
    }
}
