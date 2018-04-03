package nrw.it.javacc.bube;

import java.util.Arrays;
import java.util.List;
import nrw.it.javacc.bube.model.Monat;

/**
 *
 * @author schul13
 */
// TODO: Einzelne Werte lokal Konfigurierbar machen wie Abrechnungsmonat!
public class Konfig {

    public static final String PFAD_INPUT = "/";
    public static final String PFAD_OUTPUT = "/out/";
    public static final String ABRECHNUNGSZAHR = "2018";
    public static Monat ABREECHNUNGSMONAT = Monat.valueOf("12");
    public static final String BUCHUNGSZIEL_OFFENE_PUNKTE = "99999999";
    public static final List<String> FREIGESTELLTE_AUFTRAEGE = Arrays.asList("99999990", "99999991", "99999992", "99999993", "99999994", "99999995", "99999996", "99999997", "99999998", BUCHUNGSZIEL_OFFENE_PUNKTE);
    // Mengen unter dieser Grenze werden nicht an SAfIR übergeben
    public static final String MINDERMENGENGRENZE_PERSONAL = "0,004";
    public static final String MATERIALNUMMER_PI = "200000052";
    public static final String MATERIALNUMMER_PW = "200000053";
    // Trotz Periodenbezug wird für SAP ein konkreter Tag für die Abrechnung benötigt.
    public static final String BUCHUNGSTAG = "5";
}
