package eello.elpring.di.fixtures.factory;

import java.util.List;

public class WildcardConstructorBean {

    private final List<?> list;

    public WildcardConstructorBean(List<?> list) {
        this.list = list;
    }

    public List<?> getList() {
        return list;
    }
}
