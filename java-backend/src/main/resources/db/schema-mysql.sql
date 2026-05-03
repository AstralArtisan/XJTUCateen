CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(64) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    avatar_url TEXT NULL,
    signature TEXT NULL,
    preference_text TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS canteen (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    location VARCHAR(255) NULL,
    description TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stall (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    canteen_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NULL,
    description TEXT NULL,
    avg_rating DECIMAL(6,2) NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_canteen_stall_name (canteen_id, name),
    KEY idx_stall_category (category),
    KEY idx_stall_avg_rating (avg_rating),
    KEY idx_stall_review_count (review_count),
    CONSTRAINT fk_stall_canteen FOREIGN KEY (canteen_id) REFERENCES canteen(id)
);

CREATE TABLE IF NOT EXISTS tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stall_tag (
    stall_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (stall_id, tag_id),
    CONSTRAINT fk_stall_tag_stall FOREIGN KEY (stall_id) REFERENCES stall(id) ON DELETE CASCADE,
    CONSTRAINT fk_stall_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    stall_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content TEXT NULL,
    is_deleted INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_stall_review (user_id, stall_id),
    KEY idx_review_stall_id (stall_id),
    KEY idx_review_user_id (user_id),
    KEY idx_review_created_at (created_at),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_review_stall FOREIGN KEY (stall_id) REFERENCES stall(id)
);

CREATE TABLE IF NOT EXISTS review_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    review_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_review_like (user_id, review_id),
    KEY idx_review_like_review_id (review_id),
    CONSTRAINT fk_review_like_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_review_like_review FOREIGN KEY (review_id) REFERENCES review(id)
);

CREATE TABLE IF NOT EXISTS review_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    review_id BIGINT NOT NULL,
    reason TEXT NULL,
    status INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_review_report (user_id, review_id),
    KEY idx_review_report_review_id (review_id),
    KEY idx_review_report_status (status),
    CONSTRAINT fk_review_report_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_review_report_review FOREIGN KEY (review_id) REFERENCES review(id)
);

CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    stall_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_favorite (user_id, stall_id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_favorite_stall FOREIGN KEY (stall_id) REFERENCES stall(id)
);

CREATE TABLE IF NOT EXISTS blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    stall_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_blacklist (user_id, stall_id),
    CONSTRAINT fk_blacklist_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_blacklist_stall FOREIGN KEY (stall_id) REFERENCES stall(id)
);

CREATE TABLE IF NOT EXISTS history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    stall_id BIGINT NOT NULL,
    visited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_history_stall FOREIGN KEY (stall_id) REFERENCES stall(id)
);

INSERT IGNORE INTO user (student_id, username, password_hash, role, status)
VALUES ('admin001', '管理员', 'WEpUVUNhbnRlZW5BZG1pbpe2ZjZQ1t8UgCJlIFH5TrXNYAdPFp5ynWwNOOSeVLvb', 1, 1);
