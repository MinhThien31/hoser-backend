# Horse Racing Flow Revised Plan

## Mục tiêu tài liệu

Tài liệu này là bản roadmap mới cho luồng giải đấu horse racing, kế thừa định hướng backend hiện tại nhưng chỉnh lại các phần liên quan đến tournament, registration, lịch thi đấu, check-in, kết quả, trao giải và thống kê.

Hai tài liệu `docs/horse-racing-phase-implementation-plan.md` và `docs/role-screen-functions.md` được giữ nguyên làm nguồn tham khảo. Bản này tập trung vào 4 luồng nghiệp vụ chính:

- Admin cấu hình giải đấu, vòng đấu, số đội tham gia và giải thưởng.
- Owner quản lý hồ sơ cá nhân, hồ sơ ngựa và giấy tờ liên quan.
- Owner đăng ký ngựa tham gia giải đấu, đặt cọc, nhận email/notification, check-in, đi tiếp qua từng vòng và nhận giải.
- Admin xem thống kê kết quả giải đấu, participant, jockey, referee, giải thưởng và dòng tiền.

## Nguyên tắc chung

- Phạm vi: backend API, entity, service, repository, migration, security guard, notification/email event và test.
- Tiền tệ: dùng `BigDecimal`, mặc định currency `VND`, không dùng `double`/`float` cho tiền.
- Ví trung tâm: `AdminWallet`/`SystemWallet` giữ tiền thật/custody của hệ thống.
- Ví user: `UserWallet` ghi nhận số dư, tiền đang hold và quyền sở hữu tiền trong app.
- Ledger: mọi giao dịch tiền phải có transaction, `referenceType`, `referenceId`, audit fields và idempotency key.
- “Đội tham gia” = 1 horse team gồm `horse + owner + jockey đã accepted`.
- `minTeams` và `maxTeams` của tournament/round tính theo số horse team.
- Entry/deposit khi đăng ký giải dùng flow `hold -> capture/release` qua ví nội bộ.
- Jockey hire flow giữ nguyên: owner tạo lời mời thì hold tiền, jockey chỉ nhận tiền khi accept, owner cancel khi pending thì release toàn bộ tiền hold.
- Betting tiền thật để sau prediction và phải có feature flag.

## Luồng nghiệp vụ chính

### Luồng 1 - Admin cấu hình giải đấu

- Admin tạo tournament với thông tin cơ bản: tên, mô tả, địa điểm, thời gian đăng ký, thời gian thi đấu, trạng thái publish.
- Admin cấu hình các vòng đấu: vòng loại, bán kết, chung kết hoặc cấu trúc round tùy giải.
- Mỗi tournament hoặc từng round phải có `minTeams`, `maxTeams`, rule chọn đội đi tiếp và thời gian dự kiến.
- Admin cấu hình phí đăng ký/tiền đặt cọc, chính sách capture/release và deadline check-in.
- Admin cấu hình giải thưởng: hạng giải, số tiền/vật phẩm, điều kiện nhận giải và người nhận payout.
- Tournament chỉ mở đăng ký khi thông tin cơ bản, round config, min/max team và prize config hợp lệ.

### Luồng 2 - Owner quản lý profile cá nhân và ngựa

- Owner quản lý thông tin cá nhân, thông tin liên hệ, avatar và dữ liệu cần cho thông báo/email.
- Owner CRUD hồ sơ ngựa của mình, upload ảnh và tài liệu qua Cloudinary/storage.
- Ngựa có lifecycle rõ: draft/pending, approved, rejected, suspended.
- Admin duyệt hoặc từ chối hồ sơ ngựa, kèm lý do reject/suspend.
- Chỉ ngựa `APPROVED` mới được đăng ký tham gia tournament.

### Luồng 3 - Owner tham gia giải đấu

