package ru.nuclearius.finam.service.meta;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.repository.CrudRepository;

public class MetaUtils {
    public static <E, ID, D extends ChangedEntity> void applyChanges(
            CrudRepository<E, ID> repository,
            List<D> items,
            Function<D, E> mapper,
            Function<D, ID> idExtractor) {

        if (items == null || items.isEmpty()) {
            return;
        }

        for (D dto : items) {
            ChangeStatus changeStatus = dto.getChangeStatus();
            if (changeStatus == null)
                continue;
            else if (changeStatus.isUpsert()) {
                repository.save(mapper.apply(dto));
            } else if (changeStatus.isDeleted()) {
                repository.deleteById(idExtractor.apply(dto));
            }
        }
    }
}
