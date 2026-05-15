# Plan Tạo File Phase Checklist Cho Horse Racing System

## Summary

Tạo thêm file mới `docs/horse-racing-phase-implementation-plan.md` bằng tiếng Việt, dạng **task checklist chi tiết theo từng phase** để bạn đọc và quyết định trước khi code. File mới sẽ không thay thế spec hiện tại, mà đóng vai trò roadmap triển khai backend từ repo hiện có đến production.

Ưu tiên theo lựa chọn của bạn: **checklist task nhỏ, dễ giao việc**, và **đưa ví/thanh toán thật lên sớm** thay vì để cuối.

## Key Changes

- Tạo file Markdown mới:
  - `docs/horse-racing-phase-implementation-plan.md`
  - Encoding UTF-8.
  - Không sửa `docs/horse-racing-tournament-spec.md`.
  - Không đụng vào code/test hiện đang có thay đổi deleted trong git status.

- Cấu trúc mỗi phase trong file:
  - Mục tiêu phase.
  - Actor liên quan.
  - Entity/enum cần tạo.
  - API cần có.
  - Service/business rule cần xử lý.
  - Checklist implementation.
  - Checklist test.
  - Acceptance criteria để biết phase đã xong.

## Phase Plan Nội Dung Sẽ Viết

- Phase 0: Siết nền tảng hiện có
  - Auth/User/Security, role guard, API error chuẩn, Swagger, seed admin, audit fields.

- Phase 1: Wallet core sớm
  - `Wallet`, `WalletTransaction`, ledger, credit/debit/hold/release/capture, BigDecimal, idempotency.

- Phase 2: Payment thật MVP
  - `PaymentOrder`, deposit flow, provider callback, manual/bank transfer fallback, verify callback, duplicate callback handling.
  - Gợi ý provider mặc định: VNPay hoặc MoMo, nhưng thiết kế interface để đổi provider được.

- Phase 3: Withdraw và KYC cơ bản
  - `WithdrawalRequest`, hold tiền khi tạo yêu cầu, admin approve/reject/mark-paid, hoàn hold khi reject.

- Phase 4: Horse và Jockey profile
  - Owner quản lý horse.
  - Jockey tạo profile/license.
  - Admin duyệt hoặc suspend horse/jockey.

- Phase 5: Owner-Jockey invitation
  - Owner mời jockey, jockey accept/reject, kiểm tra trùng lịch ở phase race.

- Phase 6: Tournament setup
  - Admin tạo tournament, round, entry fee, prize pool, mở/đóng đăng ký.

- Phase 7: Registration + entry fee qua wallet
  - Owner đăng ký horse vào tournament.
  - Hold hoặc debit entry fee.
  - Admin approve/reject, xử lý refund/release/capture.

- Phase 8: Race scheduling
  - Admin tạo race từ registration approved.
  - Tạo participant, gán gate number, phân công referee.
  - Owner/Jockey xác nhận tham gia.

- Phase 9: Race day/referee
  - Check-in horse.
  - Start race.
  - Ghi violation.
  - Draft result.
  - Referee report.

- Phase 10: Result, leaderboard, prize payout
  - Admin approve result.
  - Generate leaderboard snapshot.
  - Tính prize.
  - Payout qua WalletService.

- Phase 11: Spectator prediction
  - Dự đoán miễn phí hoặc reward nhỏ qua ví/inventory.
  - Lock khi race bắt đầu.
  - Settle khi result confirmed.

- Phase 12: Betting bằng ví
  - Bet stake, odds, lock, settle won/lost/refund.
  - Feature flag để có thể tắt nếu chưa đủ điều kiện production/pháp lý.

- Phase 13: Item marketplace
  - Item, inventory, mua/bán vật phẩm qua ví.

- Phase 14: Notification/WebSocket
  - Notification cho invitation, payment, withdraw, race status, result, reward.
  - WebSocket topic cho race status/result/leaderboard.

- Phase 15: Production hardening
  - Migration Flyway/Liquibase.
  - Audit log admin.
  - Pagination.
  - Rate limit.
  - Payment security.
  - Monitoring/logging.
  - Test coverage tối thiểu.

## Public APIs/Types Sẽ Được Ghi Trong File

- API nhóm wallet/payment:
  - `GET /api/v1/wallets/me`
  - `GET /api/v1/wallets/me/transactions`
  - `POST /api/v1/wallets/me/deposit-orders`
  - `POST /api/v1/payments/{provider}/callback`
  - `POST /api/v1/wallets/me/withdrawals`
  - `PUT /api/v1/admin/withdrawals/{id}/approve`
  - `PUT /api/v1/admin/withdrawals/{id}/reject`
  - `PUT /api/v1/admin/withdrawals/{id}/mark-paid`

- API nhóm tournament/race:
  - Horse CRUD, jockey profile, invitation, tournament CRUD, registration approve/reject, race scheduling, referee result/report, leaderboard.

- Enum/type quan trọng:
  - `WalletStatus`, `WalletTransactionType`, `WalletTransactionStatus`
  - `PaymentOrderStatus`, `WithdrawalStatus`
  - `TournamentStatus`, `RegistrationStatus`, `RaceStatus`
  - `HorseStatus`, `JockeyStatus`, `AssignmentStatus`
  - `BetStatus`, `PredictionStatus`

## Test Plan

- Kiểm tra file Markdown render rõ trong IDE.
- Kiểm tra các heading phase đúng thứ tự.
- Kiểm tra mỗi phase đều có checklist implementation/test/acceptance.
- Kiểm tra payment/wallet xuất hiện sớm đúng yêu cầu.
- Không chạy test backend vì task này chỉ tạo tài liệu.
- Sau khi tạo file, kiểm tra `git status` để xác nhận chỉ thêm file docs mới, không sửa code ngoài ý muốn.

## Assumptions

- File mới dùng tiếng Việt, thuật ngữ kỹ thuật giữ tiếng Anh khi cần như entity, service, endpoint, wallet ledger.
- Payment thật được đưa sớm nhưng vẫn thiết kế qua provider abstraction để chưa bị khóa vào một cổng thanh toán.
- MVP vẫn có thể chạy với payment manual/bank transfer nếu VNPay/MoMo chưa có credential.
- Bet tiền thật sẽ được đưa sau prediction và có feature flag vì nhạy cảm về pháp lý.
- Không sửa hoặc khôi phục 3 file test đang bị deleted trong workspace trừ khi bạn yêu cầu riêng.
