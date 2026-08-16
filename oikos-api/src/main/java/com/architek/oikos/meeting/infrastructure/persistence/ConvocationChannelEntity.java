package com.architek.oikos.meeting.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The channel catalog. Not an AuditableEntity: reference rows are seeded by
 * migration, not created by anyone whose name is worth keeping.
 */
@Entity
@Table(name = "convocation_channel")
@Getter
@Setter
@NoArgsConstructor
public class ConvocationChannelEntity {

    @Id
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "automated", nullable = false)
    private boolean automated;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "active", nullable = false)
    private boolean active;
}
