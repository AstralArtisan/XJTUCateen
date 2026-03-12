INSERT INTO app_user (id, student_no, password_hash, nickname, role, created_at, updated_at) VALUES
(1, '20230001', '$2a$10$J7dErxeAv4t838enQIPLk.HzXtljBkqPtvip0Y8uSH8DZvDbbzyl.', 'Alice', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '20230002', '$2a$10$J7dErxeAv4t838enQIPLk.HzXtljBkqPtvip0Y8uSH8DZvDbbzyl.', 'Bob', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '20230003', '$2a$10$J7dErxeAv4t838enQIPLk.HzXtljBkqPtvip0Y8uSH8DZvDbbzyl.', 'Carol', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO canteen (id, name, campus, intro, created_at, updated_at) VALUES
(1, 'Canteen A', 'Main Campus', 'Nearest to teaching buildings.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Canteen B', 'Main Campus', 'Many dinner choices.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Halal Canteen', 'South Campus', 'Halal food and noodles.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO canteen_window (id, canteen_id, window_name, cuisine_type, intro, avg_score, review_count, status, created_at, updated_at) VALUES
(1, 1, 'North Noodles', 'Noodles', 'Hand-pulled noodles and soup.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Sichuan Stir Fry', 'Sichuan', 'Spicy dishes with rice.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'Light Salad', 'Light Meal', 'Low-fat meal sets.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 'Mala Mix', 'Mala', 'Self-select ingredients.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 'Teppanyaki', 'Fast Food', 'Teppanyaki combos.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 2, 'Canton Roast', 'Cantonese', 'Roast duck and pork.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 3, 'Halal Ramen', 'Noodles', 'Beef ramen with rich broth.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 3, 'Xinjiang Noodles', 'Northwest', 'Large portion, spicy flavor.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 3, 'Claypot Rice', 'Claypot', 'Warm claypot meals.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 2, 'Dumpling Soup', 'Dumpling', 'Fresh dumplings and wonton.', 0.00, 0, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO review (user_id, window_id, score, comment_text, created_at, updated_at) VALUES
(1, 1, 5, 'Great texture and tasty soup.', TIMESTAMP '2026-03-01 09:00:00', TIMESTAMP '2026-03-01 09:00:00'),
(1, 2, 4, 'Good taste but a little salty.', TIMESTAMP '2026-03-02 10:00:00', TIMESTAMP '2026-03-02 10:00:00'),
(1, 3, 5, 'Healthy and clean.', TIMESTAMP '2026-03-03 11:00:00', TIMESTAMP '2026-03-03 11:00:00'),
(1, 4, 3, 'Fresh ingredients but long queue.', TIMESTAMP '2026-03-04 12:00:00', TIMESTAMP '2026-03-04 12:00:00'),
(1, 5, 4, 'Chicken is nice and portions are fair.', TIMESTAMP '2026-03-05 13:00:00', TIMESTAMP '2026-03-05 13:00:00'),
(2, 1, 4, 'Affordable and filling.', TIMESTAMP '2026-03-06 14:00:00', TIMESTAMP '2026-03-06 14:00:00'),
(2, 2, 5, 'Excellent with rice.', TIMESTAMP '2026-03-07 15:00:00', TIMESTAMP '2026-03-07 15:00:00'),
(2, 6, 4, 'Roast meat quality is stable.', TIMESTAMP '2026-03-08 16:00:00', TIMESTAMP '2026-03-08 16:00:00'),
(2, 7, 5, 'Broth is rich and fragrant.', TIMESTAMP '2026-03-09 17:00:00', TIMESTAMP '2026-03-09 17:00:00'),
(2, 8, 3, 'A bit too spicy for me.', TIMESTAMP '2026-03-10 18:00:00', TIMESTAMP '2026-03-10 18:00:00'),
(3, 1, 5, 'Very consistent quality.', TIMESTAMP '2026-03-11 19:00:00', TIMESTAMP '2026-03-11 19:00:00'),
(3, 3, 4, 'Fresh but sauce can improve.', TIMESTAMP '2026-03-11 20:00:00', TIMESTAMP '2026-03-11 20:00:00'),
(3, 6, 5, 'Best roast combo.', TIMESTAMP '2026-03-12 08:00:00', TIMESTAMP '2026-03-12 08:00:00'),
(3, 9, 4, 'Warm and filling meal.', TIMESTAMP '2026-03-12 09:00:00', TIMESTAMP '2026-03-12 09:00:00'),
(3, 10, 2, 'Not my favorite texture.', TIMESTAMP '2026-03-12 10:00:00', TIMESTAMP '2026-03-12 10:00:00');

UPDATE canteen_window
SET review_count = (SELECT COUNT(*) FROM review r WHERE r.window_id = canteen_window.id),
    avg_score = (SELECT COALESCE(AVG(r.score), 0) FROM review r WHERE r.window_id = canteen_window.id);