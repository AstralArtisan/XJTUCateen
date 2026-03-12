(function () {
    const page = document.body.dataset.page;
    const messageEl = document.getElementById("authMessage");

    function setMessage(msg, isError = true) {
        if (!messageEl) return;
        messageEl.style.color = isError ? "#b91c1c" : "#166534";
        messageEl.textContent = msg;
    }

    async function postJson(url, payload) {
        const res = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        });
        return await res.json();
    }

    if (page === "login") {
        const form = document.getElementById("loginForm");
        form?.addEventListener("submit", async (e) => {
            e.preventDefault();
            const formData = new FormData(form);
            const payload = {
                studentNo: String(formData.get("studentNo") || "").trim(),
                password: String(formData.get("password") || "")
            };

            const result = await postJson("/api/auth/login", payload);
            if (result.code === 0) {
                window.location.href = "/";
                return;
            }
            setMessage(result.message || "登录失败");
        });
    }

    if (page === "register") {
        const form = document.getElementById("registerForm");
        form?.addEventListener("submit", async (e) => {
            e.preventDefault();
            const formData = new FormData(form);
            const payload = {
                studentNo: String(formData.get("studentNo") || "").trim(),
                nickname: String(formData.get("nickname") || "").trim(),
                password: String(formData.get("password") || "")
            };

            const result = await postJson("/api/auth/register", payload);
            if (result.code === 0) {
                setMessage("注册成功，正在跳转登录页...", false);
                setTimeout(() => window.location.href = "/login", 900);
                return;
            }
            setMessage(result.message || "注册失败");
        });
    }
})();
