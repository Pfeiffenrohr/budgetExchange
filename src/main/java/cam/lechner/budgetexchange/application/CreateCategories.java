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
        String projectname ="budgetall";


        kategories.forEach(kategorie -> {
            try {
                if (kategorie.getId() == 98 || kategorie.getId() == 140 || kategorie.getId() == 138 || kategorie.getId() == 143 || kategorie.getId() == 68 ) {
                    return;
                }
                if (mapCategoryRepository.findByBudgetCategoryAndProjectname(kategorie.getId(),projectname) != null) {
                    return;
                }

                String projectId = projectname;
                MapCategory mapCategory = new MapCategory();
                        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();


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
                Integer cospendNumber=apicall.sendCategory(map, projectId);
                mapCategory.setProjectname(projectId);
                map.add("name", kategorie.getName());
                map.add("color", "#ffaa00");
                mapCategory.setBudgetCategory(kategorie.getId());
                mapCategory.setCospendCategory(cospendNumber);
                mapCategory.setInout(1);
                mapCategoryRepository.save(mapCategory);

            } catch (Exception e ) {
                LOG.error(" Exception " +e);
                apicall.sendMessageToTalk("@richard [Cospend] !!!! Fehler  +e");
                errorOccured[0] = true;
            }
        });

    }
}
