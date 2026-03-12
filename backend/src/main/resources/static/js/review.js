(function () {
    const windowId = document.querySelector('meta[name="window-id"]')?.getAttribute("content");
    const titleEl = document.getElementById("reviewWindowTitle");
    const form = document.getElementById("reviewForm");
    const msgEl = document.getElementById("reviewMessage");
    const backLink = document.getElementById("backToDetail");

    backLink.href = `/windows/${windowId}`;

    function setMessage(msg, ok = false) {
        msgEl.style.color = ok ? "#166534" : "#b91c1c";
        msgEl.textContent = msg;
    }

    async function fetchJson(url, options) {
        const res = await fetch(url, { credentials: "include", ...options });
        if (res.status === 401) {
            window.location.href = "/login";
            return null;
        }
        return await res.json();
    }

    async function loadWindow() {
        const result = await fetchJson(`/api/windows/${windowId}`);
        if (!result) return;
        if (result.code === 0) {
            titleEl.textContent = `评价窗口：${result.data.windowName}`;
        }
    }

    form?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const formData = new FormData(form);
        const payload = {
            score: Number(formData.get("score")),
            commentText: String(formData.get("commentText") || "").trim()
        };

        const result = await fetchJson(`/api/windows/${windowId}/reviews`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!result) return;

        if (result.code === 0) {
            setMessage("提交成功，正在返回详情页...", true);
            setTimeout(() => {
                window.location.href = `/windows/${windowId}`;
            }, 800);
            return;
        }

        setMessage(result.message || "提交失败");
    });

    loadWindow();
})();
