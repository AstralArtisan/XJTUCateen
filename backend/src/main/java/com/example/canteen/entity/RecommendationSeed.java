package com.example.canteen.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recommendation_seed")
public class RecommendationSeed extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "preferred_cuisine", length = 100)
    private String preferredCuisine;

    @Column(name = "disliked_tags", length = 255)
    private String dislikedTags;

    @Column(name = "weight_profile", length = 1000)
    private String weightProfile;

    // TODO: 后续接入推荐算法时，将结构化画像替换为向量或特征快照。

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

    public String getPreferredCuisine() {
        return preferredCuisine;
    }

    public void setPreferredCuisine(String preferredCuisine) {
        this.preferredCuisine = preferredCuisine;
    }

    public String getDislikedTags() {
        return dislikedTags;
    }

    public void setDislikedTags(String dislikedTags) {
        this.dislikedTags = dislikedTags;
    }

    public String getWeightProfile() {
        return weightProfile;
    }

    public void setWeightProfile(String weightProfile) {
        this.weightProfile = weightProfile;
    }
}
