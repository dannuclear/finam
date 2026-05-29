package ru.nuclearius.finam.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Asset {

    @Id
    private String id;
    private String symbol;
    private String ticker;
    private String mic;
    private String isin;
    private String type;
    private String name;
}
