package de.featjar.base.data;

import java.util.LinkedHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public interface IAttribute extends BiFunction<IAttributable, LinkedHashMap<IAttribute, Object>, Result<Object>> {
    String getNamespace();
    String getName();
    Class<?> getType();

    default Result<Object> getDefaultValue(IAttributable attributable) {
        return Result.empty();
    }

    default BiPredicate<IAttributable, Object> getValidator() {
        return (a, o) -> true;
    }

    @Override
    default Result<Object> apply(IAttributable attributable, LinkedHashMap<IAttribute, Object> attributeToValueMap) {
        Result<Object> defaultValue = getDefaultValue(attributable);
        if (defaultValue.isPresent())
            return Result.of(attributeToValueMap.getOrDefault(this, defaultValue.get()));
        else
            return Result.ofNullable(attributeToValueMap.get(this));
    }
}