- Owner xem danh sách tournament đang mở hoặc đang diễn ra.
- Owner chọn một tournament, có thể xem chi tiết trước khi đăng ký.
- Owner chọn horse team hợp lệ: ngựa approved, owner sở hữu ngựa, jockey đã accept invitation.
- Khi gửi registration, hệ thống hold tiền đặt cọc/entry fee từ ví owner.
- Hệ thống gửi email và notification xác nhận registration đã được ghi nhận.
- Admin duyệt registration; nếu approve thì capture tiền theo policy, nếu reject/cancel thì release toàn bộ tiền hold.
- Trước lịch thi đấu 3 ngày, hệ thống gửi email và notification nhắc owner, jockey và referee.
- Đến ngày thi đấu, participant check-in theo race/round.
- Referee/admin ghi nhận kết quả race; admin xác nhận kết quả chính thức.
- Hệ thống dựa trên kết quả đã xác nhận để chọn đội thắng/đội đi tiếp vào vòng sau.
- Quy trình race -> result -> advancement lặp lại đến vòng cuối.
- Ở vòng cuối, hệ thống xác định winner chính thức và trigger trao giải/payout.

### Luồng 4 - Admin thống kê kết quả giải đấu

- Admin xem thống kê theo tournament: số owner/customer tham gia, số horse team, số jockey, số referee, số registration.
- Admin xem thống kê theo vòng/race: participant, check-in, absent, disqualified, result, winner và đội đi tiếp.
- Admin xem thống kê giải thưởng: prize config, winner, trạng thái payout, transaction liên quan.
- Admin xem thống kê tài chính: entry fee/deposit hold, capture, release/refund, prize payout và audit ledger.
- Admin có thể lọc theo tournament, round, race, trạng thái registration, trạng thái payout và khoảng thời gian.

## Public API và type cốt lõi

### Wallet API giữ nguyên định hướng

- `GET /api/v1/wallets/me`
- `GET /api/v1/wallets/me/transactions`
- `POST /api/v1/wallets/me/deposit-orders`
- `POST /api/v1/wallets/me/withdrawals`
- `GET /api/v1/wallets/me/withdrawals`
- `GET /api/v1/admin/wallet`
- `GET /api/v1/admin/wallet/transactions`
- `GET /api/v1/admin/withdrawals`

### Tournament và registration API cần có

- `POST /api/v1/admin/tournaments`
- `PUT /api/v1/admin/tournaments/{id}`
- `POST /api/v1/admin/tournaments/{id}/rounds`
- `PUT /api/v1/admin/tournaments/{id}/rounds/{roundId}`
- `PUT /api/v1/admin/tournaments/{id}/prizes`
- `PUT /api/v1/admin/tournaments/{id}/publish`
- `PUT /api/v1/admin/tournaments/{id}/open-registration`
- `PUT /api/v1/admin/tournaments/{id}/close-registration`
- `GET /api/v1/tournaments`
- `GET /api/v1/tournaments/{id}`
- `POST /api/v1/owner/tournament-registrations`
- `GET /api/v1/owner/tournament-registrations`
- `PUT /api/v1/admin/tournament-registrations/{id}/approve`
- `PUT /api/v1/admin/tournament-registrations/{id}/reject`

### Race, result và statistics API cần có

- `POST /api/v1/admin/tournaments/{id}/races/generate`
- `PUT /api/v1/admin/races/{id}/schedule`
- `PUT /api/v1/admin/races/{id}/assign-referee`
- `GET /api/v1/races`
- `GET /api/v1/races/{id}`
- `PUT /api/v1/referee/races/{id}/check-in`
- `POST /api/v1/referee/races/{id}/draft-results`
- `PUT /api/v1/admin/races/{id}/results/approve`
- `PUT /api/v1/admin/tournaments/{id}/advance-round`
- `GET /api/v1/admin/tournaments/{id}/statistics`

### Enum/type cần có

