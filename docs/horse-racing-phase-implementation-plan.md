# Roadmap Triển Khai Backend Horse Racing

Tài liệu này hợp nhất roadmap tournament và rule tài chính ví admin trung tâm. Mục tiêu là có checklist backend API theo từng phase để code, fix bug và test theo thứ tự hợp lý. Hai file `docs/plan.md` và `docs/plan_wallet.md` được giữ nguyên làm nguồn tham khảo.

Tài liệu liên quan: `docs/role-screen-functions.md` mô tả các màn hình và chức năng nên có cho từng role.

## Nguyên tắc chung

- Phạm vi: backend API, entity, service, repository, migration, security guard, test.
- Tiền tệ: dùng `BigDecimal`, mặc định currency `VND`, không dùng `double`/`float` cho tiền.
- Ví trung tâm: `AdminWallet`/`SystemWallet` giữ tiền thật/custody của hệ thống.
- Ví user: `UserWallet` ghi nhận số dư và quyền sở hữu tiền trong app.
- Ledger: mọi giao dịch tiền phải có transaction, `referenceId`, audit fields và idempotency key khi nhận callback/provider event.
- Payment MVP có thể bắt đầu bằng manual/bank transfer, nhưng thiết kế `PaymentProvider` để gắn VNPay/MoMo sau.
- Betting tiền thật để sau prediction và phải có feature flag.

## Public API Và Type Cốt Lõi

### User wallet API

- `GET /api/v1/wallets/me`
- `GET /api/v1/wallets/me/transactions`
- `POST /api/v1/wallets/me/deposit-orders`
- `POST /api/v1/wallets/me/withdrawals`
- `GET /api/v1/wallets/me/withdrawals`

### Admin wallet API

- `GET /api/v1/admin/wallet`
- `GET /api/v1/admin/wallet/transactions`
- `POST /api/v1/admin/wallet/withdrawals`
- `GET /api/v1/admin/withdrawals`
- `PUT /api/v1/admin/withdrawals/{id}/approve`
- `PUT /api/v1/admin/withdrawals/{id}/reject`
- `PUT /api/v1/admin/withdrawals/{id}/mark-paid`

### Enum/type cần có

- `WalletOwnerType`: `USER`, `ADMIN`
- `WalletStatus`: `ACTIVE`, `LOCKED`, `CLOSED`
- `WalletTransactionType`: `DEPOSIT`, `WITHDRAW`, `ADMIN_WITHDRAW`, `ENTRY_FEE`, `BET_STAKE`, `BET_PAYOUT`, `PRIZE_PAYOUT`, `ITEM_PURCHASE`, `ITEM_SALE`, `REFUND`, `ADJUSTMENT`
- `WalletTransactionStatus`: `PENDING`, `SUCCESS`, `FAILED`, `REVERSED`
- `PaymentOrderStatus`: `PENDING`, `PAID`, `FAILED`, `EXPIRED`, `CANCELLED`
- `WithdrawalStatus`: `PENDING`, `APPROVED`, `REJECTED`, `PAID`, `CANCELLED`
- `AdminWalletWithdrawalStatus`: `PAID`, `FAILED`, `REVERSED`
- `TournamentStatus`, `RegistrationStatus`, `RaceStatus`
- `HorseStatus`, `JockeyStatus`, `AssignmentStatus`
- `BetStatus`, `PredictionStatus`

## Phase 0 - Siết nền tảng hiện có

- Thời lượng ước tính: 0.5-1 ngày.
- Actor: guest, user, admin.
- Mục tiêu: backend có nền auth/security/error/API docs đủ ổn để các phase sau không phải sửa nền liên tục.
- Entity/enum/API/service: user, role, auth guard, global exception, API response, Swagger/OpenAPI, audit fields.
- Checklist implementation:
  - Kiểm tra login/register/current user và role guard cho admin/user.
  - Chuẩn hóa response lỗi validation, unauthorized, forbidden, not found, business rule.
  - Thêm hoặc kiểm tra audit fields `createdAt`, `updatedAt`, `createdBy`, `updatedBy` cho entity quan trọng.
  - Seed admin mặc định bằng migration/runner có kiểm soát.
  - Kiểm tra Swagger hiển thị endpoint và security scheme.
- Checklist test:
  - Test role user không gọi được admin API.
  - Test lỗi validation trả format thống nhất.
  - Test seed admin không tạo trùng khi chạy lại.
