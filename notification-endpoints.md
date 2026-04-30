# Notification Module Endpoints

Base path: `/api/v1/notifications`

Auth:
- `Authorization: Bearer <JWT_TOKEN>`
- Owner role required for every endpoint

## 1. Get Unread Notifications

Method: `GET`

Path:
```text
/api/v1/notifications/unread
```

Query parameters:
- `limit` optional integer, capped by backend at `100`

Example request:
```http
GET /api/v1/notifications/unread?limit=20
Authorization: Bearer <JWT_TOKEN>
```

Mocked response:
```json
{
  "unreadCount": 3,
  "important": [
    {
      "id": "11111111-1111-1111-1111-111111111111",
      "type": "WALLET_DAILY_LIMIT_NEAR",
      "priority": "MEDIUM",
      "severity": "WARNING",
      "titleKey": "notifications.walletDailyLimitNear.title",
      "messageKey": "notifications.walletDailyLimitNear.message",
      "payload": {
        "walletId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "walletName": "Ops Wallet",
        "periodDate": "2026-04-30"
      },
      "targetType": "WALLET",
      "targetId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      "createdAt": "2026-04-30T17:20:00"
    },
    {
      "id": "22222222-2222-2222-2222-222222222222",
      "type": "WALLET_MONTHLY_LIMIT_EXCEEDED",
      "priority": "HIGH",
      "severity": "DANGER",
      "titleKey": "notifications.walletMonthlyLimitExceeded.title",
      "messageKey": "notifications.walletMonthlyLimitExceeded.message",
      "payload": {
        "walletId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "walletName": "Ops Wallet",
        "periodMonth": "2026-04"
      },
      "targetType": "WALLET",
      "targetId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      "createdAt": "2026-04-30T17:21:00"
    }
  ],
  "low": [
    {
      "id": "33333333-3333-3333-3333-333333333333",
      "type": "TRANSACTION_CREATED",
      "priority": "LOW",
      "severity": "INFO",
      "titleKey": "notifications.transactionCreated.title",
      "messageKey": "notifications.transactionCreated.message",
      "payload": {
        "transactionId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
        "walletId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "walletName": "Ops Wallet",
        "amount": 150.75,
        "type": "CREDIT",
        "createdByUsername": "owner1"
      },
      "targetType": "TRANSACTION",
      "targetId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
      "createdAt": "2026-04-30T17:22:00"
    }
  ]
}
```

## 2. Get Unread Notifications Count

Method: `GET`

Path:
```text
/api/v1/notifications/unread-count
```

Request parameters:
- none

Example request:
```http
GET /api/v1/notifications/unread-count
Authorization: Bearer <JWT_TOKEN>
```

Mocked response:
```json
{
  "count": 3
}
```

## 3. Mark One Notification As Read

Method: `PATCH`

Path:
```text
/api/v1/notifications/{id}/read
```

Path parameters:
- `id` required UUID of the notification

Example request:
```http
PATCH /api/v1/notifications/11111111-1111-1111-1111-111111111111/read
Authorization: Bearer <JWT_TOKEN>
```

Mocked response:
```http
HTTP/1.1 200 OK
```

## 4. Mark All LOW Priority Notifications As Read

Method: `PATCH`

Path:
```text
/api/v1/notifications/read-low
```

Request parameters:
- none

Example request:
```http
PATCH /api/v1/notifications/read-low
Authorization: Bearer <JWT_TOKEN>
```

Mocked response:
```http
HTTP/1.1 200 OK
```

## 5. Mark All Notifications As Read

Method: `PATCH`

Path:
```text
/api/v1/notifications/read-all
```

Request parameters:
- none

Example request:
```http
PATCH /api/v1/notifications/read-all
Authorization: Bearer <JWT_TOKEN>
```

Mocked response:
```http
HTTP/1.1 200 OK
```

## Notification Payload Variants

`TRANSACTION_CREATED`
```json
{
  "transactionId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "walletId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "walletName": "Ops Wallet",
  "amount": 150.75,
  "type": "CREDIT",
  "createdByUsername": "owner1"
}
```

`WALLET_DAILY_LIMIT_NEAR` or `WALLET_DAILY_LIMIT_EXCEEDED`
```json
{
  "walletId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "walletName": "Ops Wallet",
  "periodDate": "2026-04-30"
}
```

`WALLET_MONTHLY_LIMIT_NEAR` or `WALLET_MONTHLY_LIMIT_EXCEEDED`
```json
{
  "walletId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "walletName": "Ops Wallet",
  "periodMonth": "2026-04"
}
```
