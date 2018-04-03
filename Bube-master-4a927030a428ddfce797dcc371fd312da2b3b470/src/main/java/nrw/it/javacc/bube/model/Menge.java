package nrw.it.javacc.bube.model;

import java.text.DecimalFormat;

/**
 * Mengenangaben ....
 *
 * Arbeitet wie die SAfIR-Verrechnung mit drei Nachkommastellen.
 *
 * @author schul13
 */
public class Menge {

    private final int tausenstel;

    Menge(int tausenstel) {
        this.tausenstel = tausenstel;
    }

    public static Menge from(String menge) {
        if (menge.isEmpty()) {
            return Menge.from(0);
        }
        float tausendstel = 1000f * Float.parseFloat(menge.replace(",", "."));
        // Nachkommastellen als nicht Buchbar wegfallen lassen
        return new Menge((int) tausendstel);
    }

    public static Menge from(int i) {
        return new Menge(i * 1000);
    }

    public Menge add(Menge menge) {
        if (null == menge) {
            return this;
        }
        return new Menge(tausenstel + menge.tausenstel);
    }

    public Menge div(int i) {
        return new Menge(tausenstel / i);
    }

    public Menge mult(int i) {
        return new Menge(tausenstel * i);
    }

    public Menge sub(Menge menge) {
        if (null == menge) {
            return this;
        }
        return new Menge(tausenstel - menge.tausenstel);
    }

    public boolean istNull() {
        return 0 == tausenstel;
    }

    public boolean istGleich(Menge m) {
        return tausenstel == m.tausenstel;
    }

    public boolean größerNull() {
        return tausenstel > 0;
    }

    public boolean kleinerNull() {
        return tausenstel < 0;
    }

    public boolean größerGleichNull() {
        return tausenstel >= 0;
    }
    
    public String toStringAlsDezimalzahl() {
        return new DecimalFormat("0.###").format(((double) tausenstel) / 1000d).replace(".", ",");
    }

    public Double asDouble() {
        return new Double(toStringAlsDezimalzahl().replace(",", "."));
    }

    @Override
    public String toString() {
        return new DecimalFormat("0.000").format(((double) tausenstel) / 1000d).replace(".", ",");
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.tausenstel;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Menge other = (Menge) obj;
        if (this.tausenstel != other.tausenstel) {
            return false;
        }
        return true;
    }
}
