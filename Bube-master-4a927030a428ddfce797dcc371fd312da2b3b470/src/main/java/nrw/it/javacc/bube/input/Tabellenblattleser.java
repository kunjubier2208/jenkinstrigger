package nrw.it.javacc.bube.input;

import nrw.it.javacc.bube.Konfig;
import nrw.it.javacc.bube.Log;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author schul13
 */
public class Tabellenblattleser {

    private final String dateiname;
    private final int tabellenblatt;
    private final String[] spaltennamen;
    private final int startzeilennummer;

    /**
     * Erstelle einen Leser für ein Exceltabellenblatt
     *
     * @param dateiname Name der Exceldatei im Input-Verzeichnis
     * @param tabellenblatt Nullbasierter Index des Tabellenblatts
     * @param anzahlKopfzeilen Anzahl der zu ignorierenden Kopfzeilen
     * @param spaltennamen Namen der Spalten für den späteren Zugriff
     */
    public Tabellenblattleser(String dateiname, int tabellenblatt,
            int anzahlKopfzeilen, String... spaltennamen) {
        this.dateiname = dateiname;
        this.tabellenblatt = tabellenblatt;
        this.startzeilennummer = anzahlKopfzeilen - 1;
        this.spaltennamen = spaltennamen;
    }

    public List<Wertetupel> lese() throws FileNotFoundException, ParseException {
        List<Wertetupel> ergebnis = new ArrayList<>();
        InputStream inp;
        // TODO: Böser Hack
        try {
            inp = Files.newInputStream(Paths.get(System.getProperty("user.dir"), Konfig.PFAD_INPUT, dateiname));
        } catch (IOException ex) {
            inp = getClass().getResourceAsStream((Konfig.PFAD_INPUT).concat(dateiname));
        }

        Workbook wb;
        try {
            wb = WorkbookFactory.create(inp);
        } catch (IOException | InvalidFormatException | EncryptedDocumentException ex) {
            Log.logFehler(Tabellenblattleser.class, ex);
            throw new ParseException(dateiname, 0);
        }

        // Tabellenblatt nicht vorhanden -> leeres Ergebnis
        if (tabellenblatt >= wb.getNumberOfSheets()) {
            return ergebnis;
        }

        Sheet sheet = wb.getSheetAt(tabellenblatt);

        for (Row r : sheet) {
            if (r.getRowNum() >= startzeilennummer) {
                ergebnis.add(parseZeile(r));
            }
        }
        return ergebnis;
    }

    private Wertetupel parseZeile(Row r) {
        DataFormatter df = new DataFormatter();
        Wertetupel w = new Wertetupel();
        for (Cell c : r) {
            try {
                if (c.getColumnIndex() < spaltennamen.length) {
                    w.add(spaltennamen[c.getColumnIndex()], df.formatCellValue(c));
                }
            } catch (IllegalStateException ex) {
                Log.logOffenerPunkt("Fehler in " + dateiname + ", Zelle " + c.getAddress().formatAsString() + "\n\t"
                        + ex.getMessage());
            }
        }
        return w;
    }

}
