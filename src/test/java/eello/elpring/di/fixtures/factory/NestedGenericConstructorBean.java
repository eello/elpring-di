package eello.elpring.di.fixtures.factory;

import java.util.List;

public class NestedGenericConstructorBean {

    private final List<List<InterfaceA>> nestedList;

    public NestedGenericConstructorBean(List<List<InterfaceA>> nestedList) {
        this.nestedList = nestedList;
    }

    public List<List<InterfaceA>> getNestedList() {
        return nestedList;
    }
}
