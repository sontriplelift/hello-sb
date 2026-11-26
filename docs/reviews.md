# Nhật ký review

File này lưu nội dung review chi tiết của mentor, để tiếp tục sửa và review trên máy khác. Đề bài và bảng Tiến độ nằm ở [learning-plan.md](learning-plan.md).

**Cách dùng**
- Học viên: sửa xong mục nào thì đánh dấu `[x]` mục đó, rồi nhắn **"review step X"**.
- Mentor: đọc lần review gần nhất của step, kiểm tra lại từng mục còn mở, rồi thêm lần review mới ngay dưới tiêu đề của step đó (mới nhất ở trên). Không xoá các lần review cũ.
- Số dòng trong link đúng tại thời điểm review, có thể lệch sau khi sửa code.

---

## Step 12: Refresh token

### Review lần 2 (2026-11-26): ✅ ĐẠT

Code được review: commit `9b8928c` cộng với các thay đổi chưa commit (thêm `jti`, log khi login thất bại). Theo yêu cầu của học viên, mentor sửa giúp 💡2 và 💡5 (xem bên dưới).

#### Kết quả test (app khởi động lại trên cổng 8080, test bằng user `reader`)

| Case | Kết quả | |
|---|---|---|
| Login, refresh, reuse, logout, sai mật khẩu | Như review lần 1 | ✅ |
| Token hết hạn / user không còn tồn tại | 401, cùng message `Invalid or expired refresh token` | ✅ |
| Access token có `jti` | Có (UUID) | ✅ |
| 8 request đồng thời, cùng một token | 1 × 200, 7 × 401 | ✅ |
| Dùng lại A **đồng thời** với refresh B, lặp 20 lần | 3 lần B thắng (200), 17 lần A thắng (B 401). **Cả 20 lần không còn token nào hiệu lực**, không dòng nào bị mất `replaced_by_id` | ✅ |
| Log | Có dòng `Login failed`, không chứa token gốc | ✅ |

Dữ liệu test (token của `reader`, id > 11) đã xoá. App do mentor khởi động đã được tắt.

#### Các mục của lần 1
- ❌ 1, 2, 3: đã sửa. Câu trả lời cho ❌ 3 đúng. Ý quan trọng nhất: lúc cần refresh thì access token **thường đã hết hạn**. Nếu bắt `/refresh` phải có access token thì sẽ không bao giờ refresh được.
- 💡 1: đúng, đây là việc của FE. (Phía server cũng có thể cấu hình `BearerTokenResolver` để bỏ qua header cho hai API này, nhưng không cần.)
- 💡 2, 5: **mentor đã sửa giúp** (giải thích ở dưới).
- 💡 3 (`jti`), 6 (`HttpMethod.POST`, `@Tag`, dọn import và field), 7 (`.editorconfig`, dấu cách): đã làm.
- 💡 4: mới làm được nửa: đã log khi login thất bại, nhưng handler vẫn biến lỗi hệ thống thành 401. Chuyển sang 13.0.
- Còn sót, chuyển sang 13.0:
  - 3 method không dùng trong `RefreshTokenRepository`: `findAllByUsername`, `existsByTokenHash`, và `findAllByUsernameAndRevokedAtIsNull` (method cuối không còn dùng sau khi mentor sửa).
  - `OpenApiConfig` còn 3 dòng tab.

#### Mentor sửa giúp: 💡 5, bỏ truy vấn thừa
- Tách hàm private `saveNewToken(username, rawToken)`, trả về entity mà `save()` trả lại. Với `IDENTITY`, câu `INSERT` chạy ngay trong `save()` nên entity đã có `id`. Vì vậy không cần tìm lại token theo hash nữa.
- `create()` (dùng khi login) và `refresh()` cùng gọi hàm này. Thêm hàm `refreshExpiration()` để không phải lặp chuỗi `libraryProperties.security().jwt()...`.

#### Mentor sửa giúp: 💡 2, khe hở đồng thời
**Vấn đề cũ:** `findByTokenHashForUpdate` chỉ khoá **đúng dòng** của token được gửi lên. Giả sử cùng lúc:
- T1 dùng lại A (đã rotation): khoá dòng A, rồi đọc danh sách token còn hiệu lực, thấy B.
- T2 refresh B: khoá dòng B, tạo C, rồi commit.

