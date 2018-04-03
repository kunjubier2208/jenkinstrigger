package nrw.it.javacc.bube;

import com.google.common.base.Joiner;
import nrw.it.javacc.bube.model.Monat;
import java.io.PrintStream;
import java.util.Formatter;

/**
 *
 * @author schul13
 */
public class Log {

    private static int counter = 0;
    private static final PrintStream OUT = System.out;
    private static final String TRENNER = "================================================================================";

    public static void logLeseInfo(String id1, String id2, Monat m, Integer datensatzanzahl) {
        Formatter f = new Formatter(OUT);
        String format = "%1$-21s - %2$-19s %3$6s: %4$16s \n";
        f.format(format, 
                id1,
                id2==null? "": id2,
                m==null? "" : "["+m.toString()+"]",
                datensatzanzahl==null?"--      " : datensatzanzahl.toString()+" Datensätze");        
    }

    public static void logInfo(String... nachricht) {
        OUT.println(Joiner.on(" ").join(nachricht));
    }

    public static void logWarnung(String nachricht) {
        OUT.println("Warnung: " + nachricht);
    }

    public static void logOffenerPunkt(String... nachricht) {
        OUT.println("TODO: " + Joiner.on(" ").join(nachricht));
    }

    public static void logFehler(Class cls, Exception ex) {
        OUT.println("FEHLER in Klasse: " + cls.getCanonicalName() + "\n\t" + ex.getMessage());
    }
    
    public static void logFortschrittStart(String nachricht){
        OUT.println(nachricht);
        counter = 0;
    }
    
    public static void logFortschritt(){
        if(counter++<30){
            OUT.print(".");
        } else {
            OUT.println(".");
            counter = 0;
        }
    }

    public static void logFortschrittEnde(){
        OUT.println();
    }
    
    public static void beginne(String abschnittsname) {
        OUT.print("\n\n" + TRENNER + "\n");
        OUT.print("========== " + abschnittsname + " ");
        int ziel = TRENNER.length() - abschnittsname.length() - 12;
        for (int i = 0; i < ziel; i++) {
            OUT.print("=");
        }
        OUT.print("\n" + TRENNER + "\n");
    }

}
