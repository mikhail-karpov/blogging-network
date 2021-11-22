package com.mikhailkarpov.users.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_profile")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public class UserProfile extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "follower", orphanRemoval = true)
    private final Set<Following> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private final Set<Following> followings = new HashSet<>();

    public UserProfile(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UserProfile profile = (UserProfile) o;

        return username.equals(profile.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "userId='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdDate='" + getCreatedDate() + '\'' +
                '}';
    }
}
