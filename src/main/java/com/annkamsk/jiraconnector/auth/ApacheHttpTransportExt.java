package com.annkamsk.jiraconnector.auth;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StreamingContent;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/*
  This class is ALMOST an original ApacheHttpTransport class,
  except for the first line in buildRequest method,
  where it changes the domain name to the IP address of Jira
  and HTTPS to HTTP. This change was made due to our local server
  configuration and is probably not needed in a normal scenario.
 */
public class ApacheHttpTransportExt extends HttpTransport {
    private static final String JIRA_URI = "http://local-address:8080";
    private static final String JIRA_DOMAIN = "https://jira.annkamsk.com";

    private HttpClient httpClient;

    public ApacheHttpTransportExt(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    protected ApacheHttpRequest buildRequest(String method, String url) {
        url = url.replaceAll(JIRA_DOMAIN, JIRA_URI);
        System.out.println(url);
        HttpRequestBase requestBase;
        switch (method) {
            case HttpMethods.DELETE:
                requestBase = new HttpDelete(url);
                break;
            case HttpMethods.GET:
                requestBase = new HttpGet(url);
                break;
            case HttpMethods.HEAD:
                requestBase = new HttpHead(url);
                break;
            case HttpMethods.POST:
                requestBase = new HttpPost(url);
                break;
            case HttpMethods.PUT:
                requestBase = new HttpPut(url);
                break;
            case HttpMethods.TRACE:
                requestBase = new HttpTrace(url);
                break;
            case HttpMethods.OPTIONS:
                requestBase = new HttpOptions(url);
                break;
            default:
                requestBase = new HttpExtensionMethod(method, url);
                break;
        }
        return new ApacheHttpRequest(httpClient, requestBase);
    }

    private static class HttpExtensionMethod extends HttpEntityEnclosingRequestBase {

        /**
         * Request method name.
         */
        private final String methodName;

        /**
         * @param methodName request method name
         * @param uri        URI
         */
        public HttpExtensionMethod(String methodName, String uri) {
            this.methodName = Preconditions.checkNotNull(methodName);
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return methodName;
        }
    }

    private static class ApacheHttpRequest extends LowLevelHttpRequest {
        private final HttpClient httpClient;

        private final HttpRequestBase request;

        ApacheHttpRequest(HttpClient httpClient, HttpRequestBase request) {
            this.httpClient = httpClient;
            this.request = request;
        }

        @Override
        public void addHeader(String name, String value) {
            request.addHeader(name, value);
        }

        @Override
        public LowLevelHttpResponse execute() throws IOException {
            if (getStreamingContent() != null) {
                Preconditions.checkArgument(request instanceof HttpEntityEnclosingRequest,
                        "Apache HTTP client does not support %s requests with content.",
                        request.getRequestLine().getMethod());
                ContentEntity entity = new ContentEntity(getContentLength(), getStreamingContent());
                entity.setContentEncoding(getContentEncoding());
                entity.setContentType("application/json");
                ((HttpEntityEnclosingRequest) request).setEntity(entity);
            }
            return new ApacheHttpResponse(request, httpClient.execute(request));
        }
    }

    private static class ApacheHttpResponse extends LowLevelHttpResponse {

        private final HttpRequestBase request;
        private final HttpResponse response;
        private final Header[] allHeaders;

        ApacheHttpResponse(HttpRequestBase request, HttpResponse response) {
            this.request = request;
            this.response = response;
            allHeaders = response.getAllHeaders();
        }

        @Override
        public int getStatusCode() {
            StatusLine statusLine = response.getStatusLine();
            return statusLine == null ? 0 : statusLine.getStatusCode();
        }

        @Override
        public InputStream getContent() throws IOException {
            HttpEntity entity = response.getEntity();
            return entity == null ? null : entity.getContent();
        }

        @Override
        public String getContentEncoding() {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                Header contentEncodingHeader = entity.getContentEncoding();
                if (contentEncodingHeader != null) {
                    return contentEncodingHeader.getValue();
                }
            }
            return null;
        }

        @Override
        public long getContentLength() {
            HttpEntity entity = response.getEntity();
            return entity == null ? -1 : entity.getContentLength();
        }

        @Override
        public String getContentType() {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                Header contentTypeHeader = entity.getContentType();
                if (contentTypeHeader != null) {
                    return contentTypeHeader.getValue();
                }
            }
            return null;
        }

        @Override
        public String getReasonPhrase() {
            StatusLine statusLine = response.getStatusLine();
            return statusLine == null ? null : statusLine.getReasonPhrase();
        }

        @Override
        public String getStatusLine() {
            StatusLine statusLine = response.getStatusLine();
            return statusLine == null ? null : statusLine.toString();
        }

        public String getHeaderValue(String name) {
            return response.getLastHeader(name).getValue();
        }

        @Override
        public int getHeaderCount() {
            return allHeaders.length;
        }

        @Override
        public String getHeaderName(int index) {
            return allHeaders[index].getName();
        }

        @Override
        public String getHeaderValue(int index) {
            return allHeaders[index].getValue();
        }

        /**
         * Aborts execution of the request.
         *
         * @since 1.4
         */
        @Override
        public void disconnect() {
            request.abort();
        }
    }

    private static class ContentEntity extends AbstractHttpEntity {

        /**
         * Content length or less than zero if not known.
         */
        private final long contentLength;

        /**
         * Streaming content.
         */
        private final StreamingContent streamingContent;

        /**
         * @param contentLength    content length or less than zero if not known
         * @param streamingContent streaming content
         */
        ContentEntity(long contentLength, StreamingContent streamingContent) {
            this.contentLength = contentLength;
            this.streamingContent = Preconditions.checkNotNull(streamingContent);
        }

        public InputStream getContent() {
            throw new UnsupportedOperationException();
        }

        public long getContentLength() {
            return contentLength;
        }

        public boolean isRepeatable() {
            return false;
        }

        public boolean isStreaming() {
            return true;
        }

        public void writeTo(OutputStream out) throws IOException {
            if (contentLength != 0) {
                streamingContent.writeTo(out);
            }
        }
    }
}
