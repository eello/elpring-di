package eello.elpring.di.fixtures.factory;

import java.util.Map;

public class InvalidMapKeyConstructorBean {

    private final Map<Integer, InterfaceA> map;

    public InvalidMapKeyConstructorBean(Map<Integer, InterfaceA> map) {
        this.map = map;
    }

    public Map<Integer, InterfaceA> getMap() {
        return map;
    }
}
