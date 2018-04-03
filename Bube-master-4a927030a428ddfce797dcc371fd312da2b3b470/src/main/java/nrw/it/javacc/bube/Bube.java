package nrw.it.javacc.bube;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import nrw.it.javacc.bube.input.Datenleser;
import nrw.it.javacc.bube.model.Konto;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Werkzeug;
import nrw.it.javacc.bube.output.OffenePunkteBericht;
import nrw.it.javacc.bube.output.StandardberichtPIundWZ;
import nrw.it.javacc.bube.output.Verbauchsbuchungsdatenschreiber;
import nrw.it.javacc.bube.output.StandardberichtAbrechnung;
import nrw.it.javacc.bube.output.StandardberichtWzDetails;
import nrw.it.javacc.bube.validierung.PostValidierer;
import nrw.it.javacc.bube.validierung.PreValidierer;

/**
 *
 * @author schul13
 */
public class Bube {

    public static void main(String[] args) {
        // Funktionalitätsabfrage
        System.out.println("Bitte Funktion wählen (auch Mehrfachwahl möglich):");
        System.out.println("  (a) Verbrauchsdaten berechnen");
        System.out.println("  (b) Standardbericht PI + WZ (inkludiert a)");
        System.out.println("  (c) Standardbericht WZ Details (inkludiert a)");
        System.out.println("  (d) Standardbericht Abrechnung (inkludiert a)");
        System.out.println("  (z) Sonderauswertung Monat, Werkzeug => Konten, Abrechnungsregel");

        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNext()) {
            System.out.println("Ungültige Eingabe, Programmabbruch");
            System.exit(1);
        }

        String option = scanner.next();

        System.out.println("Bitte geben sie den Zielmonat an (2 => Jan.+Feb.):");

        scanner = new Scanner(System.in);
        if (!scanner.hasNext()) {
            System.out.println("Ungültige Eingabe, Programmabbruch");
            System.exit(1);
        }

        Konfig.ABREECHNUNGSMONAT = Monat.valueOf(scanner.next());

        if (option.contains("a") || option.contains("b")
                || option.contains("c") || option.contains("d") || option.contains("z")) {
            Log.beginne("Lese Daten");
            Datenleser.lese();

            Log.beginne("Validiere Daten vor Buchungsberechnung");
            PreValidierer.validiere();

            Log.beginne("Buchungsberechnung");
            Repository.getKonten().stream().forEach(konto -> konto.entlaste());
            Repository.getBuchungsziele().stream().forEach(buchungsziel -> buchungsziel.erstelleVerbrauchsbuchungen());
            Verbauchsbuchungsdatenschreiber.schreibe();

            Log.beginne("Validiere Daten nach Buchungsberechnung");
            PostValidierer.validiere();
        }

        OffenePunkteBericht.schreibe();

        Log.beginne("Berichte");

        if (option.contains("b")) {
            new StandardberichtPIundWZ().erstelle();
        }
        if (option.contains("c")) {
            new StandardberichtWzDetails().erstelle();
        }
        if (option.contains("d")) {
            new StandardberichtAbrechnung().erstelle();
        }

        // TODO: In eigene Klasse auslagern
        // TODO: sout und Scanner-Code in Hilfsklasse zusammenfassen
        if (option.contains("z")) {
            // Zielmonat
            System.out.println("Bitte geben sie den Zielmonat an (2 => Feb.):");
            scanner = new Scanner(System.in);
            if (!scanner.hasNext()) {
                System.out.println("Ungültige Eingabe, Programmabbruch");
                System.exit(1);
            }
            final Monat monat = Monat.valueOf(scanner.next());
            // Zielwerkzeug
            System.out.println("Bitte geben sie das Zielwerkzeug an (CI => Confluence im Intranet):");
            scanner = new Scanner(System.in);
            if (!scanner.hasNext()) {
                System.out.println("Ungültige Eingabe, Programmabbruch");
                System.exit(1);
            }
            final Werkzeug werkzeug = Werkzeug.valueOf(scanner.next());
            
            StringBuilder sb = new StringBuilder();
            sb.append(Joiner.on(";").join("ID", "Name", "E-Mail", "Abrechnungsregel PI", "Abrechnungsregel PW")).append("\n");
            Repository.getKonten().stream()
                    .filter(konto -> konto.hasWerkzeugnutzung(werkzeug, monat))
                    .map(konto -> Joiner.on(";").join(konto.getId(), konto.getName(), konto.getEmail(), konto.isAbrechnungViaPI(monat), konto.getAbrechnungsregelnPW(monat, werkzeug).keySet()))
                    .forEach(s -> sb.append(s).append("\n"));
            
            Path p = Paths.get(System.getProperty("user.dir"), Konfig.PFAD_OUTPUT, "Auswertung z.csv");
            try {
                Files.write(p, sb.toString().getBytes());
            } catch (IOException ex) {
                Log.logFehler(Bube.class, ex);
            }
        }

        Log.logInfo("Programm beendet");
    }

}