- Acceptance criteria:
  - Có thể đăng nhập admin/user và dùng token gọi API đúng quyền.
  - API lỗi trả format rõ ràng, không lộ stack trace.
  - Swagger mở được và có auth bearer.

## Phase 1 - Wallet core với ví admin trung tâm

- Thời lượng ước tính: 2-3 ngày.
- Actor: user, admin, system.
- Mục tiêu: có nền ví, ledger và thao tác tiền an toàn trước khi làm payment/race fee.
- Entity/enum/API/service: `UserWallet`, `AdminWallet` hoặc `Wallet` với `WalletOwnerType`, `WalletTransaction`, `WalletService`, `WalletLedgerService`.
- Checklist implementation:
  - Tạo ví user khi user được tạo hoặc khi user gọi wallet lần đầu.
  - Tạo một ví admin/system mặc định currency `VND`.
  - Thiết kế số dư `availableBalance`, `holdBalance`, `totalBalance`.
  - Implement operation `credit`, `debit`, `hold`, `release`, `capture`, `refund`.
  - Không cho balance âm, không cho capture vượt hold.
  - Mỗi operation tạo ledger transaction có `referenceId`, `type`, `status`, `metadata`.
  - Dùng transaction database và lock phù hợp khi cập nhật số dư.
- Checklist test:
  - Credit/debit cập nhật đúng available.
  - Hold chuyển available sang hold.
  - Release trả hold về available.
  - Capture trừ hold.
  - Không thể debit/hold vượt số dư.
  - Transaction rollback nếu ghi ledger lỗi.
- Acceptance criteria:
  - User xem được ví và lịch sử giao dịch của chính mình.
  - Admin xem được ví admin và transaction admin.
  - Mọi biến động tiền đều có ledger trace được.

## Phase 2 - Payment deposit MVP

- Thời lượng ước tính: 2-3 ngày.
- Actor: user, admin, payment provider, system.
- Mục tiêu: user nạp tiền vào hệ thống, tiền được cộng cả user wallet và admin wallet.
- Entity/enum/API/service: `PaymentOrder`, `PaymentProvider`, `PaymentCallbackVerifier`, deposit API, provider callback API.
- Checklist implementation:
  - User tạo deposit order với amount, currency, provider.
  - Manual/bank transfer fallback trả thông tin chuyển khoản và mã tham chiếu.
  - Provider callback phải verify chữ ký/token trước khi xử lý.
  - Callback paid cộng `UserWallet.availableBalance`.
  - Callback paid cộng `AdminWallet.availableBalance`.
  - Tạo 2 transaction cùng `referenceId` để trace user/admin side.
  - Dùng idempotency để callback trùng không cộng tiền lần hai.
  - Nếu cộng một ví lỗi thì rollback toàn bộ.
- Checklist test:
  - Deposit paid cộng đúng cả user wallet và admin wallet.
  - Callback duplicate không cộng lần hai.
  - Callback sai chữ ký bị từ chối.
  - Payment order expired/cancelled không được paid lại tùy rule.
- Acceptance criteria:
  - User tạo được lệnh nạp và xem trạng thái.
  - Provider/manual callback thành công tạo đủ ledger hai phía.
  - Số dư admin wallet phản ánh tổng tiền thật user đã nạp.

## Phase 3 - Withdraw và audit tài chính

- Thời lượng ước tính: 2-3 ngày.
- Actor: user, admin, system.
- Mục tiêu: user rút tiền cần admin duyệt; admin rút từ ví admin không cần duyệt nhưng bắt buộc audit.
- Entity/enum/API/service: `WithdrawalRequest`, `AdminWalletWithdrawal`, `AdminAuditLog`, `WithdrawalService`.
- Checklist implementation:
  - User tạo withdrawal request với amount và bank info.
  - Khi tạo request, hold tiền user từ available sang hold.
  - Admin approve để xác nhận sẽ xử lý ngoài hệ thống.
  - Admin reject release hold về user available.
  - Admin mark-paid trừ user hold và trừ `AdminWallet.availableBalance`.
  - Admin withdraw trực tiếp từ admin wallet, không cần approval.
  - Admin withdraw phải kiểm tra số dư, ghi audit log gồm adminId, amount, bank info, reason, timestamp.
  - Tách rõ endpoint user withdrawal và admin wallet withdrawal.
