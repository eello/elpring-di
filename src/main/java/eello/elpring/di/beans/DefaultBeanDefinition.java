package eello.elpring.di.beans;

import eello.elpring.di.annotation.Configuration;
import eello.elpring.di.annotation.Lazy;
import eello.elpring.di.annotation.Primary;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class DefaultBeanDefinition implements BeanDefinition {

    private String beanClassName;
    private Class<?> beanType;
    private Class<?>[] interfaces;
    private Constructor<?> constructor;
    private Parameter[] dependsOn;
    private Class<? extends Annotation>[] metaAnnotations;
    private boolean primary;
    private boolean lazyInit;
    private BeanScope scope;
    private String factoryBeanName;
    private Method factoryMethod;

    private DefaultBeanDefinition() {
    }

    public static BeanDefinition of(Class<?> clazz) {
        DefaultBeanDefinition def = new DefaultBeanDefinition();
        def.beanClassName = clazz.getSimpleName();
        def.beanType = clazz;
        def.interfaces = clazz.getInterfaces();
        def.constructor = clazz.getConstructors()[0];
        def.dependsOn = def.constructor.getParameters();

        def.setMetaAnnotations(clazz);
        def.primary = clazz.isAnnotationPresent(Primary.class);
        def.lazyInit = clazz.isAnnotationPresent(Lazy.class);

        def.scope = BeanScope.SINGLETON;
        return def;
    }

    public static BeanDefinition of(Method factoryMethod, String factoryBeanName) {
        Class<?> returnType = factoryMethod.getReturnType();

        DefaultBeanDefinition def = new DefaultBeanDefinition();
        def.beanClassName = returnType.getSimpleName();
        def.beanType = returnType;
        def.interfaces = returnType.getInterfaces();
//        def.constructor = returnType.getConstructors()[0];
        def.dependsOn = factoryMethod.getParameters();

        def.setMetaAnnotations(factoryMethod);
        def.primary = factoryMethod.isAnnotationPresent(Primary.class);
        def.lazyInit = factoryMethod.isAnnotationPresent(Lazy.class);

        def.scope = BeanScope.SINGLETON;

        def.factoryBeanName = factoryBeanName;
        def.factoryMethod = factoryMethod;
        return def;
    }

    private void setMetaAnnotations(Class<?> clazz) {
        this.metaAnnotations = extractMetaAnnotations(clazz.getAnnotations());
    }

    private void setMetaAnnotations(Method method) {
        this.metaAnnotations = extractMetaAnnotations(method.getAnnotations());
    }

    private Class<? extends Annotation>[] extractMetaAnnotations(Annotation[] annotations) {
        Set<Class<? extends Annotation>> visited = new HashSet<>();
        return Arrays.stream(annotations)
                .map(Annotation::annotationType)
                .flatMap(annotationType -> findAllMetaAnnotations(annotationType, visited).stream())
                .distinct()
                .toArray(size -> (Class<? extends Annotation>[]) new Class[size]);
    }

    private Set<Class<? extends Annotation>> findAllMetaAnnotations(
            Class<? extends Annotation> annotation,
            Set<Class<? extends Annotation>> visited) {
        if (annotation.getName().startsWith("java.lang.annotation.")
                || visited.contains(annotation)) {
            return Collections.emptySet();
        }

        Set<Class<? extends Annotation>> metaAnnotations = new HashSet<>();
        metaAnnotations.add(annotation);
        visited.add(annotation);

        for (Annotation metaAnnotation : annotation.getAnnotations()) {
            Set<Class<? extends Annotation>> componentMetaAnnotations =
                    findAllMetaAnnotations(metaAnnotation.annotationType(), visited);

            metaAnnotations.addAll(componentMetaAnnotations);
        }

        return metaAnnotations;
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
    public Parameter[] getDependsOn() {
        return dependsOn;
    }

    @Override
    public Class<? extends Annotation>[] getMetaAnnotations() {
        return metaAnnotations;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
        for (Class<? extends Annotation> metaAnnotation : metaAnnotations) {
            if (metaAnnotation == annotation) {
                return true;
            }
        }
        return false;
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
        return primary == that.primary && lazyInit == that.lazyInit && Objects.equals(beanClassName,
                that.beanClassName) && Objects.equals(beanType, that.beanType) && scope == that.scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanClassName, beanType, primary, lazyInit, scope);
    }

    @Override
    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    @Override
    public Method getFactoryMethod() {
        return factoryMethod;
    }

    @Override
    public boolean isConfigurationClass() {
        return beanType != null && beanType.isAnnotationPresent(Configuration.class);
    }

    @Override
    public boolean isFactoryBeanMethod() {
        return factoryMethod != null;
    }
}
