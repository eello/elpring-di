package eello.elpring.di.fixtures.factory;

public class DependencyBean {
    private final SimpleBean simpleBean;

    public DependencyBean(SimpleBean simpleBean) {
        this.simpleBean = simpleBean;
    }

    public SimpleBean getSimpleBean() {
        return simpleBean;
    }
}
