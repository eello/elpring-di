package eello.elpring.di.beans;

import eello.elpring.di.annotation.Component;
import eello.elpring.di.annotation.Lazy;
import eello.elpring.di.annotation.Primary;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;

public class DefaultBeanDefinition implements BeanDefinition {

    private String beanClassName;
    private Class<?> beanType;
    private Class<?>[] interfaces;
    private Constructor<?> constructor;
    private Class<?>[] dependsOn;
    private Class<? extends Annotation>[] metaAnnotations;
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

        def.setMetaAnnotations(clazz);
        def.primary = clazz.isAnnotationPresent(Primary.class);
        def.lazyInit = clazz.isAnnotationPresent(Lazy.class);

        def.scope = BeanScope.SINGLETON;
        return def;
    }

    private void setMetaAnnotations(Class<?> clazz) {
        Set<Class<? extends Annotation>> visited = new HashSet<>();
        Set<Class<? extends Annotation>> metaAnnotationTypes = new HashSet<>();

        for (Annotation annotation : clazz.getAnnotations()) {
            metaAnnotationTypes.addAll(findAllMetaAnnotations(annotation.annotationType(), visited));
        }

        this.metaAnnotations = metaAnnotationTypes.toArray(new Class[0]);
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
    public Class<?>[] getDependsOn() {
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
        return primary == that.primary && lazyInit == that.lazyInit && Objects.equals(beanClassName, that.beanClassName) && Objects.equals(beanType, that.beanType) && scope == that.scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanClassName, beanType, primary, lazyInit, scope);
    }
}
