package dev.oop778.bindings.util;

import java.util.Collection;
import java.util.function.Function;

public class JsonUtility {

    public interface JsonSerializable {
        String toJson();
    }

    public static String write(Entry<String, Object>...entries) {
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;
        for (final Entry<String, Object> entry : entries) {
            if (!first) {
                builder.append(",");
            }
            builder.append("\"").append(entry.getKey()).append("\":").append(writeObject(entry.getValue()));
            first = false;
        }
        builder.append("}");
        return builder.toString();
    }

    public static <T> String writeObject(T object) {
        if (object == null) {
            return "\"null\"";
        } else if (object instanceof Collection<?>) {
            return writeCollection((Collection<?>) object, JsonUtility::writeObject);
        } else if (object instanceof String) {
            return "\"" + object + "\"";
        } else if (object instanceof Number) {
            return object.toString();
        } else if (object instanceof Boolean) {
            return object.toString();
        } else if (object instanceof JsonSerializable){
            return ((JsonSerializable) object).toJson();
        }

        throw new IllegalArgumentException("Cannot serialize object of type " + object.getClass().getName());
    }

    public static <T> String writeCollection(Collection<T> collection, Function<T, String> toJson) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean first = true;
        for (final T item : collection) {
            if (!first) {
                builder.append(",");
            }
            builder.append(toJson.apply(item));
            first = false;
        }
        builder.append("]");
        return builder.toString();
    }
}