- `TournamentStatus`: `DRAFT`, `PUBLISHED`, `OPEN_REGISTRATION`, `REGISTRATION_CLOSED`, `SCHEDULED`, `ONGOING`, `COMPLETED`, `CANCELLED`
- `TournamentRoundStatus`: `DRAFT`, `SCHEDULED`, `ONGOING`, `COMPLETED`, `CANCELLED`
- `RegistrationStatus`: `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`, `CANCELLED`
- `RaceStatus`: `DRAFT`, `SCHEDULED`, `CHECK_IN_OPEN`, `READY`, `ONGOING`, `PENDING_RESULT`, `RESULT_CONFIRMED`, `COMPLETED`, `CANCELLED`
- `PrizePayoutStatus`: `PENDING`, `PAID`, `FAILED`, `REVERSED`
- `WalletTransactionType`: giữ các type hiện tại và bổ sung/giữ `ENTRY_FEE`, `PRIZE_PAYOUT`, `JOCKEY_HIRE`, `JOCKEY_PAYOUT`, `JOCKEY_HIRE_TAX`, `REFUND`, `ADJUSTMENT`

## Phase 0 - Siết nền tảng hiện có

- Thời lượng ước tính: 0.5-1 ngày.
- Actor: guest, user, admin.
- Mục tiêu: auth/security/error/API docs đủ ổn để các phase sau không phải sửa nền liên tục.
- Checklist implementation:
  - Kiểm tra login/register/current user và role guard.
  - Chuẩn hóa response lỗi validation, unauthorized, forbidden, not found, business rule.
  - Kiểm tra audit fields `createdAt`, `updatedAt`, `createdBy`, `updatedBy`.
  - Seed admin mặc định bằng migration/runner có kiểm soát.
  - Kiểm tra Swagger/OpenAPI hiển thị endpoint và bearer auth.
- Acceptance criteria:
  - Admin/user đăng nhập và gọi API đúng quyền.
  - API lỗi không lộ stack trace.
  - Swagger mở được và có auth bearer.

## Phase 1 - Wallet core với ví admin trung tâm

- Thời lượng ước tính: 2-3 ngày.
- Actor: user, admin, system.
- Mục tiêu: có nền ví, ledger và thao tác tiền an toàn trước khi làm payment/race fee.
- Checklist implementation:
  - Tạo ví user khi user được tạo hoặc khi user gọi wallet lần đầu.
  - Tạo ví admin/system mặc định currency `VND`.
  - Thiết kế `availableBalance`, `holdBalance`, `totalBalance`.
  - Implement `credit`, `debit`, `hold`, `release`, `capture`, `refund`.
  - Không cho balance âm, không cho capture vượt hold.
  - Mỗi operation tạo ledger transaction có reference, type, status, metadata.
- Acceptance criteria:
  - User xem được ví và lịch sử giao dịch của mình.
  - Admin xem được ví admin và transaction admin.
  - Mọi biến động tiền đều có ledger trace.

## Phase 2 - Payment deposit MVP

- Thời lượng ước tính: 2-3 ngày.
- Actor: user, admin, payment provider, system.
- Mục tiêu: user nạp tiền vào hệ thống, tiền được cộng vào user wallet và admin wallet.
- Checklist implementation:
  - User tạo deposit order với amount, currency, provider.
  - Manual/bank transfer fallback trả thông tin chuyển khoản và mã tham chiếu.
  - Provider callback verify chữ ký/token trước khi xử lý.
  - Callback paid cộng `UserWallet.availableBalance` và `AdminWallet.availableBalance`.
  - Dùng idempotency để callback trùng không cộng tiền lần hai.
- Acceptance criteria:
  - User tạo được lệnh nạp và xem trạng thái.
  - Callback thành công tạo ledger hai phía.
  - Admin wallet phản ánh tổng tiền thật user đã nạp.

## Phase 3 - Withdraw và audit tài chính

- Thời lượng ước tính: 2-3 ngày.
- Actor: user, admin, system.
- Mục tiêu: user rút tiền cần admin duyệt; admin rút từ ví admin phải có audit.
- Checklist implementation:
  - User tạo withdrawal request với amount và bank info.
  - Khi tạo request, hold tiền user từ available sang hold.
  - Admin approve để xác nhận xử lý ngoài hệ thống.
  - Admin reject release hold về user available.
  - Admin mark-paid trừ user hold và trừ `AdminWallet.availableBalance`.
  - Admin withdraw trực tiếp từ admin wallet phải nhập reason và ghi audit log.
