package com.xjtu.canteen.service;

import com.xjtu.canteen.security.PasswordUtil;
import com.xjtu.canteen.security.TokenUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoreService {
    private final JdbcTemplate jdbc;
    private final TokenUtil tokenUtil;

    private static final List<String> CATEGORY_OPTIONS = List.of(
        "面条", "饺子馄饨", "米饭套餐", "盖饭", "炒饭", "拌饭", "米线粉丝", "快餐", "轻食", "汤食", "小吃", "夜宵烧烤"
    );

    public CoreService(JdbcTemplate jdbc, TokenUtil tokenUtil) {
        this.jdbc = jdbc;
        this.tokenUtil = tokenUtil;
    }

    public Map<String, Object> getUserById(Long userId) {
        Map<String, Object> row = one("SELECT * FROM user WHERE id = ?", userId);
        return row == null ? null : sanitizeUser(row);
    }

    public Map<String, Object> register(String studentId, String username, String password) {
        if (one("SELECT id FROM user WHERE student_id = ?", studentId) != null) return null;
        String hash = PasswordUtil.hashPassword(password);
        Long id = insertAndReturnId(
            "INSERT INTO user (student_id, username, password_hash, role, status) VALUES (?, ?, ?, 0, 1)",
            studentId, username, hash
        );
        return sanitizeUser(one("SELECT * FROM user WHERE id = ?", id));
    }

    public Map<String, Object> login(String studentId, String password) {
        Map<String, Object> row = one("SELECT * FROM user WHERE student_id = ?", studentId);
        if (row == null) return null;
        if (((Number) row.get("status")).intValue() != 1) return null;
        if (!PasswordUtil.verifyPassword(password, Objects.toString(row.get("passwordHash"), ""))) return null;
        return Map.of(
            "token", tokenUtil.createToken(((Number) row.get("id")).longValue(), ((Number) row.get("role")).intValue()),
            "user", sanitizeUser(row)
        );
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Map<String, Object> row = one("SELECT * FROM user WHERE id = ?", userId);
        if (row == null) return false;
        if (!PasswordUtil.verifyPassword(oldPassword, Objects.toString(row.get("passwordHash"), ""))) return false;
        jdbc.update("UPDATE user SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            PasswordUtil.hashPassword(newPassword), userId);
        return true;
    }

    public Map<String, Object> updateProfile(Long userId, Map<String, Object> data) {
        Map<String, Object> row = one("SELECT * FROM user WHERE id = ?", userId);
        if (row == null) return null;
        String oldStudentId = Objects.toString(row.get("studentId"), "");
        String studentId = valueOf(data.getOrDefault("student_id", oldStudentId));
        if (!studentId.equals(oldStudentId)) {
            if (one("SELECT id FROM user WHERE student_id = ? AND id != ?", studentId, userId) != null) return Map.of("_error", "该账号已存在");
        }
        jdbc.update(
            "UPDATE user SET student_id = ?, username = ?, avatar_url = ?, signature = ?, preference_text = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            studentId,
            valueOf(data.getOrDefault("username", row.get("username"))),
            nullableValue(data.getOrDefault("avatar_url", row.get("avatarUrl"))),
            nullableValue(data.getOrDefault("signature", row.get("signature"))),
            nullableValue(data.getOrDefault("preference_text", row.get("preferenceText"))),
            userId
        );
        return sanitizeUser(one("SELECT * FROM user WHERE id = ?", userId));
    }

    public List<Map<String, Object>> getAllCanteens() {
        return jdbc.queryForList("SELECT id, name, location, description FROM canteen ORDER BY id");
    }

    public Map<String, Object> getCanteenDetail(Long id) {
        return one("SELECT id, name, location, description FROM canteen WHERE id = ?", id);
    }

    public Map<String, Object> createCanteen(Map<String, Object> data) {
        Long id = insertAndReturnId("INSERT INTO canteen (name, location, description) VALUES (?, ?, ?)",
            data.get("name"), nullableValue(data.get("location")), nullableValue(data.get("description")));
        return getCanteenDetail(id);
    }

    public Map<String, Object> updateCanteen(Long id, Map<String, Object> data) {
        Map<String, Object> row = one("SELECT * FROM canteen WHERE id = ?", id);
        if (row == null) return null;
        jdbc.update("UPDATE canteen SET name = ?, location = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            valueOf(data.getOrDefault("name", row.get("name"))),
            nullableValue(data.getOrDefault("location", row.get("location"))),
            nullableValue(data.getOrDefault("description", row.get("description"))),
            id);
        return getCanteenDetail(id);
    }

    public List<String> listCategories() {
        List<String> existing = jdbc.queryForList("SELECT DISTINCT category FROM stall WHERE category IS NOT NULL", String.class);
        List<String> ordered = CATEGORY_OPTIONS.stream().filter(existing::contains).collect(Collectors.toCollection(ArrayList::new));
        List<String> extra = existing.stream().filter(x -> !CATEGORY_OPTIONS.contains(x)).sorted().toList();
        ordered.addAll(extra);
        return ordered;
    }

    public List<Map<String, Object>> listTags() {
        return jdbc.queryForList("SELECT id, name, description, created_at FROM tag ORDER BY name");
    }

    public Map<String, Object> createTag(Map<String, Object> data) {
        String name = valueOf(data.get("name"));
        Map<String, Object> existing = one("SELECT id, name, description, created_at FROM tag WHERE name = ?", name);
        if (existing != null) return existing;
        Long id = insertAndReturnId("INSERT INTO tag (name, description) VALUES (?, ?)", name, nullableValue(data.get("description")));
        return one("SELECT id, name, description, created_at FROM tag WHERE id = ?", id);
    }

    public Map<String, Object> updateTag(Long tagId, Map<String, Object> data) {
        Map<String, Object> row = one("SELECT * FROM tag WHERE id = ?", tagId);
        if (row == null) return null;
        String name = valueOf(data.getOrDefault("name", row.get("name")));
        Map<String, Object> duplicate = one("SELECT id FROM tag WHERE name = ? AND id != ?", name, tagId);
        if (duplicate != null) {
            return one("SELECT id, name, description, created_at FROM tag WHERE id = ?", duplicate.get("id"));
        }
        jdbc.update("UPDATE tag SET name = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            name, nullableValue(data.getOrDefault("description", row.get("description"))), tagId);
        return one("SELECT id, name, description, created_at FROM tag WHERE id = ?", tagId);
    }

    public Map<String, Object> queryStalls(int page, int pageSize, Long canteenId, String category, String keyword, String sortBy, String tagName) {
        StringBuilder base = new StringBuilder(" FROM stall s JOIN canteen c ON c.id = s.canteen_id ");
        List<Object> params = new ArrayList<>();
        if (notBlank(tagName)) {
            base.append(" JOIN stall_tag st ON st.stall_id = s.id JOIN tag t ON t.id = st.tag_id ");
        }
        base.append(" WHERE 1 = 1 AND s.status = 1 ");
        if (canteenId != null) { base.append(" AND s.canteen_id = ? "); params.add(canteenId); }
        if (notBlank(category)) { base.append(" AND s.category = ? "); params.add(category); }
        if (notBlank(keyword)) { base.append(" AND (s.name LIKE ? OR s.description LIKE ?) "); params.add("%" + keyword + "%"); params.add("%" + keyword + "%"); }
        if (notBlank(tagName)) { base.append(" AND t.name = ? "); params.add(tagName); }

        String orderClause = " ORDER BY id DESC ";
        if ("score".equals(sortBy)) orderClause = " ORDER BY avg_rating DESC, review_count DESC, id DESC ";
        if ("hot".equals(sortBy)) orderClause = " ORDER BY review_count DESC, avg_rating DESC, id DESC ";

        int total = jdbc.queryForObject("SELECT COUNT(DISTINCT s.id) " + base, Integer.class, params.toArray());
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(pageSize);
        queryParams.add((page - 1) * pageSize);

        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT DISTINCT s.id, s.canteen_id, c.name AS canteen_name, s.name, s.category, s.description, ROUND(s.avg_rating, 2) AS avg_rating, s.review_count, s.status "
                + base + orderClause + " LIMIT ? OFFSET ?",
            queryParams.toArray()
        );
        List<Map<String, Object>> items = rows.stream().map(item -> {
            Long id = ((Number) item.get("id")).longValue();
            item.put("tags", fetchTagNames(id));
            return item;
        }).toList();
        return pagePayload(items, total, page, pageSize);
    }

    public Map<String, Object> getStallDetail(Long stallId) {
        Map<String, Object> row = one(
            "SELECT s.id, s.canteen_id, c.name AS canteen_name, s.name, s.category, s.description, ROUND(s.avg_rating, 2) AS avg_rating, s.review_count, s.status " +
                "FROM stall s JOIN canteen c ON c.id = s.canteen_id WHERE s.id = ?", stallId);
        if (row == null) return null;
        row.put("tags", fetchTagNames(stallId));
        return row;
    }

    public Map<String, Object> createStall(Map<String, Object> data) {
        Long id = insertAndReturnId("INSERT INTO stall (canteen_id, name, category, description) VALUES (?, ?, ?, ?)",
            num(data.get("canteen_id")), data.get("name"), nullableValue(data.get("category")), nullableValue(data.get("description")));
        if (data.containsKey("tags")) replaceStallTags(id, parseTags(data.get("tags")));
        return getStallDetail(id);
    }

    public Map<String, Object> updateStall(Long stallId, Map<String, Object> data) {
        Map<String, Object> row = one("SELECT * FROM stall WHERE id = ?", stallId);
        if (row == null) return null;
        jdbc.update("UPDATE stall SET canteen_id = ?, name = ?, category = ?, description = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            num(data.getOrDefault("canteen_id", row.get("canteenId"))),
            valueOf(data.getOrDefault("name", row.get("name"))),
            nullableValue(data.getOrDefault("category", row.get("category"))),
            nullableValue(data.getOrDefault("description", row.get("description"))),
            num(data.getOrDefault("status", row.get("status"))),
            stallId);
        if (data.containsKey("tags")) replaceStallTags(stallId, parseTags(data.get("tags")));
        return getStallDetail(stallId);
    }

    public Map<String, Object> disableStall(Long stallId) {
        return updateStall(stallId, Map.of("status", 0));
    }

    public Map<String, Object> createOrUpdateReview(Long userId, Long stallId, int rating, String content) {
        if (getStallDetail(stallId) == null) return null;
        Map<String, Object> row = one("SELECT * FROM review WHERE user_id = ? AND stall_id = ?", userId, stallId);
        Long reviewId;
        if (row != null) {
            reviewId = ((Number) row.get("id")).longValue();
            jdbc.update("UPDATE review SET rating = ?, content = ?, is_deleted = 0, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                rating, nullableValue(content), reviewId);
        } else {
            reviewId = insertAndReturnId("INSERT INTO review (user_id, stall_id, rating, content) VALUES (?, ?, ?, ?)",
                userId, stallId, rating, nullableValue(content));
        }
        recalculateStallStats(stallId);
        return one("SELECT * FROM review WHERE id = ?", reviewId);
    }

    public Map<String, Object> getStallReviews(Long stallId, int page, int pageSize, String sortBy) {
        String order = " ORDER BY r.created_at DESC, r.id DESC ";
        if ("score_desc".equals(sortBy)) order = " ORDER BY r.rating DESC, r.created_at DESC, r.id DESC ";
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM review WHERE stall_id = ? AND is_deleted = 0", Integer.class, stallId);
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT r.id, r.user_id, u.username, r.rating, r.content, r.created_at, r.updated_at " +
                "FROM review r JOIN user u ON u.id = r.user_id WHERE r.stall_id = ? AND r.is_deleted = 0 " + order + " LIMIT ? OFFSET ?",
            stallId, pageSize, (page - 1) * pageSize
        );
        return pagePayload(list, total, page, pageSize);
    }

    public Map<String, Object> getMyReviews(Long userId, int page, int pageSize) {
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM review WHERE user_id = ? AND is_deleted = 0", Integer.class, userId);
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT r.id, r.user_id, r.stall_id, s.name AS stall_name, c.name AS canteen_name, r.rating, r.content, r.created_at, r.updated_at " +
                "FROM review r JOIN stall s ON s.id = r.stall_id JOIN canteen c ON c.id = s.canteen_id " +
                "WHERE r.user_id = ? AND r.is_deleted = 0 ORDER BY r.updated_at DESC, r.id DESC LIMIT ? OFFSET ?",
            userId, pageSize, (page - 1) * pageSize
        );
        return pagePayload(list, total, page, pageSize);
    }

    public Map<String, Object> updateMyReview(Long userId, Long reviewId, int rating, String content) {
        Map<String, Object> row = one("SELECT * FROM review WHERE id = ? AND user_id = ? AND is_deleted = 0", reviewId, userId);
        if (row == null) return null;
        jdbc.update("UPDATE review SET rating = ?, content = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            rating, nullableValue(content), reviewId);
        recalculateStallStats(((Number) row.get("stallId")).longValue());
        return one("SELECT * FROM review WHERE id = ?", reviewId);
    }

    public Map<String, Object> softDeleteReview(Long reviewId) {
        Map<String, Object> row = one("SELECT * FROM review WHERE id = ?", reviewId);
        if (row == null) return null;
        jdbc.update("UPDATE review SET is_deleted = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?", reviewId);
        recalculateStallStats(((Number) row.get("stallId")).longValue());
        return Map.of("result", "success");
    }

    public List<Map<String, Object>> rankingScore(int limit, Long canteenId) {
        StringBuilder sql = new StringBuilder(
            "SELECT s.id AS stall_id, s.name AS stall_name, s.canteen_id, c.name AS canteen_name, ROUND(s.avg_rating, 2) AS avg_rating, s.review_count " +
                "FROM stall s JOIN canteen c ON c.id = s.canteen_id WHERE s.status = 1");
        List<Object> params = new ArrayList<>();
        if (canteenId != null) { sql.append(" AND s.canteen_id = ?"); params.add(canteenId); }
        sql.append(" ORDER BY s.avg_rating DESC, s.review_count DESC, s.id DESC LIMIT ?");
        params.add(limit);
        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> rankingHot(int limit, Long canteenId) {
        StringBuilder sql = new StringBuilder(
            "SELECT s.id AS stall_id, s.name AS stall_name, s.canteen_id, c.name AS canteen_name, ROUND(s.avg_rating, 2) AS avg_rating, s.review_count " +
                "FROM stall s JOIN canteen c ON c.id = s.canteen_id WHERE s.status = 1");
        List<Object> params = new ArrayList<>();
        if (canteenId != null) { sql.append(" AND s.canteen_id = ?"); params.add(canteenId); }
        sql.append(" ORDER BY s.review_count DESC, s.avg_rating DESC, s.id DESC LIMIT ?");
        params.add(limit);
        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> rankingLatest(int limit, Long canteenId) {
        StringBuilder sql = new StringBuilder(
            "SELECT s.id AS stall_id, s.name AS stall_name, c.name AS canteen_name, MAX(r.updated_at) AS latest_review_time, ROUND(s.avg_rating, 2) AS avg_rating, s.review_count " +
                "FROM stall s JOIN canteen c ON c.id = s.canteen_id LEFT JOIN review r ON r.stall_id = s.id AND r.is_deleted = 0 WHERE s.status = 1");
        List<Object> params = new ArrayList<>();
        if (canteenId != null) { sql.append(" AND s.canteen_id = ?"); params.add(canteenId); }
        sql.append(" GROUP BY s.id, s.name, c.name, s.avg_rating, s.review_count ORDER BY latest_review_time DESC, s.id DESC LIMIT ?");
        params.add(limit);
        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public Map<String, Object> addFavorite(Long userId, Long stallId) {
        jdbc.update("INSERT IGNORE INTO favorite (user_id, stall_id) VALUES (?, ?)", userId, stallId);
        return one("SELECT id, user_id, stall_id, created_at FROM favorite WHERE user_id = ? AND stall_id = ?", userId, stallId);
    }

    public Map<String, Object> removeFavorite(Long userId, Long stallId) {
        jdbc.update("DELETE FROM favorite WHERE user_id = ? AND stall_id = ?", userId, stallId);
        return Map.of("result", "success");
    }

    public Map<String, Object> favorites(Long userId, int page, int pageSize) {
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM favorite WHERE user_id = ?", Integer.class, userId);
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT s.id AS stall_id, s.name AS stall_name, c.name AS canteen_name, ROUND(s.avg_rating, 2) AS avg_rating, s.review_count, f.created_at " +
                "FROM favorite f JOIN stall s ON s.id = f.stall_id JOIN canteen c ON c.id = s.canteen_id " +
                "WHERE f.user_id = ? ORDER BY f.created_at DESC, f.id DESC LIMIT ? OFFSET ?",
            userId, pageSize, (page - 1) * pageSize
        );
        return pagePayload(list, total, page, pageSize);
    }

    public Map<String, Object> addBlacklist(Long userId, Long stallId) {
        jdbc.update("INSERT IGNORE INTO blacklist (user_id, stall_id) VALUES (?, ?)", userId, stallId);
        return one("SELECT id, user_id, stall_id, created_at FROM blacklist WHERE user_id = ? AND stall_id = ?", userId, stallId);
    }

    public Map<String, Object> removeBlacklist(Long userId, Long stallId) {
        jdbc.update("DELETE FROM blacklist WHERE user_id = ? AND stall_id = ?", userId, stallId);
        return Map.of("result", "success");
    }

    public Map<String, Object> blacklists(Long userId, int page, int pageSize) {
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM blacklist WHERE user_id = ?", Integer.class, userId);
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT s.id AS stall_id, s.name AS stall_name, c.name AS canteen_name, b.created_at " +
                "FROM blacklist b JOIN stall s ON s.id = b.stall_id JOIN canteen c ON c.id = s.canteen_id " +
                "WHERE b.user_id = ? ORDER BY b.created_at DESC, b.id DESC LIMIT ? OFFSET ?",
            userId, pageSize, (page - 1) * pageSize
        );
        return pagePayload(list, total, page, pageSize);
    }

    public Map<String, Object> addHistory(Long userId, Long stallId) {
        Map<String, Object> recent = one(
            "SELECT id FROM history WHERE user_id = ? AND stall_id = ? AND visited_at >= DATE_SUB(NOW(), INTERVAL 30 MINUTE) ORDER BY visited_at DESC, id DESC LIMIT 1",
            userId, stallId
        );
        if (recent == null) {
            jdbc.update("INSERT INTO history (user_id, stall_id) VALUES (?, ?)", userId, stallId);
        }
        return Map.of("result", "success");
    }

    public Map<String, Object> histories(Long userId, int page, int pageSize) {
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM history WHERE user_id = ?", Integer.class, userId);
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT s.id AS stall_id, s.name AS stall_name, c.name AS canteen_name, h.visited_at " +
                "FROM history h JOIN stall s ON s.id = h.stall_id JOIN canteen c ON c.id = s.canteen_id " +
                "WHERE h.user_id = ? ORDER BY h.visited_at DESC, h.id DESC LIMIT ? OFFSET ?",
            userId, pageSize, (page - 1) * pageSize
        );
        return pagePayload(list, total, page, pageSize);
    }

    public List<Map<String, Object>> listUsers() {
        return jdbc.queryForList("SELECT id, student_id, username, role, status, avatar_url, signature, created_at FROM user ORDER BY role DESC, id ASC");
    }

    public Map<String, Object> updateUserRole(Long userId, int role) {
        if (one("SELECT id FROM user WHERE id = ?", userId) == null) return null;
        jdbc.update("UPDATE user SET role = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", role, userId);
        return one("SELECT id, student_id, username, role, status, avatar_url, signature, created_at FROM user WHERE id = ?", userId);
    }

    public List<Map<String, Object>> recommendToday(Long userId, Long canteenId, String category, boolean excludeBlacklist, int limit, int seed) {
        List<Map<String, Object>> ranked = rankCandidates(userId, "", canteenId, category, excludeBlacklist);
        return rotateAndPick(ranked, limit, seed + (userId == null ? 0 : userId.intValue()));
    }

    public List<Map<String, Object>> recommendPersonalized(Long userId, String preferenceText, boolean excludeBlacklist, int limit) {
        List<Map<String, Object>> ranked = rankCandidates(userId, preferenceText, null, null, excludeBlacklist);
        return ranked.stream().limit(limit).toList();
    }

    public List<Map<String, Object>> recommendFeed(Long userId, String preferenceText, Long canteenId, String category, boolean excludeBlacklist, int limit, int seed) {
        List<Map<String, Object>> ranked = rankCandidates(userId, preferenceText, canteenId, category, excludeBlacklist);
        return rotateAndPick(ranked, limit, seed + (userId == null ? 0 : userId.intValue()));
    }

    private void recalculateStallStats(Long stallId) {
        Map<String, Object> agg = one("SELECT COUNT(*) AS review_count, COALESCE(AVG(rating), 0) AS avg_rating FROM review WHERE stall_id = ? AND is_deleted = 0", stallId);
        jdbc.update("UPDATE stall SET review_count = ?, avg_rating = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            ((Number) agg.get("reviewCount")).intValue(), ((Number) agg.get("avgRating")).doubleValue(), stallId);
    }

    private List<Map<String, Object>> rankCandidates(Long userId, String preferenceText, Long canteenId, String category, boolean excludeBlacklist) {
        Set<Long> blacklist = new HashSet<>();
        Set<Long> liked = new HashSet<>();
        Set<String> preferredTags = new HashSet<>();
        List<String> keywords = splitKeywords(preferenceText);

        if (userId != null) {
            jdbc.queryForList("SELECT stall_id FROM blacklist WHERE user_id = ?", userId).forEach(r -> blacklist.add(((Number) r.get("stall_id")).longValue()));
            jdbc.queryForList("SELECT stall_id FROM favorite WHERE user_id = ?", userId).forEach(r -> liked.add(((Number) r.get("stall_id")).longValue()));
            jdbc.queryForList("SELECT stall_id FROM review WHERE user_id = ? AND is_deleted = 0 AND rating >= 4", userId).forEach(r -> liked.add(((Number) r.get("stall_id")).longValue()));
            jdbc.queryForList("SELECT stall_id FROM history WHERE user_id = ? ORDER BY visited_at DESC, id DESC LIMIT 8", userId)
                .forEach(r -> liked.add(((Number) r.get("stall_id")).longValue()));
            Map<String, Object> user = one("SELECT preference_text FROM user WHERE id = ?", userId);
            if (user != null && user.get("preferenceText") != null) keywords.addAll(splitKeywords(valueOf(user.get("preferenceText"))));
            jdbc.queryForList(
                "SELECT DISTINCT t.name FROM review r JOIN stall_tag st ON st.stall_id = r.stall_id JOIN tag t ON t.id = st.tag_id WHERE r.user_id = ? AND r.is_deleted = 0 AND r.rating >= 4",
                userId
            ).forEach(r -> preferredTags.add(valueOf(r.get("name"))));
        }

        StringBuilder sql = new StringBuilder(
            "SELECT s.id AS stall_id, s.name AS stall_name, c.name AS canteen_name, s.category, ROUND(s.avg_rating, 2) AS avg_rating, s.review_count, s.description FROM stall s JOIN canteen c ON c.id = s.canteen_id WHERE s.status = 1"
        );
        List<Object> params = new ArrayList<>();
        if (canteenId != null) { sql.append(" AND s.canteen_id = ?"); params.add(canteenId); }
        if (notBlank(category)) { sql.append(" AND s.category = ?"); params.add(category); }
        List<Map<String, Object>> items = jdbc.queryForList(sql.toString(), params.toArray());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Long stallId = ((Number) item.get("stall_id")).longValue();
            if (excludeBlacklist && blacklist.contains(stallId)) continue;
            List<String> tags = jdbc.queryForList(
                "SELECT t.name FROM stall_tag st JOIN tag t ON t.id = st.tag_id WHERE st.stall_id = ? ORDER BY t.name", String.class, stallId
            );
            int score = (int) Math.round(((Number) item.get("avg_rating")).doubleValue() * 20 + ((Number) item.get("review_count")).intValue() * 6);
            List<String> reasons = new ArrayList<>();
            if (liked.contains(stallId)) { score += 20; reasons.add("你收藏或评过高分的窗口里有类似的，应该对口"); }
            List<String> matchedTags = tags.stream().filter(preferredTags::contains).toList();
            if (!matchedTags.isEmpty()) { score += 12 + matchedTags.size() * 4; reasons.add("符合你的口味偏好：" + String.join("、", matchedTags)); }
            String text = valueOf(item.get("stall_name")) + " " + valueOf(item.get("category")) + " " + valueOf(item.get("description")) + " " + String.join(" ", tags);
            List<String> hits = keywords.stream().filter(k -> notBlank(k) && text.contains(k)).distinct().toList();
            if (!hits.isEmpty()) { score += 10 + hits.size() * 3; reasons.add("你提到的「" + String.join("、", hits) + "」这里都有"); }
            double avg = ((Number) item.get("avg_rating")).doubleValue();
            if (avg >= 4.6) { score += 6; reasons.add("评分很高，口碑不错"); }
            else if (avg >= 4.3) { reasons.add("评分挺好的，吃过的人大多满意"); }
            if (((Number) item.get("review_count")).intValue() >= 3) { score += 4; reasons.add("评价的人不少，可以参考一下"); }
            if (reasons.isEmpty()) reasons.add("根据评分和热度推荐给你，可以试试");

            Map<String, Object> mapped = new LinkedHashMap<>();
            mapped.put("stall_id", stallId);
            mapped.put("stall_name", item.get("stall_name"));
            mapped.put("canteen_name", item.get("canteen_name"));
            mapped.put("category", item.get("category"));
            mapped.put("avg_rating", item.get("avg_rating"));
            mapped.put("review_count", item.get("review_count"));
            mapped.put("tags", tags);
            mapped.put("reason", String.join("；", reasons.stream().limit(3).toList()));
            mapped.put("match_score", Math.min(100, score));
            result.add(mapped);
        }
        result.sort((a, b) -> {
            int cmp = Integer.compare(((Number) b.get("match_score")).intValue(), ((Number) a.get("match_score")).intValue());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(((Number) b.get("review_count")).intValue(), ((Number) a.get("review_count")).intValue());
            if (cmp != 0) return cmp;
            return Long.compare(((Number) a.get("stall_id")).longValue(), ((Number) b.get("stall_id")).longValue());
        });
        return result;
    }

    private List<Map<String, Object>> rotateAndPick(List<Map<String, Object>> ranked, int limit, int seed) {
        if (ranked.size() <= limit) return ranked.stream().limit(limit).toList();
        int offset = Math.floorMod(seed, ranked.size());
        List<Map<String, Object>> doubled = new ArrayList<>(ranked);
        doubled.addAll(ranked);
        return doubled.subList(offset, offset + limit);
    }

    private List<String> fetchTagNames(Long stallId) {
        return jdbc.queryForList("SELECT t.name FROM stall_tag st JOIN tag t ON t.id = st.tag_id WHERE st.stall_id = ? ORDER BY t.name", String.class, stallId);
    }

    private void replaceStallTags(Long stallId, List<String> tags) {
        jdbc.update("DELETE FROM stall_tag WHERE stall_id = ?", stallId);
        Set<String> seen = new HashSet<>();
        for (String tag : tags) {
            if (!seen.add(tag)) continue;
            Map<String, Object> existing = one("SELECT id FROM tag WHERE name = ?", tag);
            Long tagId;
            if (existing == null) {
                tagId = insertAndReturnId("INSERT INTO tag (name, description) VALUES (?, ?)", tag, tag + " 标签");
            } else {
                tagId = ((Number) existing.get("id")).longValue();
            }
            jdbc.update("INSERT INTO stall_tag (stall_id, tag_id) VALUES (?, ?)", stallId, tagId);
        }
    }

    private List<String> parseTags(Object value) {
        if (value == null) return List.of();
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
        String normalized = value.toString().replace("，", ",");
        return Arrays.stream(normalized.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private List<String> splitKeywords(String text) {
        if (!notBlank(text)) return new ArrayList<>();
        String normalized = text.replace("，", " ").replace(",", " ").replace("、", " ").replace("；", " ").replace(";", " ");
        return Arrays.stream(normalized.split("\\s+")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toCollection(ArrayList::new));
    }

    private Map<String, Object> pagePayload(List<Map<String, Object>> items, int total, int page, int pageSize) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", items);
        data.put("total", total);
        data.put("page", page);
        data.put("page_size", pageSize);
        return data;
    }

    private Map<String, Object> sanitizeUser(Map<String, Object> row) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", row.get("id"));
        user.put("student_id", row.get("studentId"));
        user.put("username", row.get("username"));
        user.put("role", row.get("role"));
        user.put("avatar_url", row.get("avatarUrl"));
        user.put("signature", row.get("signature"));
        user.put("preference_text", row.get("preferenceText"));
        user.put("status", row.get("status"));
        user.put("created_at", row.get("createdAt"));
        return user;
    }

    private Map<String, Object> one(String sql, Object... params) {
        List<Map<String, Object>> list = jdbc.queryForList(sql, params);
        return list.isEmpty() ? null : normalizeKeys(list.get(0));
    }

    private Long insertAndReturnId(String sql, Object... params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps;
        }, keyHolder);
        Objects.requireNonNull(keyHolder.getKey(), "generated key missing");
        return keyHolder.getKey().longValue();
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Object nullableValue(Object value) {
        if (value == null) return null;
        String s = String.valueOf(value);
        return s.isEmpty() ? null : s;
    }

    private Long num(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private Map<String, Object> normalizeKeys(Map<String, Object> raw) {
        Map<String, Object> out = new LinkedHashMap<>(raw);
        for (String key : raw.keySet()) {
            if (key.contains("_")) {
                String camel = toCamel(key);
                out.putIfAbsent(camel, raw.get(key));
            }
        }
        return out;
    }

    private String toCamel(String key) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : key.toCharArray()) {
            if (c == '_') {
                upper = true;
                continue;
            }
            if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
