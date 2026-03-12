package com.example.canteen.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "blocked_window",
        uniqueConstraints = @UniqueConstraint(name = "uk_block_user_window", columnNames = {"user_id", "window_id"}))
public class BlockedWindow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "window_id", nullable = false)
    private CanteenWindow window;

    @Column(name = "reason", length = 255)
    private String reason;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
