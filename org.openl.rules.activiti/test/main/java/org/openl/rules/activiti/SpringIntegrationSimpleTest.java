package org.openl.rules.activiti;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti-spring.cfg.xml" })
public class SpringIntegrationSimpleTest {

    @Autowired
    private ProcessEngine processEngine;

    @Before
    public void deploy() {
        processEngine.getRepositoryService()
            .createDeployment()
            .addClasspathResource("activiti-definition-spring-integration-test.bpmn20.xml")
            .addClasspathResource("Tutorial1 - Intro to Decision Tables.xlsx")
            .deploy();
    }

    @Test
    public void test() {
        Assert.assertNotNull(processEngine);
        Map<String, Object> variables = new HashMap<String, Object>();

        variables.put("driverAge", "Standard Driver");
        variables.put("driverMaritalStatus", "Single");

        processEngine.getRuntimeService().startProcessInstanceByKey("openLTaskServiceTest", variables);

        Task task = processEngine.getTaskService().createTaskQuery().singleResult();

        Assert.assertEquals("result task 1", task.getName());

        processEngine.getTaskService().complete(task.getId());

        // Test second condition
        variables.put("driverAge", "Senior Driver");
        variables.put("driverMaritalStatus", "Single");

        processEngine.getRuntimeService().startProcessInstanceByKey("openLTaskServiceTest", variables);

        task = processEngine.getTaskService().createTaskQuery().singleResult();

        Assert.assertEquals("result task 2", task.getName());

        processEngine.getTaskService().complete(task.getId());
    }
}
