package de.featjar.base.computation;

import java.util.ArrayList;

public class DependencyList extends ArrayList<Object> {
    public <T> T get(Dependency<T> dependency) {
        return dependency.get(this);
    }
}
