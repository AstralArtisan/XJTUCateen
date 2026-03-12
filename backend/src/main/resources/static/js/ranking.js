(function () {
    const topEl = document.getElementById("rankingTopRated");
    const hotEl = document.getElementById("rankingHot");
    const latestEl = document.getElementById("rankingLatest");

    async function fetchJson(url) {
        const res = await fetch(url, { credentials: "include" });
        return await res.json();
    }

    function renderWindowList(el, items) {
        el.innerHTML = (items || []).map((item, idx) => `
            <li class="rank-item">
                <h4>${idx + 1}. <a href="/windows/${item.windowId}">${item.windowName}</a></h4>
                <p class="meta">${item.canteenName} · ${item.cuisineType}</p>
                <p class="meta">评分：${item.avgScore} | 评论数：${item.reviewCount}</p>
            </li>
        `).join("");
    }

    function renderLatest(el, items) {
        el.innerHTML = (items || []).map((item, idx) => `
            <li class="rank-item">
                <h4>${idx + 1}. <a href="/windows/${item.windowId}">${item.windowName}</a> - ${item.score} 分</h4>
                <p>${item.commentText}</p>
                <p class="meta">${item.userNickname} · ${new Date(item.createdAt).toLocaleString()}</p>
            </li>
        `).join("");
    }

    async function init() {
        const [top, hot, latest] = await Promise.all([
            fetchJson("/api/rankings/top-rated?limit=20"),
            fetchJson("/api/rankings/hot?limit=20"),
            fetchJson("/api/rankings/latest-reviews?limit=20")
        ]);

        renderWindowList(topEl, top.data || []);
        renderWindowList(hotEl, hot.data || []);
        renderLatest(latestEl, latest.data || []);
    }

    init();
})();
