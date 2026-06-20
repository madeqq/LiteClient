package me.madeq.client.protocol.components.data;

import java.util.ArrayList;
import java.util.List;

public class DataComponents {
    private final List<DataComponent> dataComponents = new ArrayList<>();

    public List<DataComponent> getDataComponents() {
        return dataComponents;
    }

    public void put(DataComponent component){
        this.dataComponents.add(component);
    }
}
