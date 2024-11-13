package org.jorion.trainingtool.report;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulate a collection of resources. Useful for REST API.
 */
public class CollectionResource<T> {

    private final List<T> items;

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
