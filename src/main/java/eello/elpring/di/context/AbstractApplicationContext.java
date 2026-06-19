package eello.elpring.di.context;

import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.factory.support.DefaultListableBeanFactory;
import eello.elpring.di.exception.BeansException;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
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
        List<String> factoryBeans = new ArrayList<>();
        List<String> normalBeans = new ArrayList<>();

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            if (beanDefinition.isConfigurationClass()) {
                factoryBeans.add(beanName);
            } else normalBeans.add(beanName);
        }

        /*
            FactoryBean들부터 준비: 스프링에서는 Factory Bean Method로 만들어지는 빈들의 싱글톤을 보장하기 위해 CGLIB으로 컴파일 시 내부 코드를 조작
            (=> getBean()을 활용해 항상 ApplicationContext에서 가져오도록)
            현재는 CGLIB을 적용하진 않지만 이후 코드 변경없이 적용할 수 있도록 Factory Bean부터 준비
         */
        for (String factoryBeanName : factoryBeans) {
            /**
             * TODO: FactoryBean, FactoryBeanMethod에 대해서도 Lazy 적용
             * 적용하기 위해 DefaultBeanDefinition에서 Lazy를 파싱하는 방법을 수정해야함. Annotation을 파싱하는 것도 수정해야 할 수도 있음.
             */
            beanFactory.getBean(factoryBeanName);
        }

        for (String normalBean : normalBeans) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(normalBean);
            if (!beanDefinition.isLazyInit()) {
                beanFactory.getBean(normalBean);
            }
        }
    }
}
