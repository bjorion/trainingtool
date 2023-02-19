package org.jorion.trainingtool.dto;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulate a collection of resources. Useful for REST API.
 */
public class CollectionResource<T> {

    private List<T> items;

    public CollectionResource(List<T> items) {
        this.items = items;
    }

    public long getTotal() {
        return items.size();
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

}
