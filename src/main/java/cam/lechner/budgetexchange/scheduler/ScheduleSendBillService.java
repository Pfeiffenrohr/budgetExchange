package cam.lechner.budgetexchange.scheduler;


import cam.lechner.budgetexchange.application.SendBillToCospend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ScheduleSendBillService {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleSendBillService.class);
    @Autowired
    SendBillToCospend sendBillToCospend;

    public void processData() {

        sendBillToCospend.getMissingTransactionsAndSendToCospend();
    }
}