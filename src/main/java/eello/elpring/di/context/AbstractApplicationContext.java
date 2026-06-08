package eello.elpring.di.context;

import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.factory.support.DefaultListableBeanFactory;
import eello.elpring.di.exception.BeansException;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class AbstractApplicationContext implements ConfigurableApplicationContext {

    public abstract DefaultListableBeanFactory getBeanFactory();

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
        return this.getBeanFactory().getBeansWithAnnotation(annotationType);
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return this.getBeanFactory().getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return this.getBeanFactory().getBean(name, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return this.getBeanFactory().getBean(requiredType);
    }

    // BeanDefinitionRegistry 에 등록된 빈 정보(BeanDefinition)를 읽고 실제 빈 인스턴스를 생성
    @Override
    public void refresh() {
        DefaultListableBeanFactory beanFactory = this.getBeanFactory();

        // 빈 등록 순서 결정 후 인스턴스 생성 후 등록
        // 현재 빈 인스턴스 생성 순서는 DFS 방식의 그래프 탐색으로 주입에 필요한 빈들 우선으로 생성
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            if (!beanDefinition.isLazyInit()) {
                beanFactory.getBean(beanName);
            }
        }
    }
}
