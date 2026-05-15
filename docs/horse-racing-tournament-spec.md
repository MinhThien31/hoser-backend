# Horse Racing Tournament Management System - BA Spec

## 1. Mục tiêu sản phẩm

Hệ thống quản lý giải đua ngựa cần giải quyết 5 nhóm việc chính:

1. Quản lý người dùng và phân quyền: Admin, Horse Owner, Jockey, Race Referee, Spectator.
2. Quản lý tài sản thi đấu: ngựa, jockey, hồ sơ chủ ngựa, đăng ký tham gia giải.
3. Quản lý giải đấu: tournament, vòng đua, race, lịch thi đấu, phân công trọng tài.
4. Quản lý kết quả: kiểm tra trước race, vi phạm, biên bản, kết quả chính thức, bảng xếp hạng, tiền thưởng.
5. Quản lý tài chính: ví người dùng, nạp tiền, rút tiền, phí đăng ký, cược/dự đoán, thưởng dự đoán, thưởng giải đấu, vật phẩm.

Repo hiện tại đã có nền tảng tốt cho Phase 0:

- Auth: register, login, me, logout, forgot/reset password, Google/Facebook login.
- User: lấy thông tin user, đổi password, deactivate/activate, đổi role.
- Security: JWT, role enum gồm USER, OWNER, ADMIN, JOCKEY, SPECTATOR, REFEREE.
- WebSocket dependency đã có, phù hợp cho live result/notification sau này.

## 2. Nguyên tắc thiết kế luồng

### 2.1. Tách rõ 3 loại tiền

Không nên cộng/trừ tiền trực tiếp vào `User.balance` đơn giản. Nên dùng ví + sổ cái giao dịch.

- `Wallet.availableBalance`: tiền có thể dùng/rút.
- `Wallet.holdBalance`: tiền đang bị giữ để chờ kết quả, ví dụ tiền cược đang pending hoặc tiền rút đang chờ duyệt.
- `WalletLedger`: lịch sử bất biến của mọi biến động tiền.

Rule quan trọng: mọi giao dịch tiền phải có idempotency key hoặc reference để tránh cộng/trừ trùng khi retry API.

### 2.2. Tách prediction và betting

Trong đề bài có "dự đoán kết quả". Bạn có thể triển khai 2 mức:

- Prediction miễn phí: khán giả chọn ngựa thắng, nhận điểm/voucher/vật phẩm.
- Bet có tiền: khán giả dùng ví đặt tiền, thắng nhận tiền. Luồng này nhạy cảm hơn, cần rule chặt, audit log, KYC/rút tiền, và tùy khu vực có thể liên quan pháp lý.

Gợi ý production: Phase đầu làm `Prediction` miễn phí kết hợp bật `Bet` tiền thật bằng feature flag.

### 2.3. Mọi nghiệp vụ chính nên có status machine

Không dùng boolean rời rạc như `isApproved`, `isConfirmed`, `isFinished` nếu flow có nhiều bước. Dùng enum status giúp code dễ hơn, API rõ hơn, test dễ hơn.

Ví dụ:

- Tournament: `DRAFT`, `OPEN_REGISTRATION`, `REGISTRATION_CLOSED`, `SCHEDULED`, `ONGOING`, `COMPLETED`, `CANCELLED`.
- Registration: `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`, `CANCELLED`.
- Race: `DRAFT`, `SCHEDULED`, `CHECK_IN_OPEN`, `READY`, `ONGOING`, `PENDING_RESULT`, `RESULT_CONFIRMED`, `CANCELLED`.
- JockeyAssignment: `INVITED`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `CONFIRMED_BY_OWNER`.
- Bet: `PENDING`, `CONFIRMED`, `LOCKED`, `WON`, `LOST`, `REFUNDED`, `CANCELLED`, `SETTLED`.
- WalletTransaction: `PENDING`, `SUCCESS`, `FAILED`, `CANCELLED`, `REVERSED`.
- WithdrawalRequest: `PENDING`, `APPROVED`, `REJECTED`, `PAID`, `CANCELLED`.

## 3. Role và quyền truy cập

| Role      | Mục tiêu chính                  | Quyền chính                                                                                                      |
| --------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| USER      | Tài khoản mới chưa chọn vai trò | Chọn role, cập nhật profile                                                                                      |
| OWNER     | Chủ ngựa                        | Quản lý ngựa, đăng ký giải, mời/chọn jockey, xác nhận race, xem thưởng                                           |
| JOCKEY    | Người điều khiển ngựa           | Nhận/từ chối lời mời, xem race được phân công, xem thành tích                                                    |
| REFEREE   | Trọng tài                       | Check-in ngựa, ghi vi phạm, lập biên bản, xác nhận kết quả                                                       |
| SPECTATOR | Khán giả                        | Xem giải/lịch/kết quả, dự đoán/cược, nhận thưởng                                                                 |
| ADMIN     | Ban tổ chức                     | Quản trị toàn hệ thống, tạo giải, duyệt đăng ký, lập lịch, phân công trọng tài, công bố kết quả, duyệt tài chính |

