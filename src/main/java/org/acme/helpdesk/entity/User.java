package org.acme.helpdesk.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

//Defining user roles (Can be part )
public enum UserRole {
    USER,
    OPERATOR
}

@Entity
@Table(name = "user")
public class User extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    public String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)   
    public UserRole role;
    
}
