package nrw.it.javacc.bube.validierung;

import nrw.it.javacc.bube.Log;
import nrw.it.javacc.bube.model.Buchungsziel;
import nrw.it.javacc.bube.model.Repository;

/**
 *
 * @author schul13
 */
public class PostValidierer {

    public static void validiere() {
        obsoleteBuchungsziele();
    }

    private static void obsoleteBuchungsziele() {
        Log.logInfo("\n\n=== Identifiziere alle potentiell obsoleten Buchungsziele.\n"
                + "    (Keine direkte Buchungen, Nutzungen, …)\n");

        for (Buchungsziel buchungsziel : Repository.getBuchungsziele()) {
            if (!buchungsziel.hasDatenZurAbrechnung()) {
                Log.logInfo(buchungsziel.getId() + " wird womöglich nicht genutzt");
            }
        }
    }
}
