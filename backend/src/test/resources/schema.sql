DROP TABLE IF EXISTS recommendation_seed;
DROP TABLE IF EXISTS share_record;
DROP TABLE IF EXISTS blocked_window;
DROP TABLE IF EXISTS favorite_window;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS canteen_window;
DROP TABLE IF EXISTS canteen;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(32) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE canteen (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    campus VARCHAR(100),
    intro VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE canteen_window (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    canteen_id BIGINT NOT NULL,
    window_name VARCHAR(100) NOT NULL,
    cuisine_type VARCHAR(100) NOT NULL,
    intro VARCHAR(500),
    avg_score DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    review_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_window_canteen FOREIGN KEY (canteen_id) REFERENCES canteen(id)
);

CREATE INDEX idx_window_name ON canteen_window(window_name);
CREATE INDEX idx_cuisine_type ON canteen_window(cuisine_type);
CREATE INDEX idx_canteen_id ON canteen_window(canteen_id);

CREATE TABLE review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    window_id BIGINT NOT NULL,
    score TINYINT NOT NULL,
    comment_text VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_review_score CHECK (score BETWEEN 1 AND 5),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_review_window FOREIGN KEY (window_id) REFERENCES canteen_window(id),
    CONSTRAINT uk_user_window UNIQUE (user_id, window_id)
);

CREATE INDEX idx_window_created ON review(window_id, created_at);

CREATE TABLE favorite_window (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    window_id BIGINT NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_favorite_window FOREIGN KEY (window_id) REFERENCES canteen_window(id),
    CONSTRAINT uk_favorite_user_window UNIQUE (user_id, window_id)
);

CREATE TABLE blocked_window (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    window_id BIGINT NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_block_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_block_window FOREIGN KEY (window_id) REFERENCES canteen_window(id),
    CONSTRAINT uk_block_user_window UNIQUE (user_id, window_id)
);

CREATE TABLE share_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    window_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    share_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_share_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_share_window FOREIGN KEY (window_id) REFERENCES canteen_window(id)
);

CREATE TABLE recommendation_seed (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    preferred_cuisine VARCHAR(100),
    disliked_tags VARCHAR(255),
    weight_profile VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommend_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);
