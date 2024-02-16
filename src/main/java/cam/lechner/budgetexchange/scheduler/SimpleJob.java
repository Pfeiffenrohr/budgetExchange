package cam.lechner.budgetexchange.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

public class SimpleJob implements Job{

    @Autowired
    private SimpleService service;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        service.processData();
    }
}