- Checklist test:
  - User withdraw pending làm user balance bị hold, admin wallet chưa bị trừ.
  - Reject trả hold về available.
  - Mark-paid trừ user hold và admin available.
  - Admin withdraw không làm admin wallet âm.
  - Mọi thao tác admin có audit log.
- Acceptance criteria:
  - Flow withdraw user chạy được từ pending đến paid/rejected.
  - Admin wallet chỉ giảm khi user withdraw mark-paid hoặc admin withdraw trực tiếp.
  - Có audit/reference đầy đủ cho giao dịch nhạy cảm.

## Phase 4 - Horse và Jockey profile

- Thời lượng ước tính: 2 ngày.
- Actor: owner, jockey, admin.
- Mục tiêu: có hồ sơ ngựa và jockey đủ điều kiện tham gia tournament.
- Entity/enum/API/service: `Horse`, `HorseStatus`, `JockeyProfile`, `JockeyStatus`, approval service.
- Checklist implementation:
  - Owner CRUD horse của mình.
  - Jockey tạo/cập nhật profile và license info.
  - Admin approve/reject/suspend horse.
  - Admin approve/reject/suspend jockey.
  - Chặn horse/jockey chưa approved khi đăng ký tournament.
- Checklist test:
  - Owner không sửa horse của owner khác.
  - Jockey chưa approved không nhận race assignment.
  - Admin suspend làm horse/jockey không còn hợp lệ.
- Acceptance criteria:
  - Horse và jockey có lifecycle rõ từ draft/pending đến approved/suspended.

## Phase 5 - Owner-Jockey invitation

- Thời lượng ước tính: 1-2 ngày.
- Actor: owner, jockey, admin/system.
- Mục tiêu: owner mời jockey, jockey accept/reject để tạo assignment hợp lệ.
- Entity/enum/API/service: `JockeyInvitation`, `AssignmentStatus`, invitation API/service.
- Checklist implementation:
  - Owner gửi invitation tới jockey approved.
  - Jockey accept/reject invitation.
  - Không cho tạo invitation trùng active cho cùng horse/jockey/context.
  - Ghi status history hoặc audit cho invitation.
  - Kiểm tra trùng lịch chi tiết để ở phase race scheduling.
- Checklist test:
  - Jockey không thể accept invitation của người khác.
  - Invitation rejected không dùng để đăng ký race.
  - Invitation duplicate bị chặn.
- Acceptance criteria:
  - Owner và jockey có thể tạo quan hệ hợp lệ để dùng ở tournament/race.

## Phase 6 - Tournament setup

- Thời lượng ước tính: 2 ngày.
- Actor: admin, owner, spectator.
- Mục tiêu: admin tạo tournament, round, entry fee, prize pool và thời gian đăng ký.
- Entity/enum/API/service: `Tournament`, `TournamentRound`, `TournamentStatus`, tournament API/service.
- Checklist implementation:
  - Admin CRUD tournament.
  - Cấu hình registration window, entry fee, max participants, prize pool.
  - Tạo round/heat structure cơ bản.
  - Mở/đóng đăng ký theo status và thời gian.
  - Public API xem tournament đang mở.
- Checklist test:
  - User thường không tạo/sửa tournament.
  - Không đăng ký khi tournament chưa mở hoặc đã đóng.
  - Entry fee/prize pool validate không âm.
- Acceptance criteria:
  - Tournament có thể đi từ draft đến open registration và close registration.

## Phase 7 - Registration + entry fee qua wallet

- Thời lượng ước tính: 2-3 ngày.
- Actor: owner, admin, system.
- Mục tiêu: owner đăng ký horse vào tournament và xử lý phí qua ví.
- Entity/enum/API/service: `TournamentRegistration`, `RegistrationStatus`, registration service, wallet integration.
- Checklist implementation:
  - Owner đăng ký horse approved vào tournament open.
  - Kiểm tra horse thuộc owner, jockey assignment hợp lệ nếu bắt buộc.
  - Hold hoặc debit entry fee theo policy đã chọn trong code.
  - Admin approve thì capture/debit entry fee.
  - Admin reject thì release/refund entry fee.
  - Chặn duplicate registration cho cùng horse/tournament.
- Checklist test:
  - Không đủ tiền thì không đăng ký được.
  - Approve capture đúng tiền.
  - Reject hoàn/release đúng tiền.
  - Duplicate registration bị chặn.
