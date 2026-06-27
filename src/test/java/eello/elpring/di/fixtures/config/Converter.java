package eello.elpring.di.fixtures.config;

public interface Converter<T> {
    T convert(Object source);
}
