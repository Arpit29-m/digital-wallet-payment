package com.digitalwallet.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Simple role entity. We keep it in the DB rather than hardcoding so
 * an admin panel can assign roles without a redeploy.
 *
 * Values expected: ROLE_USER, ROLE_ADMIN
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;   // e.g. "ROLE_USER", "ROLE_ADMIN"

    @Column(name = "description", length = 200)
    private String description;
}
