package com.annkamsk.jiraconnector.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraTask {

    private String type;
    private String module;
    private String name;
    private TaskPriority priority;
    private Double timeEstimate;
    private String description;

    public Map<String, Object> toJSON(String projectKey, String projectVersion, Map<String, String> moduleNameToKey) {
        Map<String, Object> result = new HashMap<>();

        result.put("project", Map.of("key", projectKey));
        result.put("summary", name);
        result.put("parent", Map.of("key", moduleNameToKey.get(module)));
        result.put("issuetype", Map.of("name", "Sub-task"));
        result.put("fixVersions", List.of(Map.of("name", projectVersion)));

        if (description != null) {
            result.put("description", description);
        }
        if (timeEstimate != null) {
            result.put("timetracking", Map.of("originalEstimate", timeEstimate));
        }
        if (priority != null) {
            result.put("priority", Map.of("name", priority.toString()));
        }

        return Map.of("fields", result);
    }

    public Map<String, Object> moduleToJSON(String projectKey, String projectVersion, Map<String, String> epicNameToKey) {
        Map<String, Object> result = new HashMap<>();

        result.put("project", Map.of("key", projectKey));
        result.put("summary", module);
        result.put("issuetype", Map.of("name", "Task"));
        result.put("fixVersions", List.of(Map.of("name", projectVersion)));

        // Epic goes to the module, sub-tasks automatically get the same epic as their parents
        result.put("customfield_10006", epicNameToKey.get(type));

        return Map.of("fields", result);
    }
}
