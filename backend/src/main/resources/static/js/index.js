(function () {
    const windowListEl = document.getElementById("windowList");
    const topRatedEl = document.getElementById("topRatedList");
    const hotEl = document.getElementById("hotList");
    const latestEl = document.getElementById("latestReviewList");
    const pageInfoEl = document.getElementById("pageInfo");
    const prevPageBtn = document.getElementById("prevPageBtn");
    const nextPageBtn = document.getElementById("nextPageBtn");

    const loginLink = document.getElementById("loginLink");
    const registerLink = document.getElementById("registerLink");
    const logoutBtn = document.getElementById("logoutBtn");
    const currentUserEl = document.getElementById("currentUser");

    const state = {
        page: 0,
        size: 8,
        totalPages: 1,
        params: {
            keyword: "",
            canteenName: "",
            cuisineType: "",
            sort: "score"
        }
    };

    async function fetchJson(url, options) {
        const res = await fetch(url, {
            credentials: "include",
            ...options
        });
        return await res.json();
    }

    function stars(score) {
        const full = Math.round(score || 0);
        return "★".repeat(Math.min(full, 5)) + "☆".repeat(Math.max(0, 5 - full));
    }

    function renderWindows(data) {
        if (!data.content || data.content.length === 0) {
            windowListEl.innerHTML = '<p class="meta">暂无符合条件的窗口。</p>';
            return;
        }

        windowListEl.innerHTML = data.content.map(item => `
            <article class="window-item">
                <h3><a href="/windows/${item.id}">${item.windowName}</a></h3>
                <p class="meta">${item.canteenName} · ${item.cuisineType}</p>
                <p>${item.intro || "暂无简介"}</p>
                <p class="meta">评分：${item.avgScore} (${stars(Number(item.avgScore))}) | 评论数：${item.reviewCount}</p>
            </article>
        `).join("");
    }

    function renderWindowRanking(el, items) {
        el.innerHTML = (items || []).map((item, idx) => `
            <li class="rank-item">
                <h4>${idx + 1}. <a href="/windows/${item.windowId || item.id}">${item.windowName}</a></h4>
                <p class="meta">${item.canteenName} · ${item.cuisineType || ""}</p>
                <p class="meta">评分：${item.avgScore} | 评论数：${item.reviewCount}</p>
            </li>
        `).join("");
    }

    function renderLatest(el, items) {
        el.innerHTML = (items || []).map(item => `
            <li class="rank-item">
                <h4><a href="/windows/${item.windowId}">${item.windowName}</a></h4>
                <p class="meta">${item.userNickname} · ${item.score} 分</p>
                <p>${item.commentText}</p>
            </li>
        `).join("");
    }

    async function loadWindows() {
        const search = new URLSearchParams({
            page: String(state.page),
            size: String(state.size),
            sort: state.params.sort,
            keyword: state.params.keyword,
            canteenName: state.params.canteenName,
            cuisineType: state.params.cuisineType
        });

        const result = await fetchJson(`/api/windows?${search.toString()}`);
        if (result.code !== 0) {
            windowListEl.innerHTML = `<p class="message">${result.message || "加载失败"}</p>`;
            return;
        }

        renderWindows(result.data);
        state.totalPages = Math.max(result.data.totalPages || 1, 1);
        pageInfoEl.textContent = `第 ${state.page + 1} / ${state.totalPages} 页`;
        prevPageBtn.disabled = state.page <= 0;
        nextPageBtn.disabled = state.page >= state.totalPages - 1;
    }

    async function loadRankings() {
        const [top, hot, latest] = await Promise.all([
            fetchJson("/api/rankings/top-rated?limit=5"),
            fetchJson("/api/rankings/hot?limit=5"),
            fetchJson("/api/rankings/latest-reviews?limit=5")
        ]);

        renderWindowRanking(topRatedEl, top.data || []);
        renderWindowRanking(hotEl, hot.data || []);
        renderLatest(latestEl, latest.data || []);
    }

    async function initAuthStatus() {
        const result = await fetchJson("/api/auth/me");
        if (result.code === 0 && result.data) {
            currentUserEl.textContent = `当前用户：${result.data.nickname}(${result.data.studentNo})`;
            loginLink.style.display = "none";
            registerLink.style.display = "none";
            logoutBtn.style.display = "inline-block";
            return;
        }
        currentUserEl.textContent = "未登录";
    }

    document.getElementById("searchForm")?.addEventListener("submit", (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        state.params.keyword = String(formData.get("keyword") || "").trim();
        state.params.canteenName = String(formData.get("canteenName") || "").trim();
        state.params.cuisineType = String(formData.get("cuisineType") || "").trim();
        state.params.sort = String(formData.get("sort") || "score");
        state.page = 0;
        loadWindows();
    });

    prevPageBtn?.addEventListener("click", () => {
        if (state.page > 0) {
            state.page -= 1;
            loadWindows();
        }
    });

    nextPageBtn?.addEventListener("click", () => {
        if (state.page < state.totalPages - 1) {
            state.page += 1;
            loadWindows();
        }
    });

    logoutBtn?.addEventListener("click", async () => {
        await fetchJson("/api/auth/logout", { method: "POST" });
        window.location.reload();
    });

    initAuthStatus();
    loadWindows();
    loadRankings();
})();
