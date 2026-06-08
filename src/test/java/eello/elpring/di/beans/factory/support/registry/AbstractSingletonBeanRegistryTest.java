package eello.elpring.di.beans.factory.support.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractSingletonBeanRegistryTest {

    private SingletonBeanRegistry registry;

    protected abstract SingletonBeanRegistry createRegistry();

    @BeforeEach
    void setUp() {
        this.registry = createRegistry();
    }

    @Test
    void testAddAndGetSingleton() {
        Object bean = new Object();
        String beanName = "testBean";

        registry.addSingleton(beanName, bean);

        assertSame(bean, registry.getSingleton(beanName));
    }

    @Test
    void testAddDuplicateSingletonThrowsException() {
        Object bean1 = new Object();
        Object bean2 = new Object();
        String beanName = "duplicateBean";

        registry.addSingleton(beanName, bean1);

        assertThrows(IllegalStateException.class, () -> {
            registry.addSingleton(beanName, bean2);
        });
    }

    @Test
    void testGetNonExistentSingletonReturnsNull() {
        assertNull(registry.getSingleton("nonExistent"));
    }

    @Test
    void testGetAllBeans() {
        Object bean1 = new Object();
        Object bean2 = new Object();

        registry.addSingleton("bean1", bean1);
        registry.addSingleton("bean2", bean2);

        Object[] allBeans = registry.getAllBeans();
        assertEquals(2, allBeans.length);
        assertTrue(allBeans[0] == bean1 || allBeans[0] == bean2);
        assertTrue(allBeans[1] == bean1 || allBeans[1] == bean2);
    }

    @Test
    void testCurrentlyInCreationStateTracking() {
        String beanName = "circularBean";

        assertFalse(registry.isSingletonCurrentlyInCreation(beanName));

        registry.setCurrentlyInCreation(beanName);
        assertTrue(registry.isSingletonCurrentlyInCreation(beanName));

        registry.completeCurrentlyInCreation(beanName);
        assertFalse(registry.isSingletonCurrentlyInCreation(beanName));
    }
}