Gợi ý cải tiến: một user có thể cần nhiều role, ví dụ vừa là spectator vừa là owner. Hiện code đang dùng 1 role trong `User`. Phase đầu giữ 1 role để đơn giản; phase production nên cân nhắc bảng `user_roles`.

## 4. Domain model đề xuất

### 4.1. Identity

`User`

- `id`, `username`, `email`, `fullName`, `phone`, `password`, `role`, `active`, `avatarUrl`, `provider`, `location`, `createdAt`
- Đã có trong repo.

`UserProfile`

- `userId`
- `dateOfBirth`
- `identityNumber`
- `kycStatus`: `NOT_SUBMITTED`, `PENDING`, `APPROVED`, `REJECTED`
- `bankAccountInfo` hoặc tách bảng riêng

### 4.2. Horse owner và jockey

`Horse`

- `id`
- `ownerId`
- `name`
- `breed`
- `gender`
- `dateOfBirth`
- `color`
- `height`
- `weight`
- `healthStatus`
- `registrationNumber`
- `avatarUrl`
- `status`: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `RETIRED`
- `createdAt`, `updatedAt`

`JockeyProfile`

- `id`
- `userId`
- `licenseNumber`
- `height`
- `weight`
- `experienceYears`
- `bio`
- `status`: `AVAILABLE`, `BUSY`, `SUSPENDED`, `INACTIVE`

`JockeyContract` hoặc `OwnerJockey`

- `id`
- `ownerId`
- `jockeyId`
- `status`: `INVITED`, `ACCEPTED`, `REJECTED`, `ENDED`
- `startDate`, `endDate`

`RaceJockeyAssignment`

- `id`
- `raceId`
- `horseId`
- `ownerId`
- `jockeyId`
- `status`: `INVITED`, `ACCEPTED`, `REJECTED`, `CONFIRMED_BY_OWNER`, `CANCELLED`

### 4.3. Tournament và race

`Tournament`

- `id`
- `name`
- `description`
- `location`
- `startDate`
- `endDate`
- `registrationOpenAt`
- `registrationCloseAt`
- `maxHorses`
- `entryFee`
- `prizePool`
- `status`
- `createdBy`

`TournamentRound`

- `id`
- `tournamentId`
- `name`
- `roundOrder`
- `qualificationRule`
- `status`

`Race`

- `id`
- `tournamentId`
- `roundId`
- `name`
- `raceNo`
- `distance`
- `trackType`
- `scheduledAt`
- `maxParticipants`
- `status`
- `refereeId`

`RaceParticipant`

- `id`
- `raceId`
- `horseId`
- `ownerId`
- `jockeyId`
- `gateNumber`
- `checkInStatus`: `PENDING`, `PASSED`, `FAILED`, `ABSENT`
- `finalStatus`: `REGISTERED`, `READY`, `RACING`, `FINISHED`, `DISQUALIFIED`, `WITHDRAWN`

### 4.4. Đăng ký và duyệt

`TournamentRegistration`

- `id`
- `tournamentId`
- `ownerId`
- `horseId`
- `preferredJockeyId`
- `status`: `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`
- `entryFeeTransactionId`
- `note`
- `submittedAt`, `reviewedAt`, `reviewedBy`

### 4.5. Kết quả, vi phạm, bảng xếp hạng

`RaceResult`

- `id`
- `raceId`
- `participantId`
- `horseId`
- `jockeyId`
- `rank`
- `finishTimeMillis`
- `point`
- `prizeAmount`
- `status`: `DRAFT`, `CONFIRMED`, `DISQUALIFIED`
- `confirmedBy`
- `confirmedAt`

`RaceViolation`

- `id`
- `raceId`
- `participantId`
- `refereeId`
- `type`
- `description`
- `penalty`
- `createdAt`

`RefereeReport`

- `id`
- `raceId`
- `refereeId`
- `summary`
- `weather`
- `trackCondition`
- `status`: `DRAFT`, `SUBMITTED`, `APPROVED`
- `submittedAt`

`LeaderboardSnapshot`

- `id`
- `tournamentId`
- `roundId`
- `horseId`
- `jockeyId`
- `ownerId`
- `totalPoint`
- `totalPrize`
- `rank`
- `generatedAt`

### 4.6. Dự đoán, cược và ví

`Wallet`

- `id`
- `userId`
- `currency`: `VND`, `USD`, hoặc coin nội bộ
- `availableBalance`
- `holdBalance`
- `status`: `ACTIVE`, `LOCKED`, `CLOSED`
- `createdAt`, `updatedAt`

