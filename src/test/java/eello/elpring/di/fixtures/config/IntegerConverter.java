package eello.elpring.di.fixtures.config;

import eello.elpring.di.annotation.Component;

@Component
public class IntegerConverter implements Converter<Integer> {
    @Override
    public Integer convert(Object source) {
        if (source instanceof Number) {
            return ((Number) source).intValue();
        }
        return source != null ? Integer.parseInt(source.toString()) : null;
    }
}
