package br.com.erudio.factory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import br.com.erudio.config.Cron;
import br.com.erudio.config.QuartzJob;

public class TriggerSchedulerFactoryBean extends SchedulerFactoryBean {

    protected Log log = LogFactory.getLog(TriggerSchedulerFactoryBean.class.getName());

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void registerJobsAndTriggers() throws SchedulerException {
        try {
            // Get all the bean name
            String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
            for (String beanName : beanNames) {
                Class<?> targetClass = applicationContext.getType(beanName);
                // To determine whether marked with the MyTriggerType annotation cycle
                if (targetClass.isAnnotationPresent(QuartzJob.class)) {
                    Object targetObject = applicationContext.getBean(beanName);
                    // Gets the time expression
                    String cronExpression = "";
                    String targetMethod = "";
                    Cron triggerMethod = null;
                    // Determine the marker method of the MyTriggerMethod annotation name
                    Method[] methods = targetClass.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(Cron.class)) {
                            targetMethod = method.getName();
                            triggerMethod = (Cron) method.getAnnotation(Cron.class);
                            cronExpression = triggerMethod.cronExpression();
                            // Register timer service class
                            registerJobs(targetObject, targetMethod, beanName, cronExpression);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void registerJobs(Object targetObject, String targetMethod, String beanName, String cronExpression) throws Exception {
        
    	// The statement packaging business class
        MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactoryBean.setTargetObject(targetObject);
        jobDetailFactoryBean.setTargetMethod(targetMethod);
        jobDetailFactoryBean.setBeanName(beanName + "_" + targetMethod + "_Task");
        jobDetailFactoryBean.setName(beanName + "_" + targetMethod + "_Task");
        jobDetailFactoryBean.setConcurrent(false);
        jobDetailFactoryBean.afterPropertiesSet();

        // Access to JobDetail
        JobDetail jobDetail = jobDetailFactoryBean.getObject();

        // The statement timer
        CronTriggerFactoryBean cronTriggerBean = new CronTriggerFactoryBean();
        cronTriggerBean.setJobDetail(jobDetail);
        cronTriggerBean.setCronExpression(cronExpression);
        cronTriggerBean.setName(beanName + "_" + targetMethod + "_Trigger");
        cronTriggerBean.setBeanName(beanName + "_" + targetMethod + "_Trigger");
        cronTriggerBean.afterPropertiesSet();

        CronTrigger trigger = cronTriggerBean.getObject();

        // The timer is registered to the factroy
        List<Trigger> triggerList = new ArrayList<Trigger>();
        triggerList.add(trigger);
        Trigger[] triggers = (Trigger[]) triggerList.toArray(new Trigger[triggerList.size()]);
        setTriggers(triggers);
        super.registerJobsAndTriggers();
    }

}