A và B là hai dòng khác nhau nên T1 và T2 không chặn nhau. T1 không thấy C, nên C sống sót. Thêm nữa, T1 đã nạp B **trước khi** T2 commit. Khi T1 flush, Hibernate `UPDATE` **mọi cột** của B, nên ghi đè `replaced_by_id` của B về `null`.

**Cách sửa** trong [RefreshTokenService.refresh()](../src/main/java/com/example/library/service/RefreshTokenService.java) và [RefreshTokenRepository](../src/main/java/com/example/library/repository/RefreshTokenRepository.java):
1. `findUsernameByTokenHash`: chỉ lấy `username`, **chưa nạp entity**.
2. `lockActiveTokensByUsername`: `SELECT ... FOR UPDATE` mọi token còn hiệu lực của user, `ORDER BY id` để mọi transaction khoá theo cùng thứ tự (tránh deadlock). Từ đây, mọi request refresh của **cùng một user** chạy lần lượt. Trong ví dụ trên, cả T1 và T2 đều phải khoá B, nên một bên phải chờ bên kia commit.
3. `findByTokenHash`: nạp entity **sau khi** đã có khoá, nên đọc được trạng thái mới nhất.
4. `revokeAllActiveByUsername`: thu hồi bằng **một câu `UPDATE`** (`@Modifying`). Câu này chỉ sửa cột `revoked_at`, không ghi đè `replaced_by_id`. Nó là một câu lệnh mới, nên thấy được C vừa được commit.

Code cũ được giữ lại dưới dạng comment, kèm giải thích. Login (`create`) không lấy khoá, vì login bằng mật khẩu là phiên hợp lệ mới. Câu hỏi 1 ở 13.8 yêu cầu học viên tự giải thích lại bước 1 và bước 3.

#### 🎓 Câu trả lời
- **10.6, câu 1–7:** đã nhận xét ở lần 1, không đổi.
- **❌ 3 (vì sao `permitAll`):** đúng, xem bổ sung ở trên.

---

### Review lần 1 (2026-11-25): 🔁 CẦN SỬA

Code được review: commit `8d37b7f` cộng với các thay đổi **chưa commit** lúc đó (API `refresh`, `logout`, `InvalidRefreshTokenException`, các DTO mới).

Rotation, reuse detection và khoá đồng thời đều làm đúng. Còn 3 điểm nhỏ cần sửa để đạt.

#### Kết quả test

| # | Case | Kết quả | |
|---|---|---|---|
| 1 | Login | 200, đủ `accessToken` và `refreshToken` | ✅ |
| 2 | `token_hash` trong DB | Bằng SHA-256 của token, không phải token gốc | ✅ |
| 3 | `refresh` | 200, cả hai token đều mới | ✅ |
| 4 | Dùng lại token cũ | 401, mọi phiên của user bị thu hồi | ✅ |
| 5 | Chuỗi rác | 401, message giống case 4 | ✅ |
| 6 | Thiếu / toàn khoảng trắng / JSON hỏng | 400 (thiếu hoặc trắng thì có `fieldErrors`) | ✅ |
| 7 | Logout rồi refresh | 204, sau đó 401 (logout lại, logout chuỗi rác: 204) | ✅ |
| 8 | Access token cũ sau logout | `POST` vẫn 201 | ✅ |
| 9 | 2 thiết bị, logout 1 | Thiết bị 2 vẫn refresh được | ✅ |
| 10 | Access token lấy từ `refresh` | `roles: ["ADMIN"]`, `POST` được | ✅ |
| 11 | Restart app, dùng refresh token còn hạn | **Mentor chưa test** | ⏳ |
| 12 | Token hết hạn | 401 (test bằng cách chèn sẵn một dòng đã hết hạn vào DB) | ✅ |
| 13 | Log | **Mentor chỉ đọc code**, không xem console | ⏳ |
| + | 8 request đồng thời, cùng một token | 1 × 200, 7 × 401 | ✅ |
| + | User của token không còn tồn tại | 401 nhưng sai message | ❌ |
| + | `refresh` / `logout` kèm access token hỏng trong header | 401 (xem 💡 1) | 💡 |

