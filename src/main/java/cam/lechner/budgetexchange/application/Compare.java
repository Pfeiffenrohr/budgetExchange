package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.apicall.ApiCall;
import cam.lechner.budgetexchange.cospend.BillRespond;
import cam.lechner.budgetexchange.cospend.Bills;
import cam.lechner.budgetexchange.entity.TransactionIds;
import cam.lechner.budgetexchange.scheduler.ScheduleSendBillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Compare {

    @Autowired
    private ApiCall apicall;

    @Autowired
    private CompareRepository compareRepository;

    private static final Logger LOG = LoggerFactory.getLogger(Compare.class);

    public void doCompare() {
        compareCospendBudget();
        compareBudgetCospend();
    }

    public void compareCospendBudget() {
        //LOG.info("Start compareCospendBudget");
        //Liest alle Rechnungen von cospend und überprüft, ob alle in Budget voehanden sind
        BillRespond respond = apicall.getAllBills("budgetall");
        //LOG.info("Found {} bills", respond.getOcs().getData().getNb_bills());
        List<Integer> allNextcloudIds = compareRepository.findNextCloudBillIdsByProject("budgetall");
        for (int i = 0; i < respond.getOcs().getData().getBills().length; i++) {
            if (! allNextcloudIds.contains(respond.getOcs().getData().getBills()[i].getId())) {
                LOG.warn(" Bill not found in Budget " + respond.getOcs().getData().getBills()[i].getId());
                LOG.warn("Delete {} in cospend, because it was not in Budget", respond.getOcs().getData().getBills()[i].getId());
                if (apicall.deleteBill(respond.getOcs().getData().getBills()[i].getId().toString(), "budgetall")) {
                    LOG.info("Bill {} successfully deleted");
                    apicall.sendMessageToTalk("Deleted bill " + respond.getOcs().getData().getBills()[i].getId() + " in Cospend");
                } else {
                    LOG.error("!! Can not delete bill {} in Cospend", respond.getOcs().getData().getBills()[i].getId());
                    apicall.sendMessageToTalk("!!!Can not delete " + respond.getOcs().getData().getBills()[i].getId() + " in Cospend");
                }
            }
        }
        //LOG.info("End compareCospendBudget");
    }

    public void compareBudgetCospend() {
        //LOG.info("Start comparing BudgetCospend");
        List<TransactionIds> allBudgetids = compareRepository.findByProjectId("budgetall");
        //LOG.info("Found {} bills in Budget", allBudgetids.size());
        BillRespond respond = apicall.getAllBills("budgetall");
        List<Integer> listCospend = Arrays.asList(respond.getOcs().getData().getAllBillIds());
        allBudgetids.forEach( transaction -> {
            if (! listCospend.contains(transaction.getNextcloudBillId())) {
                LOG.warn("ID is misssing in Cospend {}",transaction.getBudgetTransId() );
                compareRepository.deleteById(transaction.getId());
                apicall.sendMessageToTalk("Deleted " + transaction.getBudgetTransId() + " in Budget, because it was not in Cospend");
            }
        });
        //LOG.info("End comparing BudgetCospend");
    }
}