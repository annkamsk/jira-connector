package com.annkamsk.jiraconnector.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraProject {
    private Long id;
    private String key;
    private String name;
    private String description;

    public JiraProject(JSONObject json) {
        id = Long.decode(json.getString("id"));
        key = json.getString("key");
        name = json.getString("name");
        description = json.optString("description", "");
    }

    public static List<JiraProject> projectListFromJSON(JSONArray json) {
        List<JiraProject> result = new ArrayList<>();
        for (Object obj : json) {
            result.add(new JiraProject((JSONObject) obj));
        }
        return result;
    }
}
