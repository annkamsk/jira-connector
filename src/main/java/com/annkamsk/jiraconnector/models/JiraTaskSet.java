package com.annkamsk.jiraconnector.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class JiraTaskSet {

    public static List<String> PREFIX_EPIC_NAMES = List.of("DEV", "PM/ANL", "TST", "TST_TF");

    private String projectKey;
    private String projectName;
    private String version;
    private List<JiraTask> tasks = new ArrayList<>();

    public JiraTask createTask() {
        return new JiraTask();
    }

    public void addTask(JiraTask task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    public void prettyPrint() {
        StringBuilder result = new StringBuilder("JiraTaskSet(");

        result.append("project=");
        if (projectKey != null) result.append("\"").append(projectKey).append("\"");
        else result.append("null");

        result.append(", version=");
        if (version != null) result.append("\"").append(version).append("\"");
        else result.append("null");

        result.append(", tasks=");
        if (tasks == null) result.append("null");
        else if (tasks.isEmpty()) result.append("[]");
        else {
            result.append("[\n");
            tasks.forEach(task -> result.append("    ").append(task.toString()).append(",\n"));
            result.deleteCharAt(result.lastIndexOf(","));
            result.append("]");
        }

        result.append(")");

        System.out.println(result.toString());
    }

    public List<String> getEpics() {
        System.out.println(version);
        return PREFIX_EPIC_NAMES.stream()
                .map(x->"[" + x + "] " + projectName + " " + version)
                .collect(Collectors.toList());
    }

    public Map<String, Object> epicsToJSON() {
        return Map.of("issueUpdates",
                getEpics().stream().map(this::epicToJSON).collect(Collectors.toList()));
    }

    public Map<String, Object> modulesToJSON(Map<String, String> epicNameToKey) {
        return Map.of("issueUpdates",
                tasks.stream().map(
                        task -> task.moduleToJSON(projectKey, version, epicNameToKey)
                ).distinct().collect(Collectors.toList()));
    }

    public Map<String, Object> tasksToJSON(Map<String, String> moduleNameToKey) {
        return Map.of("issueUpdates",
                tasks.stream()
                        .map(task -> task.toJSON(projectKey, version, moduleNameToKey))
                        .collect(Collectors.toList()));
    }

    private Map<String, Object> epicToJSON(String epicName) {
        return Map.of("fields", Map.of(
                "project", Map.of("key", projectKey),
                "summary", epicName,
                "issuetype", Map.of("name", "Epic"),
                "fixVersions", List.of(Map.of("name", version)),
                "customfield_10005", epicName
        ));
    }
}