- Acceptance criteria:
  - Flow withdraw chạy được từ pending đến paid/rejected.
  - Admin wallet chỉ giảm khi mark-paid hoặc admin withdraw trực tiếp.
  - Có audit/reference đầy đủ cho giao dịch nhạy cảm.

## Phase 4 - Horse và Jockey profile

- Thời lượng ước tính: 2 ngày.
- Actor: owner, jockey, admin.
- Mục tiêu: có hồ sơ ngựa và jockey đủ điều kiện tham gia tournament.
- Checklist implementation:
  - Owner CRUD horse của mình, upload ảnh/tài liệu.
  - Public API xem horse `APPROVED`.
  - Jockey tạo/cập nhật profile, license, avatar, tài liệu, giá thuê, awards, achievements, specialties.
  - Public API xem jockey `APPROVED`/available kèm giá thuê.
  - Admin approve/reject/suspend horse và jockey.
  - Chặn horse/jockey chưa approved khi đăng ký tournament hoặc tạo assignment.
- Acceptance criteria:
  - Horse và jockey có lifecycle rõ từ pending đến approved/suspended.
  - Owner chỉ quản lý ngựa của mình.
  - Jockey profile approved mới xuất hiện cho owner thuê.

## Phase 5 - Owner-Jockey invitation

- Thời lượng ước tính: 1-2 ngày.
- Actor: owner, jockey, admin/system.
- Mục tiêu: owner mời jockey, jockey accept/reject để tạo horse team hợp lệ.
- Checklist implementation:
  - Owner gửi invitation tới jockey approved.
  - Khi tạo invitation, hệ thống hold tiền thuê theo `hirePrice` snapshot.
  - Owner cancel invitation pending thì release toàn bộ tiền hold.
  - Jockey reject invitation pending thì release toàn bộ tiền hold.
  - Jockey accept thì capture hold, credit net cho jockey và credit tax cho admin.
  - Không cho tạo invitation trùng active cho cùng horse/jockey/context.
- Acceptance criteria:
  - Jockey chỉ nhận tiền sau khi accept.
  - Owner cancel khi pending được hoàn lại toàn bộ tiền hold.
  - Invitation accepted tạo quan hệ hợp lệ để dùng ở tournament registration.

## Phase 6 - Tournament setup + round/prize configuration

- Thời lượng ước tính: 2-3 ngày.
- Actor: admin, owner, spectator.
- Mục tiêu: admin tạo tournament đầy đủ thông tin, vòng đấu, min/max horse team và giải thưởng trước khi mở đăng ký.
- Entity/enum/API/service: `Tournament`, `TournamentRound`, `TournamentPrize`, `TournamentStatus`, `TournamentSetupService`.
- Checklist implementation:
  - Admin CRUD tournament với tên, mô tả, địa điểm, thời gian đăng ký, thời gian thi đấu.
  - Cấu hình `entryFee`/deposit amount, registration window và check-in deadline.
  - Cấu hình `minTeams`, `maxTeams` ở tournament và round nếu cần.
  - Cấu hình round structure: vòng loại, bán kết, chung kết hoặc số vòng tùy giải.
  - Cấu hình rule advancement: số đội thắng/đi tiếp mỗi race hoặc mỗi round.
  - Cấu hình prize: hạng, amount/item, payout recipient policy, note.
  - Validate tournament không được publish/open nếu thiếu round config, min/max team hoặc prize config bắt buộc.
  - Public API chỉ thấy tournament đã publish/open/scheduled/ongoing.
- Checklist test:
  - User thường không tạo/sửa tournament.
  - `minTeams` phải lớn hơn 0 và không vượt `maxTeams`.
  - `maxTeams` phải đủ cho round structure đã cấu hình.
  - Entry fee/prize amount không âm.
  - Không open registration khi tournament setup chưa đủ.
