package de.featjar.base.io.graphviz;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;

import java.util.Objects;

public class GraphVizComputationTreeFormat extends GraphVizTreeFormat<IComputation<?>> {
    protected boolean includeResults = true;

    public boolean isIncludeResults() {
        return includeResults;
    }

    public void setIncludeResults(boolean includeResults) {
        this.includeResults = includeResults;
    }

    @Override
    protected String getNodeOptions(IComputation<?> computation) {
        if (!includeResults)
            return options(option("label", shorten(computation.toString())));
        long numberOfHits = FeatJAR.cache().getNumberOfHits(computation);
        Result<?> result = computation.get();
        String resultString = Objects.toString(result.orElse(null));
        return options(
                option("label", String.format("{%s|%s|%s|%s}",
                        shorten(computation.toString()),
                        result.map(Object::getClass)
                                .map(Class::getSimpleName)
                                .orElse(""),
                        shorten(resultString),
                        result.getProblem().map(Problem::toString).orElse(""))),
                option("xlabel", String.valueOf(numberOfHits)));
    }

    private static String shorten(String resultString) {
        return resultString.substring(0, Math.min(resultString.length(), 80));
    }

    @Override
    protected String getEdgeOptions(IComputation<?> parent, IComputation<?> child, int idx) {
        return options(option("label", String.valueOf(idx)));
    }
}
