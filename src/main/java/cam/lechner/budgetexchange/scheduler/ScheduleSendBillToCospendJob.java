package cam.lechner.budgetexchange.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ScheduleSendBillToCospendJob implements Job{

    @Autowired
    private ScheduleSendBillService service;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        service.processData();
    }
}