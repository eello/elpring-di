package eello.elpring.di.fixtures.factory;

import java.util.List;

public class ListConstructorBean {

    private final List<InterfaceA> list;

    public ListConstructorBean(List<InterfaceA> list) {
        this.list = list;
    }

    public List<InterfaceA> getList() {
        return list;
    }
}
