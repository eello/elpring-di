package eello.container.context;

import eello.container.core.ClassPathBeanDefinitionScanner;
import eello.container.core.BeanFactory;
import eello.container.core.BeanInitializer;
import eello.container.core.DefaultBeanInitializer;
import eello.container.core.DefaultBeanFactory;
import eello.container.core.registry.DefaultSingletonBeanRegistry;

import java.lang.reflect.InvocationTargetException;

public class AnnotationConfigApplicationContext implements ApplicationContext {

    private final String basePackage;
    private final BeanFactory beanFactory;
    private final ClassPathBeanDefinitionScanner scanner;
    private final BeanInitializer initializer;

    public AnnotationConfigApplicationContext(String basePackage) {
        this.basePackage = basePackage;
        beanFactory = new DefaultBeanFactory(new DefaultSingletonBeanRegistry());
        scanner = new ClassPathBeanDefinitionScanner();
        initializer = new DefaultBeanInitializer(beanFactory, scanner);
    }

    @Override
    public void refresh() throws
            ClassNotFoundException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {
        initializer.initialize(basePackage);
    }

    @Override
    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    @Override
    public <T> T[] getBean(Class<T> requiredType) {
        return beanFactory.getBean(requiredType);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) {
        return beanFactory.getBean(beanName, requiredType);
    }
}
