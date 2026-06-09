package eello.elpring.di.context;

import eello.elpring.di.exception.BeansException;

public interface ApplicationContextAware {

    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;
}