- Acceptance criteria:
  - Admin tạo được giải đấu có vòng đấu, min/max đội và giải thưởng rõ ràng.
  - Tournament có thể đi từ draft đến published/open registration.

## Phase 7 - Tournament registration + deposit hold

- Thời lượng ước tính: 2-3 ngày.
- Actor: owner, admin, system.
- Mục tiêu: owner đăng ký horse team vào tournament và hệ thống giữ tiền đặt cọc an toàn qua wallet.
- Entity/enum/API/service: `TournamentRegistration`, `RegistrationStatus`, `TournamentRegistrationService`, wallet integration.
- Checklist implementation:
  - Owner xem tournament đang open hoặc ongoing theo rule public.
  - Owner xem chi tiết tournament trước khi đăng ký nếu muốn.
  - Owner đăng ký horse team gồm horse approved, owner sở hữu horse, jockey assignment accepted.
  - Kiểm tra tournament còn trong registration window và chưa vượt `maxTeams`.
  - Nếu số team approved/pending chưa đạt `minTeams`, tournament vẫn nhận đăng ký nhưng chưa đủ điều kiện schedule.
  - Khi tạo registration, hold `entryFee`/deposit từ ví owner.
  - Gửi email/notification cho owner và jockey khi registration được tạo.
  - Admin approve registration thì capture tiền hold theo policy của tournament.
  - Admin reject registration thì release toàn bộ tiền hold về owner.
  - Chặn duplicate registration cho cùng horse/tournament.
- Checklist test:
  - Không đủ tiền thì không đăng ký được.
  - Horse không approved hoặc không thuộc owner bị chặn.
  - Jockey chưa accept invitation bị chặn.
  - Tournament đóng đăng ký hoặc vượt `maxTeams` bị chặn.
  - Approve capture đúng tiền và reject release đúng tiền.
  - Duplicate registration bị chặn.
- Acceptance criteria:
  - Registration có luồng pending/approved/rejected.
  - Entry fee/deposit có ledger hold/capture/release đầy đủ.
  - Owner nhận notification/email đúng các trạng thái chính.

## Phase 8 - Race scheduling/check-in reminder

- Thời lượng ước tính: 2 ngày.
- Actor: admin, referee, owner, jockey, system.
- Mục tiêu: tạo lịch race từ registration approved, phân công referee và gửi nhắc lịch trước 3 ngày.
- Entity/enum/API/service: `Race`, `RaceParticipant`, `RefereeAssignment`, `RaceScheduleService`, notification/email scheduler.
- Checklist implementation:
  - Admin generate race/heat từ registrations approved theo round config.
  - Chỉ schedule tournament khi số horse team approved đạt `minTeams`.
  - Tạo participant list, gán gate number không trùng trong race.
  - Phân công referee cho race.
  - Kiểm tra jockey/referee không bị trùng lịch cùng thời điểm.
  - Chuyển race sang `SCHEDULED` và tournament/round sang trạng thái phù hợp.
  - Gửi notification/email khi race scheduled cho owner, jockey, referee.
  - Scheduler gửi reminder trước lịch thi đấu 3 ngày.
  - Public/spectator có thể xem lịch race đã publish.
- Checklist test:
  - Registration chưa approved không được schedule.
  - Tournament chưa đủ `minTeams` không được generate race.
  - Gate number duplicate bị chặn.
  - Referee hoặc jockey trùng lịch bị chặn.
  - Reminder 3 ngày chỉ gửi một lần cho cùng race/recipient.
- Acceptance criteria:
  - Race có participant list, gate number, referee và lịch rõ ràng.
  - Owner/jockey/referee nhận email/notification trước ngày thi đấu.

## Phase 9 - Check-in + result recording + round advancement

