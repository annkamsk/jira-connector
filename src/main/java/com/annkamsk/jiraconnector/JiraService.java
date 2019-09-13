package com.annkamsk.jiraconnector;

import com.annkamsk.jiraconnector.auth.OAuthClient;
import com.annkamsk.jiraconnector.models.JiraProject;
import com.annkamsk.jiraconnector.models.JiraProperties;
import com.annkamsk.jiraconnector.models.JiraTaskSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class JiraService {
    private JiraProperties properties;

    private static final String JIRA_API = "https://[domain-name]/rest/api/2/";

    @Autowired
    public JiraService(JiraProperties properties) {
        this.properties = properties;
    }

    public Optional<String> getRequestToken() {
        return new OAuthClient(properties).handleGetRequestTokenAction();
    }

    public void getAccess(String access) {
        new OAuthClient(properties).handleGetAccessToken(access);
    }

    public List<JiraProject> getProjects() {
        String url = JIRA_API + "project";
        String response = getRequest(url);
        return JiraProject.projectListFromJSON(new JSONArray(response));
    }

    public List<String> getVersions(String projectIdOrKey) {
        String url = JIRA_API + "project/" + projectIdOrKey + "/versions";
        String response = getRequest(url);
        return new JSONArray(response).toList().stream()
                .map(obj -> ((HashMap<String, Object>) obj).get("name").toString())
                .collect(Collectors.toList());
    }

    public String createIssue(Object values) {
        String url = JIRA_API + "issue";
        return postRequest(url, values);
    }

    public void importTaskSet(JiraTaskSet taskSet) {
        Map<String, String> epicNameToKey = createEpics(taskSet);
        Map<String, String> moduleNameToKey = createModules(taskSet, epicNameToKey);
        createTasks(taskSet, moduleNameToKey);
    }

    private String getRequest(String url) {
        return new OAuthClient(this.properties).handleRequest(url);
    }

    private String postRequest(String url, Object values) {
        return new OAuthClient(this.properties).handleRequest(url, values);
    }

    /**
     * Makes a request to create all epics in a project and returns a mapping from epic names to their keys.
     */
    private Map<String, String> createEpics(JiraTaskSet taskSet) {
        Map<String, Object> json = taskSet.epicsToJSON();
        JSONObject response = new JSONObject(postRequest(JIRA_API + "issue/bulk", json));

        return getIssueKeys(JiraTaskSet.PREFIX_EPIC_NAMES, response);
    }

    private Map<String, String> createModules(JiraTaskSet taskSet, Map<String, String> epicNameToKey) {
        Map<String, Object> preJson = taskSet.modulesToJSON(epicNameToKey);
        JSONObject json = new JSONObject(preJson);
        JSONObject response = new JSONObject(postRequest(JIRA_API + "issue/bulk", preJson));
        List<String> taskNames = IntStream.range(0, json.getJSONArray("issueUpdates").length()).boxed().map(i ->
                json.getJSONArray("issueUpdates").getJSONObject(i).getJSONObject("fields").getString("summary")
        ).collect(Collectors.toList());
        return getIssueKeys(taskNames, response);
    }

    private void createTasks(JiraTaskSet taskSet, Map<String, String> moduleNameToKey) {
        Map<String, Object> json = taskSet.tasksToJSON(moduleNameToKey);
        postRequest(JIRA_API + "issue/bulk", json);
    }

    /**
     * Associates names from a list with keys returned after creating issues.
     */
    private Map<String, String> getIssueKeys(List<String> names, JSONObject jsonResponse) {
        return IntStream.range(0, names.size()).boxed().collect(Collectors.toMap(
                names::get,
                i -> jsonResponse.getJSONArray("issues").getJSONObject(i).getString("key")
        ));
    }

    private String getIssueName(String idOrKey) {
        final String url = JIRA_API + "issue/" + idOrKey + "?fields=summary";
        return new JSONObject(getRequest(url)).getJSONObject("fields").getString("summary");
    }
}
