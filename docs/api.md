# API Document (MVP)

## Response Format

All APIs return:

```json
{
  "code": 0,
  "message": "OK",
  "data": {},
  "timestamp": "2026-03-13T12:00:00"
}
```

`code = 0` means success.

## 1) Register
- Method: `POST`
- Path: `/api/auth/register`
- Body:

```json
{
  "studentNo": "20235555",
  "password": "password",
  "nickname": "new_user"
}
```

- Success response:

```json
{
  "code": 0,
  "message": "注册成功",
  "data": {
    "id": 10,
    "studentNo": "20235555",
    "nickname": "new_user",
    "role": "USER"
  },
  "timestamp": "2026-03-13T12:00:00"
}
```

## 2) Login
- Method: `POST`
- Path: `/api/auth/login`
- Body:

```json
{
  "studentNo": "20230001",
  "password": "password"
}
```

- Success response:

```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "id": 1,
    "studentNo": "20230001",
    "nickname": "Alice",
    "role": "USER"
  },
  "timestamp": "2026-03-13T12:00:00"
}
```

## 3) Current User
- Method: `GET`
- Path: `/api/auth/me`
- Params: none
- Success response:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "studentNo": "20230001",
    "nickname": "Alice",
    "role": "USER"
  },
  "timestamp": "2026-03-13T12:00:00"
}
```

## 4) Logout
- Method: `POST`
- Path: `/api/auth/logout`
- Params: none
- Success response:

```json
{
  "code": 0,
  "message": "退出登录成功",
  "data": null,
  "timestamp": "2026-03-13T12:00:00"
}
```

## 5) Window List / Search
- Method: `GET`
- Path: `/api/windows`
- Query params:
  - `keyword` (optional)
  - `canteenName` (optional)
  - `cuisineType` (optional)
  - `page` (default `0`)
  - `size` (default `10`)
  - `sort` (`score` / `hot` / `latest` / `name`)

- Example:
`/api/windows?keyword=ramen&canteenName=Halal&page=0&size=10&sort=score`

- Success response (simplified):

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "content": [
      {
        "id": 7,
        "canteenName": "Halal Canteen",
        "windowName": "Halal Ramen",
        "cuisineType": "Noodles",
        "intro": "Beef ramen with rich broth.",
        "avgScore": 5.00,
        "reviewCount": 1
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  },
  "timestamp": "2026-03-13T12:00:00"
}
```

## 6) Window Detail
- Method: `GET`
- Path: `/api/windows/{id}`
- Path param: `id`
- Success response:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "canteenId": 1,
    "canteenName": "Canteen A",
    "windowName": "North Noodles",
    "cuisineType": "Noodles",
    "intro": "Hand-pulled noodles and soup.",
    "avgScore": 4.67,
    "reviewCount": 3,
    "status": "OPEN"
  },
  "timestamp": "2026-03-13T12:00:00"
}
```

## 7) Window Review List
- Method: `GET`
- Path: `/api/windows/{id}/reviews`
- Query params: `page`, `size`
- Success response (simplified):

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "content": [
      {
        "id": 11,
        "windowId": 1,
        "windowName": "North Noodles",
        "canteenName": "Canteen A",
        "userId": 3,
        "userNickname": "Carol",
        "score": 5,
        "commentText": "Very consistent quality.",
        "createdAt": "2026-03-11T19:00:00",
        "updatedAt": "2026-03-11T19:00:00"
      }
    ],
    "totalElements": 3,
    "totalPages": 1,
    "number": 0,
    "size": 10
  },
  "timestamp": "2026-03-13T12:00:00"
}
```

## 8) Submit/Update Review
- Method: `POST`
- Path: `/api/windows/{id}/reviews`
- Login required: yes (Session)
- Body:

```json
{
  "score": 5,
  "commentText": "Great taste."
}
```

- Success response:

```json
{
  "code": 0,
  "message": "评价提交成功",
  "data": {
    "id": 1,
    "windowId": 1,
    "windowName": "North Noodles",
    "canteenName": "Canteen A",
    "userId": 1,
    "userNickname": "Alice",
    "score": 5,
    "commentText": "Great taste.",
    "createdAt": "2026-03-01T09:00:00",
    "updatedAt": "2026-03-13T12:00:00"
  },
  "timestamp": "2026-03-13T12:00:00"
}
```

## 9) Ranking - Top Rated
- Method: `GET`
- Path: `/api/rankings/top-rated`
- Query param: `limit` (default `10`)
- Success response:

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "windowId": 7,
      "canteenName": "Halal Canteen",
      "windowName": "Halal Ramen",
      "cuisineType": "Noodles",
      "avgScore": 5.00,
      "reviewCount": 1
    }
  ],
  "timestamp": "2026-03-13T12:00:00"
}
```

## 10) Ranking - Hot
- Method: `GET`
- Path: `/api/rankings/hot`
- Query param: `limit` (default `10`)

## 11) Ranking - Latest Reviews
- Method: `GET`
- Path: `/api/rankings/latest-reviews`
- Query param: `limit` (default `10`)