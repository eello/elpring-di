package eello.elpring.di.beans;

import eello.elpring.di.annotation.Lazy;
import eello.elpring.di.annotation.Primary;

import java.lang.reflect.Constructor;
import java.util.Objects;

public class DefaultBeanDefinition implements BeanDefinition {

    private String beanClassName;
    private Class<?> beanType;
    private Class<?>[] interfaces;
    private Constructor<?> constructor;
    private Class<?>[] dependsOn;
    private boolean primary;
    private boolean lazyInit;
    private BeanScope scope;

    private DefaultBeanDefinition() {
    }

    public static BeanDefinition of(Class<?> clazz) {
        return of(clazz, null);
    }

    public static BeanDefinition of(Class<?> clazz, String beanName) {
        DefaultBeanDefinition def = new DefaultBeanDefinition();
        def.beanClassName = clazz.getSimpleName();
        def.beanType = clazz;
        def.interfaces = clazz.getInterfaces();
        def.constructor = clazz.getConstructors()[0];
        def.dependsOn = def.constructor.getParameterTypes();
        def.primary = clazz.isAnnotationPresent(Primary.class);
        def.lazyInit = clazz.isAnnotationPresent(Lazy.class);
        def.scope = BeanScope.SINGLETON;
        return def;
    }

    @Override
    public String getBeanClassName() {
        return beanClassName;
    }

    @Override
    public Class<?> getBeanType() {
        return beanType;
    }

    @Override
    public String getBeanTypeName() {
        return beanType.getName();
    }

    @Override
    public Class<?>[] getInterfaceTypes() {
        return interfaces;
    }

    @Override
    public Constructor<?> getConstructors() {
        return constructor;
    }

    @Override
    public Class<?>[] getDependsOn() {
        return dependsOn;
    }

    @Override
    public boolean isPrimary() {
        return primary;
    }

    @Override
    public boolean isLazyInit() {
        return lazyInit;
    }

    @Override
    public BeanScope getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBeanDefinition that = (DefaultBeanDefinition) o;
        return primary == that.primary && lazyInit == that.lazyInit && Objects.equals(beanClassName, that.beanClassName) && Objects.equals(beanType, that.beanType) && scope == that.scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanClassName, beanType, primary, lazyInit, scope);
    }
}
