package de.featjar.util.data;

import java.util.HashMap;
import java.util.Map;

public class MemoryStore implements Store {
    private final Map<Computation<?, ?>, Result<?>> computationResultCache = new HashMap<>();

    @Override
    public <R> boolean has(Computation<?, R> computation) { // todo: return false for lambdas
        return computationResultCache.containsKey(computation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Result<R> get(Computation<?, R> computation) {
        return (Result<R>) computationResultCache.get(computation);
    }

    @Override
    public <R> void put(Computation<?, R> computation, Result<R> result) {
        computationResultCache.put(computation, result);
    }

    //    /**
//     * Get an arbitrary element that can be derived from any element in the
//     * cache.<br>
//     * This method first checks whether there is a cached instance and only
//     * computes the requested object otherwise.
//     *
//     * @param <T>      the type of the element
//     * @param computation the provider that is used in case the element is not already
//     *                 contained in the cache.
//     * @return a {@link Result} with a suitable element.
//     */
//    default <T> Result<T> get(Computation<T> computation) {
//        return get(computation, null);
//    }
//
//    /**
//     * Get an arbitrary element that can be derived from the associated feature
//     * model.<br>
//     * This method first checks whether there is a cached instance and only
//     * computes the requested object otherwise.
//     *
//     * @param <T>      the type of the element
//     * @param computation the provider that is used in case the element is not already
//     *                 contained in the cache.
//     * @param monitor  a monitor for keep track of progress and canceling the
//     *                 computation of the requested element.
//     * @return a {@link Result} with a suitable element.
//     */
//    @SuppressWarnings("unchecked")
//    default <T> Result<T> get(Computation<T> computation, Monitor monitor) {
//        monitor = monitor != null ? monitor : new CancelableMonitor();
//        try {
//            final Map<Object, Object> cachedElements = getCachedElement(computation.getIdentifier());
//            synchronized (cachedElements) {
//                T element = (T) cachedElements.get(computation.getParameters());
//                if (element == null) {
//                    final Result<T> computedElement = executeComputation(computation, monitor);
//                    if (computedElement.isPresent()) {
//                        element = computedElement.get();
//                        cachedElements.put(computation.getParameters(), element);
//                    } else {
//                        return computedElement;
//                    }
//                }
//                return Result.of(element);
//            }
//        }
//    } finally {
//        monitor.setDone();
//    }
//}
//
//    default <T> Result<T> set(Computation<T> computation) {
//        return set(computation, null);
//    }
//
//    default <T> Result<T> set(Computation<T> computation, Monitor monitor) {
//        monitor = monitor != null ? monitor : new CancelableMonitor();
//        try {
//            final Map<Object, Object> cachedElements = getCachedElement(computation.getIdentifier());
//            synchronized (cachedElements) {
//                final Result<T> computedElement = executeComputation(computation, monitor);
//                if (computedElement.isPresent()) {
//                    final T element = computedElement.get();
//                    cachedElements.put(computation.getParameters(), element);
//                    return Result.of(element);
//                } else {
//                    return computedElement;
//                }
//            }
//        } finally {
//            monitor.setDone();
//        }
//    }
//
//    default <T> void reset(Computation<T> computation) {
//        reset(computation, null);
//    }
//
//    default <T> void reset(Computation<T> computation, Monitor monitor) {
//        Map<Object, Object> cachedElement;
//        monitor = monitor != null ? monitor : new CancelableMonitor();
//        try {
//            synchronized (map) {
//                map.clear();
//                cachedElement = getCachedElement(computation.getIdentifier());
//            }
//            synchronized (cachedElement) {
//                final Result<T> computedElement = executeComputation(computation, monitor);
//                if (computedElement.isPresent()) {
//                    final T element = computedElement.get();
//                    cachedElement.put(computation.getParameters(), element);
//                }
//            }
//        } finally {
//            monitor.setDone();
//        }
//    }
//
//    default void reset() {
//        synchronized (map) {
//            map.clear();
//        }
//    }
//
//    default <T> void reset(Object identifier) {
//        synchronized (map) {
//            map.remove(identifier);
//        }
//    }
//
//    default <T> void reset(Object identifier, Object parameters) {
//        synchronized (map) {
//            final Map<Object, Object> cachedElements = map.get(identifier);
//            if (cachedElements != null) {
//                cachedElements.remove(parameters);
//            }
//        }
//    }
//
//    private Map<Object, Object> getCachedElement(Identifier<?> identifier) {
//        synchronized (map) {
//            Map<Object, Object> cachedElement = map.get(identifier);
//            if (cachedElement == null) {
//                cachedElement = new HashMap<>();
//                map.put(identifier, cachedElement);
//            }
//            return cachedElement;
//        }
//    }
//
//    private <T> Result<T> executeComputation(Computation<T> computation, Monitor monitor) {
//        return Executor.apply(computation, this, monitor);
//    }
}
