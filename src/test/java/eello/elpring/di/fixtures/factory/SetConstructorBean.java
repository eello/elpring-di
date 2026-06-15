package eello.elpring.di.fixtures.factory;

import java.util.Set;

public class SetConstructorBean {

    private final Set<InterfaceA> set;

    public SetConstructorBean(Set<InterfaceA> set) {
        this.set = set;
    }

    public Set<InterfaceA> getSet() {
        return set;
    }
}
