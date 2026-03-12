# Database Design (MVP)

## Core Tables

### `app_user`
- `id` BIGINT PK
- `student_no` unique, not null
- `password_hash` not null (BCrypt)
- `nickname` not null
- `role` default `USER`
- `created_at`, `updated_at`

### `canteen`
- `id` BIGINT PK
- `name` unique, not null
- `campus`
- `intro`
- `created_at`, `updated_at`

### `canteen_window`
- `id` BIGINT PK
- `canteen_id` FK -> `canteen(id)`
- `window_name`
- `cuisine_type`
- `intro`
- `avg_score` decimal(3,2)
- `review_count` int
- `status` default `OPEN`
- `created_at`, `updated_at`

### `review`
- `id` BIGINT PK
- `user_id` FK -> `app_user(id)`
- `window_id` FK -> `canteen_window(id)`
- `score` (1-5)
- `comment_text`
- `created_at`, `updated_at`
- unique key `uk_user_window(user_id, window_id)`

## Extension-Reserved Tables
- `favorite_window`
- `blocked_window`
- `share_record`
- `recommendation_seed`

## Search/Ranking Indexes
- `idx_window_name(window_name)`
- `idx_cuisine_type(cuisine_type)`
- `idx_canteen_id(canteen_id)`
- `idx_window_created(window_id, created_at)`