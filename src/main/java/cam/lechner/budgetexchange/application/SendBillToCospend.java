package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.apicall.ApiCall;
import cam.lechner.budgetexchange.entity.MapCategory;
import cam.lechner.budgetexchange.entity.TransactionIds;
import cam.lechner.budgetexchange.entity.Transaktion;
import cam.lechner.budgetexchange.service.KontoService;
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
    @Autowired
    private KontoService kontoService;

    private static final Logger LOG = LoggerFactory.getLogger(SendBillToCospend.class);

    public void getMissingTransactionsAndSendToCospend() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE,cal.getActualMaximum(Calendar.DATE));
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        List<MapCategory> t = new ArrayList<MapCategory>();
        List<TransactionIds> transIds = new ArrayList<TransactionIds>();
        final Boolean[] errorOccured = {false};
        mapCategoryRepository.findAll().forEach(t::add);
        compareRepository.findAll().forEach(transIds::add);
        compareRepository.setIscheckedTo0();

        t.forEach(kat -> {
            try {
                String projectId = kat.getProjectname();
                List<Transaktion> trans = apicall.getTransactionWithCategoryAndDate(kat.getBudgetCategory() + "", "2011-01-01", formater.format(cal.getTime()));
                String payer = getPayer(kat);
                String payed_for = getPayedFor(kat);
                for (Transaktion tr : trans) {
                    TransactionIds transactionIds = new TransactionIds();
                    transactionIds = compareRepository.findByBudgetTransIdAndProjectId(tr.getId(), projectId);
                    if (notToCalculate(tr, projectId)) {
                        continue;
                    }
                    if (transactionIds == null) {
                        TransactionIds newtransactionIds = new TransactionIds();
                        newtransactionIds.setBudgetTransId(tr.getId());
                        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
                        map.add("amount", tr.getWert() * kat.getInout() + "");
                        map.add("what", tr.getName());
                        map.add("comment", tr.getBeschreibung());
                        map.add("payer", payer);
                        map.add("repeat", "n");
                        map.add("payedFor", payed_for);
                        map.add("date", tr.getDatum());
                        map.add("categoryId", kat.getCospendCategory() + "");
                        if (kontoService.getMapKontoOrDefault(tr.getKonto_id(),projectId).getCospendKonto() != 0) {
                            map.add("paymentModeId", kontoService.getMapKontoOrDefault(tr.getKonto_id(),projectId).getCospendKonto() + "");
                        }
                        newtransactionIds.setNextcloudBillId(apicall.sendBill(map, projectId));
                        newtransactionIds.setIsChecked(1);
                        newtransactionIds.setProjectId(projectId);
                        compareRepository.save(newtransactionIds);
                        transaktionRepository.save(tr);
                    } else {

                        Transaktion storedTrans = transaktionRepository.findById(transactionIds.getBudgetTransId()).orElseThrow();
                        if (!storedTrans.getName().equals(tr.getName())
                                || storedTrans.getWert() != tr.getWert()
                                || !storedTrans.getBeschreibung().equals(tr.getBeschreibung())) {
                            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
                            map.add("amount", tr.getWert() * kat.getInout() + "");
                            map.add("what", tr.getName());
                            map.add("comment", tr.getBeschreibung());
                            map.add("payer", payer);
                            map.add("repeat", "n");
                            map.add("payedFor", payed_for);
                            map.add("date", tr.getDatum());
                            map.add("id", +transactionIds.getNextcloudBillId() + "");
                            map.add("categoryId", kat.getCospendCategory() + "");
                            if (kontoService.getMapKontoOrDefault(tr.getKonto_id(),projectId).getCospendKonto() != 0) {
                                map.add("paymentModeId", kontoService.getMapKontoOrDefault(tr.getKonto_id(),projectId).getCospendKonto() + "");
                            }
                            apicall.updateBill(map, "/" + transactionIds.getNextcloudBillId(), projectId);
                            if (storedTrans.getKategorie() == tr.getKategorie()) {
                                transactionIds.setIsChecked(1);
                                compareRepository.save(transactionIds);
                            }
                            if (compareRepository.findByBudgetTransIdAndIsChecked(tr.getId(), 0).size() == 0) {
                                // Die neue Transaktion darf nur gespeichert werden, wenn keine andere Transaktion besteht, die noch updated werden muss.
                                transaktionRepository.save(tr);
                            }
                        } else {
                            if (storedTrans.getKategorie() == tr.getKategorie()) {
                                transactionIds.setIsChecked(1);
                                compareRepository.save(transactionIds);
                            }
                        }
                    }

                }
            } catch (Exception e) {
                LOG.error(" Exception " + e);
                apicall.sendMessageToTalk("@richard [Cospend] !!!! Fehler " +e);
                errorOccured[0] = true;
            }
        });
        if (!errorOccured[0]) {
            List<TransactionIds> deleteList = compareRepository.findByIsChecked(0);
            deleteList.forEach(deleteBill -> {
                apicall.deleteBill(deleteBill.getNextcloudBillId() + "", deleteBill.getProjectId());
                compareRepository.delete(deleteBill);
                deleteSaveTransaktion(deleteBill.getBudgetTransId(), transaktionRepository, compareRepository);
            });
        }
    }

    private String getPayer(MapCategory map) throws Exception {

        if (map.getKind() == 0) {
            //Miete
            return mapMemberRepository.findByNameAndProject("Mieter", map.getProjectname()).getCospendMemberId() + "";
            //return "59";
        }
        if (map.getKind() == 2) {
            //Ausgaben
            return mapMemberRepository.findByNameAndProject("Hausverwaltung", map.getProjectname()).getCospendMemberId() + "";
            //return "58";
        }
        if (map.getKind() == 1) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Richard", map.getProjectname()).getCospendMemberId() + "";
            // return "58";
        }
        if (map.getKind() == 3) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Hausverwaltung", map.getProjectname()).getCospendMemberId() + "";
            // return "58";
        }
        if (map.getKind() == 4) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Ausgabe", map.getProjectname()).getCospendMemberId() + "";
            // return "58";
        }
        if (map.getKind() == 5) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Einnahme", map.getProjectname()).getCospendMemberId() + "";
            // return "58";
        }
        return "";
    }

    private Boolean notToCalculate(Transaktion trans, String project) {
        //Rentenversicherung soll nicht beachtet werden !!!
        if (trans.getKonto_id() == 12 || trans.getKonto_id() == 142 || trans.getKonto_id() == 30 || trans.getKonto_id() == 17) {
            return true;
        }

        if (trans.getKategorie() != 89 && trans.getKategorie() != 122
                && trans.getKategorie() != 39
                && trans.getKategorie() != 139
                && trans.getKategorie() != 142
                && trans.getKategorie() != 155
                && trans.getKategorie() != 158
                && trans.getKategorie() != 71) {
            return false;
        }
        if (trans.getKategorie() == 158 && trans.getKonto_id() != 32) {
            //Alles was Kategorie Rücklagen ist und nocht Konto Sprkasse Giro 2
            return true;
        }
        if (trans.getKategorie() == 155 && trans.getKonto_id() == 92) {
            //Alles was Kategorie Ahornstrasse 39 ist und Konto Haus Ahornstr. 39
            return true;
        }
        if (trans.getKategorie() == 139 && trans.getKonto_id() != 32) {
            return true;
        }
        if (trans.getKategorie() == 142 && trans.getKonto_id() != 32) {
            return true;
        }
        if (trans.getKategorie() == 71 && isNotMugRueckzahlung(trans)) {
            return true;
        }

        if (trans.getKategorie() == 39 && isNotMugGrundsteuer(trans, project)) {
            return true;
        }

        if (trans.getKategorie() == 89 && !isCorrectBausparer(trans, project)) {
            return true;
        }

        if (trans.getKategorie() == 122 && trans.getKonto_id() == 71) {
            return true;
        }
        return false;
    }

    private Boolean isNotMugGrundsteuer(Transaktion trans, String project) {
        if (trans.getName().equals("Grundsteuer Muggensturm") && project.equals("muggensturmneu")) {
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

    private Boolean isCorrectBausparer(Transaktion trans, String project) {
        if (project.equals("test2") || project.equals("wg17neu")) {
            if ((trans.getKategorie() == 89 && trans.getKonto_id() == 32 && trans.getName().equals("Bausparen WG17"))) {
                return true;
            }
            return false;
        }
        if (project.equals("wg-26") || project.equals("wg-26-neu") ) {
            if ((trans.getKategorie() == 89 && trans.getKonto_id() == 32 && trans.getName().equals("Bausparen WG26"))) {
                return true;
            }
            return false;
        }
        if (trans.getKategorie() == 89 && (trans.getKonto_id() == 32 || trans.getKonto_id() == 9)) {
            return true;
        }
        return false;
    }

    private String getPayedFor(MapCategory map) throws Exception {

        if (map.getKind() == 0) {
            //Miete
            return mapMemberRepository.findByNameAndProject("Hausverwaltung", map.getProjectname()).getCospendMemberId() + "";
            //return "58";
        }
        if (map.getKind() == 2) {
            //ausgaben
            return mapMemberRepository.findByNameAndProject("Mieter", map.getProjectname()).getCospendMemberId() + "";
            //return "59";
        }
        if (map.getKind() == 1) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Mieter", map.getProjectname()).getCospendMemberId() + "";
            //return "9";
        }
        if (map.getKind() == 3) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Richard", map.getProjectname()).getCospendMemberId() + "";
            //return "9";
        }
        if (map.getKind() == 4) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Hausverwaltung", map.getProjectname()).getCospendMemberId() + "";
            //return "9";
        }
        if (map.getKind() == 5) {
            //Reperaturen
            return mapMemberRepository.findByNameAndProject("Hausverwaltung", map.getProjectname()).getCospendMemberId() + "";
            //return "9";
        }
        return "";
    }

    public void deleteSaveTransaktion(Integer transaktionId, TransaktionRepository transaktionRepository, CompareRepository compareRepository) {
       try {if (compareRepository.findByBudgetTransId(transaktionId).size() < 1) {
               transaktionRepository.deleteById(transaktionId);
           }
       } catch (Exception e) {
           LOG.error("Transaktion ID {} kann nicht gelöscht werden !!!", transaktionId);
       }
    }
}
