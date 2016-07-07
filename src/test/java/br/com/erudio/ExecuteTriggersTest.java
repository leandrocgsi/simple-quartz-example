package br.com.erudio;

import org.junit.Test;

public class ExecuteTriggersTest {
    
    @Test
    public void executeCronTest() throws Exception {
        ExecuteTriggers executeTriggers = new ExecuteTriggers();
        executeTriggers.executeCron();
    }

}
