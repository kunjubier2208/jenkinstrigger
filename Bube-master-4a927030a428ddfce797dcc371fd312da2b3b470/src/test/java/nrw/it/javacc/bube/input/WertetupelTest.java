package nrw.it.javacc.bube.input;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import nrw.it.javacc.bube.model.Menge;
import nrw.it.javacc.bube.model.Monat;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * @author schul13
 */
public class WertetupelTest {

    @Test
    public void testGetText() {
        Wertetupel t = new Wertetupel();
        t.add("w1", "value w1");
        t.add("w2", "value w2");

        assertThat(t.getText("w1")).isEqualTo("value w1");
        assertThat(t.getText("w2")).isEqualTo("value w2");
    }

    @Test
    public void testGetMenge() {
        Wertetupel t = new Wertetupel();
        t.add("w1", "1,234");
        t.add("w2", "1.021,234");

        assertThat(t.getMenge("w1")).isEqualTo(Menge.from("1,234"));
        assertThat(t.getMenge("w2")).isEqualTo(Menge.from("1021,234"));
    }

    @Test
    public void testGetMonat() {
        Wertetupel t = new Wertetupel();
        t.add("w1", "10");
        t.add("w2", "2018.11");
        
        assertThat(t.getMonat("w1")).isEqualTo(Monat.valueOf("10"));        
        assertThat(t.getMonat("w2")).isEqualTo(Monat.valueOf("11"));
    }

    @Test
    public void testGetDatum() {
        Wertetupel t = new Wertetupel();
        t.add("w1", "2017-02-03");
        t.add("w2", "");
        
        assertThat(t.getDatum("w1").get()).isEqualTo(LocalDate.of(2017, Month.FEBRUARY, 3));
        assertThat(t.getDatum("w2")).isEqualTo(Optional.empty());
    }
}
