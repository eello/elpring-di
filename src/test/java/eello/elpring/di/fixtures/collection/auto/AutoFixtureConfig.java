package eello.elpring.di.fixtures.collection.auto;

import eello.elpring.di.annotation.Bean;
import eello.elpring.di.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class AutoFixtureConfig {

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
        private final Set<Converter<?>> converterSet;
        private final Map<String, Converter<?>> converterMap;
        private final Converter<?>[] converterArray;

        public CollectionInjectionClient(
                List<Converter<?>> converterList,
                Set<Converter<?>> converterSet,
                Map<String, Converter<?>> converterMap,
                Converter<?>[] converterArray) {
            this.converterList = converterList;
            this.converterSet = converterSet;
            this.converterMap = converterMap;
            this.converterArray = converterArray;
        }

        public List<Converter<?>> getConverterList() { return converterList; }
        public Set<Converter<?>> getConverterSet() { return converterSet; }
        public Map<String, Converter<?>> getConverterMap() { return converterMap; }
        public Converter<?>[] getConverterArray() { return converterArray; }
    }

    @Bean
    public Converter<String> stringConverter() {
        return new StringConverter();
    }

    @Bean
    public Converter<Integer> integerConverter() {
        return new IntegerConverter();
    }

    @Bean
    public CollectionInjectionClient client(
            List<Converter<?>> converterList,
            Set<Converter<?>> converterSet,
            Map<String, Converter<?>> converterMap,
            Converter<?>[] converterArray) {
        return new CollectionInjectionClient(converterList, converterSet, converterMap, converterArray);
    }
}
