package eello.elpring.di.context;

import eello.elpring.di.fixtures.scanner.EagerComponent;
import eello.elpring.di.fixtures.scanner.LazyComponent;
import eello.elpring.di.beans.factory.support.BeanDefinitionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractApplicationContextTest {

    protected abstract ConfigurableApplicationContext createApplicationContext(String... basePackages);

    @BeforeEach
    void resetFixtures() {
        EagerComponent.constructorCount = 0;
        LazyComponent.constructorCount = 0;
    }

    @Test
    void testContextInitializationAndEagerVsLazyLoading() {
        assertEquals(0, EagerComponent.constructorCount);
        assertEquals(0, LazyComponent.constructorCount);

        // Initialize context and scan package
        ConfigurableApplicationContext context = createApplicationContext("eello.elpring.di.fixtures.scanner");

        // Eager component should be created immediately during refresh()
        assertEquals(1, EagerComponent.constructorCount);
        // Lazy component should NOT be created during refresh()
        assertEquals(0, LazyComponent.constructorCount);

        // Explicitly get lazy bean
        LazyComponent lazyBean = context.getBean(LazyComponent.class);
        assertNotNull(lazyBean);
        // Lazy component should be created now
        assertEquals(1, LazyComponent.constructorCount);

        // Retrieve again
        LazyComponent lazyBean2 = context.getBean(LazyComponent.class);
        assertSame(lazyBean, lazyBean2);
        assertEquals(1, LazyComponent.constructorCount);
    }

    @Test
    void testGetBeansWithAnnotationLazyIntegration() {
        assertEquals(0, EagerComponent.constructorCount);
        assertEquals(0, LazyComponent.constructorCount);

        ConfigurableApplicationContext context = createApplicationContext("eello.elpring.di.fixtures.scanner");

        assertEquals(1, EagerComponent.constructorCount);
        assertEquals(0, LazyComponent.constructorCount);

        // Retrieve beans with Component.class annotation
        java.util.Map<String, Object> components = context.getBeansWithAnnotation(eello.elpring.di.annotation.Component.class);

        // This should force the instantiation of LazyComponent since it is annotated with @Component
        assertEquals(1, LazyComponent.constructorCount);
        assertTrue(components.containsKey("lazyComponent"));
        assertTrue(components.containsKey("eagerComponent"));
    }

    @Test
    void testApplicationContextAwareInjection() {
        ConfigurableApplicationContext context = createApplicationContext();
        ((BeanDefinitionRegistry) context).registerBeanDefinition("awareBean", eello.elpring.di.beans.DefaultBeanDefinition.of(eello.elpring.di.fixtures.factory.AwareBean.class));

        eello.elpring.di.fixtures.factory.AwareBean awareBean = context.getBean("awareBean", eello.elpring.di.fixtures.factory.AwareBean.class);
        assertNotNull(awareBean);
        assertNotNull(awareBean.getApplicationContext());
        assertSame(context, awareBean.getApplicationContext());
    }
}