- Thời lượng ước tính: 2-3 ngày.
- Actor: referee, admin, owner, jockey, system.
- Mục tiêu: hỗ trợ check-in, ghi nhận kết quả, admin xác nhận và hệ thống chọn đội đi tiếp qua từng vòng.
- Entity/enum/API/service: `RaceCheckIn`, `RaceViolation`, `RaceResultDraft`, `RaceResult`, `RoundAdvancement`, `RefereeReport`.
- Checklist implementation:
  - Referee mở check-in cho race đúng trạng thái.
  - Check-in từng participant, ghi chú sức khỏe/giấy tờ/trạng thái.
  - Đánh dấu absent/disqualified nếu rule cho phép.
  - Race chỉ start khi số participant check-in đạt điều kiện tối thiểu.
  - Referee ghi violation, penalty và draft result theo finish order/time.
  - Admin review và approve/reject draft result.
  - Khi result approved, race chuyển `RESULT_CONFIRMED`/`COMPLETED`.
  - Hệ thống dùng advancement rule để chọn winner/qualifier vào round tiếp theo.
  - Nếu còn vòng tiếp theo, hệ thống tạo participant seed cho round kế tiếp để admin schedule tiếp.
  - Gửi notification khi result published và khi team được vào vòng tiếp theo.
- Checklist test:
  - Không start race đã completed/cancelled.
  - Participant chưa check-in bị xử lý đúng rule.
  - Draft result không cho duplicate rank.
  - Admin approve result chỉ một lần.
  - Advancement chọn đúng số team đi tiếp theo rule.
- Acceptance criteria:
  - Kết quả race được xác nhận chính thức.
  - Đội thắng/đi tiếp được tạo cho vòng sau một cách trace được.
  - Quy trình lặp được qua nhiều vòng đến chung kết.

## Phase 10 - Final result + prize payout + tournament statistics

- Thời lượng ước tính: 2-3 ngày.
- Actor: admin, owner, jockey, referee, system.
- Mục tiêu: hoàn tất chung kết, trao giải, payout và cung cấp thống kê tournament cho admin.
- Entity/enum/API/service: `TournamentResult`, `LeaderboardSnapshot`, `PrizePayout`, `TournamentStatistics`, `ResultService`.
- Checklist implementation:
  - Khi vòng cuối completed, hệ thống xác định winner và final ranking.
  - Generate leaderboard snapshot cho tournament.
  - Tính prize theo prize config đã snapshot.
  - Payout prize qua wallet service theo recipient policy.
  - Gửi notification/email cho winner, owner, jockey và admin.
  - Chuyển tournament sang `COMPLETED` khi result và payout hoàn tất hoặc được admin xác nhận.
  - Admin xem statistics: owner/customer count, horse team count, jockey count, referee count, registration count.
  - Admin xem statistics theo round/race: scheduled, completed, cancelled, check-in, absent, disqualified, winner.
  - Admin xem finance statistics: entry fee captured, deposit released/refunded, prize payout, admin/system wallet transaction.
  - Admin export/filter statistics theo tournament, round, race, status và thời gian nếu cần.
- Checklist test:
  - Final result chỉ được confirm một lần.
  - Prize payout không bị trả hai lần khi retry.
  - Leaderboard snapshot không đổi sau khi confirmed nếu không có admin adjustment.
  - Statistics tính đúng số registration, horse team, jockey, referee và dòng tiền.
- Acceptance criteria:
  - Tournament hoàn tất có winner, leaderboard, prize payout và thống kê admin đầy đủ.
  - Mọi payout và fee đều trace được qua ledger.

## Phase 11 - Spectator prediction

- Thời lượng ước tính: 1-2 ngày.
- Actor: spectator, system.
- Mục tiêu: spectator dự đoán miễn phí hoặc nhận reward nhỏ, chưa dùng betting tiền thật.
- Checklist implementation:
  - Spectator chọn participant dự đoán trước giờ race start.
  - Lock prediction khi race bắt đầu.
  - Settle prediction khi result confirmed.
  - Reward nhỏ qua wallet hoặc inventory nếu có policy.
