# Revised Plan: Phase Checklist Với Ví Admin Trung Tâm

## Summary

Tạo file mới `docs/horse-racing-phase-implementation-plan.md` theo dạng checklist từng phase, có cập nhật rule tài chính mới:

- Tiền user nạp vào hệ thống sẽ đi vào **ví admin trung tâm**.
- Ví user vẫn tồn tại để hiển thị số dư và ghi nhận quyền sở hữu tiền, nhưng về nghiệp vụ tiền thật nằm trong ví/admin custody.
- User rút tiền phải tạo yêu cầu rút và chờ admin duyệt.
- Khi user rút thành công, hệ thống trừ số dư user và trừ tiền khỏi ví admin.
- Admin rút tiền từ ví admin không cần ai duyệt, nhưng bắt buộc có audit log.

## Key Changes

- Thêm khái niệm `AdminWallet` hoặc `SystemWallet`:
  - Là ví trung tâm giữ tổng tiền thật của hệ thống.
  - Có thể có một ví mặc định currency `VND`.
  - Chỉ admin được xem và thao tác.
  - Mọi deposit của user làm tăng `AdminWallet.availableBalance`.

- Giữ `UserWallet`:
  - User wallet là ví/sổ phụ để ghi số dư của từng user.
  - User deposit thành công: cộng user wallet và cộng admin wallet.
  - User bet/mua item/entry fee: trừ hoặc hold trên user wallet.
  - User withdraw: hold/trừ user wallet trước, chờ admin duyệt.

- Thêm ledger kép:
  - `WalletTransaction` cho user wallet.
  - `AdminWalletTransaction` hoặc dùng chung `WalletTransaction` với `walletOwnerType = USER | ADMIN`.
  - Mỗi giao dịch deposit/withdraw phải có reference chung để trace hai phía.

## Updated Financial Flow

- User deposit:
  - User tạo payment order.
  - Payment provider xác nhận paid.
  - Hệ thống cộng `UserWallet.availableBalance`.
  - Hệ thống cộng `AdminWallet.availableBalance`.
  - Tạo 2 transaction cùng `referenceId`.

- User withdrawal:
  - User tạo withdrawal request.
  - Hệ thống chuyển tiền user từ `availableBalance` sang `holdBalance`.
  - Admin approve.
  - Admin chuyển khoản ngoài hệ thống.
  - Admin mark-paid.
  - Hệ thống trừ user hold balance.
  - Hệ thống trừ `AdminWallet.availableBalance`.
  - Nếu reject thì release hold về user available.

- Admin withdrawal:
  - Admin tạo lệnh rút tiền từ admin wallet.
  - Không cần approval.
  - Hệ thống trừ `AdminWallet.availableBalance`.
  - Bắt buộc ghi audit log: adminId, amount, bank info, reason, timestamp.
  - Không ảnh hưởng user wallet, trừ khi đó là payout/withdraw của user.

## Phase Plan Updates

- Phase 1: Wallet core
  - Tạo user wallet, admin wallet, ledger chung.
  - Tạo seed/default admin wallet khi app start hoặc migration seed.
  - Tất cả tiền dùng `BigDecimal`.

- Phase 2: Payment deposit thật
  - Deposit callback cộng cả user wallet và admin wallet.
  - Chống callback trùng bằng `idempotencyKey`.
  - Nếu cộng user wallet thành công nhưng admin wallet lỗi, rollback toàn bộ transaction.

- Phase 3: Withdraw
  - User withdraw cần admin duyệt.
  - Admin withdraw không cần duyệt.
  - Tách rõ endpoint user withdrawal và admin wallet withdrawal.

- Phase 4 trở đi
  - Entry fee, bet stake, prize payout, item purchase vẫn xử lý trên user wallet.
  - Prize/bet payout nếu là tiền hệ thống trả cho user thì cộng user wallet và ghi nhận nguồn từ admin/system wallet theo policy.
  - User rút tiền thật mới làm giảm admin wallet cash.

## Public APIs/Types

- User wallet:
  - `GET /api/v1/wallets/me`
  - `GET /api/v1/wallets/me/transactions`
  - `POST /api/v1/wallets/me/deposit-orders`
  - `POST /api/v1/wallets/me/withdrawals`
  - `GET /api/v1/wallets/me/withdrawals`

- Admin wallet:
  - `GET /api/v1/admin/wallet`
  - `GET /api/v1/admin/wallet/transactions`
  - `POST /api/v1/admin/wallet/withdrawals`
  - `GET /api/v1/admin/withdrawals`
  - `PUT /api/v1/admin/withdrawals/{id}/approve`
  - `PUT /api/v1/admin/withdrawals/{id}/reject`
  - `PUT /api/v1/admin/withdrawals/{id}/mark-paid`

- Core enums:
  - `WalletOwnerType`: `USER`, `ADMIN`
  - `WalletTransactionType`: `DEPOSIT`, `WITHDRAW`, `ADMIN_WITHDRAW`, `ENTRY_FEE`, `BET_STAKE`, `BET_PAYOUT`, `PRIZE_PAYOUT`, `ITEM_PURCHASE`, `ITEM_SALE`, `REFUND`, `ADJUSTMENT`
  - `WithdrawalStatus`: `PENDING`, `APPROVED`, `REJECTED`, `PAID`, `CANCELLED`
  - `AdminWalletWithdrawalStatus`: `PAID`, `FAILED`, `REVERSED`

## Test Plan

- User deposit thành công cộng cả user wallet và admin wallet.
- Deposit callback bị gọi lại không cộng tiền lần hai.
- User withdraw tạo request thì user balance bị hold, admin wallet chưa bị trừ.
- User withdraw rejected thì hold trả về available.
- User withdraw mark-paid thì user hold bị trừ và admin wallet bị trừ.
- Admin withdraw không cần approval và trừ admin wallet ngay.
- Admin withdraw không được làm admin wallet âm.
- Mọi transaction tiền có audit/reference đầy đủ.

## Assumptions

- `AdminWallet` là ví tiền thật/custody của hệ thống, còn `UserWallet` là số dư user trong app.
- Chỉ admin được thao tác admin wallet.
- Admin rút tiền không cần duyệt, nhưng vẫn phải kiểm tra số dư và ghi audit log.
- MVP có thể dùng payment manual/bank transfer trước, sau đó gắn VNPay/MoMo.
- File mới chỉ là tài liệu kế hoạch; chưa sửa code cho đến khi bạn yêu cầu triển khai.
