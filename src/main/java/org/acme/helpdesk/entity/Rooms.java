package org.acme.helpdesk.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "rooms")
public class Rooms extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false)
    public String name;

    @Column(nullable = false)
    public String description;

    public static Rooms findByName(String name) {
        return find("name", name).firstResult();
    }

}
