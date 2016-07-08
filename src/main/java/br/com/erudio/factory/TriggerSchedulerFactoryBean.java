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
                    getCronExpressionInTriggerMethods(beanName, targetClass);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

	private void getCronExpressionInTriggerMethods(String beanName, Class<?> targetClass) throws Exception {
		Object targetObject = applicationContext.getBean(beanName);
		Method[] methods = targetClass.getDeclaredMethods();
		for (Method method : methods) {
		    if (isCronMethod(method)) registerTimerServiceClass(beanName, targetObject, method);
		}
	}

	private void registerTimerServiceClass(String beanName, Object targetObject, Method method) throws Exception {
		String cronExpression = "";
		String targetMethod = "";
		Cron triggerMethod = null;
		targetMethod = method.getName();
		triggerMethod = (Cron) method.getAnnotation(Cron.class);
		cronExpression = triggerMethod.cronExpression();
		registerJobs(targetObject, targetMethod, beanName, cronExpression);
	}

	private boolean isCronMethod(Method method) {
		return method.isAnnotationPresent(Cron.class);
	}

    private void registerJobs(Object targetObject, String targetMethod, String beanName, String cronExpression) throws Exception {
        
        MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = packagingBusinessClass(targetObject, targetMethod, beanName);

        // Access to JobDetail
        JobDetail jobDetail = jobDetailFactoryBean.getObject();
        CronTriggerFactoryBean cronTriggerBean = cofigureTimer(targetMethod, beanName, cronExpression, jobDetail);

        CronTrigger trigger = cronTriggerBean.getObject();
        registerJobsAndTriggersOnFactory(trigger);
    }

	private void registerJobsAndTriggersOnFactory(CronTrigger trigger) throws SchedulerException {
		List<Trigger> triggerList = new ArrayList<Trigger>();
        triggerList.add(trigger);
        Trigger[] triggers = (Trigger[]) triggerList.toArray(new Trigger[triggerList.size()]);
        setTriggers(triggers);
        super.registerJobsAndTriggers();
	}

	private CronTriggerFactoryBean cofigureTimer(String targetMethod, String beanName, String cronExpression, JobDetail jobDetail) {
		CronTriggerFactoryBean cronTriggerBean = new CronTriggerFactoryBean();
        cronTriggerBean.setJobDetail(jobDetail);
        cronTriggerBean.setCronExpression(cronExpression);
        cronTriggerBean.setName(beanName + "_" + targetMethod + "_Trigger");
        cronTriggerBean.setBeanName(beanName + "_" + targetMethod + "_Trigger");
        cronTriggerBean.afterPropertiesSet();
		return cronTriggerBean;
	}

	private MethodInvokingJobDetailFactoryBean packagingBusinessClass(Object targetObject, String targetMethod, String beanName) throws ClassNotFoundException, NoSuchMethodException {
		MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactoryBean.setTargetObject(targetObject);
        jobDetailFactoryBean.setTargetMethod(targetMethod);
        jobDetailFactoryBean.setBeanName(beanName + "_" + targetMethod + "_Task");
        jobDetailFactoryBean.setName(beanName + "_" + targetMethod + "_Task");
        jobDetailFactoryBean.setConcurrent(false);
        jobDetailFactoryBean.afterPropertiesSet();
		return jobDetailFactoryBean;
	}

}