package ca.koona.cron_job.configs;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionListener extends JobExecutionListenerSupport {
    @Override
    public void afterJob(JobExecution jobExecution) {
        System.exit(jobExecution.getStatus().isUnsuccessful() ? 1 : 0);
    }
}
