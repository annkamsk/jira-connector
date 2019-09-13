# jira-connector

Spring Boot application presenting a way to authenticate with Jira using OAuth (based on the examples from [here](https://developer.atlassian.com/server/jira/platform/jira-rest-api-example-oauth-authentication-6291692/)) 
and do some basic tasks including: getting the list of projects/versions or creating new issues. 

The usage requires setting your Jira domain/IP address in the properties. The logic of creating the requests happens in JiraService. Currently the application downloads all of the projects (that the authenticated user has access to) and creates issues with some dummy data.

The API GET calls are listed in JiraController:
* /createIssue 
* /getJIRAProjects
* /requestToken
* /accessToken/{secret}

## Authentication

To authenticate in Jira, you need to call the /requestToken first. You will be prompted to agree for giving the application 
right to access Jira in your name. Then you should get a secret token that you need to use with /accessToken/{secret} call.

## Run
`mvn package`

`java -jar target/jira-connector-0.0.1-SNAPSHOT.jar`