- Acceptance criteria:
  - Prediction hoạt động độc lập với betting và không tạo rủi ro tiền thật.

## Phase 12 - Betting bằng ví

- Thời lượng ước tính: 3-5 ngày.
- Actor: user, admin, system.
- Mục tiêu: đặt cược bằng ví với stake, odds, settle won/lost/refund và feature flag.
- Checklist implementation:
  - Thêm feature flag bật/tắt betting.
  - User đặt bet trước race start, stake bị hold/debit theo policy.
  - Lock bet khi race start.
  - Settle won/lost khi result confirmed.
  - Refund khi race cancelled/result voided.
  - Audit và ledger đầy đủ cho stake, payout, refund.
- Acceptance criteria:
  - Betting có thể tắt hoàn toàn.
  - Khi bật, mọi luồng tiền betting trace được.

## Phase 13 - Item marketplace

- Thời lượng ước tính: 2-3 ngày.
- Actor: user, admin, system.
- Mục tiêu: mua/bán vật phẩm bằng ví và quản lý inventory.
- Checklist implementation:
  - Admin CRUD item và giá.
  - User mua item, ví bị debit và inventory tăng.
  - User bán item nếu policy cho phép, inventory giảm và ví credit.
  - Chặn mua item inactive/out of stock nếu có stock.
- Acceptance criteria:
  - Marketplace có ledger wallet và inventory đồng bộ.

## Phase 14 - Notification/WebSocket/Email

- Thời lượng ước tính: 2 ngày.
- Actor: user, admin, referee, spectator, system.
- Mục tiêu: gửi notification, email và realtime update cho các sự kiện quan trọng.
- Checklist implementation:
  - Notification cho invitation, payment, withdraw, registration, race status, check-in, result, advancement, prize payout.
  - Email cho registration created/approved/rejected, race reminder trước 3 ngày, result published và prize payout.
  - API lấy danh sách notification và mark read.
  - WebSocket topic cho race status/result/leaderboard.
  - Không gửi dữ liệu nhạy cảm qua public topic.
  - Scheduler/idempotency để reminder không gửi trùng.
- Acceptance criteria:
  - Người dùng nhận update cần thiết mà không phải refresh liên tục.
  - Reminder trước 3 ngày được gửi đúng recipient và không bị duplicate.

## Phase 15 - Production hardening

- Thời lượng ước tính: 3-5 ngày.
- Actor: admin, developer, operator.
- Mục tiêu: làm chắc backend trước khi production hoặc demo lớn.
- Checklist implementation:
  - Dùng Flyway/Liquibase cho schema migration.
  - Pagination/sorting/filtering cho list API quan trọng.
  - Rate limit cho auth, payment callback, betting, withdrawal và registration.
  - Payment callback security: signature, timestamp, replay protection, IP allowlist nếu provider hỗ trợ.
  - Audit log cho admin action, tournament setup, result approval và money movement.
  - Logging có correlation id/reference id.
  - Monitoring/health check cho DB/payment provider/email scheduler.
  - Rà soát index DB cho query list và lookup theo reference/status.
- Acceptance criteria:
  - Backend deploy được với migration rõ ràng.
  - Log/audit đủ điều tra sự cố.
  - Test coverage tốt cho các flow rủi ro cao.

## Checklist kiểm tra sau khi viết tài liệu

- Markdown render đúng tiếng Việt UTF-8.
- File mới có đủ 4 luồng nghiệp vụ chính.
- Phase 6-10 khớp flow tournament mới.
- Rule `horse team`, `minTeams`, `maxTeams`, `hold -> capture/release` được ghi rõ.
- Notification/email trước lịch thi đấu 3 ngày được ghi rõ.
- Các phần wallet, deposit, withdraw, betting, marketplace, production hardening giữ nguyên định hướng cũ.
- Không sửa `docs/horse-racing-phase-implementation-plan.md`.
- Không sửa `docs/role-screen-functions.md`.
- Không chạy backend test vì thay đổi chỉ là tài liệu.
