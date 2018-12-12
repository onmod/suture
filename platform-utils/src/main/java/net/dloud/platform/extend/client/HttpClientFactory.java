package net.dloud.platform.extend.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

/**
 * httpclient 4.x.x
 */
@Slf4j
public class HttpClientFactory {

    public static synchronized CloseableHttpClient createHttpClient() {
        return createHttpClient(100, 10, 5000, 2);
    }

    /**
     * @param maxTotal            maxTotal
     * @param maxPerRoute         maxPerRoute
     * @param timeout             timeout
     * @param retryExecutionCount retryExecutionCount
     * @return CloseableHttpClient
     */
    public static CloseableHttpClient createHttpClient(int maxTotal, int maxPerRoute, int timeout, int retryExecutionCount) {
        try {
            SSLContext sslContext = SSLContext.getDefault();
            SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
            poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
            poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
            SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(timeout).build();
            poolingHttpClientConnectionManager.setDefaultSocketConfig(socketConfig);
            return HttpClientBuilder.create()
                    .setConnectionManager(poolingHttpClientConnectionManager)
                    .setSSLSocketFactory(sslFactory)
                    .setRetryHandler(new HttpRequestRetryHandlerImpl(retryExecutionCount))
                    .build();
        } catch (NoSuchAlgorithmException e) {
            log.error("Create http client error", e);
        }
        return null;
    }

    /**
     * HttpClient 超时重试
     */
    private static class HttpRequestRetryHandlerImpl implements HttpRequestRetryHandler {

        private int retryExecutionCount;

        public HttpRequestRetryHandlerImpl(int retryExecutionCount) {
            this.retryExecutionCount = retryExecutionCount;
        }

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (executionCount > retryExecutionCount) {
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                return true;
            } else if (exception instanceof ConnectTimeoutException) {
                return true;
            } else if (exception instanceof SocketTimeoutException) {
                return true;
            } else if (exception instanceof InterruptedIOException) {
                return false;
            } else if (exception instanceof UnknownHostException) {
                return false;
            } else if (exception instanceof SSLException) {
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // Retry if the request is considered idempotent
            return !(request instanceof HttpEntityEnclosingRequest);
        }
    }

    /**
     * KeepAlive 策略
     */
    private static class ConnectionKeepAliveStrategyImpl implements ConnectionKeepAliveStrategy {

        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && "timeout".equalsIgnoreCase(param)) {
                    return Long.parseLong(value) * 1000;
                }
            }

            // 如果没有约定，则默认定义时长为60s
            return 60 * 1000;
        }
    }
}
