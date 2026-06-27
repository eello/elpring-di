package eello.elpring.di.fixtures.collection.explicit;

import eello.elpring.di.annotation.Bean;
import eello.elpring.di.annotation.Configuration;

import java.util.List;

@Configuration
public class ExplicitFixtureConfig {

    public interface Converter<T> {
        T convert(String source);
    }

    public static class StringConverter implements Converter<String> {
        @Override
        public String convert(String source) {
            return source;
        }
    }

    public static class IntegerConverter implements Converter<Integer> {
        @Override
        public Integer convert(String source) {
            return Integer.parseInt(source);
        }
    }

    public static class CollectionInjectionClient {
        private final List<Converter<?>> converterList;

        public CollectionInjectionClient(List<Converter<?>> converterList) {
            this.converterList = converterList;
        }

        public List<Converter<?>> getConverterList() { return converterList; }
    }

    @Bean
    public Converter<String> singleConverter() {
        return new StringConverter();
    }

    @Bean
    public List<Converter<?>> customConverterList() {
        return List.of(new IntegerConverter());
    }

    @Bean
    public CollectionInjectionClient clientWithListOnly(List<Converter<?>> list) {
        return new CollectionInjectionClient(list);
    }
}