`WalletTransaction`

- `id`
- `walletId`
- `userId`
- `type`: `DEPOSIT`, `WITHDRAW`, `ENTRY_FEE`, `BET_STAKE`, `BET_PAYOUT`, `PRIZE_PAYOUT`, `ITEM_PURCHASE`, `ITEM_SALE`, `REFUND`, `ADJUSTMENT`
- `direction`: `CREDIT`, `DEBIT`
- `amount`
- `balanceBefore`
- `balanceAfter`
- `status`
- `referenceType`
- `referenceId`
- `idempotencyKey`
- `note`
- `createdAt`

`PaymentOrder`

- `id`
- `userId`
- `walletId`
- `provider`: `VNPAY`, `MOMO`, `PAYPAL`, `MANUAL`, `BANK_TRANSFER`
- `amount`
- `status`: `CREATED`, `PENDING`, `PAID`, `FAILED`, `EXPIRED`, `CANCELLED`
- `providerTransactionId`
- `paymentUrl`
- `createdAt`, `paidAt`

`WithdrawalRequest`

- `id`
- `userId`
- `walletId`
- `amount`
- `bankName`
- `bankAccountNo`
- `bankAccountName`
- `status`: `PENDING`, `APPROVED`, `REJECTED`, `PAID`, `CANCELLED`
- `holdTransactionId`
- `processedBy`
- `processedAt`

`Prediction`

- `id`
- `raceId`
- `userId`
- `predictedHorseId`
- `status`: `OPEN`, `LOCKED`, `WON`, `LOST`, `CANCELLED`
- `rewardType`
- `rewardAmount`
- `createdAt`, `settledAt`

`Bet`

- `id`
- `raceId`
- `userId`
- `horseId`
- `stakeAmount`
- `odds`
- `potentialPayout`
- `status`
- `stakeTransactionId`
- `payoutTransactionId`
- `createdAt`, `settledAt`

`Item`

- `id`
- `name`
- `type`
- `price`
- `status`

`InventoryItem`

- `id`
- `userId`
- `itemId`
- `quantity`

## 5. Phase triển khai đề xuất

## Phase 0 - Foundation đã có, cần siết lại

Mục tiêu: làm nền ổn trước khi code domain lớn.

Việc nên hoàn thiện:

