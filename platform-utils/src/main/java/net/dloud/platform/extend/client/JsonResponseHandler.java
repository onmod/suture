package net.dloud.platform.extend.client;

import net.dloud.platform.common.serialize.Jsons;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonResponseHandler {

    private static Logger log = LoggerFactory.getLogger(JsonResponseHandler.class);

    public static <T> ResponseHandler<T> createResponseHandler(final Class<T> clazz) {
        return new JsonResponseHandlerImpl<T>(null, clazz);
    }

    public static class JsonResponseHandlerImpl<T> extends LocalResponseHandler implements ResponseHandler<T> {

        private Class<T> clazz;

        public JsonResponseHandlerImpl(String uriId, Class<T> clazz) {
            this.uriId = uriId;
            this.clazz = clazz;
        }

        @Override
        public T handleResponse(HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                String str = EntityUtils.toString(entity, "utf-8");
                log.info("URI[{}] elapsed time:{} ms RESPONSE DATA:{}", super.uriId, System.currentTimeMillis() - super.startTime, str);
                return Jsons.getDefault().fromJson(str, clazz);
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }

    }
}
