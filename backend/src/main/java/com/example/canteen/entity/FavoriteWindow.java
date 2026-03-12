package com.example.canteen.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "favorite_window",
        uniqueConstraints = @UniqueConstraint(name = "uk_favorite_user_window", columnNames = {"user_id", "window_id"}))
public class FavoriteWindow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "window_id", nullable = false)
    private CanteenWindow window;

    @Column(length = 255)
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public CanteenWindow getWindow() {
        return window;
    }

    public void setWindow(CanteenWindow window) {
        this.window = window;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
