package ru.nuclearius.finam.service.meta;

import java.util.Arrays;

public enum ChangeStatus {
    NEW, UPDATED, DELETED;

    public boolean in(ChangeStatus... statuses) {
        return Arrays.stream(statuses).anyMatch(this::is);
    }

    public boolean isUpsert() {
        return in(ChangeStatus.NEW, ChangeStatus.UPDATED);
    }

    public boolean isDeleted() {
        return this == ChangeStatus.DELETED;
    }

    public boolean is(ChangeStatus status) {
        return this == status;
    }
}
