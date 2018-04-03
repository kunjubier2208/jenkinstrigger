package nrw.it.javacc.bube.model;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * @author schul13
 */
public class MengeTest {

    @Test
    public void testKonstruktion() {
        Menge m1 = Menge.from("1,234");
        assertThat(m1.asDouble()).isEqualTo(1.234d);

        Menge m2 = Menge.from(3);
        assertThat(m2.asDouble()).isEqualTo(3d);

        Menge m3 = Menge.from("-1,234");
        assertThat(m3.asDouble()).isEqualTo(-1.234d);

    }

    @Test
    public void testAdd() {
        Menge m1 = Menge.from("1,234");
        Menge m2 = Menge.from("2,468");
        Menge summe = m1.add(m2);

        assertThat(m1.asDouble()).isEqualTo(1.234d);
        assertThat(m2.asDouble()).isEqualTo(2.468d);
        assertThat(summe.asDouble()).isEqualTo(3.702d);
    }

    @Test
    public void testSub() {
        Menge m1 = Menge.from("1,234");
        Menge m2 = Menge.from("2,468");
        Menge differenz = m1.sub(m2);

        assertThat(m1.asDouble()).isEqualTo(1.234d);
        assertThat(m2.asDouble()).isEqualTo(2.468d);
        assertThat(differenz.asDouble()).isEqualTo(-1.234d);
    }

    @Test
    public void testMult() {
        Menge m1 = Menge.from("1,234");
        Menge produkt = m1.mult(3);

        assertThat(m1.asDouble()).isEqualTo(1.234d);
        assertThat(produkt.asDouble()).isEqualTo(3.702d);
    }

    @Test
    public void testDiv() {
        Menge m1 = Menge.from("1,234");
        Menge quotient = m1.div(3);

        assertThat(m1.asDouble()).isEqualTo(1.234d);
        // Berechnung auf drei Nachkommastellen genau
        assertThat(quotient.asDouble()).isEqualTo(0.411d);
    }

    @Test
    public void testVergleiche() {
        Menge pos = Menge.from("1,234");
        Menge _null = Menge.from(0);
        Menge neg = Menge.from("-1,234");

        assertThat(pos.größerGleichNull()).isTrue();
        assertThat(_null.größerGleichNull()).isTrue();
        assertThat(neg.größerGleichNull()).isFalse();

        assertThat(pos.größerNull()).isTrue();
        assertThat(_null.größerNull()).isFalse();
        assertThat(neg.größerNull()).isFalse();

        assertThat(pos.kleinerNull()).isFalse();
        assertThat(_null.kleinerNull()).isFalse();
        assertThat(neg.kleinerNull()).isTrue();

        assertThat(pos.istNull()).isFalse();
        assertThat(_null.istNull()).isTrue();
        assertThat(neg.istNull()).isFalse();

        assertThat(pos.istGleich(pos)).isTrue();
        assertThat(pos.istGleich(neg)).isFalse();
    }

}
