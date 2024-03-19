package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.apicall.ApiCall;
import cam.lechner.budgetexchange.entity.Kategorie;
import cam.lechner.budgetexchange.entity.MapCategory;
import cam.lechner.budgetexchange.entity.TransactionIds;
import cam.lechner.budgetexchange.entity.Transaktion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class CreateCategories {
    @Autowired
    private ApiCall apicall;
    @Autowired
    private MapCategoryRepository mapCategoryRepository;
    @Autowired
    private MapMemberRepository mapMemberRepository;
    private static final Logger LOG = LoggerFactory.getLogger(CreateCategories.class);

    public void sendCategorysToCospend() {
        List<MapCategory> t = new ArrayList<MapCategory>();
        List<TransactionIds> transIds = new ArrayList<TransactionIds>();
        final Boolean[] errorOccured = {false};

        //Find all Categories
        List<Kategorie> kategories = apicall.getAllCategories();
        String projectname ="budget";


        kategories.forEach(kategorie -> {
            try {
                if (! kategorie.getName().equals("Auto"))
                {
                    return;
                }
                String projectId = projectname;
                MapCategory mapCategory = new MapCategory();
                        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
                        map.add("name", kategorie.getName());
                        map.add("color", "#ffaa00");
                        Integer cospendNumber=apicall.sendCategory(map, projectId);
                mapCategory.setBudgetCategory(kategorie.getId());
                mapCategory.setCospendCategory(cospendNumber);
                mapCategory.setProjectname(projectId);
                if (kategorie.getMode().equals("ausgabe")) {
                    mapCategory.setKind(4);
                }
                else {
                    if (kategorie.getMode().equals("einnahme")) {
                    mapCategory.setKind(5);
                    }
                    else {
                        return;
                    }

                }
                mapCategory.setInout(1);
                mapCategoryRepository.save(mapCategory);

            } catch (Exception e ) {
                LOG.error(" Exception " +e);
                apicall.sendMessageToTalk("@richard [Cospend] !!!! Fehler  +e");
                errorOccured[0] = true;
            }
        });

    }

    private String getPayer(MapCategory map) throws Exception{

        if (map.getKind() == 0) {
            //Miete
            return mapMemberRepository.findByNameAndProject("Mieter",map.getProjectname()).getCospendMemberId()+"";
            //return "59";
        }
        if (map.getKind() == 2) {
            //Ausgaben
            return mapMemberRepository.findByNameAndProject("Hausverwaltung",map.getProjectname()).getCospendMemberId()+"";
            //return "58";
        }
        if (map.getKind() == 1) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Richard",map.getProjectname()).getCospendMemberId()+"";
           // return "58";
        }
        if (map.getKind() == 3) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Hausverwaltung",map.getProjectname()).getCospendMemberId()+"";
            // return "58";
        }
        return "";
    }

    private Boolean notToCalculate(Transaktion trans) {
        if (trans.getKategorie() != 89 && trans.getKategorie() != 122
            && trans.getKategorie() != 39
                && trans.getKategorie() != 139
                && trans.getKategorie() != 142
                && trans.getKategorie() != 155
                && trans.getKategorie() != 158
                && trans.getKategorie() != 71) {
            return false;
        }
        if (trans.getKategorie()== 158  &&  trans.getKonto_id()  != 32) {
            //Alles was Kategorie Rücklagen ist und nocht Konto Sprkasse Giro 2
            return true;
        }
        if (trans.getKategorie()== 155  && trans.getKonto_id()  == 92) {
            //Alles was Kategorie Ahornstrasse 39 ist und Konto Haus Ahornstr. 39
            return true;
        }
        if (trans.getKategorie()== 139  && trans.getKonto_id() != 32) {
            return true;
        }
        if (trans.getKategorie()== 142  && trans.getKonto_id() != 32) {
            return true;
        }
        if (trans.getKategorie()== 71  && isNotMugRueckzahlung(trans)) {
            return true;
        }

        if (trans.getKategorie()== 39  && isNotMugGrundsteuer(trans)) {
            return true;
        }

        if (trans.getKategorie() == 89 && !isCorrectBausparer(trans)) {
            return true;
        }

        if (trans.getKategorie() == 122 && trans.getKonto_id() == 71) {
            return true;
        }
        return false;
    }

    private Boolean isNotMugGrundsteuer(Transaktion trans) {
        if (trans.getName().equals("Grundsteuer Muggensturm")) {
            return false;
        }
        return true;
    }

    private Boolean isNotMugRueckzahlung(Transaktion trans) {
        if (trans.getName().trim().equals("Kredit KFW") && (trans.getKonto_id() == 32 || trans.getKonto_id() == 9)) {
            return false;
        }
        return true;
    }
    private Boolean isCorrectBausparer(Transaktion trans) {
        if ((trans.getKategorie() == 89 && trans.getKonto_id() == 32 && trans.getName().equals("Bausparen WG17"))) {
            return true;
        }
        return false;
    }

    private String getPayedFor(MapCategory map) throws Exception {

        if (map.getKind() == 0) {
            //Miete
            return mapMemberRepository.findByNameAndProject("Hausverwaltung",map.getProjectname()).getCospendMemberId()+"";
            //return "58";
        }
        if (map.getKind() == 2) {
            //ausgaben
            return mapMemberRepository.findByNameAndProject("Mieter",map.getProjectname()).getCospendMemberId()+"";
            //return "59";
        }
        if (map.getKind() == 1) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Mieter",map.getProjectname()).getCospendMemberId()+"";
            //return "9";
        }
        if (map.getKind() == 3) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Richard",map.getProjectname()).getCospendMemberId()+"";
            //return "9";
        }
        return "";
    }

    /*
    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        System.out.println("hello world, I have just started up");
        getMissingTransactions();
    }*/
}
