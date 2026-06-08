package eello.elpring.di.beans;

public class BeanDefinitionHolder {

    private final String beanName;
    private final BeanDefinition beanDefinition;

    public BeanDefinitionHolder(String beanName, BeanDefinition beanDefinition) {
        this.beanName = beanName;
        this.beanDefinition = beanDefinition;
    }

    public String getBeanName() {
        return beanName;
    }

    public BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }
}
