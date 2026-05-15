# Chức Năng Màn Hình Theo Từng Role

Tài liệu này chuyển roadmap backend thành góc nhìn màn hình/chức năng cho từng role. Mục tiêu là giúp thiết kế UI, chia menu sidebar, và đối chiếu API cần có cho từng nhóm người dùng.

## Nguyên tắc hiển thị

- Role hiện tại của repo: `USER`, `OWNER`, `ADMIN`, `JOCKEY`, `SPECTATOR`, `REFEREE`.
- `GUEST` là người chưa đăng nhập, không phải role lưu trong database.
- Phase đầu dùng 1 role cho mỗi user. Nếu sau này hỗ trợ nhiều role, thêm màn hình chuyển workspace/role.
- Mọi role đã đăng nhập đều có thể có ví, nhưng menu ví hiển thị theo nhu cầu nghiệp vụ của role.
- Màn hình betting chỉ hiển thị khi feature flag betting bật.
- Màn hình admin tài chính phải yêu cầu quyền admin, reason/reference cho thao tác nhạy cảm, và ghi audit log.

## GUEST - Chưa đăng nhập

### Đăng nhập / đăng ký

- Đăng ký tài khoản.
- Đăng nhập bằng username/email và password.
- Đăng nhập Google nếu backend đã bật OAuth.
- Quên mật khẩu nếu có module reset password.

### Xem công khai

- Xem danh sách tournament đang mở hoặc đã publish.
- Xem chi tiết tournament.
- Xem lịch race.
- Xem kết quả race đã công bố.
- Xem leaderboard công khai.

## USER - Tài khoản mới chưa chọn vai trò

### Xem công khai

- Xem danh sách tournament đang mở hoặc đã publish.
- Xem chi tiết tournament.
- Xem lịch race.
- Xem kết quả race đã công bố.
- Xem leaderboard công khai.s

### Chọn vai trò

- Chọn trở thành `OWNER`, `JOCKEY`, hoặc `SPECTATOR`.
- Không cho tự chọn `ADMIN`.
- Hiển thị mô tả ngắn cho từng role trước khi chọn.

### Hồ sơ cá nhân

- Xem thông tin cá nhân.
- Cập nhật tên, phone, avatar, địa chỉ.
- Đổi mật khẩu.
- Xem trạng thái tài khoản.

### Ví cơ bản

- Xem số dư ví.
- Xem lịch sử giao dịch.
- Nạp tiền.
- Rút tiền.
- Xem lịch sử yêu cầu rút tiền.

## OWNER - Chủ ngựa

### Dashboard owner

- Xem tổng số ngựa.
- Xem số registration đang chờ duyệt.
- Xem race sắp diễn ra.
- Xem số dư ví.
- Xem tổng thưởng đã nhận.

### Quản lý ngựa

- Xem danh sách ngựa của tôi.
- Tạo hồ sơ ngựa.
- Cập nhật thông tin ngựa.
- Upload ảnh hoặc giấy tờ nếu có.
- Xem trạng thái: pending, approved, rejected, suspended.
- Xem lý do bị từ chối hoặc suspend.

### Jockey marketplace / lời mời

- Xem danh sách jockey khả dụng.
- Xem hồ sơ và thành tích jockey.
- Gửi lời mời jockey.
- Xem lời mời đã gửi.
- Hủy lời mời nếu chưa accept.
- Xem danh sách jockey đã accept.

### Tournament registration

- Xem tournament đang mở đăng ký.
- Xem phí tham gia, prize pool, thời gian đăng ký.
- Chọn ngựa để đăng ký.
- Chọn jockey đã accept.
- Gửi registration.
- Xem trạng thái registration: pending, approved, rejected.
- Nhận refund hoặc release hold nếu registration bị reject.

### Race của tôi

- Xem danh sách race có ngựa của mình.
- Xác nhận ngựa tham gia race.
- Xem gate number, jockey, referee, thời gian race.
- Xem trạng thái check-in.
- Xem kết quả sau khi admin công bố.

### Ví owner

- Nạp tiền để trả entry fee.
- Rút tiền.
- Xem entry fee bị hold, capture hoặc refund.
- Xem prize payout.
- Xem lịch sử giao dịch.

### Thưởng / prize

- Xem danh sách giải thưởng theo tournament/race.
- Xem trạng thái payout.
- Xem transaction liên quan.

### Notification

- Nhận thông báo registration approved/rejected.
- Nhận thông báo jockey accept/reject invitation.
- Nhận thông báo race scheduled.
- Nhận thông báo race result published.
- Nhận thông báo prize payout.
- Nhận thông báo wallet deposit/withdraw status.

## JOCKEY - Nài ngựa

### Dashboard jockey

- Xem lời mời mới.
- Xem race sắp diễn ra.
- Xem thành tích gần đây.
- Xem trạng thái hồ sơ/license.

