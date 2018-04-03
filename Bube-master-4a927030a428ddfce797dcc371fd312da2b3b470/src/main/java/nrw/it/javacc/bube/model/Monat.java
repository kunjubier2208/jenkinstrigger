package nrw.it.javacc.bube.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nrw.it.javacc.bube.Konfig;

/**
 * Definition von Monaten TODO: In Periode umbenennen und die Formate jjjj.mm,
 * sowie mm unterstützen?!
 *
 * @author schul13
 */
public class Monat {

    private static final Monat[] values = {
        new Monat(0, "01"),
        new Monat(1, "02"),
        new Monat(2, "03"),
        new Monat(3, "04"),
        new Monat(4, "05"),
        new Monat(5, "06"),
        new Monat(6, "07"),
        new Monat(7, "08"),
        new Monat(8, "09"),
        new Monat(9, "10"),
        new Monat(10, "11"),
        new Monat(11, "12")
    };

    int index;
    String name;

    private Monat(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public static Monat valueOf(String name) {
        // ggf. führende Null ergänzen
        name = name.length() == 1 ? "0" + name : name;
        for (Monat m : values) {
            if (m.name.equals(name)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Wert " + name + " ist ungültige Monatsbezeichnung.");
    }

    public static Monat[] values() {
        return Arrays.copyOf(values, values.length);
    }

    public static Stream<Monat> getMonate(Monat anfang, Monat ende) {
        return Stream.of(values).filter(m -> m.index >= anfang.index && m.index <= ende.index);
    }

    public static Stream<Monat> getAbrechnungsmonate() {
        return Stream.of(values).filter(m -> m.index >= 0 && m.index <= Konfig.ABREECHNUNGSMONAT.index);
    }

    public static List<Monat> getAbrechnungsmonateAsList() {
        return Stream.of(values).filter(m -> m.index >= 0 && m.index <= Konfig.ABREECHNUNGSMONAT.index).collect(Collectors.toList());
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.name);
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
        final Monat other = (Monat) obj;
        return Objects.equals(this.name, other.name);
    }

}
