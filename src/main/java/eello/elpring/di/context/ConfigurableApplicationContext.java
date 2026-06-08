package eello.elpring.di.context;

import eello.elpring.di.exception.BeansException;

public interface ConfigurableApplicationContext extends ApplicationContext {

    void refresh() throws BeansException;
}
