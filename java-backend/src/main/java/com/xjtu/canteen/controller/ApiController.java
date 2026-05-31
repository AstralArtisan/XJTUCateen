package com.xjtu.canteen.controller;

import com.xjtu.canteen.common.ApiResponse;
import com.xjtu.canteen.service.CoreService;
import com.xjtu.canteen.service.LlmService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final CoreService coreService;
    private final LlmService llmService;

    public ApiController(CoreService coreService, LlmService llmService) {
        this.coreService = coreService;
        this.llmService = llmService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse> register(@RequestBody Map<String, Object> body) {
        String studentId = valueOf(body.get("student_id"));
        String username = valueOf(body.get("username"));
        String password = valueOf(body.get("password"));
        if (isBlank(studentId) || isBlank(username) || isBlank(password)) {
            return badRequest("账号、昵称和密码不能为空");
        }
        Map<String, Object> user = coreService.register(studentId, username, password);
        if (user == null) return badRequest("该账号已存在");
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse> login(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = coreService.login(valueOf(body.get("student_id")), valueOf(body.get("password")));
        if (result == null) return badRequest("账号或密码错误");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse> me(HttpServletRequest request) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        Map<String, Object> user = coreService.getUserById(uid);
        if (user == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        if (authUserId(request) == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(Map.of("result", "success")));
    }

    @PutMapping("/auth/password")
    public ResponseEntity<ApiResponse> changePassword(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        boolean ok = coreService.changePassword(uid, valueOf(body.get("old_password")), valueOf(body.get("new_password")));
        if (!ok) return badRequest("原密码错误");
        return ResponseEntity.ok(ApiResponse.success(Map.of("result", "success")));
    }

    @PutMapping("/users/me/profile")
    public ResponseEntity<ApiResponse> updateProfile(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        Map<String, Object> result = coreService.updateProfile(uid, body);
        if (result == null) return notFound();
        if (result.containsKey("_error")) return badRequest(valueOf(result.get("_error")));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/canteens")
    public ResponseEntity<ApiResponse> canteens() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", coreService.getAllCanteens())));
    }

    @GetMapping("/canteens/{id}")
    public ResponseEntity<ApiResponse> canteenDetail(@PathVariable Long id) {
        Map<String, Object> item = coreService.getCanteenDetail(id);
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse> categories() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", coreService.listCategories())));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse> tags() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", coreService.listTags())));
    }

    @GetMapping("/stalls")
    public ResponseEntity<ApiResponse> stalls(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(name = "page_size", defaultValue = "10") int pageSize,
        @RequestParam(name = "canteen_id", required = false) Long canteenId,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String keyword,
        @RequestParam(name = "sort_by", required = false) String sortBy,
        @RequestParam(name = "tag_name", required = false) String tagName
    ) {
        return ResponseEntity.ok(ApiResponse.success(coreService.queryStalls(page, pageSize, canteenId, category, keyword, sortBy, tagName)));
    }

    @GetMapping("/stalls/{id}")
    public ResponseEntity<ApiResponse> stallDetail(@PathVariable Long id) {
        Map<String, Object> item = coreService.getStallDetail(id);
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @GetMapping("/stalls/{id}/reviews")
    public ResponseEntity<ApiResponse> stallReviews(
        @PathVariable Long id,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(name = "page_size", defaultValue = "10") int pageSize,
        @RequestParam(name = "sort_by", defaultValue = "latest") String sortBy
    ) {
        return ResponseEntity.ok(ApiResponse.success(coreService.getStallReviews(id, page, pageSize, sortBy)));
    }

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse> submitReview(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        int rating = num(body.get("rating")).intValue();
        if (rating < 1 || rating > 5) return badRequest("评分必须是 1 到 5");
        Map<String, Object> item = coreService.createOrUpdateReview(uid, num(body.get("stall_id")), rating, nullableString(body.get("content")));
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @PostMapping("/reviews/{id}/likes")
    public ResponseEntity<ApiResponse> likeReview(HttpServletRequest request, @PathVariable Long id) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        Map<String, Object> item = coreService.likeReview(uid, id);
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @PostMapping("/reviews/{id}/reports")
    public ResponseEntity<ApiResponse> reportReview(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        Map<String, Object> item = coreService.reportReview(uid, id, nullableString(body.get("reason")));
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @GetMapping("/rankings/score")
    public ResponseEntity<ApiResponse> rankScore(@RequestParam(defaultValue = "10") int limit, @RequestParam(name = "canteen_id", required = false) Long canteenId) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", coreService.rankingScore(limit, canteenId))));
    }

    @GetMapping("/rankings/hot")
    public ResponseEntity<ApiResponse> rankHot(@RequestParam(defaultValue = "10") int limit, @RequestParam(name = "canteen_id", required = false) Long canteenId) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", coreService.rankingHot(limit, canteenId))));
    }

    @GetMapping("/rankings/latest")
    public ResponseEntity<ApiResponse> rankLatest(@RequestParam(defaultValue = "10") int limit, @RequestParam(name = "canteen_id", required = false) Long canteenId) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", coreService.rankingLatest(limit, canteenId))));
    }

    @GetMapping("/users/me/reviews")
    public ResponseEntity<ApiResponse> myReviews(HttpServletRequest request,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(coreService.getMyReviews(uid, page, pageSize)));
    }

    @PutMapping("/users/me/reviews/{id}")
    public ResponseEntity<ApiResponse> updateMyReview(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        Map<String, Object> item = coreService.updateMyReview(uid, id, num(body.get("rating")).intValue(), nullableString(body.get("content")));
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @DeleteMapping("/users/me/reviews/{id}")
    public ResponseEntity<ApiResponse> deleteMyReview(HttpServletRequest request, @PathVariable Long id) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        Map<String, Object> mine = coreService.getMyReviews(uid, 1, 1000);
        List<?> list = (List<?>) mine.getOrDefault("list", List.of());
        boolean exists = list.stream().anyMatch(row -> Objects.equals(num(((Map<?, ?>) row).get("id")), id));
        if (!exists) return notFound();
        return ResponseEntity.ok(ApiResponse.success(coreService.softDeleteReview(id)));
    }

    @PostMapping("/users/me/favorites")
    public ResponseEntity<ApiResponse> addFavorite(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(coreService.addFavorite(uid, num(body.get("stall_id")))));
    }

    @DeleteMapping("/users/me/favorites/{stallId}")
    public ResponseEntity<ApiResponse> removeFavorite(HttpServletRequest request, @PathVariable Long stallId) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(coreService.removeFavorite(uid, stallId)));
    }

    @GetMapping("/users/me/favorites")
    public ResponseEntity<ApiResponse> favorites(HttpServletRequest request,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(coreService.favorites(uid, page, pageSize)));
    }

    @PostMapping("/users/me/blacklist")
    public ResponseEntity<ApiResponse> addBlacklist(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(coreService.addBlacklist(uid, num(body.get("stall_id")))));
    }

    @DeleteMapping("/users/me/blacklist/{stallId}")
    public ResponseEntity<ApiResponse> removeBlacklist(HttpServletRequest request, @PathVariable Long stallId) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(coreService.removeBlacklist(uid, stallId)));
    }

    @GetMapping("/users/me/blacklist")
    public ResponseEntity<ApiResponse> blacklists(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(coreService.blacklists(uid, page, pageSize)));
    }

    @GetMapping("/users/me/history")
    public ResponseEntity<ApiResponse> histories(HttpServletRequest request,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(coreService.histories(uid, page, pageSize)));
    }

    @PostMapping("/users/me/history")
    public ResponseEntity<ApiResponse> addHistory(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(ApiResponse.success(coreService.addHistory(uid, num(body.get("stall_id")))));
    }

    @GetMapping("/recommendations/today")
    public ResponseEntity<ApiResponse> recommendToday(HttpServletRequest request,
                                                      @RequestParam(name = "canteen_id", required = false) Long canteenId,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(name = "exclude_blacklist", defaultValue = "false") boolean excludeBlacklist,
                                                      @RequestParam(defaultValue = "3") int limit,
                                                      @RequestParam(defaultValue = "0") int seed) {
        Long uid = authUserId(request);
        List<Map<String, Object>> list = coreService.recommendToday(uid, canteenId, category, excludeBlacklist, limit, seed);
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", list)));
    }

    @PostMapping("/recommendations/personalized")
    public ResponseEntity<ApiResponse> recommendPersonalized(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        List<Map<String, Object>> list = coreService.recommendPersonalized(
            uid, valueOf(body.get("preference_text")), bool(body.get("exclude_blacklist")), intValue(body.get("limit"), 5)
        );
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", list)));
    }

    @PostMapping("/recommendations/feed")
    public ResponseEntity<ApiResponse> recommendFeed(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        String preferenceText = valueOf(body.get("preference_text"));
        String category = nullableString(body.get("category"));
        Long canteenId = body.get("canteen_id") == null ? null : num(body.get("canteen_id"));
        int limit = intValue(body.get("limit"), 5);
        int seed = intValue(body.get("seed"), 0);

        List<Map<String, Object>> items = coreService.recommendFeed(uid, preferenceText, canteenId, category, true, limit, seed);
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("logged_in", uid != null);
        context.put("category", category == null ? "" : category);
        context.put("canteen_id", canteenId == null ? "" : canteenId);
        Map<String, Object> ai = llmService.recommendWithDeepseek(preferenceText, category, items, context);

        List<Integer> pickedIds = ((List<?>) ai.getOrDefault("picked_ids", List.of()))
            .stream().map(v -> Integer.parseInt(String.valueOf(v))).toList();
        if (Boolean.TRUE.equals(ai.get("enabled")) && !pickedIds.isEmpty()) {
            Set<Integer> pickedSet = new HashSet<>(pickedIds);
            Map<Integer, Integer> order = new HashMap<>();
            for (int i = 0; i < pickedIds.size(); i++) order.put(pickedIds.get(i), i);
            items = items.stream()
                .filter(item -> pickedSet.contains(intValue(item.get("stall_id"), 0)))
                .sorted(Comparator.comparingInt(item -> order.getOrDefault(intValue(item.get("stall_id"), 0), 999)))
                .toList();
        } else {
            items = items.stream()
                .sorted((a, b) -> {
                    int c = Integer.compare(intValue(b.get("match_score"), 0), intValue(a.get("match_score"), 0));
                    if (c != 0) return c;
                    c = Integer.compare(intValue(b.get("review_count"), 0), intValue(a.get("review_count"), 0));
                    if (c != 0) return c;
                    return Long.compare(num(a.get("stall_id")), num(b.get("stall_id")));
                }).toList();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", items);
        data.put("ai_summary", valueOf(ai.get("summary")));
        data.put("ai_tips", valueOf(ai.get("tips")));
        data.put("ai_enabled", bool(ai.get("enabled")));
        data.put("ai_model", valueOf(ai.get("model")));
        data.put("ai_source", valueOf(ai.get("source")));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/recommendations/profile")
    public ResponseEntity<ApiResponse> recommendationProfile(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(coreService.recommendationProfile(authUserId(request))));
    }

    @PostMapping("/recommendations/refine")
    public ResponseEntity<ApiResponse> refineRecommendation(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long uid = authUserId(request);
        String preferenceText = (valueOf(body.get("preference_text")) + " " + valueOf(body.get("feedback"))).trim();
        String category = nullableString(body.get("category"));
        Long canteenId = body.get("canteen_id") == null ? null : num(body.get("canteen_id"));
        int limit = intValue(body.get("limit"), 5);
        int seed = intValue(body.get("seed"), 0);
        List<Map<String, Object>> items = coreService.recommendFeed(uid, preferenceText, canteenId, category, true, limit, seed);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "list", items,
            "summary", items.isEmpty() ? "没有找到符合新条件的窗口，可以放宽筛选。" : "已根据你的补充要求重新推荐。"
        )));
    }

    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<ApiResponse> adminDeleteReview(HttpServletRequest request, @PathVariable Long id) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        Map<String, Object> item = coreService.softDeleteReview(id);
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<ApiResponse> adminDashboard(HttpServletRequest request) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        return ResponseEntity.ok(ApiResponse.success(coreService.adminDashboard()));
    }

    @GetMapping("/admin/reviews")
    public ResponseEntity<ApiResponse> adminReviews(HttpServletRequest request,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(name = "page_size", defaultValue = "20") int pageSize,
                                                    @RequestParam(name = "stall_id", required = false) Long stallId,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String status) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        return ResponseEntity.ok(ApiResponse.success(coreService.adminReviews(page, pageSize, stallId, keyword, status)));
    }

    @PostMapping("/admin/stalls")
    public ResponseEntity<ApiResponse> adminCreateStall(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        return ResponseEntity.ok(ApiResponse.success(coreService.createStall(body)));
    }

    @PutMapping("/admin/stalls/{id}")
    public ResponseEntity<ApiResponse> adminUpdateStall(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        Map<String, Object> item = coreService.updateStall(id, body);
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @DeleteMapping("/admin/stalls/{id}")
    public ResponseEntity<ApiResponse> adminDeleteStall(HttpServletRequest request, @PathVariable Long id) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        Map<String, Object> item = coreService.disableStall(id);
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(Map.of("result", "success", "stall", item)));
    }

    @PostMapping("/admin/canteens")
    public ResponseEntity<ApiResponse> adminCreateCanteen(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        return ResponseEntity.ok(ApiResponse.success(coreService.createCanteen(body)));
    }

    @PutMapping("/admin/canteens/{id}")
    public ResponseEntity<ApiResponse> adminUpdateCanteen(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        Map<String, Object> item = coreService.updateCanteen(id, body);
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse> adminUsers(HttpServletRequest request) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", coreService.listUsers())));
    }

    @PutMapping("/admin/users/{id}/role")
    public ResponseEntity<ApiResponse> adminUpdateRole(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        Long uid = authUserId(request);
        if (Objects.equals(uid, id)) return badRequest("不能修改自己的权限");
        int role = intValue(body.get("role"), 0);
        if (role != 0 && role != 1) return badRequest("角色只能是 0 或 1");
        Map<String, Object> item = coreService.updateUserRole(id, role);
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @GetMapping("/admin/tags")
    public ResponseEntity<ApiResponse> adminTags(HttpServletRequest request) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        return ResponseEntity.ok(ApiResponse.success(Map.of("list", coreService.listTags())));
    }

    @PostMapping("/admin/tags")
    public ResponseEntity<ApiResponse> adminCreateTag(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        return ResponseEntity.ok(ApiResponse.success(coreService.createTag(body)));
    }

    @PutMapping("/admin/tags/{id}")
    public ResponseEntity<ApiResponse> adminUpdateTag(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        ResponseEntity<ApiResponse> guard = requireAdmin(request);
        if (guard != null) return guard;
        Map<String, Object> item = coreService.updateTag(id, body);
        if (item == null) return notFound();
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    private ResponseEntity<ApiResponse> requireAdmin(HttpServletRequest request) {
        Long uid = authUserId(request);
        if (uid == null) return unauthorized();
        Integer role = authRole(request);
        if (role == null || role < 1) return forbidden();
        return null;
    }

    private Long authUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("auth_user_id");
        return uid == null ? null : ((Number) uid).longValue();
    }

    private Integer authRole(HttpServletRequest request) {
        Object role = request.getAttribute("auth_role");
        return role == null ? null : ((Number) role).intValue();
    }

    private ResponseEntity<ApiResponse> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(4010, message, null));
    }

    private ResponseEntity<ApiResponse> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(4001, "not_login", null));
    }

    private ResponseEntity<ApiResponse> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(4003, "forbidden", null));
    }

    private ResponseEntity<ApiResponse> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(4004, "not_found", null));
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String nullableString(Object value) {
        String s = valueOf(value);
        return s.isEmpty() ? null : s;
    }

    private boolean bool(Object value) {
        if (value instanceof Boolean b) return b;
        return value != null && "true".equalsIgnoreCase(String.valueOf(value));
    }

    private Long num(Object value) {
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private int intValue(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
