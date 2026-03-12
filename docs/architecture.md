# Backend Module Responsibilities

- `controller`: HTTP entrypoint, parameter validation, REST response wrapping.
- `service`: core business logic (auth, window query, review submit/update, ranking).
- `repository`: JPA data access and query declarations.
- `entity`: table mappings and relationships.
- `dto`: request/response contracts for API layer.
- `config`: interceptor, password encoder, MVC settings.
- `common`: common response model (`ApiResponse`).
- `exception`: business exception and global error handler.
- `util`: session helper (`LOGIN_USER_ID` extraction and login checks).

## Review Update Rule
- Same user reviewing same window updates existing record (`uk_user_window`).
- After each submit/update, service recalculates:
  - `avg_score`
  - `review_count`

## Extension Placeholders
Reserved entities/tables:
- `FavoriteWindow`
- `BlockedWindow`
- `ShareRecord`
- `RecommendationSeed`

TODO marks are kept where future recommendation/profile logic can be added.