package org.jorion.trainingtool.report;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulate a collection of resources. Useful for REST API.
 */
public record CollectionResource<T>(List<T> items) {

    public long getTotal() {
        return items.size();
    }

    @Override
    public List<T> items() {
        return Collections.unmodifiableList(items);
    }

}