### Hồ sơ jockey

- Tạo hoặc cập nhật profile.
- Nhập license, chiều cao, cân nặng, kinh nghiệm.
- Xem trạng thái duyệt: pending, approved, rejected, suspended.
- Xem lý do admin từ chối hoặc suspend.

### Lời mời từ owner

- Xem danh sách invitation.
- Accept invitation.
- Reject invitation.
- Xem horse, owner, tournament liên quan.
- Không accept nếu bị suspend hoặc trùng lịch.

### Race được phân công

- Xem race sắp tới.
- Xác nhận tham gia race.
- Xem gate number, horse, owner, thời gian.
- Xem trạng thái race.
- Xem kết quả race.

### Thành tích

- Xem lịch sử race.
- Xem thứ hạng.
- Xem thống kê win/place/show hoặc thống kê cơ bản.
- Xem prize nếu rule chia thưởng cho jockey.

### Ví jockey

- Xem số dư.
- Nhận thưởng nếu có.
- Rút tiền.
- Xem lịch sử giao dịch.

### Notification

- Nhận invitation mới.
- Nhận race assignment.
- Nhận race schedule change.
- Nhận result published.
- Nhận prize payout.

## REFEREE - Trọng tài

### Dashboard referee

- Xem race được phân công hôm nay.
- Xem race sắp tới.
- Xem race cần submit report.
- Xem race cần sửa draft result nếu admin yêu cầu.

### Race được phân công

- Xem danh sách race của tôi.
- Xem chi tiết participant, horse, jockey, gate number.
- Xem thời gian và trạng thái race.

### Check-in race

- Mở check-in.
- Check-in từng participant.
- Ghi chú sức khỏe, giấy tờ, trạng thái.
- Đánh dấu absent/disqualified nếu rule cho phép.

### Điều khiển ngày đua

- Start race.
- Cập nhật trạng thái race.
- Ghi violation.
- Ghi penalty/note.
- Upload biên bản hoặc hình ảnh nếu có.

### Draft result

- Nhập thứ hạng.
- Nhập finish time nếu có.
- Kiểm tra duplicate rank.
- Submit draft result cho admin duyệt.

### Referee report

- Viết báo cáo race.
- Gửi report.
- Xem trạng thái report: submitted, approved, change requested.

### Notification

- Nhận thông báo được phân công race.
- Nhận race schedule change.
- Nhận yêu cầu sửa result/report từ admin.

## SPECTATOR - Khán giả

### Khám phá tournament/race

- Xem tournament đang diễn ra hoặc sắp diễn ra.
- Xem lịch race.
- Xem participant, horse, jockey.
- Xem kết quả và leaderboard.

### Prediction

- Chọn race để dự đoán.
- Chọn horse dự đoán thắng.
- Xem prediction đã gửi.
- Prediction bị lock khi race bắt đầu.
- Xem kết quả prediction sau khi result confirmed.
- Nhận reward nhỏ nếu có.

### Betting

- Xem race đang mở bet.
- Xem odds.
- Đặt cược bằng ví.
- Xem bet đang pending, locked, won, lost, refunded.
- Nhận payout hoặc refund.
- Ẩn toàn bộ màn hình này nếu feature flag betting off.

### Ví spectator

- Nạp tiền.
- Rút tiền.
- Xem lịch sử giao dịch.
- Xem bet stake, payout, refund nếu betting bật.
- Xem reward từ prediction.

### Marketplace / inventory

- Xem shop item.
- Mua item bằng ví.
- Xem inventory của tôi.
- Bán item nếu policy cho phép.

### Notification

- Nhận thông báo race sắp bắt đầu.
- Nhận prediction settled.
- Nhận bet settled.
- Nhận reward/payout.
- Nhận wallet deposit/withdraw status.

## ADMIN - Ban tổ chức / quản trị

### Admin dashboard

- Xem tổng user theo role.
- Xem tournament đang mở.
- Xem registration chờ duyệt.
- Xem withdrawal chờ xử lý.
- Xem race hôm nay.
- Xem doanh thu, phí, prize payout tổng quan.
- Xem cảnh báo payment callback lỗi hoặc giao dịch cần kiểm tra.

### Quản lý user/role

- Xem danh sách user.
- Khóa hoặc mở tài khoản.
- Đổi role user.
- Xem profile user.
- Không cho thao tác nguy hiểm nếu thiếu audit log.

### Duyệt horse/jockey

- Xem horse pending.
- Approve, reject hoặc suspend horse.
- Xem jockey profile/license pending.
- Approve, reject hoặc suspend jockey.
- Ghi lý do reject/suspend.

### Quản lý tournament

- Tạo/sửa tournament.
- Cấu hình registration window.
- Cấu hình entry fee.
- Cấu hình prize pool.
- Tạo round/heat.
- Mở/đóng đăng ký.
- Publish/unpublish tournament nếu cần.

