package eello.elpring.di.fixtures.config;

import eello.elpring.di.annotation.Component;

@Component
public class StringConverter implements Converter<String> {
    @Override
    public String convert(Object source) {
        return source != null ? source.toString() : null;
    }
}
