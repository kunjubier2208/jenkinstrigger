package nrw.it.javacc.bube.input;

import nrw.it.javacc.bube.model.Konto;
import nrw.it.javacc.bube.model.Monat;
import nrw.it.javacc.bube.model.Repository;
import nrw.it.javacc.bube.model.Werkzeug;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;

/**
 * @author schul13
 */
public class DatenleserTest {
// TODO: auf JUnit 5 Umstellen und abhängigkeit der tests pflegen oder Mocks verwenden.

    @Before
    public void init() {
        Repository.clear();
    }

    @Test
    public void test1LeseStammdatenKonten() {
        Datenleser.leseStammdatenKonten();

        assertThat(Repository.getKonten().size()).isEqualTo(3);
        Konto test1 = Repository.getKonto("test1").get();
        assertThat(test1.getId()).isEqualTo("test1");
        assertThat(test1.getName()).isEqualTo("Mustermann, Max");
        assertThat(test1.getLand()).isEqualTo("DE-NW");
        assertThat(test1.getReferat()).isEqualTo("Ref. 343");
        assertThat(test1.getEmail()).isEqualTo("test1@example.com");
        Konto test3 = Repository.getKonto("test3").get();
        assertThat(test3.getReferat()).isEqualTo("Ref. 369");

        assertThat(Repository.getWiedervorlage().size()).isEqualTo(2);
    }

    @Test
    public void test2LeseStammdaten2Buchungsziele() {
        Datenleser.leseStammdatenKonten();
        Datenleser.leseStammdatenBuchungsziele();

        assertThat(Repository.getBuchungsziele().size()).isEqualTo(4);
        assertThat(Repository.getBuchungszieleByVerantwortung(Repository.getKonto("test2").get()).size()).isEqualTo(2);
        assertThat(Repository.getBuchungszielFürPI("11123456").get().getId()).isEqualTo("10123456");
        assertThat(Repository.getBuchungszielFürPI("11223456").get().getId()).isEqualTo("99999990");
        assertThat(Repository.getBuchungszielFürPW("11223456").get().getId()).isEqualTo("10223456");

        assertThat(Repository.getWiedervorlage().size()).isEqualTo(3);
    }

    @Test
    public void test3LeseAbrechnungsregelnPI() {
        Datenleser.leseStammdatenKonten();
        Datenleser.leseStammdatenBuchungsziele();
        Datenleser.leseAbrechnungsregelnPI();

        assertThat(Repository.getKonto("test1").get().isAbrechnungViaPI(Monat.valueOf("01"))).isEqualTo(true);
        assertThat(Repository.getKonto("test1").get().isAbrechnungViaPI(Monat.valueOf("11"))).isEqualTo(true);
        assertThat(Repository.getKonto("test1").get().isAbrechnungViaPI(Monat.valueOf("12"))).isEqualTo(false);

        assertThat(Repository.getKonto("test2").get().isAbrechnungViaPI(Monat.valueOf("01"))).isEqualTo(false);
    }

    @Test
    public void test4LeseAbrechnungsregelnPW() {
        Datenleser.leseStammdatenKonten();
        Datenleser.leseStammdatenBuchungsziele();
        Datenleser.leseAbrechnungsregelnPW();

        assertThat(Repository.getKonto("test1").get().getAbrechnungsregelnPW(Monat.valueOf("12"), Werkzeug.CI)).isEmpty();

        assertThat(Repository.getKonto("test2").get().getAbrechnungsregelnPW(Monat.valueOf("01"), Werkzeug.CI)).hasSize(1);
        assertThat(Repository.getKonto("test2").get().getAbrechnungsregelnPW(Monat.valueOf("01"), Werkzeug.JE)).hasSize(1);
        assertThat(Repository.getKonto("test2").get().getAbrechnungsregelnPW(Monat.valueOf("03"), Werkzeug.CI)).hasSize(1);
        assertThat(Repository.getKonto("test2").get().getAbrechnungsregelnPW(Monat.valueOf("03"), Werkzeug.JE)).hasSize(1);
        assertThat(Repository.getKonto("test2").get().getAbrechnungsregelnPW(Monat.valueOf("04"), Werkzeug.CI)).hasSize(1);
        assertThat(Repository.getKonto("test2").get().getAbrechnungsregelnPW(Monat.valueOf("04"), Werkzeug.JE)).isEmpty();    
    }

    @Test
    public void test5LeseWerkzeugnutzung() {
        Datenleser.leseStammdatenKonten();
        Datenleser.leseStammdatenBuchungsziele();
        Datenleser.leseWerkzeugnutzung();
                

        assertThat(Repository.getKonto("test1").get().hasWerkzeugnutzung(Werkzeug.JI, Monat.valueOf("01"))).isTrue();
        assertThat(Repository.getKonto("test2").get().hasWerkzeugnutzung(Werkzeug.JI, Monat.valueOf("02"))).isTrue();
        assertThat(Repository.getKonto("test3").get().hasWerkzeugnutzung(Werkzeug.JI, Monat.valueOf("01"))).isFalse();
        assertThat(Repository.getKonto("test1").get().hasWerkzeugnutzung(Werkzeug.JI, Monat.valueOf("03"))).isFalse();
        assertThat(Repository.getKonto("test1").get().hasWerkzeugnutzung(Werkzeug.CI, Monat.valueOf("01"))).isFalse();    
    }

    @Test
    public void test6LesePersonalaufwand() {
        Datenleser.leseStammdatenKonten();
        Datenleser.leseStammdatenBuchungsziele();
        Datenleser.lesePersonalaufwand();
                
        //TODO: Test entwickeln (Personalaufwandsbuchungen sind zZ nicht direkt zugreifbar
    }
    
    @Test
    public void test7LeseIstBuchungen() {
        Datenleser.leseStammdatenKonten();
        Datenleser.leseStammdatenBuchungsziele();
        Datenleser.leseIstBuchungen();
                
        //TODO: Test entwickeln (Personalaufwandsbuchungen sind zZ nicht direkt zugreifbar
    }
    
}
