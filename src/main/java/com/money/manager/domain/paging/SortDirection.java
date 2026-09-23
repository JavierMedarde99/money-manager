package com.money.manager.domain.paging;

public enum SortDirection {
    ASC, DESC;

    public static SortDirection getByName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (Exception e) {
            return DESC;
        }
    }
}
