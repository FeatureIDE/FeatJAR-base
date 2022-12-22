package de.featjar.base.computation;

import de.featjar.base.tree.structure.Tree;

import java.util.*;

import static de.featjar.base.computation.Computations.async;

public class Dependency<U> extends Tree.Entry<Computable<?>, Computable<U>> {
    protected static final LinkedHashMap<Class<? extends Computable<?>>, List<Dependency<?>>>
            indexMap = new LinkedHashMap<>(); //todo: dependency manager in Feat instance

    public Dependency() {
        this(null);
    }

    public Dependency(U defaultValue) {
        super(async(defaultValue));
    }

    public static void register(Class<? extends Computable<?>> computableClass, Dependency<?> dependency) {
        indexMap.putIfAbsent(computableClass, new ArrayList<>());
        if (!indexMap.get(computableClass).contains(dependency))
            indexMap.get(computableClass).add(dependency);
        dependency.setIndex(indexMap.get(computableClass).indexOf(dependency));
    }

    public static void register(Class<? extends Computable<?>> computableClass, Dependency<?>... dependencies) {
        if (!indexMap.containsKey(computableClass))
            Arrays.stream(dependencies).forEach(dependency ->
                    Dependency.register(computableClass, dependency));
    }
}
