package com.annkamsk.jiraconnector.auth;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.annkamsk.jiraconnector.models.JiraProperties;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Component
public class JiraOAuthClient {

    private final JiraOAuthTokenFactory oAuthGetAccessTokenFactory;
    private final String authorizationUrl;
    private JiraProperties properties;


    @Autowired
    public JiraOAuthClient(JiraProperties properties) {
        this.properties = properties;
        String jiraBaseUrl = properties.getJiraHome();
        this.oAuthGetAccessTokenFactory = new JiraOAuthTokenFactory(jiraBaseUrl);
        authorizationUrl = jiraBaseUrl + "/plugins/servlet/oauth/authorize";
    }


    /**
     * Gets temporary request token and creates url to authorize it
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public OAuthAuthorizeTemporaryTokenUrl getAndAuthorizeTemporaryToken(String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        JiraOAuthGetTemporaryToken temporaryToken = oAuthGetAccessTokenFactory.getTemporaryToken(consumerKey, privateKey);
        HttpClient httpClient = HttpClientBuilder.create()
                .useSystemProperties()
                .build();
        temporaryToken.transport = new ApacheHttpTransportExt(httpClient);
        OAuthCredentialsResponse response = temporaryToken.execute();
        System.out.println("Response:\t\t\t" + response.toString());

        System.out.println("Token:\t\t\t" + response.token);
        System.out.println("Token secret:\t" + response.tokenSecret);

        OAuthAuthorizeTemporaryTokenUrl authorizationURL = new OAuthAuthorizeTemporaryTokenUrl(authorizationUrl);
        authorizationURL.temporaryToken = response.token;
        System.out.println("Retrieve request token. Go to " + authorizationURL.toString() + " to authorize it.");

        return authorizationURL;
    }

    /**
     * Gets acces token from JIRA
     *
     * @param tmpToken    temporary request token
     * @param secret      secret (verification code provided by JIRA after request token authorization)
     * @param consumerKey consumer ey
     * @param privateKey  private key in PKCS8 format
     * @return access token valueOimgIN
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public String getAccessToken(String tmpToken, String secret, String consumerKey, String privateKey) throws
            NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        JiraOAuthGetAccessToken oAuthAccessToken = oAuthGetAccessTokenFactory.getJiraOAuthGetAccessToken(tmpToken, secret, consumerKey, privateKey);
        HttpClient httpClient = HttpClientBuilder.create()
                .useSystemProperties()
                .build();
        oAuthAccessToken.transport = new ApacheHttpTransportExt(httpClient);
        OAuthCredentialsResponse response = oAuthAccessToken.execute();

        System.out.println("Access token:\t\t\t" + response.token);
        return response.token;
    }

    /**
     * Creates OAuthParameters used to make authorized request to JIRA
     *
     * @param tmpToken
     * @param secret
     * @param consumerKey
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public OAuthParameters getParameters(String tmpToken, String secret, String consumerKey, String privateKey) throws
            NoSuchAlgorithmException, InvalidKeySpecException {
        JiraOAuthGetAccessToken oAuthAccessToken = oAuthGetAccessTokenFactory.getJiraOAuthGetAccessToken(tmpToken, secret, consumerKey, privateKey);
        oAuthAccessToken.verifier = secret;
        return oAuthAccessToken.createParameters();
    }

    public OAuthParameters getDefaultParameters() throws InvalidKeySpecException, NoSuchAlgorithmException {
        String tmpToken = properties.getRequestToken();
        String secret = properties.getSecret();
        String consumerKey = properties.getConsumerKey();
        String privateKey = properties.getPrivateKey();
        return getParameters(tmpToken, secret, consumerKey, privateKey);
    }
}
