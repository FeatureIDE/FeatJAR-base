package de.featjar.base.computation;

import de.featjar.base.Feat;
import de.featjar.base.extension.IInitializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Registers dependencies of computations.
 * This is required for each computation to know its dependencies' indices into its children.
 *
 * @author Elias Kuiter
 */
public class DependencyManager implements IInitializer {
    protected final LinkedHashMap<Class<? extends IComputation<?>>, List<Dependency<?>>>
            dependencyMap = new LinkedHashMap<>();

    /**
     * Creates a dependency manager.
     */
    public DependencyManager() {
        Feat.log().debug("initializing dependency manager");
    }

    /**
     * {@inheritDoc}
     * Clears this dependency manager.
     */
    @Override
    public void close() {
        Feat.log().debug("de-initializing dependency manager");
        clear();
    }

    /**
     * Registers a dependency of a given computation class.
     *
     * @param computationClass the computation class
     * @param dependency the dependency
     */
    public void register(Class<? extends IComputation<?>> computationClass, Dependency<?> dependency) {
        dependencyMap.putIfAbsent(computationClass, new ArrayList<>());
        if (!dependencyMap.get(computationClass).contains(dependency))
            dependencyMap.get(computationClass).add(dependency);
        dependency.setIndex(dependencyMap.get(computationClass).indexOf(dependency));
    }

    /**
     * Registers all dependencies of a given computation class.
     * Only has an effect when called for the first time for the given computation class.
     *
     * @param computationClass the computation class
     * @param dependencies the dependencies
     */
    public void register(Class<? extends IComputation<?>> computationClass, Dependency<?>... dependencies) {
        if (!dependencyMap.containsKey(computationClass))
            Arrays.stream(dependencies).forEach(dependency ->
                    register(computationClass, dependency));
    }

    /**
     * Removes all registered dependencies.
     */
    public void clear() {
        Feat.log().debug("clearing dependency manager");
        dependencyMap.clear();
    }
}
