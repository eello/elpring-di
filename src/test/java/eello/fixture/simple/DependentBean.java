package eello.fixture.simple;

import eello.container.annotation.Component;

@Component
public class DependentBean {

    private final SimpleBean simpleBean;

    public DependentBean(SimpleBean simpleBean) {
        this.simpleBean = simpleBean;
    }

    public SimpleBean getSimpleBean() {
        return simpleBean;
    }
}