- Acceptance criteria:
  - Registration có luồng pending/approved/rejected và ledger entry fee đầy đủ.

## Phase 8 - Race scheduling

- Thời lượng ước tính: 2 ngày.
- Actor: admin, referee, owner, jockey.
- Mục tiêu: tạo race từ registration approved và phân công participant/referee.
- Entity/enum/API/service: `Race`, `RaceParticipant`, `RaceStatus`, `RefereeAssignment`, scheduling service.
- Checklist implementation:
  - Admin tạo race/heat từ registrations approved.
  - Tạo participants, gán gate number không trùng trong race.
  - Phân công referee.
  - Owner/jockey xác nhận tham gia nếu cần.
  - Kiểm tra jockey không bị trùng lịch race cùng thời điểm.
- Checklist test:
  - Gate number duplicate bị chặn.
  - Registration chưa approved không được schedule.
  - Referee không được nhập result race không được phân công.
- Acceptance criteria:
  - Race có participant list, gate number và referee rõ ràng.

## Phase 9 - Race day/referee

- Thời lượng ước tính: 2-3 ngày.
- Actor: referee, admin, owner, jockey.
- Mục tiêu: hỗ trợ check-in, start race, violation, draft result và referee report.
- Entity/enum/API/service: `RaceCheckIn`, `RaceViolation`, `RaceResultDraft`, `RefereeReport`.
- Checklist implementation:
  - Referee check-in horse/participant.
  - Start race chỉ khi race đúng trạng thái.
  - Ghi violation với participant, rule, note, penalty.
  - Nhập draft result theo finish order/time.
  - Referee submit report để admin duyệt.
- Checklist test:
  - Không thể start race đã completed/cancelled.
  - Participant chưa check-in xử lý theo rule.
  - Draft result không cho duplicate rank.
- Acceptance criteria:
  - Referee hoàn thành được dữ liệu ngày đua để admin review.

## Phase 10 - Result, leaderboard, prize payout

- Thời lượng ước tính: 2-3 ngày.
- Actor: admin, referee, owner, jockey, system.
- Mục tiêu: admin duyệt kết quả, tạo leaderboard và payout giải thưởng qua ví.
- Entity/enum/API/service: `RaceResult`, `LeaderboardSnapshot`, `PrizePayout`, result service.
- Checklist implementation:
  - Admin approve/reject draft result.
  - Khi approve, race chuyển confirmed/completed.
  - Generate leaderboard snapshot theo tournament/round.
  - Tính prize theo prize pool/rule.
  - Payout prize qua wallet service.
  - Nếu prize là tiền hệ thống trả cho user, ghi ledger nguồn từ admin/system policy.
- Checklist test:
  - Result approved chỉ một lần.
  - Leaderboard snapshot không đổi sau khi confirmed nếu không có admin adjustment.
  - Prize payout tạo transaction đúng user.
- Acceptance criteria:
  - Race hoàn tất có result chính thức, leaderboard và payout trace được.

## Phase 11 - Spectator prediction

- Thời lượng ước tính: 1-2 ngày.
- Actor: spectator, system.
- Mục tiêu: spectator dự đoán miễn phí hoặc nhận reward nhỏ, chưa dùng betting tiền thật.
- Entity/enum/API/service: `Prediction`, `PredictionStatus`, prediction service.
- Checklist implementation:
  - Spectator chọn participant dự đoán trước giờ race start.
  - Lock prediction khi race bắt đầu.
  - Settle prediction khi result confirmed.
  - Reward nhỏ qua wallet hoặc inventory nếu có policy.
- Checklist test:
  - Không dự đoán sau race start.
  - Một spectator không tạo nhiều prediction active cho cùng race nếu policy là 1 lần.
  - Settle đúng winner/lost.
- Acceptance criteria:
  - Prediction hoạt động độc lập với betting và không tạo rủi ro tiền thật.

## Phase 12 - Betting bằng ví

- Thời lượng ước tính: 3-5 ngày.
- Actor: user, admin, system.
- Mục tiêu: đặt cược bằng ví với stake, odds, settle won/lost/refund và feature flag.
- Entity/enum/API/service: `Bet`, `BetStatus`, odds service, settlement service.
- Checklist implementation:
  - Thêm feature flag bật/tắt betting.
  - User đặt bet trước race start, stake bị hold/debit theo policy.
  - Lock bet khi race start.
  - Settle won/lost khi result confirmed.
  - Refund khi race cancelled/result voided.
  - Audit và ledger đầy đủ cho stake, payout, refund.
  - Validate pháp lý/production trước khi bật mặc định.
