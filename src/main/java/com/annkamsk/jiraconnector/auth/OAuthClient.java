package com.annkamsk.jiraconnector.auth;

import com.annkamsk.jiraconnector.models.JiraProperties;
import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.*;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.Scanner;

public class OAuthClient {

    private JiraProperties jiraProperties;
    private final JiraOAuthClient jiraOAuthClient;

    public OAuthClient(JiraProperties jiraProperties) {
        this.jiraProperties = jiraProperties;
        this.jiraOAuthClient = new JiraOAuthClient(jiraProperties);
    }

    /**
     * Gets request token and saves it to jiraProperties file
     */
    public Optional<String> handleGetRequestTokenAction() {
        try {
            OAuthAuthorizeTemporaryTokenUrl url = jiraOAuthClient.getAndAuthorizeTemporaryToken(jiraProperties.getConsumerKey(),
                    jiraProperties.getPrivateKey());
            jiraProperties.setRequestToken(url.temporaryToken);
            return Optional.of(url.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Gets access token and saves it to jiraProperties file
     */
    public void handleGetAccessToken(String access) {
        String tmpToken = jiraProperties.getRequestToken();
        try {
            String accessToken = jiraOAuthClient.getAccessToken(tmpToken, access, jiraProperties.getConsumerKey(), jiraProperties.getPrivateKey());
            jiraProperties.setAccessToken(accessToken);
            jiraProperties.setSecret(access);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes GET request to JIRA to provided url and returns the response content
     */
    public String handleRequest(String url) {
        try {
            OAuthParameters parameters = jiraOAuthClient.getParameters(jiraProperties.getAccessToken(), jiraProperties.getSecret(),
                    jiraProperties.getConsumerKey(), jiraProperties.getPrivateKey());
            HttpResponse response = getResponseFromUrl(parameters, new GenericUrl(url));
            return parseResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Makes POST request to JIRA to provided url and returns the response content
     */
    public String handleRequest(String url, Object values) {
        try {
            OAuthParameters parameters = jiraOAuthClient.getParameters(jiraProperties.getAccessToken(), jiraProperties.getSecret(),
                    jiraProperties.getConsumerKey(), jiraProperties.getPrivateKey());
            JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), values);
            GenericUrl gurl = new GenericUrl(new URL(url));
            HttpResponse response = getResponseFromUrl(parameters, gurl, content);
            return parseResponse(response);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Returns the response content
     */
    private String parseResponse(HttpResponse response) throws IOException {
        Scanner s = new Scanner(response.getContent()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Authanticates to JIRA with given OAuthParameters and makes GET request to url
     */
    private static HttpResponse getResponseFromUrl(OAuthParameters parameters, GenericUrl jiraUrl) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create()
                .useSystemProperties()
                .build();
        HttpRequestFactory requestFactory = new ApacheHttpTransportExt(httpClient).createRequestFactory(parameters);
        HttpRequest request = requestFactory.buildGetRequest(jiraUrl);
        return request.execute();
    }

    /**
     * Authanticates to JIRA with given OAuthParameters and makes POST request to url
     */
    private static HttpResponse getResponseFromUrl(OAuthParameters parameters, GenericUrl jiraUrl, HttpContent content) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create()
                .useSystemProperties()
                .build();
        HttpRequestFactory requestFactory = new ApacheHttpTransportExt(httpClient).createRequestFactory(parameters);
        HttpRequest request = requestFactory.buildPostRequest(jiraUrl, content);
        return request.execute();
    }


}
