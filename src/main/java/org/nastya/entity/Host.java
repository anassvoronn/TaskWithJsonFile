package org.nastya.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.nastya.enums.HostType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hosts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Host {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String name;
    String comment;
    boolean isNegate;
    String fqdn;
    @Enumerated(EnumType.STRING)
    HostType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ips", columnDefinition = "jsonb")
    List<String> ips = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_properties", columnDefinition = "jsonb")
    private List<AdditionalProperty> additionalProperties = new ArrayList<>();
}