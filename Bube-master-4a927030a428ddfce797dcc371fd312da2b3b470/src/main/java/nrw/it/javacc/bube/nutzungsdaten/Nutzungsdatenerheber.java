package nrw.it.javacc.bube.nutzungsdaten;

import java.util.Scanner;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

/**
 *
 * @author schul13
 */
public class Nutzungsdatenerheber {
    
    public static void erhebeJiraDaten() {
        // Anmelden am Server
        Scanner scanner = new Scanner(System.in);
        System.out.println("Benutzername");
        String benutzername = scanner.next();
        System.out.println("Passwort");
        String passwort = scanner.next();

//        // Client initiieren
//        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(benutzername, passwort);
//        Client client = ClientBuilder.newClient(new ClientConfig().register(feature));
//        WebTarget target = client.target("https://jira.it.nrw.de").path("https://jira.it.nrw.de/secure/admin/user/ViewUserProjectRoles!default.jspa");
//
//        System.out.println("Anzahl Externer Konten in Betrachtung: " + externeBenutzer.length + "\n\n");
//
//        // Abfragen
//        for (String name : externeBenutzer) {
//            Response get = target.queryParam("name", name).request(MediaType.APPLICATION_JSON_TYPE).get();
//            String entity = get.readEntity(String.class);
//            String[] projekte = StringUtils.substringsBetween(entity, "<td class=\"cell-type-key\">", "</td>");
//            System.out.println(name + ": " + Arrays.toString(projekte));
//            if (null != projekte) {
//                for (String projekt : projekte) {
//                    if (externeImProjekt.containsKey(projekt)) {
//                        externeImProjekt.put(projekt, 1 + externeImProjekt.get(projekt));
//                    } else {
//                        externeImProjekt.put(projekt, 1);
//                    }
//                }
//            }
//        }
//
//        System.out.println("\n\nProjekte mit Anzahl der externen Nutzer (ohne rein interne Projekte)\n");
//        for (Map.Entry<String, Integer> e : externeImProjekt.entrySet()) {
//            System.out.println(e.getKey() + "\t" + e.getValue());
//        }

    }
}
