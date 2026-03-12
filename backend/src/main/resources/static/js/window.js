(function () {
    const windowId = document.querySelector('meta[name="window-id"]')?.getAttribute("content");
    const nameEl = document.getElementById("windowName");
    const metaEl = document.getElementById("windowMeta");
    const introEl = document.getElementById("windowIntro");
    const reviewListEl = document.getElementById("reviewList");
    const pageInfoEl = document.getElementById("reviewPageInfo");

    const prevBtn = document.getElementById("reviewPrevBtn");
    const nextBtn = document.getElementById("reviewNextBtn");
    const writeBtn = document.getElementById("writeReviewBtn");

    const state = { page: 0, size: 6, totalPages: 1 };

    async function fetchJson(url, options) {
        const res = await fetch(url, { credentials: "include", ...options });
        return await res.json();
    }

    function stars(score) {
        const full = Math.max(Math.min(score, 5), 1);
        return "★".repeat(full) + "☆".repeat(5 - full);
    }

    async function loadDetail() {
        const result = await fetchJson(`/api/windows/${windowId}`);
        if (result.code !== 0) {
            nameEl.textContent = result.message || "加载失败";
            return;
        }

        const d = result.data;
        nameEl.textContent = d.windowName;
        metaEl.textContent = `${d.canteenName} · ${d.cuisineType} · 平均分 ${d.avgScore} · 评论 ${d.reviewCount}`;
        introEl.textContent = d.intro || "暂无简介";
    }

    function renderReviews(pageData) {
        if (!pageData.content || pageData.content.length === 0) {
            reviewListEl.innerHTML = '<li class="review-item">暂无评论，快来抢沙发！</li>';
            return;
        }

        reviewListEl.innerHTML = pageData.content.map(item => `
            <li class="review-item">
                <p><strong>${item.userNickname}</strong> · ${stars(item.score)} · ${item.score} 分</p>
                <p>${item.commentText}</p>
                <p class="meta">${new Date(item.createdAt).toLocaleString()}</p>
            </li>
        `).join("");
    }

    async function loadReviews() {
        const result = await fetchJson(`/api/windows/${windowId}/reviews?page=${state.page}&size=${state.size}`);
        if (result.code !== 0) {
            reviewListEl.innerHTML = `<li class="review-item">${result.message || "加载失败"}</li>`;
            return;
        }

        renderReviews(result.data);
        state.totalPages = Math.max(result.data.totalPages || 1, 1);
        pageInfoEl.textContent = `第 ${state.page + 1} / ${state.totalPages} 页`;
        prevBtn.disabled = state.page <= 0;
        nextBtn.disabled = state.page >= state.totalPages - 1;
    }

    prevBtn?.addEventListener("click", () => {
        if (state.page > 0) {
            state.page -= 1;
            loadReviews();
        }
    });

    nextBtn?.addEventListener("click", () => {
        if (state.page < state.totalPages - 1) {
            state.page += 1;
            loadReviews();
        }
    });

    writeBtn?.addEventListener("click", () => {
        window.location.href = `/windows/${windowId}/review`;
    });

    loadDetail();
    loadReviews();
})();
