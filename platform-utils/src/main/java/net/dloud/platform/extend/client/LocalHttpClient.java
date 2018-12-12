package net.dloud.platform.extend.client;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class LocalHttpClient {

    protected static final Header userAgentHeader = new BasicHeader(HttpHeaders.USER_AGENT, "Dloud Java v1.0");
    private static final Logger log = LoggerFactory.getLogger(LocalHttpClient.class);
    private static int timeout = 8000;
    private static int retryExecutionCount = 2;
    protected static CloseableHttpClient httpClient = HttpClientFactory.createHttpClient(100, 10, timeout, retryExecutionCount);
    private static ResultErrorHandler resultErrorHandler;

    /**
     * @param timeout timeout
     */
    public static void setTimeout(int timeout) {
        LocalHttpClient.timeout = timeout;
    }

    /**
     * @param retryExecutionCount retryExecutionCount
     */
    public static void setRetryExecutionCount(int retryExecutionCount) {
        LocalHttpClient.retryExecutionCount = retryExecutionCount;
    }

    /**
     * @param resultErrorHandler 数据返回错误处理
     */
    public static void setResultErrorHandler(ResultErrorHandler resultErrorHandler) {
        LocalHttpClient.resultErrorHandler = resultErrorHandler;
    }

    /**
     * @param maxTotal    maxTotal
     * @param maxPerRoute maxPerRoute
     */
    public static void init(int maxTotal, int maxPerRoute) {
        try {
            httpClient.close();
        } catch (IOException e) {
            log.error("init error", e);
        }
        httpClient = HttpClientFactory.createHttpClient(maxTotal, maxPerRoute, timeout, retryExecutionCount);
    }

    public static CloseableHttpResponse execute(HttpUriRequest request) {
        loggerRequest(request);
        userAgent(request);
        try {
            return httpClient.execute(request, HttpClientContext.create());
        } catch (Exception e) {
            log.error("execute error", e);
        }
        return null;
    }

    public static <T> T execute(HttpUriRequest request, ResponseHandler<T> responseHandler) {
        String uriId = loggerRequest(request);
        userAgent(request);
        if (responseHandler instanceof LocalResponseHandler) {
            LocalResponseHandler lrh = (LocalResponseHandler) responseHandler;
            lrh.setUriId(uriId);
        }
        try {
            T t = httpClient.execute(request, responseHandler, HttpClientContext.create());
            if (resultErrorHandler != null) {
                resultErrorHandler.doHandle(uriId, request, t);
            }
            return t;
        } catch (Exception e) {
            log.error("execute error", e);
        }
        return null;
    }


    /**
     * 数据返回自动JSON对象解析
     *
     * @param request request
     * @param clazz   clazz
     * @param <T>     T
     * @return result
     */
    public static <T> T executeJsonResult(HttpUriRequest request, Class<T> clazz) {
        return execute(request, JsonResponseHandler.createResponseHandler(clazz));
    }

    /**
     * 日志记录
     *
     * @param request request
     * @return log request id
     */
    private static String loggerRequest(HttpUriRequest request) {
        String id = UUID.randomUUID().toString();
        if (log.isInfoEnabled() || log.isDebugEnabled()) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                HttpEntityEnclosingRequestBase base = (HttpEntityEnclosingRequestBase) request;
                HttpEntity entity = base.getEntity();
                String content = null;
                //MULTIPART_FORM_DATA 请求类型判断
                if (!entity.getContentType().toString().contains(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
                    try {
                        content = EntityUtils.toString(entity);
                    } catch (Exception e) {
                        log.error("logger content data get error", e);
                    }
                }
                log.info("URI[{}] {} {} ContentLength:{} Content:{}",
                        id,
                        request.getURI().toString(),
                        entity.getContentType(),
                        entity.getContentLength(),
                        content == null ? "multipart_form_data" : content);
            } else {
                log.info("URI[{}] {}", id, request.getURI().toString());
            }
        }
        return id;
    }

    private static void userAgent(HttpUriRequest httpUriRequest) {
        httpUriRequest.addHeader(userAgentHeader);
    }
}
