package eello.elpring.di.fixtures.config;

import eello.elpring.di.annotation.Bean;
import eello.elpring.di.annotation.Configuration;
import java.util.List;
import java.util.Set;

@Configuration
public class AppConfig {

    @Bean
    public TestBean simpleBean() {
        return new TestBean();
    }

    @Bean("customName")
    public TestBean customNameBean() {
        return new TestBean();
    }

    @Bean
    public TestBean dependentBean(DependencyBean dependencyBean) {
        return new TestBean(dependencyBean);
    }

    @Bean
    public TestBean multiDependentBean(DependencyBean dependencyBean, AnotherDependencyBean anotherDependencyBean) {
        return new TestBean(dependencyBean, anotherDependencyBean);
    }

    @Bean
    public TestBean collectionDependentBean(List<DependencyBean> dependencyBeans, DependencyBean[] dependencyBeanArray, Set<DependencyBean> dependencyBeanSet) {
        return new TestBean(dependencyBeans, dependencyBeanArray, dependencyBeanSet);
    }

    @Bean
    public ChildAwareBean childAwareBean() {
        return new ChildAwareBean();
    }

    @Bean
    public TestBean converterTargetBean(List<Converter<?>> converters) {
        return new TestBean(converters);
    }
}
