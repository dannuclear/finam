package ru.nuclearius.finam.service.meta;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ChangedEntity {
    private ChangeStatus changeStatus;
}
