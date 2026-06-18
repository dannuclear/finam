package ru.nuclearius.finam.repository.specs;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import ru.nuclearius.finam.utils.StringHelper;

public class CommonSpec {
    public static <T> Specification<T> byQueryOfName(String q) {
        return (root, query, cb) -> {
            if (StringUtils.isEmpty(q))
                return cb.conjunction();
            final String tQ = StringHelper.transliterate(q);
            Expression<String> filterFields = cb.upper(root.get("name"));
            Predicate predicate = cb.or(
                    cb.like(filterFields, cb.upper(cb.literal("%" + q + "%"))),
                    cb.like(filterFields, cb.upper(cb.literal("%" + tQ + "%"))));
            return predicate;
        };
    }
}