- Checklist test:
  - Feature flag off thì API betting bị chặn.
  - Không bet sau race start.
  - Won/lost/refund cập nhật ví đúng.
  - Duplicate settlement không trả tiền hai lần.
- Acceptance criteria:
  - Betting có thể tắt hoàn toàn và khi bật thì mọi luồng tiền trace được.

## Phase 13 - Item marketplace

- Thời lượng ước tính: 2-3 ngày.
- Actor: user, admin, system.
- Mục tiêu: mua/bán vật phẩm bằng ví và quản lý inventory.
- Entity/enum/API/service: `Item`, `InventoryItem`, `ItemOrder`, marketplace service.
- Checklist implementation:
  - Admin CRUD item và giá.
  - User mua item, ví bị debit và inventory tăng.
  - User bán item nếu policy cho phép, inventory giảm và ví credit.
  - Chặn mua item inactive/out of stock nếu có stock.
- Checklist test:
  - Không đủ tiền thì không mua được.
  - Mua thành công tạo wallet transaction và inventory record.
  - Bán item không sở hữu bị chặn.
- Acceptance criteria:
  - Marketplace có ledger wallet và inventory đồng bộ.

## Phase 14 - Notification/WebSocket

- Thời lượng ước tính: 2 ngày.
- Actor: user, admin, referee, spectator, system.
- Mục tiêu: gửi notification và realtime update cho các sự kiện quan trọng.
- Entity/enum/API/service: `Notification`, notification service, WebSocket topics.
- Checklist implementation:
  - Notification cho invitation, payment, withdraw, registration, race status, result, reward.
  - API lấy danh sách notification và mark read.
  - WebSocket topic cho race status/result/leaderboard.
  - Không gửi dữ liệu nhạy cảm qua public topic.
- Checklist test:
  - User chỉ xem notification của mình.
  - Event quan trọng tạo notification đúng recipient.
  - WebSocket topic không leak admin/private data.
- Acceptance criteria:
  - Người dùng nhận được update cần thiết mà không phải refresh liên tục.

## Phase 15 - Production hardening

- Thời lượng ước tính: 3-5 ngày.
- Actor: admin, developer, operator.
- Mục tiêu: làm chắc backend trước khi production hoặc demo lớn.
- Entity/enum/API/service: migration, audit, monitoring, security config, test coverage.
- Checklist implementation:
  - Dùng Flyway/Liquibase cho schema migration.
  - Pagination/sorting/filtering cho list API quan trọng.
  - Rate limit cho auth, payment callback, betting, withdrawal.
  - Payment callback security: signature, timestamp, replay protection, IP allowlist nếu provider hỗ trợ.
  - Audit log cho admin action và money movement.
  - Logging có correlation id/reference id.
  - Monitoring/health check cho DB/payment provider.
  - Rà soát index DB cho query list và lookup theo reference/status.
- Checklist test:
  - Integration test các money flow chính.
  - Security test role guard endpoint admin.
  - Migration chạy sạch từ DB trống.
  - Payment callback replay bị chặn.
- Acceptance criteria:
  - Backend có thể deploy với migration rõ ràng, log/audit đủ điều tra sự cố, và test coverage cho flow rủi ro cao.

## Checklist kiểm tra sau khi viết tài liệu

- Markdown render đúng tiếng Việt UTF-8.
- Đủ phase 0-15, đúng thứ tự và có thời lượng ước tính.
- Wallet/payment/withdraw nằm sớm ở phase 1-3.
- Rule tài chính được ghi rõ:
  - Deposit cộng cả user wallet và admin wallet.
  - Callback trùng không cộng tiền lần hai.
  - User withdraw hold tiền trước, mark-paid mới trừ admin wallet.
  - Reject withdraw release hold.
  - Admin withdraw không cần duyệt nhưng phải audit.
- Không chạy backend test vì thay đổi chỉ là tài liệu.
- Sau khi tạo file, kiểm tra `git status` để đảm bảo chỉ thêm file docs mới và không sửa code ngoài ý muốn.
