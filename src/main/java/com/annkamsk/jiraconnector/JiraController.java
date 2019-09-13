package com.annkamsk.jiraconnector;

import com.annkamsk.jiraconnector.models.JiraProject;
import com.annkamsk.jiraconnector.models.JiraTask;
import com.annkamsk.jiraconnector.models.JiraTaskSet;
import com.annkamsk.jiraconnector.models.TaskPriority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class JiraController {
    private static final Logger logger = LogManager.getLogger(JiraController.class);

    private JiraService jiraService;

    @Autowired
    public JiraController(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @RequestMapping(value = "/createIssue", method = RequestMethod.GET)
    public ResponseEntity<String> createIssue() {
        JiraTaskSet taskSet = new JiraTaskSet();
        taskSet.setProjectKey("TEST");

        JiraTask task = taskSet.createTask();

        task.setName("Undertask");
        task.setDescription("Description.");
        task.setModule("Overtask");
        task.setType("DEV");
        task.setTimeEstimate(5.0);
        task.setPriority(TaskPriority.MEDIUM);

        taskSet.addTask(task);

        jiraService.importTaskSet(taskSet);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @RequestMapping(value = "/getJIRAProjects", method = RequestMethod.GET)
    public HttpEntity<List<JiraProject>> getProjects() {
        var projects = jiraService.getProjects();
        projects.forEach(p -> logger.info(p.toString()));
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @RequestMapping(value = "/requestToken", method = RequestMethod.GET)
    public ResponseEntity<String> requestToken() {
        var url = jiraService.getRequestToken();
        return new ResponseEntity<>(url.orElse("Couldn't retrieve request token"), HttpStatus.OK);
    }

    @RequestMapping(value = "/accessToken/{secret}", method = RequestMethod.GET)
    public ResponseEntity<String> accessToken(@PathVariable(value = "secret") String secret) {
        jiraService.getAccess(secret);
        return new ResponseEntity<>((HttpStatus.OK));
    }

}
