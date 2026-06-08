package eello.elpring.di.beans.factory.support;

import eello.elpring.di.beans.DefaultBeanDefinition;
import eello.elpring.di.beans.factory.AbstractBeanFactoryTest;
import eello.elpring.di.beans.factory.ListableBeanFactory;
import eello.elpring.di.exception.BeanDefinitionStoreException;
import eello.elpring.di.fixtures.factory.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultListableBeanFactoryTest extends AbstractBeanFactoryTest {

    @Override
    protected void initBeanFactoryAndRegistry() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        this.beanFactory = factory;
        this.registry = factory;
    }

    @Test
    void testDuplicateBeanDefinitionRegistration() {
        registry.registerBeanDefinition("simpleBean", DefaultBeanDefinition.of(SimpleBean.class));

        // Registering the exact same bean definition should be skipped without exception (does not throw)
        assertDoesNotThrow(() -> {
            registry.registerBeanDefinition("simpleBean", DefaultBeanDefinition.of(SimpleBean.class));
        });

        // Registering a different bean definition with the same name should throw BeanDefinitionStoreException
        assertThrows(BeanDefinitionStoreException.class, () -> {
            registry.registerBeanDefinition("simpleBean", DefaultBeanDefinition.of(DependencyBean.class));
        });
    }
}
