from __future__ import annotations

import json
import os
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
DEFAULT_API_KEY = "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"
DEFAULT_MODEL = os.getenv("OPENROUTER_MODEL", "qwen/qwen3.6-plus:free")
SITE_URL = os.getenv("OPENROUTER_SITE_URL", "http://127.0.0.1:5173")
SITE_TITLE = os.getenv("OPENROUTER_SITE_TITLE", "xjtu-canteen-review")


def _ascii_header(value: str, fallback: str):
    try:
        value.encode("latin-1")
        return value
    except UnicodeEncodeError:
        return fallback


def _extract_json(text: str):
    text = (text or "").strip()
    if not text:
        return None
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        pass
    start = text.find("{")
    end = text.rfind("}")
    if start != -1 and end != -1 and end > start:
        try:
            return json.loads(text[start : end + 1])
        except json.JSONDecodeError:
            return None
    return None


def _fallback(candidates, message):
    return {
        "enabled": False,
        "source": "local",
        "model": DEFAULT_MODEL,
        "summary": message,
        "tips": "下面是根据评分和热度给你挑的，仅供参考。",
        "picked_ids": [item["stall_id"] for item in candidates[:3]],
    }


def recommend_with_openrouter(preference_text: str, category: str | None, candidates: list[dict], user_context: dict | None = None):
    api_key = os.getenv("OPENROUTER_API_KEY", DEFAULT_API_KEY).strip()
    if not candidates:
        return _fallback([], "这个条件下没找到合适的窗口，换个筛选试试？")
    if not api_key:
        return _fallback(candidates, "AI 助手暂时不在线，先给你看看口碑不错的几家。")

    compact_candidates = [
        {
            "stall_id": item["stall_id"],
            "stall_name": item["stall_name"],
            "canteen_name": item["canteen_name"],
            "category": item["category"],
            "avg_rating": item["avg_rating"],
            "review_count": item["review_count"],
            "tags": item["tags"],
            "reason": item["reason"],
        }
        for item in candidates[:6]
    ]
    context_text = json.dumps(
        {
            "category": category or "",
            "preference_text": preference_text or "",
            "candidate_windows": compact_candidates,
            "user_context": user_context or {},
        },
        ensure_ascii=False,
    )

    system_prompt = (
        "你是西安交通大学食堂推荐助手。"
        "你必须只依据给定候选窗口做推荐，不要虚构新窗口。"
        "请输出 JSON，格式为："
        '{"summary":"一句到三句中文总结","tips":"一句中文建议","picked_ids":[1,2,3]}。'
        "picked_ids 只保留候选窗口中的 stall_id，最多 3 个。"
    )

    body = {
        "model": DEFAULT_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": context_text},
        ],
        "temperature": 0.6,
    }
    payload = json.dumps(body, ensure_ascii=False).encode("utf-8")
    request = Request(
        OPENROUTER_URL,
        data=payload,
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
            "HTTP-Referer": SITE_URL,
            "X-Title": _ascii_header(SITE_TITLE, "xjtu-canteen-review"),
        },
        method="POST",
    )

    try:
        with urlopen(request, timeout=30) as response:
            data = json.loads(response.read().decode("utf-8"))
    except HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="ignore")
        return _fallback(candidates, f"网络开小差了，先看看这几家吧。")
    except URLError as exc:
        return _fallback(candidates, f"连不上推荐服务，先给你展示本地结果。")
    except Exception as exc:
        return _fallback(candidates, "推荐服务出了点问题，先看看这几家口碑不错的。")

    content = (
        data.get("choices", [{}])[0]
        .get("message", {})
        .get("content", "")
    )
    parsed = _extract_json(content)
    if not parsed:
        return {
            "enabled": True,
            "source": "openrouter",
            "model": DEFAULT_MODEL,
            "summary": content or "给你找到了几家，具体看下面的推荐。",
            "tips": "可以换个描述再试试，说得越具体推荐越准。",
            "picked_ids": [item["stall_id"] for item in candidates[:3]],
        }

    picked_ids = [int(item) for item in parsed.get("picked_ids", []) if str(item).isdigit()]
    if not picked_ids:
        picked_ids = [item["stall_id"] for item in candidates[:3]]

    return {
        "enabled": True,
        "source": "openrouter",
        "model": DEFAULT_MODEL,
        "summary": parsed.get("summary") or "给你挑了几家，看看合不合口味。",
        "tips": parsed.get("tips") or "口味变了随时可以重新说，我再给你找找。",
        "picked_ids": picked_ids[:3],
    }
