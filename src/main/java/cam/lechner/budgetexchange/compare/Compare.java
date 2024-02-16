package cam.lechner.budgetexchange.compare;

import cam.lechner.budgetexchange.apicall.ApiCall;
import cam.lechner.budgetexchange.entity.Transaktion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class Compare {
    @Autowired
    private ApiCall apicall;
    @Autowired
    private CompareRepository compareRepository;

    @Autowired
    private MapCategoryRepository mapCategoryRepository;
    public void getMissingTransactions() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        List<MapCategory> t = new ArrayList<MapCategory>();
        mapCategoryRepository.findAll().forEach(t::add);
        t.forEach( kat -> {
            List<Transaktion> trans = apicall.getTransactionWithCategoryAndDate(kat.getBudgetCategory()+"", "2019-01-01", formater.format(cal.getTime()));
           String payer = getPayer(kat);
           String payed_for = getPayedFor(kat);
            for (Transaktion tr : trans) {
                if (compareRepository.findByBudgetTransId(tr.getId()) == null) {
                    if (notToCalculate(tr))
                    {
                        continue;
                    }
                    TransactionIds transactionIds = new TransactionIds();
                    transactionIds.setBudgetTransId(tr.getId());
                    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
                    map.add("amount", tr.getWert() * kat.getInout() + "");
                    map.add("what", tr.getName());
                    map.add("payer", payer);
                    map.add("repeat", "n");
                    map.add("payed_for", payed_for);
                    map.add("date", tr.getDatum());
                   // map.add("categoryid", mapCategoryRepository.findByBudgetCategory(tr.getKategorie()).getCospendCategory() + "");
                    map.add("categoryid", kat.getCospendCategory()+"");
                    transactionIds.setNextcloudBillId(apicall.sendBill(map));
                    compareRepository.save(transactionIds);
                }
            }
        });
    }

    private String getPayer(MapCategory map) {

        if (map.getKind() == 0 ) {
            //Miete
            return "59";
        }
        if (map.getKind() == 2 ) {
            //Ausgaben
            return "58";
        }
        if (map.getKind() == 1 ) {
            //Reperaturen
            return "58";
        }
       return "";
    }

    private Boolean notToCalculate(Transaktion trans) {
        if (trans.getKategorie() != 89 && trans.getKategorie() != 122 ) {
            return false;
        }

        if (trans.getKategorie() == 89 &&  ! isCorrectBausparer(trans) )
        {
            return true;
        }

        if (trans.getKategorie() == 122 && trans.getKonto_id() == 71)
        {
            return true;
        }
        return false;
    }

    private Boolean isCorrectBausparer (Transaktion trans) {
        if ((trans.getKategorie()==89 && trans.getKonto_id()== 32 && trans.getName().equals("Bausparen WG17"))) {
            return true;
        }
        return false;
    }
    private String getPayedFor(MapCategory map) {

        if (map.getKind() == 0 ) {
            //Miete
            return "58";
        }
        if (map.getKind() == 2 ) {
            //ausgaben
            return "59";
        }
        if (map.getKind() == 1 ) {
            //Reperaturen
            return "9";
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
