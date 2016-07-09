package br.com.erudio.job;

import br.com.erudio.config.Cron;
import br.com.erudio.config.QuartzJob;

@QuartzJob
public class MyJob {
    
    @Cron(cronExpression = "0/1 * * * * ?")
    public void execute() {
        System.out.println("Timing task executed per second, a situation");
    }

    @Cron(cronExpression = "0/3 * * * * ?")
    public void execute2() {
        System.out.println("Timing task execution situation, once every three seconds");
    }
}