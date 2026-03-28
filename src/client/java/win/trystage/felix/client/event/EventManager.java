package win.trystage.felix.client.event;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    public interface Event {
    }
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EventTarget {
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface EventPriority {
        int value() default 10;
    }
    private record HandlerInfo(Object handlerObject, Class<? extends Event> eventClass) {
    }
    private final Map<Method, HandlerInfo> handlerInfoMap;
    private final Map<Class<? extends Event>, List<Method>> priorityMethodMap;
    public EventManager() {
        handlerInfoMap = new ConcurrentHashMap<>();
        priorityMethodMap = new ConcurrentHashMap<>();
    }
    public void register(Object obj) {
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == EventTarget.class && method.getParameterTypes().length == 1) {
                    Class<? extends Event> eventClass = method.getParameterTypes()[0].asSubclass(Event.class);
                    HandlerInfo handlerInfo = new HandlerInfo(obj, eventClass);
                    handlerInfoMap.put(method, handlerInfo);
                    priorityMethodMap.computeIfAbsent(eventClass, EventManager::newHandlerList).add(method);
                }
            }
        }
    }
    public void call(Event event) {
        Class<? extends Event> eventClass = event.getClass();
        List<Method> methods = priorityMethodMap.get(eventClass);
        if (methods != null) {
            methods.sort(Comparator.comparingInt(method -> {
                EventPriority priority = method.getAnnotation(EventPriority.class);
                return (priority != null) ? priority.value() : 10;
            }));
            for (Method method : methods) {
                HandlerInfo handlerInfo = handlerInfoMap.get(method);
                if (handlerInfo != null) {
                    method.setAccessible(true);
                    try {
                        method.invoke(handlerInfo.handlerObject(), event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    @SuppressWarnings("unused")
    private static List<Method> newHandlerList(Class<? extends Event> ignored) {
        return new CopyOnWriteArrayList<Method>();
    }
    public static final EventManager INSTANCE = new EventManager();
}