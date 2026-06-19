package eello.elpring.di.fixtures.config;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class TestBean implements Serializable {
    private final DependencyBean dependencyBean;
    private final AnotherDependencyBean anotherDependencyBean;
    private final List<DependencyBean> dependencyBeans;
    private final DependencyBean[] dependencyBeanArray;
    private final Set<DependencyBean> dependencyBeanSet;

    public TestBean() {
        this.dependencyBean = null;
        this.anotherDependencyBean = null;
        this.dependencyBeans = null;
        this.dependencyBeanArray = null;
        this.dependencyBeanSet = null;
    }

    public TestBean(DependencyBean dependencyBean) {
        this.dependencyBean = dependencyBean;
        this.anotherDependencyBean = null;
        this.dependencyBeans = null;
        this.dependencyBeanArray = null;
        this.dependencyBeanSet = null;
    }

    public TestBean(DependencyBean dependencyBean, AnotherDependencyBean anotherDependencyBean) {
        this.dependencyBean = dependencyBean;
        this.anotherDependencyBean = anotherDependencyBean;
        this.dependencyBeans = null;
        this.dependencyBeanArray = null;
        this.dependencyBeanSet = null;
    }

    public TestBean(List<DependencyBean> dependencyBeans, DependencyBean[] dependencyBeanArray, Set<DependencyBean> dependencyBeanSet) {
        this.dependencyBean = null;
        this.anotherDependencyBean = null;
        this.dependencyBeans = dependencyBeans;
        this.dependencyBeanArray = dependencyBeanArray;
        this.dependencyBeanSet = dependencyBeanSet;
    }

    public DependencyBean getDependencyBean() {
        return dependencyBean;
    }

    public AnotherDependencyBean getAnotherDependencyBean() {
        return anotherDependencyBean;
    }

    public List<DependencyBean> getDependencyBeans() {
        return dependencyBeans;
    }

    public DependencyBean[] getDependencyBeanArray() {
        return dependencyBeanArray;
    }

    public Set<DependencyBean> getDependencyBeanSet() {
        return dependencyBeanSet;
    }
}
