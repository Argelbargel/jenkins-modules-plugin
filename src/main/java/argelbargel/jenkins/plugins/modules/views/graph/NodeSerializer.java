package argelbargel.jenkins.plugins.modules.views.graph;


import argelbargel.jenkins.plugins.modules.views.graph.model.Node;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;


class NodeSerializer implements JsonSerializer<Node> {
    private static final Logger LOGGER = Logger.getLogger(NodeSerializer.class.getName());

    @Override
    public JsonElement serialize(Node src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        try {
            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(src.getClass(), Object.class).getPropertyDescriptors()) {
                object.add(propertyDescriptor.getName(), context.serialize(propertyDescriptor.getReadMethod().invoke(src)));
            }
        } catch (Exception e) {
            LOGGER.log(WARNING, "could not serialize node " + src, e);
        }

        return object;
    }
}
