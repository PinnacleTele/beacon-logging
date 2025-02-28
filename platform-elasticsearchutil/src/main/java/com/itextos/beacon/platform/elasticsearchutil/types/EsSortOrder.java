package com.itextos.beacon.platform.elasticsearchutil.types;

import org.elasticsearch.search.sort.SortOrder;

public enum EsSortOrder
{

    ASCENDING(SortOrder.ASC),
    DECESNDING(SortOrder.DESC);

    private final SortOrder order;

    EsSortOrder(
            SortOrder aOrder)
    {
        order = aOrder;
    }

    public SortOrder getSortOrder()
    {
        return order;
    }

}