- Chuẩn hóa `User.Phone` thành `phone` để tránh lệch naming Java.
- Chuẩn hóa API response và error code.
- Bổ sung role guard bằng `@PreAuthorize`.
- Thêm audit fields: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`.
- Tạo seed admin.
- Tạo Swagger grouping theo module: Auth, User, Horse, Tournament, Race, Wallet.
- Thêm migration tool như Flyway hoặc Liquibase trước production.

Acceptance criteria:

- User đăng ký, login, chọn role, gọi `/me` ổn định.
- Admin có thể khóa/mở tài khoản và đổi role.
- API lỗi trả format thống nhất.
- Swagger chạy được cho toàn bộ API.

## Phase 1 - User, role và profile theo nghiệp vụ

Mục tiêu: biến user chung thành actor nghiệp vụ.

Luồng 1: đăng ký Horse Owner

1. User register/login.
2. User chọn role `OWNER`.
3. Hệ thống tạo owner profile mặc định.
4. Owner cập nhật thông tin cá nhân.
5. Nếu sau này có nạp/rút tiền thật, yêu cầu KYC trước khi withdraw.

Luồng 2: đăng ký Jockey

1. User register/login.
2. User chọn role `JOCKEY`.
3. Jockey tạo `JockeyProfile` gồm license, chiều cao, cân nặng, kinh nghiệm.
4. Admin duyệt hoặc hệ thống cho pending nếu cần xác minh giấy phép.

Luồng 3: Spectator

1. User register/login.
2. User chọn role `SPECTATOR`.
3. Hệ thống tạo ví và profile spectator.
4. Spectator có thể xem public tournament/race.

Endpoint gợi ý:

- `POST /api/v1/auth/register`
- `PUT /api/v1/auth/me/role`
- `GET /api/v1/users/me/profile`
- `PUT /api/v1/users/me/profile`
- `POST /api/v1/jockey-profiles`
- `GET /api/v1/jockey-profiles/me`
- `PUT /api/v1/jockey-profiles/me`
- `PUT /api/v1/admin/jockey-profiles/{id}/status`

## Phase 2 - Quản lý ngựa

Mục tiêu: owner có tài sản thi đấu rõ ràng trước khi đăng ký giải.

Luồng chính:

1. Owner tạo hồ sơ ngựa.
2. Owner upload ảnh/giấy tờ nếu có.
3. Owner cập nhật thông tin ngựa.
4. Admin có thể khóa ngựa nếu vi phạm hoặc thiếu hồ sơ.
5. Owner xem danh sách ngựa của mình.

Business rules:

- Chỉ `OWNER` được tạo ngựa.
- Owner chỉ sửa ngựa thuộc sở hữu của mình.
- Ngựa `SUSPENDED` hoặc `RETIRED` không được đăng ký giải.
- Tên ngựa có thể trùng, nhưng `registrationNumber` nên unique nếu có.

Endpoint gợi ý:

- `POST /api/v1/horses`
- `GET /api/v1/horses/me`
- `GET /api/v1/horses/{id}`
- `PUT /api/v1/horses/{id}`
- `DELETE /api/v1/horses/{id}` hoặc soft delete
- `PUT /api/v1/admin/horses/{id}/status`

## Phase 3 - Jockey marketplace và lời mời

Mục tiêu: owner có thể chọn/mời jockey trước khi tham gia race.

Luồng owner mời jockey:

1. Owner xem danh sách jockey khả dụng.
2. Owner gửi lời mời cho jockey.
3. Jockey nhận notification.
4. Jockey accept/reject.
5. Nếu accept, owner có thể chọn jockey đó cho horse trong registration/race.

Business rules:

- Một jockey không được nhận 2 race trùng giờ.
- Jockey bị `SUSPENDED` không thể accept.
- Owner không được confirm jockey nếu jockey chưa accept.

Endpoint gợi ý:

- `GET /api/v1/jockeys?status=AVAILABLE`
- `POST /api/v1/owner-jockey-invitations`
- `GET /api/v1/jockey-invitations/me`
- `PUT /api/v1/jockey-invitations/{id}/accept`
- `PUT /api/v1/jockey-invitations/{id}/reject`
- `GET /api/v1/owners/me/jockeys`

## Phase 4 - Tournament setup bởi Admin

Mục tiêu: admin tạo giải, mở đăng ký, quản lý vòng và race.

Luồng admin tạo tournament:

1. Admin tạo tournament ở trạng thái `DRAFT`.
2. Admin cấu hình thời gian đăng ký, phí tham gia, prize pool, số ngựa tối đa.
3. Admin tạo round: vòng loại, bán kết, chung kết.
4. Admin chuyển trạng thái sang `OPEN_REGISTRATION`.
5. Owner mới có thể gửi đăng ký.

Business rules:

- Chỉ admin được tạo/sửa tournament.
- Không được sửa `entryFee` sau khi đã có registration approved, trừ khi có policy refund.
- `registrationCloseAt` phải trước `startDate`.
- Tournament chỉ mở public khi status không còn `DRAFT`.

Endpoint gợi ý:

- `POST /api/v1/admin/tournaments`
- `PUT /api/v1/admin/tournaments/{id}`
- `PUT /api/v1/admin/tournaments/{id}/open-registration`
- `PUT /api/v1/admin/tournaments/{id}/close-registration`
- `GET /api/v1/tournaments`
- `GET /api/v1/tournaments/{id}`
- `POST /api/v1/admin/tournaments/{id}/rounds`
- `GET /api/v1/tournaments/{id}/rounds`

## Phase 5 - Registration và phí tham gia

Mục tiêu: owner đăng ký ngựa vào giải, admin duyệt, tiền phí được xử lý qua ví.

Luồng có phí:

1. Owner chọn tournament đang `OPEN_REGISTRATION`.
2. Owner chọn horse đủ điều kiện.
3. Owner chọn jockey đã accept hoặc để pending.
4. Hệ thống kiểm tra ví đủ tiền entry fee.
5. Hệ thống trừ ví hoặc hold tiền entry fee.
6. Registration tạo trạng thái `PENDING`.
7. Admin duyệt: status thành `APPROVED`, tiền entry fee thành success.
8. Admin từ chối: status thành `REJECTED`, hoàn tiền nếu đã hold/trừ.

Business rules:

- Mỗi horse chỉ có một registration active trong một tournament.
- Registration sau deadline bị từ chối.
- Nếu tournament full, không cho tạo thêm pending hoặc cho waitlist tùy policy.
- Nếu owner withdraw trước khi admin duyệt, hoàn tiền.
- Nếu owner withdraw sau khi approved, cần rule refund riêng.

Endpoint gợi ý:

- `POST /api/v1/tournaments/{id}/registrations`
- `GET /api/v1/owners/me/registrations`
- `GET /api/v1/admin/tournaments/{id}/registrations`
- `PUT /api/v1/admin/registrations/{id}/approve`
- `PUT /api/v1/admin/registrations/{id}/reject`
- `PUT /api/v1/registrations/{id}/withdraw`

## Phase 6 - Lập lịch race và phân công trọng tài

Mục tiêu: từ danh sách registration approved, admin tạo race có participant, jockey, referee.

Luồng admin lập lịch:

1. Admin đóng đăng ký.
2. Admin chọn danh sách registration approved.
3. Admin tạo race cho từng round.
4. Hệ thống gán gate number hoặc admin nhập thủ công.
5. Admin phân công referee.
6. Owner xác nhận horse tham gia race.
7. Jockey xác nhận assignment race.
8. Race chuyển `SCHEDULED`.

Business rules:

- Một horse không được có 2 race trùng giờ.
- Một jockey không được có 2 race trùng giờ.
- Một referee có thể quản nhiều race nếu không trùng giờ.
- Race chỉ `READY` khi đủ: participants, jockey accepted, owner confirmed, referee assigned.

Endpoint gợi ý:

- `POST /api/v1/admin/races`
- `PUT /api/v1/admin/races/{id}`
- `POST /api/v1/admin/races/{id}/participants`
- `PUT /api/v1/admin/races/{id}/assign-referee`
- `PUT /api/v1/races/{id}/owner-confirm`
- `PUT /api/v1/races/{id}/jockey-confirm`
- `GET /api/v1/races`
- `GET /api/v1/races/{id}`
- `GET /api/v1/owners/me/races`
- `GET /api/v1/jockeys/me/races`
- `GET /api/v1/referees/me/races`

## Phase 7 - Race day: check-in, live tracking, vi phạm

Mục tiêu: trọng tài quản lý race thực tế.

Luồng check-in:

1. Referee mở check-in.
2. Referee kiểm tra từng ngựa: sức khỏe, giấy tờ, jockey.
3. Participant status: `PASSED`, `FAILED`, hoặc `ABSENT`.
4. Race chỉ được start khi số participant passed đạt điều kiện tối thiểu.

Luồng race:

1. Referee chuyển race sang `ONGOING`.
2. Hệ thống khóa prediction/bet.
3. Referee ghi nhận vi phạm nếu có.
4. Referee nhập kết quả nháp.
5. Race chuyển `PENDING_RESULT`.

Realtime gợi ý:

- WebSocket topic: `/topic/races/{raceId}/status`
- WebSocket topic: `/topic/races/{raceId}/results`
- WebSocket topic: `/topic/tournaments/{tournamentId}/leaderboard`

Endpoint gợi ý:

- `PUT /api/v1/referee/races/{id}/open-check-in`
- `PUT /api/v1/referee/race-participants/{id}/check-in`
- `PUT /api/v1/referee/races/{id}/start`
- `POST /api/v1/referee/races/{id}/violations`
- `POST /api/v1/referee/races/{id}/draft-results`
- `POST /api/v1/referee/races/{id}/report`

## Phase 8 - Xác nhận kết quả, xếp hạng và thưởng

Mục tiêu: kết quả được kiểm duyệt rồi công bố chính thức.

Luồng kết quả:

1. Referee submit draft result và report.
2. Admin xem lại kết quả.
3. Admin yêu cầu sửa hoặc approve.
4. Khi approve, race status thành `RESULT_CONFIRMED`.
5. Hệ thống cập nhật leaderboard.
6. Hệ thống tính prize cho owner/horse/jockey nếu có.
7. Hệ thống settle prediction/bet.
8. Notification gửi cho owner, jockey, spectator.

Business rules:

- Chỉ settle bet/prediction sau khi result confirmed.
- Nếu race cancelled, hoàn tiền bet và có policy hoàn entry fee.
- Nếu participant bị disqualified, không nhận rank/prize.
- Leaderboard nên generate snapshot để lịch sử không bị thay đổi khi sửa dữ liệu hồ sơ.

Endpoint gợi ý:

- `GET /api/v1/admin/races/{id}/draft-results`
- `PUT /api/v1/admin/races/{id}/results/approve`
- `PUT /api/v1/admin/races/{id}/results/request-changes`
- `GET /api/v1/races/{id}/results`
- `GET /api/v1/tournaments/{id}/leaderboard`
- `GET /api/v1/owners/me/prizes`
- `GET /api/v1/jockeys/me/performance`

## Phase 9 - Spectator prediction và bet

Mục tiêu: khán giả xem lịch, dự đoán, theo dõi kết quả dự đoán.

Luồng prediction miễn phí:

1. Spectator xem race `SCHEDULED`.
2. Spectator chọn predicted horse trước giờ khóa.
3. Khi race start, prediction bị lock.
4. Khi result confirmed, hệ thống đánh dấu won/lost.
5. Nếu won, phát reward vào ví hoặc inventory.

Luồng bet bằng ví:

1. Spectator nạp tiền vào ví.
2. Spectator chọn race đang mở bet.
3. Spectator chọn horse, nhập số tiền.
4. Hệ thống kiểm tra số dư available.
5. Hệ thống trừ/hold stake.
6. Race start thì bet locked.
7. Result confirmed thì hệ thống settle: lost mất stake, won nhận payout.

Business rules:

- Không cho bet sau `betCloseAt` hoặc khi race không còn `SCHEDULED`.
- User không được bet nếu ví locked.
- Stake min/max cần cấu hình.
- Odds có thể fixed odds hoặc pool betting. Phase đầu nên fixed odds đơn giản.
- Mọi payout qua `WalletTransaction`, không update balance trực tiếp.

Endpoint gợi ý:

- `POST /api/v1/races/{id}/predictions`
- `GET /api/v1/users/me/predictions`
- `GET /api/v1/races/{id}/bet-options`
- `POST /api/v1/races/{id}/bets`
- `GET /api/v1/users/me/bets`
- `GET /api/v1/bets/{id}`

## Phase 10 - Wallet, deposit, withdraw, item marketplace

Mục tiêu: tất cả tiền đi qua ví, có lịch sử rõ ràng và admin kiểm soát rút tiền.

### 10.1. Tạo ví

Luồng:

1. Khi user register thành công, hệ thống tạo ví mặc định.
2. Nếu user cũ chưa có ví, tạo ví khi lần đầu gọi `/wallets/me`.
3. Một user có thể có nhiều ví nếu nhiều currency, nhưng phase đầu nên 1 ví VND.

Endpoint:

- `GET /api/v1/wallets/me`
- `GET /api/v1/wallets/me/transactions`

### 10.2. Nạp tiền

Luồng online payment:

1. User tạo payment order.
2. Hệ thống trả payment URL hoặc thông tin chuyển khoản.
3. Payment provider callback.
4. Backend verify callback.
5. Nếu paid, tạo transaction `DEPOSIT/CREDIT/SUCCESS`.
6. Cộng `availableBalance`.
7. Gửi notification.

Endpoint:

- `POST /api/v1/wallets/me/deposit-orders`
- `GET /api/v1/wallets/me/deposit-orders/{id}`
- `POST /api/v1/payments/{provider}/callback`

### 10.3. Rút tiền

Luồng:

1. User gửi withdrawal request.
2. Hệ thống kiểm tra KYC nếu áp dụng.
3. Hệ thống chuyển tiền từ available sang hold.
4. Admin duyệt hoặc từ chối.
5. Nếu duyệt và đã chuyển khoản, request thành `PAID`, hold bị trừ hẳn.
6. Nếu từ chối, tiền từ hold quay lại available.

Endpoint:

- `POST /api/v1/wallets/me/withdrawals`
- `GET /api/v1/wallets/me/withdrawals`
- `GET /api/v1/admin/withdrawals`
- `PUT /api/v1/admin/withdrawals/{id}/approve`
- `PUT /api/v1/admin/withdrawals/{id}/reject`
- `PUT /api/v1/admin/withdrawals/{id}/mark-paid`

### 10.4. Mua/bán vật phẩm

Luồng mua:

1. User xem shop item.
2. User mua item bằng ví.
3. Hệ thống trừ tiền, tạo inventory item.

Luồng bán:

1. User chọn item trong inventory.
2. Hệ thống tính giá bán lại.
3. Hệ thống giảm inventory, cộng tiền vào ví.

Endpoint:

- `GET /api/v1/items`
- `POST /api/v1/items/{id}/purchase`
- `GET /api/v1/users/me/inventory`
- `POST /api/v1/inventory/{id}/sell`
- `POST /api/v1/admin/items`
- `PUT /api/v1/admin/items/{id}`

## 6. SpecKit triển khai cho từng module

Bạn có thể dùng checklist này trước khi code mỗi module.

### 6.1. Module template

Mỗi module nên có:

- Entity + enum status.
- Repository.
- DTO request/response.
- Mapper.
- Service interface.
- Service impl.
- Controller.
- Validation.
- Security rule.
- Unit test service.
- Integration test controller.
- Swagger annotation.

### 6.2. Package gợi ý

```text
com.minhthien.hoser_backend
  config
  controller
    auth
    user
    horse
    jockey
    tournament
    race
    wallet
    admin
  dto
    request
    response
  entity
  enums
  exception
  mapper
  repository
  security
  service
    impl
  websocket
