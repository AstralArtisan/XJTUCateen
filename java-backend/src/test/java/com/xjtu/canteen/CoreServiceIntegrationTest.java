package com.xjtu.canteen;

import com.xjtu.canteen.service.CoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CoreServiceIntegrationTest extends CanteenTestBase {
    @Autowired
    private CoreService coreService;

    @Test
    void registerLoginAndDuplicateRegisterFollowAuthRules() {
        Map<String, Object> user = coreService.register("flow001", "流程用户", "mypass");
        Map<String, Object> login = coreService.login("flow001", "mypass");

        assertThat(user).isNotNull();
        assertThat(login).containsKey("token");
        assertThat(((Map<?, ?>) login.get("user")).get("student_id")).isEqualTo("flow001");
        assertThat(coreService.register("flow001", "重复用户", "mypass")).isNull();
        assertThat(coreService.login("flow001", "wrong")).isNull();
        assertThat(coreService.login("nobody999", "pass")).isNull();
    }

    @Test
    void queryStallsFiltersSortsAndExcludesDisabledStalls() {
        Long c1 = insertCanteen("测试食堂1");
        Long c2 = insertCanteen("测试食堂2");
        Long low = insertStall(c1, "低分拉面窗口", "面条");
        Long high = insertStall(c1, "高分快餐窗口", "快餐");
        insertStall(c2, "其他食堂窗口", "快餐");
        Long disabled = insertStall(c1, "停用窗口", "快餐");
        jdbc.update("UPDATE stall SET status = 0 WHERE id = ?", disabled);
        Long u1 = insertUser("u001", "用户1", 0);
        Long u2 = insertUser("u002", "用户2", 0);

        coreService.createOrUpdateReview(u1, low, 2, "一般");
        coreService.createOrUpdateReview(u2, high, 5, "很好");

        Map<String, Object> all = coreService.queryStalls(1, 20, null, null, null, null, null);
        Map<String, Object> byCanteen = coreService.queryStalls(1, 20, c1, null, null, null, null);
        Map<String, Object> byKeyword = coreService.queryStalls(1, 20, null, null, "拉面", null, null);
        Map<String, Object> byCategory = coreService.queryStalls(1, 20, null, "面条", null, null, null);
        Map<String, Object> byScore = coreService.queryStalls(1, 20, null, null, null, "score", null);

        assertThat((Integer) all.get("total")).isEqualTo(3);
        assertThat((Integer) byCanteen.get("total")).isEqualTo(2);
        assertThat(firstName(byKeyword)).contains("拉面");
        assertThat(first(byCategory).get("category")).isEqualTo("面条");
        assertThat(firstName(byScore)).isEqualTo("高分快餐窗口");
    }

    @Test
    void reviewsAreUpsertedSoftDeletedAndRecalculateStallStats() {
        Long cid = insertCanteen("评论食堂");
        Long sid = insertStall(cid, "评论窗口", "快餐");
        Long u1 = insertUser("r001", "用户1", 0);
        Long u2 = insertUser("r002", "用户2", 0);

        Map<String, Object> first = coreService.createOrUpdateReview(u1, sid, 3, "一般");
        coreService.createOrUpdateReview(u1, sid, 5, "改变想法");
        coreService.createOrUpdateReview(u2, sid, 1, "不太行");

        Map<String, Object> reviews = coreService.getStallReviews(sid, 1, 1, "latest");
        Map<String, Object> detail = coreService.getStallDetail(sid);

        assertThat((Integer) reviews.get("total")).isEqualTo(2);
        assertThat((List<?>) reviews.get("list")).hasSize(1);
        assertThat(((Number) detail.get("avg_rating")).doubleValue()).isEqualTo(3.0);
        assertThat(detail.get("review_count")).isEqualTo(2);

        coreService.softDeleteReview(((Number) first.get("id")).longValue());
        detail = coreService.getStallDetail(sid);
        assertThat(detail.get("review_count")).isEqualTo(1);
        assertThat(((Number) detail.get("avg_rating")).doubleValue()).isEqualTo(1.0);
        assertThat(coreService.createOrUpdateReview(u1, 99999L, 5, "不存在")).isNull();
    }

    @Test
    void rankingsReflectReviewsAndIgnoreDisabledStalls() {
        Long cid = insertCanteen("排行食堂");
        Long high = insertStall(cid, "高分窗口", "快餐");
        Long hot = insertStall(cid, "热门窗口", "面条");
        Long latest = insertStall(cid, "新评窗口", "小吃");
        Long u1 = insertUser("rank001", "用户1", 0);
        Long u2 = insertUser("rank002", "用户2", 0);
        Long u3 = insertUser("rank003", "用户3", 0);

        coreService.createOrUpdateReview(u1, high, 5, "高分");
        coreService.createOrUpdateReview(u2, hot, 3, "热门1");
        coreService.createOrUpdateReview(u3, hot, 4, "热门2");
        coreService.createOrUpdateReview(u1, latest, 4, "最新");

        assertThat(coreService.rankingScore(10, null).get(0).get("stall_id")).isEqualTo(high);
        assertThat(coreService.rankingHot(10, null).get(0).get("stall_id")).isEqualTo(hot);
        assertThat(coreService.rankingScore(2, null)).hasSizeLessThanOrEqualTo(2);

        jdbc.update("UPDATE stall SET status = 0 WHERE id = ?", high);
        assertThat(coreService.rankingScore(10, null))
            .extracting(row -> row.get("stall_id"))
            .doesNotContain(high);
    }

    @Test
    void userBehaviorListsFavoriteBlacklistAndDeduplicatedHistory() {
        Long cid = insertCanteen("行为食堂");
        Long a = insertStall(cid, "收藏窗口", "快餐");
        Long b = insertStall(cid, "黑名单窗口", "面条");
        Long user = insertUser("beh001", "行为用户", 0);

        coreService.addFavorite(user, a);
        assertThat((Integer) coreService.favorites(user, 1, 10).get("total")).isEqualTo(1);
        coreService.removeFavorite(user, a);
        assertThat((Integer) coreService.favorites(user, 1, 10).get("total")).isEqualTo(0);

        coreService.addBlacklist(user, b);
        assertThat((Integer) coreService.blacklists(user, 1, 10).get("total")).isEqualTo(1);

        coreService.addHistory(user, a);
        coreService.addHistory(user, a);
        assertThat((Integer) coreService.histories(user, 1, 10).get("total")).isEqualTo(1);
    }

    @Test
    void adminStyleOperationsCreateAndHideData() {
        Long cid = insertCanteen("管理食堂");
        Map<String, Object> created = coreService.createStall(Map.of(
            "canteen_id", cid,
            "name", "新增测试窗口",
            "category", "小吃",
            "description", "管理员新增",
            "tags", List.of("新标签")
        ));

        assertThat(created.get("name")).isEqualTo("新增测试窗口");
        assertThat(coreService.getStallDetail(((Number) created.get("id")).longValue()).get("tags")).asList().contains("新标签");

        coreService.disableStall(((Number) created.get("id")).longValue());
        assertThat((Integer) coreService.queryStalls(1, 20, null, null, "新增测试窗口", null, null).get("total")).isZero();
    }

    @Test
    void reviewLikesReportsAndAdminDashboardExposeGovernanceData() {
        Long cid = insertCanteen("治理食堂");
        Long sid = insertStall(cid, "治理窗口", "快餐");
        Long author = insertUser("gov001", "作者", 0);
        Long reader = insertUser("gov002", "读者", 0);
        Long otherReader = insertUser("gov003", "另一个读者", 0);
        Map<String, Object> review = coreService.createOrUpdateReview(author, sid, 4, "味道不错");
        Long reviewId = ((Number) review.get("id")).longValue();

        assertThat(coreService.likeReview(reader, reviewId).get("like_count")).isEqualTo(1);
        assertThat(coreService.reportReview(reader, reviewId, "疑似广告").get("report_count")).isEqualTo(1);
        assertThat(coreService.reportReview(reader, reviewId, "重复点击").get("report_count")).isEqualTo(1);
        assertThat(coreService.reportReview(otherReader, reviewId, "内容不合适").get("report_count")).isEqualTo(2);

        Map<String, Object> page = coreService.adminReviews(1, 10, sid, "治理", null);
        assertThat((Integer) page.get("total")).isEqualTo(1);
        assertThat(first(page).get("like_count")).isEqualTo(1L);
        assertThat(first(page).get("report_count")).isEqualTo(2L);

        Map<String, Object> dashboard = coreService.adminDashboard();
        assertThat(dashboard.get("pending_report_count")).isEqualTo(2);
        assertThat((List<?>) dashboard.get("top_stalls")).isNotEmpty();

        coreService.softDeleteReview(reviewId);
        dashboard = coreService.adminDashboard();
        assertThat(dashboard.get("pending_report_count")).isEqualTo(0);
    }

    @Test
    void recommendationProfileSummarizesUserBehavior() {
        Long cid = insertCanteen("画像食堂");
        Long sid = insertStall(cid, "画像窗口", "面条");
        Long user = insertUser("profile001", "画像用户", 0);
        jdbc.update("UPDATE user SET preference_text = ? WHERE id = ?", "少油，喜欢面", user);
        coreService.addFavorite(user, sid);
        coreService.addHistory(user, sid);
        coreService.createOrUpdateReview(user, sid, 5, "会再来");

        Map<String, Object> profile = coreService.recommendationProfile(user);

        assertThat(profile.get("logged_in")).isEqualTo(true);
        assertThat(String.valueOf(profile.get("summary"))).contains("少油");
        assertThat((List<?>) profile.get("favorite_categories")).isNotEmpty();
        assertThat(profile.get("review_count")).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> first(Map<String, Object> page) {
        return ((List<Map<String, Object>>) page.get("list")).get(0);
    }

    private String firstName(Map<String, Object> page) {
        return String.valueOf(first(page).get("name"));
    }
}
