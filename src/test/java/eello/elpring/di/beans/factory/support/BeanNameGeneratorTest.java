package eello.elpring.di.beans.factory.support;

import eello.elpring.di.annotation.Bean;
import eello.elpring.di.annotation.Component;
import eello.elpring.di.annotation.Configuration;
import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.DefaultBeanDefinition;
import eello.elpring.di.fixtures.config.AppConfig;
import eello.elpring.di.fixtures.scanner.CustomNamedComponent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeanNameGeneratorTest {

    @Component
    public static class SimpleComponent {
        public SimpleComponent() {}
    }

    @Configuration("namedConfig")
    public static class NamedConfig {
        public NamedConfig() {}
    }

    @Test
    void generateForClass_ComponentWithoutName() {
        String name = BeanNameGenerator.generate(SimpleComponent.class);
        assertEquals("simpleComponent", name);
    }

    @Test
    void generateForClass_ComponentWithName() {
        String name = BeanNameGenerator.generate(CustomNamedComponent.class);
        assertEquals("customBeanName", name);
    }

    @Test
    void generateForClass_ConfigurationWithoutName() {
        String name = BeanNameGenerator.generate(AppConfig.class);
        assertEquals("appConfig", name);
    }

    @Test
    void generateForClass_ConfigurationWithName() {
        String name = BeanNameGenerator.generate(NamedConfig.class);
        assertEquals("namedConfig", name);
    }

    @Test
    void generateForMethod_BeanWithoutName() throws NoSuchMethodException {
        Method method = AppConfig.class.getMethod("simpleBean");
        String name = BeanNameGenerator.generate(method);
        assertEquals("simpleBean", name);
    }

    @Test
    void generateForMethod_BeanWithName() throws NoSuchMethodException {
        Method method = AppConfig.class.getMethod("customNameBean");
        String name = BeanNameGenerator.generate(method);
        assertEquals("customName", name);
    }

    @Test
    void generateForBeanDefinition_ClassComponent() {
        BeanDefinition def = DefaultBeanDefinition.of(SimpleComponent.class);
        String name = BeanNameGenerator.generate(def);
        assertEquals("simpleComponent", name);
    }

    @Test
    void generateForBeanDefinition_FactoryBeanMethod() throws NoSuchMethodException {
        Method method = AppConfig.class.getMethod("simpleBean");
        BeanDefinition def = DefaultBeanDefinition.of(method, "appConfig");
        String name = BeanNameGenerator.generate(def);
        assertEquals("simpleBean", name);
    }
}
