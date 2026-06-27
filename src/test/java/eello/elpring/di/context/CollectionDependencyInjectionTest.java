package eello.elpring.di.context;

import eello.elpring.di.fixtures.collection.auto.AutoFixtureConfig;
import eello.elpring.di.fixtures.collection.explicit.ExplicitFixtureConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollectionDependencyInjectionTest {

    @Test
    void collectionDependencyAutoAggregation() {
        ApplicationContext context = new AnnotationConfigApplicationContext("eello.elpring.di.fixtures.collection.auto");
        AutoFixtureConfig.CollectionInjectionClient client = context.getBean(AutoFixtureConfig.CollectionInjectionClient.class);

        assertEquals(2, client.getConverterList().size());
        assertEquals(2, client.getConverterSet().size());
        assertEquals(2, client.getConverterMap().size());
        assertTrue(client.getConverterMap().containsKey("stringConverter"));
        assertTrue(client.getConverterMap().containsKey("integerConverter"));
        assertEquals(2, client.getConverterArray().length);
    }

    @Test
    void explicitCollectionBeanTakesPrecedence() {
        ApplicationContext context = new AnnotationConfigApplicationContext("eello.elpring.di.fixtures.collection.explicit");
        ExplicitFixtureConfig.CollectionInjectionClient client = context.getBean("clientWithListOnly", ExplicitFixtureConfig.CollectionInjectionClient.class);

        // singleConverter 가 있지만, 명시적으로 등록된 customConverterList(IntegerConverter)가 주입되어야 함
        assertEquals(1, client.getConverterList().size());
        assertTrue(client.getConverterList().get(0) instanceof ExplicitFixtureConfig.IntegerConverter);
    }
}
