package net.dloud.platform.gateway.pack;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-14 00:37
 **/
public class MapAnnotationIntrospector extends JacksonAnnotationIntrospector {
    private static final long serialVersionUID = 4935076804235961638L;

    @Override
    public Object findFilterId(Annotated annotated) {
        if (Map.class.isAssignableFrom(annotated.getRawType())) {
            return "mapFilter";
        }
        return super.findFilterId(annotated);
    }
}
