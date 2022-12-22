package de.featjar.base.computation;

/**
 * asynchronous automorphism
 * @param <T>
 */
public interface Transformation<T> extends Analysis<T, T> {
    //todo: add ITransformation and Transformation as standard implementation

//    protected final Dependency<T> inputDependency;
//
//    protected Transformation(Computable<T> input) {
//        inputDependency = new Dependency<T>(input);
//    }
//
//    @Override
//    public Computable<T> getInput() {
//        return inputDependency.get();
//    }
//
//    @Override
//    public Transformation<T> setInput(Computable<T> input) {
//        inputDependency.set(input);
//        return this;
//    }
}
