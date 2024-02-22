package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.apicall.ApiCall;
import cam.lechner.budgetexchange.entity.MapCategory;
import cam.lechner.budgetexchange.entity.TransactionIds;
import cam.lechner.budgetexchange.entity.Transaktion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class SendBillToCospend {
    @Autowired
    private ApiCall apicall;
    @Autowired
    private CompareRepository compareRepository;

    @Autowired
    private TransaktionRepository transaktionRepository;

    @Autowired
    private MapCategoryRepository mapCategoryRepository;
    @Autowired
    private MapMemberRepository mapMemberRepository;
    private static final Logger LOG = LoggerFactory.getLogger(SendBillToCospend.class);

    public void getMissingTransactionsAndSendToCospend() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        List<MapCategory> t = new ArrayList<MapCategory>();
        List<TransactionIds> transIds = new ArrayList<TransactionIds>();
        final Boolean[] errorOccured = {false};
        mapCategoryRepository.findAll().forEach(t::add);

        compareRepository.findAll().forEach(transIds::add);
        transIds.forEach(trans -> {
            //set all isCheked to 0;
            trans.setIsChecked(0);
            compareRepository.save(trans);

        });

        //compareRepository.updateIsChecked(0);

        t.forEach(kat -> {
            try {
                String projectId = kat.getProjectname();
                List<Transaktion> trans = apicall.getTransactionWithCategoryAndDate(kat.getBudgetCategory() + "", "2013-01-01", formater.format(cal.getTime()));
                String payer = getPayer(kat);
                String payed_for = getPayedFor(kat);
                for (Transaktion tr : trans) {
                    TransactionIds transactionIds = new TransactionIds();
                    transactionIds = compareRepository.findByBudgetTransId(tr.getId());
                    if (transactionIds == null) {
                        if (notToCalculate(tr)) {
                            continue;
                        }
                        TransactionIds newtransactionIds = new TransactionIds();
                        newtransactionIds.setBudgetTransId(tr.getId());
                        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
                        map.add("amount", tr.getWert() * kat.getInout() + "");
                        map.add("what", tr.getName());
                        map.add("payer", payer);
                        map.add("repeat", "n");
                        map.add("payed_for", payed_for);
                        map.add("date", tr.getDatum());
                        // map.add("categoryid", mapCategoryRepository.findByBudgetCategory(tr.getKategorie()).getCospendCategory() + "");
                        map.add("categoryid", kat.getCospendCategory() + "");
                        newtransactionIds.setNextcloudBillId(apicall.sendBill(map, projectId));
                        newtransactionIds.setIsChecked(1);
                        newtransactionIds.setProjectId(projectId);
                        compareRepository.save(newtransactionIds);
                        transaktionRepository.save(tr);
                    } else {

                        Transaktion storedTrans = transaktionRepository.findById(transactionIds.getBudgetTransId()).orElseThrow();
                        if (!storedTrans.getName().equals(tr.getName()) || storedTrans.getWert() != tr.getWert()) {
                            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
                            map.add("amount", tr.getWert() * kat.getInout() + "");
                            map.add("what", tr.getName());
                            map.add("payer", payer);
                            map.add("repeat", "n");
                            map.add("payed_for", payed_for);
                            map.add("date", tr.getDatum());
                            map.add("id", +transactionIds.getNextcloudBillId() + "");
                            map.add("categoryid", kat.getCospendCategory() + "");
                            apicall.updateBill(map, "/" + transactionIds.getNextcloudBillId(), projectId);
                            transaktionRepository.save(tr);

                        }
                        transactionIds.setIsChecked(1);
                        compareRepository.save(transactionIds);
                    }

                }
            } catch (Exception e ) {
                LOG.error(" Exception " +e);
                apicall.sendMessageToTalk("[Cospend] !!!! Fehler  +e");
                errorOccured[0] = true;
            }
        });
        if ( ! errorOccured[0]) {
            List<TransactionIds> deleteList = compareRepository.findByIsChecked(0);
            deleteList.forEach(deleteBill -> {
                apicall.deleteBill(deleteBill.getNextcloudBillId() + "", deleteBill.getProjectId());
                compareRepository.delete(deleteBill);
                transaktionRepository.deleteById(deleteBill.getBudgetTransId());
            });
        }
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
                && trans.getKategorie() != 71) {
            return false;
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