```

Vì repo hiện đang để controller chung trong `controller`, bạn có thể giữ cấu trúc hiện tại ở phase đầu. Khi số file tăng, hãy tách subpackage.

### 6.3. API response chuẩn

Gợi ý format:

```json
{
  "success": true,
  "message": "Race result confirmed",
  "data": {},
  "timestamp": "2026-05-15T14:00:00"
}
```

Error:

```json
{
  "success": false,
  "message": "Wallet balance is insufficient",
  "errorCode": "WALLET_INSUFFICIENT_BALANCE",
  "details": [],
  "timestamp": "2026-05-15T14:00:00"
}
```

### 6.4. Error code gợi ý

- `AUTH_UNAUTHORIZED`
- `AUTH_FORBIDDEN`
- `USER_NOT_FOUND`
- `HORSE_NOT_FOUND`
- `HORSE_NOT_OWNED_BY_USER`
- `TOURNAMENT_NOT_FOUND`
- `TOURNAMENT_REGISTRATION_CLOSED`
- `REGISTRATION_DUPLICATED`
- `RACE_NOT_FOUND`
- `RACE_ALREADY_STARTED`
- `JOCKEY_TIME_CONFLICT`
- `REFEREE_TIME_CONFLICT`
- `WALLET_NOT_FOUND`
- `WALLET_LOCKED`
- `WALLET_INSUFFICIENT_BALANCE`
- `PAYMENT_CALLBACK_INVALID`
- `BET_CLOSED`
- `BET_ALREADY_SETTLED`

## 7. Thứ tự code khuyến nghị

Để dễ code và ít bị vỡ luồng, nên đi theo thứ tự này:

1. Fix nền tảng user/auth/security.
2. Wallet cơ bản: wallet, transaction, get balance, internal credit/debit service.
3. Horse CRUD.
4. Jockey profile + owner-jockey invitation.
5. Tournament CRUD + round.
6. Registration + entry fee qua wallet.
7. Race scheduling + participant + referee assignment.
8. Referee check-in + violation + report.
9. Race result + leaderboard + prize payout.
10. Prediction miễn phí.
11. Bet bằng ví.
12. Deposit/withdraw thật qua payment provider.
13. Item marketplace.
14. Notification/WebSocket realtime.
15. Admin dashboard/reporting.

Lý do wallet nên làm sớm: registration fee, bet, prize, item đều phụ thuộc cùng một cơ chế tiền. Nếu để cuối, sau này phải sửa nhiều module.

## 8. Luồng tổng thể end-to-end

### 8.1. Luồng giải đấu chuẩn

1. Admin tạo tournament.
2. Admin tạo round và cấu hình rule.
3. Admin mở đăng ký.
4. Owner tạo horse.
5. Owner mời/chọn jockey.
6. Owner nạp tiền nếu tournament có entry fee.
7. Owner đăng ký horse vào tournament.
8. Admin duyệt registration.
9. Admin đóng đăng ký.
10. Admin lập race và phân công referee.
11. Owner xác nhận horse tham gia race.
12. Jockey xác nhận điều khiển horse.
13. Referee check-in trước race.
14. Race bắt đầu, prediction/bet bị lock.
15. Referee nhập kết quả và biên bản.
16. Admin xác nhận kết quả.
17. Hệ thống cập nhật leaderboard.
18. Hệ thống trả prize, settle prediction/bet.
19. User xem kết quả, ví và lịch sử thưởng.

### 8.2. Luồng tiền chuẩn

1. User có wallet.
2. User nạp tiền: deposit order -> provider callback -> wallet credit.
3. User dùng tiền:
   - Entry fee: wallet debit hoặc hold.
   - Bet stake: wallet hold/debit.
   - Item purchase: wallet debit.
4. User nhận tiền:
   - Prize payout.
   - Bet payout.
   - Item sale.
   - Refund.
5. User rút tiền: withdrawal request -> hold balance -> admin approve -> paid.

## 9. Chính sách trạng thái quan trọng

### 9.1. Tournament

```text
DRAFT
 -> OPEN_REGISTRATION
 -> REGISTRATION_CLOSED
 -> SCHEDULED
 -> ONGOING
 -> COMPLETED

