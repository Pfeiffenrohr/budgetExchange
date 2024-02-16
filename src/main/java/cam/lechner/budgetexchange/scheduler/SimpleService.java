package cam.lechner.budgetexchange.scheduler;


import cam.lechner.budgetexchange.compare.Compare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SimpleService {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleService.class);
    @Autowired
    Compare compare;

    public void processData() {

        compare.getMissingTransactions();
    }
}