### Duyệt registration

- Xem registration theo tournament.
- Approve registration.
- Reject registration.
- Khi approve: capture/debit entry fee theo policy.
- Khi reject: release/refund entry fee.
- Xem ledger liên quan.

### Race scheduling

- Tạo race từ registration approved.
- Thêm participant.
- Gán gate number.
- Phân công referee.
- Sửa lịch race.
- Kiểm tra trùng lịch jockey/referee.
- Chuyển race sang ready/scheduled.

### Result approval

- Xem draft result từ referee.
- Xem violation/report.
- Approve result.
- Request changes.
- Công bố kết quả.
- Generate leaderboard snapshot.
- Trigger prize payout.

### Quản lý tài chính

- Xem admin wallet.
- Xem admin wallet transactions.
- Xem payment orders/deposit history.
- Xem withdrawal requests của user.
- Approve withdrawal.
- Reject withdrawal.
- Mark-paid withdrawal.
- Admin withdraw trực tiếp từ admin wallet.
- Bắt buộc nhập reason khi admin withdraw.
- Xem audit log giao dịch tiền.

### Quản lý betting

- Bật/tắt feature flag betting.
- Cấu hình odds hoặc policy odds.
- Xem danh sách bet.
- Trigger settlement nếu cần.
- Refund race cancelled/voided.

### Quản lý item marketplace

- CRUD item.
- Set giá.
- Active/inactive item.
- Xem inventory/user item nếu cần hỗ trợ.

### Notification / realtime

- Gửi announcement.
- Xem notification event log.
- Theo dõi WebSocket race/result/leaderboard event.

### Audit / system

- Xem admin audit log.
- Xem payment callback log.
- Xem lỗi hệ thống quan trọng.
- Health check DB/payment provider.
- Cấu hình rate limit/security nếu có màn hình admin system.

## Sidebar gợi ý theo MVP

| Role        | Sidebar                                                                                                                                            |
| ----------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| `USER`      | Choose Role, Profile, Wallet                                                                                                                       |
| `OWNER`     | Dashboard, Horses, Jockeys, Tournaments, Registrations, My Races, Wallet, Prizes, Notifications, Profile                                           |
| `JOCKEY`    | Dashboard, Profile, Invitations, My Races, Performance, Wallet, Notifications                                                                      |
| `REFEREE`   | Dashboard, Assigned Races, Check-in, Results, Reports, Notifications                                                                               |
| `SPECTATOR` | Tournaments, Races, Predictions, Betting, Wallet, Shop, Inventory, Leaderboard, Notifications                                                      |
| `ADMIN`     | Dashboard, Users, Horse Approval, Jockey Approval, Tournaments, Registrations, Races, Results, Finance, Items, Notifications, Audit Logs, Settings |

## Mapping nhanh theo phase

| Phase    | Màn hình chính cần ưu tiên                                           |
| -------- | -------------------------------------------------------------------- |
| Phase 0  | Login/Register, Choose Role, Profile, Admin Users                    |
| Phase 1  | Wallet, Admin Finance overview                                       |
| Phase 2  | Deposit, Payment Order, Payment Callback Log                         |
| Phase 3  | User Withdrawals, Admin Withdrawals, Admin Wallet Audit              |
| Phase 4  | Owner Horses, Jockey Profile, Admin Horse/Jockey Approval            |
| Phase 5  | Jockey Marketplace, Owner Invitations, Jockey Invitations            |
| Phase 6  | Admin Tournaments, Public Tournaments                                |
| Phase 7  | Owner Registrations, Admin Registration Approval                     |
| Phase 8  | Admin Race Scheduling, Owner/Jockey My Races, Referee Assigned Races |
| Phase 9  | Referee Check-in, Referee Violations, Referee Draft Results          |
| Phase 10 | Admin Result Approval, Leaderboard, Prize Payout                     |
| Phase 11 | Spectator Prediction                                                 |
| Phase 12 | Spectator Betting, Admin Betting                                     |
| Phase 13 | Shop, Inventory, Admin Items                                         |
| Phase 14 | Notifications, Realtime Race/Result/Leaderboard                      |
| Phase 15 | Audit Logs, Settings, Health/Monitoring                              |

## Assumptions

- `ADMIN` không dùng màn hình của `OWNER`, `JOCKEY`, `REFEREE`, `SPECTATOR` trừ khi sau này hỗ trợ multi-role.
- `Betting` và `Marketplace` làm sau MVP core, đúng Phase 12-13.
- Các màn hình tài chính admin phải có audit log và không cho thao tác thiếu reason/reference.
- Các chức năng upload ảnh/giấy tờ chỉ bật khi backend có storage hoặc Cloudinary/S3 tương đương.