Any state -> CANCELLED
```

### 9.2. Race

```text
DRAFT
 -> SCHEDULED
 -> CHECK_IN_OPEN
 -> READY
 -> ONGOING
 -> PENDING_RESULT
 -> RESULT_CONFIRMED

Any pre-result state -> CANCELLED
```

### 9.3. Wallet withdrawal

```text
PENDING
 -> APPROVED
 -> PAID

PENDING -> REJECTED
PENDING -> CANCELLED
```

## 10. Production checklist

### 10.1. Backend

- Dùng migration: Flyway/Liquibase.
- Không dùng `ddl-auto=update` ở production.
- Tất cả tiền dùng `BigDecimal`, không dùng `double`.
- Transaction tiền phải có `@Transactional`.
- Wallet update cần lock hoặc optimistic locking để tránh race condition.
- Mọi payment callback phải verify chữ ký provider.
- Idempotency cho deposit callback, withdraw, bet settle, prize payout.
- Log audit cho admin action.
- Pagination cho list API.
- Không trả entity trực tiếp, chỉ trả response DTO.
- Không lưu secret trong `application.properties` commit lên git.

### 10.2. Security

- `@PreAuthorize` theo role.
- CORS cấu hình theo domain frontend thật.
- Rate limit login, forgot password, payment callback.
- Refresh token nếu app cần login lâu.
- KYC trước withdraw nếu dùng tiền thật.
- Admin action cần audit.

### 10.3. Data integrity

- Unique constraint:
  - `users.email`
  - `users.username`
  - `wallet.user_id + currency`
  - `tournament_registration.tournament_id + horse_id`
  - `race_participant.race_id + horse_id`
  - `wallet_transaction.idempotency_key`
- Index:
  - `race.scheduled_at`
  - `race.tournament_id`
  - `wallet_transaction.user_id, created_at`
  - `bet.race_id, status`
  - `prediction.race_id, status`

### 10.4. Testing

- Auth service test.
- Role permission test.
- Horse ownership test.
- Registration duplicate test.
- Wallet insufficient balance test.
- Wallet concurrent debit test.
- Race result confirmation test.
- Bet settlement idempotency test.
- Payment callback duplicate test.

## 11. Những điểm đề bài còn thiếu nên bổ sung

1. Chính sách phí:
   - Entry fee có không?
   - Refund khi owner rút đăng ký?
   - Refund khi race/tournament bị hủy?

2. Chính sách prize:
   - Thưởng cho owner, jockey, hay cả hai?
   - Chia theo rank cố định hay phần trăm prize pool?
   - Có thu phí nền tảng không?

3. Luật xếp hạng:
   - Theo điểm từng race?
   - Theo thời gian hoàn thành?
   - DNF/DQ tính thế nào?

4. Luật jockey:
   - Jockey có cần license được admin duyệt không?
   - Jockey có được tự đăng ký vào race không, hay phải qua owner?

5. Luật prediction/bet:
   - Dự đoán miễn phí hay cược tiền?
   - Một user được dự đoán mấy lần cho một race?
   - Có cho sửa dự đoán trước giờ khóa không?
   - Tỷ lệ payout tính fixed odds hay pool?

6. Luật thanh toán:
   - Dùng provider nào: VNPay, MoMo, PayPal, Stripe, chuyển khoản thủ công?
   - Có KYC không?
   - Min/max deposit/withdraw?
   - Admin duyệt rút tiền thủ công hay tự động?

7. Notification:
   - Email, in-app, WebSocket, push notification?
   - Những event nào cần gửi thông báo?

8. Media:
   - Ảnh ngựa, giấy phép jockey, ảnh biên bản có upload Cloudinary không?

## 12. MVP nên chốt

Nếu mục tiêu là nhanh có sản phẩm demo tốt, MVP nên gồm:

1. Auth/user/role.
2. Wallet nội bộ, chưa tích hợp cổng thanh toán thật.
3. Owner CRUD horse.
4. Jockey profile và invitation.
5. Admin CRUD tournament/race.
6. Owner registration.
7. Admin approve registration.
8. Referee submit result.
9. Admin confirm result.
10. Public leaderboard.
11. Prediction miễn phí.

Sau MVP mới mở rộng:

- Deposit/withdraw thật.
- Bet tiền thật.
- Item marketplace.
- WebSocket realtime chi tiết.
- Dashboard thống kê nâng cao.

## 13. Gợi ý implementation cụ thể cho repo hiện tại

### 13.1. Việc nên làm ngay

1. Sửa `User.Phone` thành `phone`.
2. Thêm `updatedAt` cho `User`.
3. Thêm `Wallet` và `WalletTransaction`.
4. Trong `AuthService.register`, sau khi tạo user thì tạo wallet mặc định.
5. Thêm `WalletService` với các method nội bộ:
   - `credit(userId, amount, type, referenceType, referenceId, idempotencyKey)`
   - `debit(userId, amount, type, referenceType, referenceId, idempotencyKey)`
   - `hold(userId, amount, referenceType, referenceId, idempotencyKey)`
   - `releaseHold(userId, amount, referenceType, referenceId, idempotencyKey)`
   - `captureHold(userId, amount, referenceType, referenceId, idempotencyKey)`

### 13.2. Gợi ý module đầu tiên nên code

Sau nền auth, module đầu tiên nên code là Wallet vì:

- Entry fee cần ví.
- Bet cần ví.
- Prize cần ví.
- Item purchase/sale cần ví.
- Withdraw/deposit cần ví.

Nếu code tournament trước mà bỏ ví, sau này sẽ phải chỉnh lại registration, prize, bet, item.

## 14. Definition of Done cho từng phase

Một phase chỉ nên coi là xong khi có:

- Entity và migration.
- API CRUD hoặc command chính.
- Role permission.
- Validation nghiệp vụ.
- Swagger hiển thị rõ request/response.
- Unit test cho service rule quan trọng.
- Integration test cho endpoint chính.
- Postman/Swagger flow chạy được từ đầu đến cuối.
- Không có transaction tiền nào update balance trực tiếp ngoài WalletService.
