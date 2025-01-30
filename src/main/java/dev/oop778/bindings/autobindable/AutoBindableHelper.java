package dev.oop778.bindings.autobindable;

import dev.oop778.bindings.type.Bindable;
import dev.oop778.bindings.type.BindableTyped;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;

class AutoBindableHelper {
    private static final Map<Class<?>, Map<Boolean, Collection<MethodHandle>>> METHOD_CACHE = new IdentityHashMap<>();

    // Collect methods that have no parameters and return anything that returns bindable
    protected static synchronized Collection<MethodHandle> collectMethods(Class<?> clazz, boolean hierarchy) {
        final Collection<MethodHandle> currentHandles = Optional
            .ofNullable(METHOD_CACHE.get(clazz))
            .map((map) -> map.get(hierarchy))
            .orElse(null);
        if (currentHandles != null) {
            return currentHandles;
        }

        final Collection<MethodHandle> result = new ArrayList<>();
        final Set<String> visitedMethods = new HashSet<>();

        Class<?> currentClass = clazz;
        while (currentClass != Object.class && currentClass != null) {
            result.addAll(AutoBindableHelper.collectMethods(currentClass, visitedMethods));
            if (hierarchy) {
                for (final Class<?> anInterface : currentClass.getInterfaces()) {
                    if (Bindable.class == anInterface || BindableTyped.class == anInterface) {
                        continue;
                    }
                    result.addAll(AutoBindableHelper.collectMethods(anInterface, visitedMethods));
                }
            }
            currentClass = hierarchy ? currentClass.getSuperclass() : null;
        }

        METHOD_CACHE.put(clazz, new HashMap<Boolean, Collection<MethodHandle>>() {{
            this.put(hierarchy, result);
        }});

        return result;
    }

    @SneakyThrows
    private static Collection<MethodHandle> collectMethods(Class<?> clazz, Set<String> visitedMethods) {
        final Method[] declaredMethods = clazz.getDeclaredMethods();
        final List<MethodHandle> result = new ArrayList<>();

        for (final Method declaredMethod : declaredMethods) {
            final int modifiers = declaredMethod.getModifiers();

            // Neither static nor abstract
            if (Modifier.isAbstract(modifiers) || Modifier.isStatic(modifiers)) {
                continue;
            }

            declaredMethod.setAccessible(true);
            if (!visitedMethods.add(declaredMethod.getName())) {
                continue;
            }

            // Return type implements Bindable
            if (!Bindable.class.isAssignableFrom(declaredMethod.getReturnType())) {
                continue;
            }

            // No parameters
            if (declaredMethod.getParameterTypes().length != 0) {
                continue;
            }

            result.add(MethodHandles.lookup().unreflect(declaredMethod));
        }

        return result;
    }
}
