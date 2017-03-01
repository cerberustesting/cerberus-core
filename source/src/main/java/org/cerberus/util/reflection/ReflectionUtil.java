package org.cerberus.util.reflection;

import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A set of utility methods when dealing with Java reflection
 *
 * @author abourdon
 */
public final class ReflectionUtil {

    private static final ReflectionUtil INSTANCE = new ReflectionUtil();

    /**
     * Singleton class
     */
    private ReflectionUtil() {
    }

    /**
     * Get the unique instance of this class
     *
     * @return the unique instance of this class
     */
    public static ReflectionUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Check if the given {@link Field} is a simply attribute, i.e.:
     * <ul>
     * <li>no static</li>
     * <li>no abstract</li>
     * <li>no transient</li>
     * <li>no volatile</li>
     * </ul>
     *
     * @param field the {@link Field} to check
     * @return <code>true</code> if the given {@link Field} is a simply attribute, <code>false</code> otherwise
     */
    public boolean isSimplyAttribute(final Field field) {
        return (
                field.getModifiers() & (
                        Modifier.STATIC
                                | Modifier.ABSTRACT
                                | Modifier.TRANSIENT
                                | Modifier.VOLATILE
                )
        ) == 0;
    }

    /**
     * Get all simply attribute names from the given {@link Root}.
     * <p>
     * A simply attribute must match the {@link #isSimplyAttribute(Field)} criteria
     *
     * @param root the {@link Root} from which getting all simply attributes
     * @return a {@link List} of all simply attribute names from the given {@link Root}
     * @see #isSimplyAttribute(Field)
     */
    public List<String> getSimplyAttributes(final Root<?> root) {
        return getSimplyAttributes(root, null);
    }

    /**
     * Get all simply attribute names from the given {@link Root} that match the given type.
     * <p>
     * A simply attribute must match the {@link #isSimplyAttribute(Field)} criteria
     *
     * @param root the {@link Root} from which getting all simply attributes
     * @param type the given type to use for type matching
     * @return a {@link List} of all simply attribute names from the given {@link Root} that match the given type
     * @see #isSimplyAttribute(Field)
     */
    public List<String> getSimplyAttributes(final Root<?> root, final Class<?> type) {
        final List<String> attributes = new ArrayList<>();
        for (final Field field : root.getJavaType().getDeclaredFields()) {
            if (isSimplyAttribute(field) && (type == null || field.getType().equals(type))) {
                attributes.add(field.getName());
            }
        }
        return attributes;
    }

    /**
     * Get the type of the given attribute name from the given {@link Class}
     *
     * @param clazz         the {@link Class} from which getting the attribute name
     * @param attributeName the {@link Class}'s attribute name from which getting type
     * @return tye type of the given attribute name from the given {@link Class}
     * @throws NoSuchFieldException if attribute name is not defined from the given {@link Class}
     */
    public Class<?> getAttributeType(Class<?> clazz, String attributeName) throws NoSuchFieldException {
        return clazz.getDeclaredField(attributeName).getType();
    }

}
