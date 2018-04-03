package nrw.it.javacc.bube.input;

import java.util.List;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * @author schul13
 */
public class TabellenblattleserTest {
    
    public TabellenblattleserTest() {
    }

    @Test
    public void testLese() throws Exception {
        // TODO: Meines Erachtens sollten hier nur drei Kopfzeilen sein! Potentieller Bug!
        Tabellenblattleser tbl = new Tabellenblattleser("TabellenblattleserTest.xlsx", 
                0, 4, "Spalte 1", "Spalte 2", "Spalte 3");
        
        List<Wertetupel> result = tbl.lese();

        assertThat(result.size()).isEqualTo(3);

        // Potentiell gefährlich, Reihenfolge der Tupel nicht garantiert
        assertThat(result.get(0).getText("Spalte 3")).isEqualTo("Wert 1.3");
        assertThat(result.get(1).getText("Spalte 1")).isEqualTo("Wert 2.1");
        assertThat(result.get(2).getText("Spalte 2")).isEqualTo("");
    }
        
}
