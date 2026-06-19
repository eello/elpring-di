package eello.elpring.di.beans.factory.support;

import eello.elpring.di.annotation.Bean;
import eello.elpring.di.annotation.Component;
import eello.elpring.di.annotation.Configuration;
import eello.elpring.di.beans.BeanDefinition;

import java.beans.Introspector;
import java.lang.reflect.Method;

public class BeanNameGenerator {

    private BeanNameGenerator() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    /**
     * beanDefinition을 분석해 Component Class, Configuration Class, Factory Bean Method의 상황에 따라
     * 빈 이름을 리턴
     */
    public static String generate(BeanDefinition beanDefinition) {
        if (beanDefinition.isFactoryBeanMethod()) {
            return generate(beanDefinition.getFactoryMethod());
        }

        return generate(beanDefinition.getBeanType());
    }

    /**
     * @Component, @Configuration에 주어진 빈 이름이 있다면 해당 빈 이름 리턴
     * 없다면 어노테이션이 적용되지 않은 경우 클래스 이름을 카멜 케이스로 변환해 리턴
     */
    public static String generate(Class<?> clazz) {
        String beanName = "";
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getAnnotation(Component.class);
            beanName = component.value();
        } else if (clazz.isAnnotationPresent(Configuration.class)) {
            Configuration configuration = clazz.getAnnotation(Configuration.class);
            beanName = configuration.value();
        }

        return beanName.isBlank() ? toLowerCamelCase(clazz.getSimpleName()) : beanName;
    }

    /**
     * @Bean에 주어진 빈 이름이 있다면 해당 빈 이름 리턴
     * 없다면 메소드 이름 리턴
     */
    public static String generate(Method method) {
        String beanName = "";
        if (method.isAnnotationPresent(Bean.class)) {
            Bean bean = method.getAnnotation(Bean.class);
            beanName = bean.value();
        }

        return beanName.isBlank() ? method.getName() : beanName;
    }

    public static String toLowerCamelCase(String name) {
        return Introspector.decapitalize(name);
    }
}
