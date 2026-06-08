package eello.elpring.di.beans.factory.support;

import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.BeanDefinitionHolder;

public class BeanDefinitionReaderUtils {

    public static void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
        String beanName = definitionHolder.getBeanName();
        BeanDefinition beanDefinition = definitionHolder.getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
    }
}
