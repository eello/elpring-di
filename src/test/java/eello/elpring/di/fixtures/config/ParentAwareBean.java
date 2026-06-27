package eello.elpring.di.fixtures.config;

import eello.elpring.di.context.ApplicationContext;
import eello.elpring.di.context.ApplicationContextAware;

public class ParentAwareBean implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
