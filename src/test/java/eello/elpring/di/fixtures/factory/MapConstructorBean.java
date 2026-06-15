package eello.elpring.di.fixtures.factory;

import java.util.Map;

public class MapConstructorBean {

    private final Map<String, InterfaceA> map;

    public MapConstructorBean(Map<String, InterfaceA> map) {
        this.map = map;
    }

    public Map<String, InterfaceA> getMap() {
        return map;
    }
}