#### ✅ Điểm tốt
- **Reuse detection không bị rollback.** Nếu ném exception ngay trong transaction thì việc thu hồi cũng mất theo. Code trả về `RefreshResult(true, null)` để commit trước, rồi controller mới ném lỗi: [RefreshTokenService.java:103-112](../src/main/java/com/example/library/service/RefreshTokenService.java#L103-L112).
- **Khoá rồi mới kiểm tra trạng thái**, nên 8 request đồng thời chỉ có đúng 1 request thành công.
- **Phân biệt "thu hồi do rotation" và "do logout"** bằng `replacedById`: dùng lại token đã logout không làm mất phiên khác.
- **Role lấy từ `UserDetailsService`**, kiểm tra đủ 4 trạng thái tài khoản.
- V4 có index cho `username` và `expires_at`. Logout lặp lại nhiều lần vẫn trả 204.
- Mục 12.0 xong gần hết, gồm cả phần tuỳ chọn kiểm tra `iss`. Sơ đồ `visualize/flow-refresh.png` khớp với code.

#### ❌ Cần sửa (bắt buộc để đạt)
- [ ] **1. Log token gốc:** [RefreshTokenService.java:178-179](../src/main/java/com/example/library/service/RefreshTokenService.java#L178-L179). Token hiện chưa bị in ra chỉ vì quên `{}`. Ai đó "sửa" thêm `{}` là lộ ngay. Xoá cả hai dòng.
- [ ] **2. Sai message khi user không còn tồn tại.** `loadUserByUsername` ném `UsernameNotFoundException`, vốn là con của `AuthenticationException`. Handler của login bắt nó, nên `/refresh` trả về `"Invalid username or password"`. Trường hợp này cũng phải trả `"Invalid or expired refresh token"` như các lỗi khác.
- [ ] **3. Thiếu phần giải thích ở 12.4:** vì sao `/refresh` và `/logout` để `permitAll`? Trả lời vài câu là được.

#### 💡 Nên sửa (chưa làm thì chuyển sang 13.0)
- [ ] **1. Header `Authorization` khi gọi `/refresh`, `/logout`.** Gửi kèm access token hết hạn thì bị 401 ngay ở filter, dù đã `permitAll` (mentor test bằng token hỏng, cùng cơ chế). Interceptor của FE phải bỏ header này khi gọi hai API đó. Đây là điều cần nhớ khi viết FE, không bắt buộc sửa server.
- [ ] **2. Khe hở đồng thời trong `revokeAllActiveTokens`** (suy ra từ code, chưa test). Hàm đọc danh sách mà không khoá. Nếu lúc đó token B đang được đổi thành C:
  - C có thể thoát khỏi đợt thu hồi.
  - Hibernate update toàn bộ cột, nên có thể ghi đè `replacedById` của B về `null`.

  Câu `UPDATE` hàng loạt chỉ sửa được ý thứ hai. Muốn kín hẳn thì cần khoá theo user hoặc theo "token family".
- [ ] **3. Access token nên có `jti`.** Hai token của cùng user tạo trong cùng một giây sẽ giống hệt nhau, vì HS256 luôn cho cùng kết quả với cùng dữ liệu.
- [ ] **4. Handler `AuthenticationException` hơi rộng.** Lỗi hệ thống như `InternalAuthenticationServiceException` cũng thành 401 và không có log. Login thất bại cũng nên có `logger.warn`.
- [ ] **5. Truy vấn thừa:** `refresh()` tạo token mới rồi lại tìm nó theo hash ([RefreshTokenService.java:139-147](../src/main/java/com/example/library/service/RefreshTokenService.java#L139-L147)). Nên tách một hàm private trả về entity.
- [ ] **6. Dọn code:**
  - Kiểm tra null ở [AuthController.java:136-138](../src/main/java/com/example/library/controller/AuthController.java#L136-L138) không bao giờ đúng.
  - Field `tokenWithRefreshResponse` của `RefreshResult` vẫn mang tên class đã xoá.
  - Import `SecurityRequirement` trong `AuthController`, cùng `findAllByUsername` và `existsByTokenHash` trong repository, không dùng.
  - Hai rule `/api/auth/refresh`, `/api/auth/logout` trong `SecurityConfig` thiếu `HttpMethod.POST`.
  - `AuthController` thiếu `@Tag`.
- [ ] **7. Thụt lề (nợ từ Step 10):** còn 9 file dùng tab. Gợi ý: tạo `.editorconfig` để IDE tự giữ. Danh sách: `CustomAuthenticationEntryPoint`, `JwtConfig`, `OpenApiConfig`, `SecurityConfig`, `AuthorRequest`, `BookRequest`, `LoginRequest`, `LibraryApplication`, `TokenService` (vừa bị đổi từ dấu cách sang tab ở step này).

#### 🎓 Nhận xét câu trả lời

**10.6 (`SecurityContextHolder` và `@AuthenticationPrincipal`): Rất tốt, nợ đã trả.** Bổ sung:
- Với JWT, principal là `Jwt`, nên phải viết `@AuthenticationPrincipal Jwt jwt` rồi lấy `jwt.getSubject()`.
- `SecurityContextHolder` dùng ThreadLocal, nên mặc định mất dữ liệu trong `@Async`.

**Câu 1 (hash, SHA-256 và BCrypt): Xuất sắc.** Thêm một lý do: BCrypt có salt ngẫu nhiên nên mỗi lần hash ra kết quả khác, không thể tìm bằng `WHERE token_hash = ?`.

**Câu 2 (opaque token): Đúng.** Lý do cốt lõi: muốn thu hồi được thì đằng nào cũng phải tra DB, nên chữ ký JWT gần như không còn lợi ích gì.

**Câu 3 (reuse detection, 2 tab): Xuất sắc.** Test xác nhận đúng: 7 request thua làm token mới cũng bị thu hồi. Bổ sung:
- Chiều ngược lại cũng bị phát hiện: kẻ tấn công dùng A trước, rồi user thật dùng A.
- Nếu user không bao giờ refresh nữa thì không phát hiện được.
- Phía server có thể thêm *grace period* vài giây, đổi lại kém chặt hơn.

**Câu 4 (hai request đồng thời): Đúng nhưng quá ngắn.** So sánh các cách:

| Cách | Request thua |
|---|---|
| `SELECT ... FOR UPDATE` (đang dùng) | Chờ, rồi đọc thấy `revoked_at` mới nhất |
| `UPDATE ... WHERE revoked_at IS NULL` | Nhận 0 dòng, không cần đọc trước |
| `@Version` | Lỗi lúc commit, phải tự đổi thành 401 |

**Câu 5 (access token sau logout): Rất tốt.** Nếu áp dụng denylist thì lưu ý: logout hiện chỉ nhận refresh token, nên server không biết `jti` của access token. Khi đó client phải gửi cả hai, hoặc lưu `jti` cùng refresh token.

**Câu 6 (cookie `HttpOnly`): Đúng hướng, nhưng thiếu phần `Path`, `SameSite`, CSRF:**
- `Path=/api/auth`: cookie chỉ được gửi tới nhóm API này.
- `SameSite=Strict` hoặc `Lax`: site khác gửi POST thì trình duyệt không kèm cookie. Dùng thêm `Secure`.
- Project đang tắt CSRF. Điều đó ổn vì token nằm trong header/body, nhưng khi chuyển sang cookie thì phải xét lại cho `/refresh` và `/logout`.
- FE khác origin: cần `allowCredentials` cùng origin cụ thể (không dùng `*`), và FE bật `withCredentials`. Nếu khác hẳn *site* thì phải dùng `SameSite=None; Secure`, và bắt buộc có chống CSRF.

**Câu 7 (dọn bảng): Đúng, cần chốt thành một quy tắc.** Chỉ xoá token đã hết hạn (có thể giữ thêm vài ngày để điều tra), không xoá token đã revoke mà chưa hết hạn. Hai bẫy:
- Khoá ngoại `replaced_by_id`: nếu **giảm** `refresh-expiration`, token mới có thể hết hạn trước token cũ, và lệnh xoá sẽ vi phạm khoá ngoại.
- Chạy nhiều instance thì `@Scheduled` chạy trên tất cả instance.

**Case 11 so với case 13 của Step 11:** refresh token còn dùng được sau restart vì nó nằm trong DB. Access token còn dùng được vì secret không đổi.

#### Ghi chú cho mentor khi review lần 2
- Test reuse detection bằng user **`reader`**, vì nó thu hồi mọi phiên của user, và học viên thường đang dùng phiên `librarian`.
- Test user không còn tồn tại hoặc token hết hạn: chèn một dòng vào `refresh_tokens` với `token_hash` là SHA-256 (hex) của một chuỗi tự chọn, rồi gọi `/refresh` bằng chuỗi đó.
- Ghi lại id lớn nhất trước khi test, sau đó chỉ xoá các dòng do mình tạo. Đặt `replaced_by_id = NULL` trước khi xoá, vì có khoá ngoại.
- File DB không được đưa lên Git, nên dữ liệu (kể cả phiên đăng nhập) trên mỗi máy là khác nhau.
