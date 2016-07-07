package br.com.erudio;

import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ExecuteTriggers {
    
    ApplicationContext context;

    public void executeCron() throws Exception {
        context = new ClassPathXmlApplicationContext("classpath:config/myquartz-annoconfig.xml");
        TimeUnit.SECONDS.sleep(8);
    }
}