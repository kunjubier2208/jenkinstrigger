package nrw.it.javacc.bube.model;

import nrw.it.javacc.bube.Konfig;

/**
 *
 * @author schul13
 */
public class Verbrauchsbuchung {

    private final Monat monat;
    private final String buchungsziel;
    private final String materialnummer;
    private final String buchungsmenge;
    private final String buchungstext;
    private final boolean storno;

    private Verbrauchsbuchung(Monat monat, Buchungsziel buchungsziel, String materialnummer, Menge buchungsmenge, String buchungstext) {
        if (buchungsmenge.istNull()) {
            throw new IllegalArgumentException();
        }

        this.monat = monat;
        this.buchungsziel = buchungsziel.getId();
        this.materialnummer = materialnummer;
        if (buchungsmenge.kleinerNull()) {
            this.buchungsmenge = buchungsmenge.mult(-1).toStringAlsDezimalzahl();
            this.storno = true;
        } else {
            this.buchungsmenge = buchungsmenge.toStringAlsDezimalzahl();
            this.storno = false;
        }
        this.buchungstext = buchungstext;
    }

    public static Verbrauchsbuchung fürPI(Monat monat, Buchungsziel buchungsziel, Menge buchungsmenge) {
        return new Verbrauchsbuchung(monat, buchungsziel, Konfig.MATERIALNUMMER_PI, buchungsmenge, "PI basierend auf Zeitanschreibung");
    }

    public static Verbrauchsbuchung fürPW(Werkzeug werkzeug, Konto konto, Monat monat, Buchungsziel buchungsziel, Menge buchungsmenge) {
        String buchungstext = "Projektwerkzeug " + werkzeug.name() + " fuer " + konto.getId();
        return new Verbrauchsbuchung(monat, buchungsziel, Konfig.MATERIALNUMMER_PW, buchungsmenge, buchungstext);
    }

    public String getSapRepresentation() {
        return Konfig.ABRECHNUNGSZAHR + ";"
                + monat.toString() + ";"
                + Konfig.BUCHUNGSTAG + ";"
                + buchungsziel + ";"
                + materialnummer + ";"
                + buchungsmenge + ";"
                + buchungstext;
    }

    public boolean isStorno() {
        return storno;
    }

    public Monat getMonat() {
        return monat;
    }
}
