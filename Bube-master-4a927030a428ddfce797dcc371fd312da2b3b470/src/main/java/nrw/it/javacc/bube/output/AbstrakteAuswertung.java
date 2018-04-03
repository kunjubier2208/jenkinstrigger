package nrw.it.javacc.bube.output;

import nrw.it.javacc.bube.Konfig;
import nrw.it.javacc.bube.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Rahmenklasse für Excel-basierte Auswertungen
 *
 * @author schul13
 */
public abstract class AbstrakteAuswertung {

    private final String nameBerichtsdatei;
    private final Workbook wb;
    private final CellStyle styleTitel;
    protected final CellStyle styleHeader;
    protected final CellStyle styleOffenerPunkt;
    protected final CellStyle styleZelleMitUmbruch;
    protected final CellStyle styleZelleOhneUmbruch;

    private final Map<String, CellStyle> styles;

    protected AbstrakteAuswertung(String nameBerichtsdatei) {
        Log.logInfo("Erstelle Bericht " + nameBerichtsdatei);
        this.nameBerichtsdatei = nameBerichtsdatei;
        this.wb = new XSSFWorkbook();
        this.styles = new HashMap<>();

        CellStyle style;
        Font font;

        // Titel
        font = wb.createFont();
        font.setFontHeightInPoints((short) 18);
        font.setBold(true);
        this.styleTitel = wb.createCellStyle();
        this.styleTitel.setAlignment(HorizontalAlignment.LEFT);
        this.styleTitel.setVerticalAlignment(VerticalAlignment.CENTER);
        this.styleTitel.setFont(font);

        font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        this.styleHeader = wb.createCellStyle();
        this.styleHeader.setAlignment(HorizontalAlignment.CENTER);
        this.styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        this.styleHeader.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        this.styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.styleHeader.setFont(font);
        this.styleHeader.setWrapText(true);

        font = wb.createFont();
        font.setColor(IndexedColors.DARK_RED.getIndex());
        this.styleOffenerPunkt = wb.createCellStyle();
        this.styleOffenerPunkt.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        this.styleOffenerPunkt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.styleOffenerPunkt.setFont(font);
        this.styleOffenerPunkt.setWrapText(true);

        this.styleZelleMitUmbruch = wb.createCellStyle();
        this.styleZelleMitUmbruch.setWrapText(true);

        this.styleZelleOhneUmbruch = wb.createCellStyle();

    }

    protected void schreibeBericht() {
        // TODO: Böser Hack
        try {
            Files.createDirectories(Paths.get(System.getProperty("user.dir"), Konfig.PFAD_OUTPUT));
            FileOutputStream result = new FileOutputStream(System.getProperty("user.dir") + Konfig.PFAD_OUTPUT + nameBerichtsdatei);
            wb.write(result);
        } catch (IOException ex) {
            try {
                Files.createDirectories(Paths.get(Konfig.PFAD_OUTPUT));
                FileOutputStream result = new FileOutputStream(Konfig.PFAD_OUTPUT + nameBerichtsdatei);
                wb.write(result);
            } catch (IOException ex2) {
                Log.logFehler(AbstrakteAuswertung.class, ex2);
            }
        }
    }

    protected Sheet erstelleTabellenblatt(String titel) {
        Sheet sheet = wb.createSheet(titel);
        sheet.createRow(0).createCell(0).setCellValue(titel);
        sheet.getRow(0).getCell(0).setCellStyle(this.styleTitel);
        Row r = sheet.createRow(2);
        r.createCell(0).setCellValue("Stand: ");
        r.createCell(1).setCellValue(LocalDateTime.now().toString());
        return sheet;
    }

    protected Koordinate getErsteBerichtskoordinate() {
        return new Koordinate();
    }

    protected void formatiereTabellenblatt(Sheet sheet) {
        int maxSpalten = 0;
        Iterator<Row> rowIt = sheet.iterator();
        while (rowIt.hasNext()) {
            int test = rowIt.next().getLastCellNum();
            if (test > maxSpalten) {
                maxSpalten = test;
            }
        }

        for (int i = 1; i <= maxSpalten; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    protected void erstelleZellen(Sheet sheet, Koordinate start, CellStyle style, Object... zellinhalte) {
        Row row = sheet.getRow(start.zeile);
        if (null == row) {
            row = sheet.createRow(start.zeile);
        }

        for (Object o : zellinhalte) {
            Cell c = row.createCell(start.nS().spalte);
            if (String.class.isAssignableFrom(o.getClass())) {
                c.setCellValue((String) o);
            } else if (Double.class.isAssignableFrom(o.getClass())) {
                c.setCellValue((Double) o);
            } else {
                throw new IllegalArgumentException("Nur String / Double implementiert.");
            }
            c.setCellStyle(style);
        }
    }

    protected void markiereAlsOffenenPunkt(Sheet sheet, Koordinate koordinate, String nachricht) {
        Cell c = sheet.getRow(koordinate.zeile).getCell(koordinate.spalte);
        // 1. Farblich hervorheben
        c.setCellStyle(styleOffenerPunkt);
        // 2. Nachricht als Kommentar ergänzen
        Drawing d = sheet.getDrawingPatriarch();
        if (null == d) {
            d = sheet.createDrawingPatriarch();
        }
        CreationHelper factory = wb.getCreationHelper();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(koordinate.spalte);
        anchor.setCol2(koordinate.spalte + 2);
        anchor.setRow1(koordinate.zeile);
        anchor.setRow2(koordinate.zeile + 3);

        Comment comment = d.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(nachricht);
        comment.setString(str);
        comment.setAuthor("System");

        c.setCellComment(comment);

    }

    protected static class Koordinate {

        protected int zeile = 4; // Davor Kopfzeilen
        protected int spalte = -1;

        public Koordinate nZ() {
            zeile++;
            spalte = -1;
            return this;
        }

        public Koordinate nS() {
            spalte++;
            return this;
        }

    }

}
