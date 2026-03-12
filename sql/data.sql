-- Demo data for School Canteen MVP
-- Default demo password for all users: password

INSERT INTO app_user (id, student_no, password_hash, nickname, role, created_at, updated_at) VALUES
(1, '20230001', '$2a$10$J7dErxeAv4t838enQIPLk.HzXtljBkqPtvip0Y8uSH8DZvDbbzyl.', 'Alice', 'USER', NOW(), NOW()),
(2, '20230002', '$2a$10$J7dErxeAv4t838enQIPLk.HzXtljBkqPtvip0Y8uSH8DZvDbbzyl.', 'Bob', 'USER', NOW(), NOW()),
(3, '20230003', '$2a$10$J7dErxeAv4t838enQIPLk.HzXtljBkqPtvip0Y8uSH8DZvDbbzyl.', 'Carol', 'USER', NOW(), NOW());

INSERT INTO canteen (id, name, campus, intro, created_at, updated_at) VALUES
(1, 'Canteen A', 'Main Campus', 'Nearest to teaching buildings.', NOW(), NOW()),
(2, 'Canteen B', 'Main Campus', 'Many dinner choices.', NOW(), NOW()),
(3, 'Halal Canteen', 'South Campus', 'Halal food and noodles.', NOW(), NOW());

INSERT INTO canteen_window (id, canteen_id, window_name, cuisine_type, intro, avg_score, review_count, status, created_at, updated_at) VALUES
(1, 1, 'North Noodles', 'Noodles', 'Hand-pulled noodles and soup.', 0.00, 0, 'OPEN', NOW(), NOW()),
(2, 1, 'Sichuan Stir Fry', 'Sichuan', 'Spicy dishes with rice.', 0.00, 0, 'OPEN', NOW(), NOW()),
(3, 1, 'Light Salad', 'Light Meal', 'Low-fat meal sets.', 0.00, 0, 'OPEN', NOW(), NOW()),
(4, 2, 'Mala Mix', 'Mala', 'Self-select ingredients.', 0.00, 0, 'OPEN', NOW(), NOW()),
(5, 2, 'Teppanyaki', 'Fast Food', 'Teppanyaki combos.', 0.00, 0, 'OPEN', NOW(), NOW()),
(6, 2, 'Canton Roast', 'Cantonese', 'Roast duck and pork.', 0.00, 0, 'OPEN', NOW(), NOW()),
(7, 3, 'Halal Ramen', 'Noodles', 'Beef ramen with rich broth.', 0.00, 0, 'OPEN', NOW(), NOW()),
(8, 3, 'Xinjiang Noodles', 'Northwest', 'Large portion, spicy flavor.', 0.00, 0, 'OPEN', NOW(), NOW()),
(9, 3, 'Claypot Rice', 'Claypot', 'Warm claypot meals.', 0.00, 0, 'OPEN', NOW(), NOW()),
(10, 2, 'Dumpling Soup', 'Dumpling', 'Fresh dumplings and wonton.', 0.00, 0, 'OPEN', NOW(), NOW());

INSERT INTO review (user_id, window_id, score, comment_text, created_at, updated_at) VALUES
(1, 1, 5, 'Great texture and tasty soup.', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1, 2, 4, 'Good taste but a little salty.', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(1, 3, 5, 'Healthy and clean.', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 4, 3, 'Fresh ingredients but long queue.', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 5, 4, 'Chicken is nice and portions are fair.', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 1, 4, 'Affordable and filling.', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 2, 5, 'Excellent with rice.', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 6, 4, 'Roast meat quality is stable.', DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(2, 7, 5, 'Broth is rich and fragrant.', DATE_SUB(NOW(), INTERVAL 10 HOUR), DATE_SUB(NOW(), INTERVAL 10 HOUR)),
(2, 8, 3, 'A bit too spicy for me.', DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(3, 1, 5, 'Very consistent quality.', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(3, 3, 4, 'Fresh but sauce can improve.', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(3, 6, 5, 'Best roast combo.', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(3, 9, 4, 'Warm and filling meal.', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 10, 2, 'Not my favorite texture.', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR));

UPDATE canteen_window w
SET review_count = (SELECT COUNT(*) FROM review r WHERE r.window_id = w.id),
    avg_score = (SELECT COALESCE(AVG(r.score), 0) FROM review r WHERE r.window_id = w.id);