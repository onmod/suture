package net.dloud.platform.gateway.conf;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import net.dloud.platform.gateway.pack.MapAnnotationIntrospector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.text.SimpleDateFormat;

/**
 * @author QuDasheng
 * @create 2018-09-12 20:00
 **/
@Primary
@Component
@EnableWebFlux
public class WebfluxConfig implements WebFluxConfigurer {
    @Value("${default.date-format}")
    private String dateFormat;

    @Value("${default.field-filter}")
    private String fieldFilter;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*")
                .allowedMethods("*");
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(dateFormat));
        mapper.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        FilterProvider filterProvider = new SimpleFilterProvider()
                .addFilter("mapFilter", SimpleBeanPropertyFilter.serializeAllExcept(fieldFilter.split(",")));
        mapper.setFilterProvider(filterProvider);
        mapper.setAnnotationIntrospector(new MapAnnotationIntrospector());

        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
    }
}
