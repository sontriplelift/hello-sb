# CLAUDE.md — Hướng dẫn cho Claude khi làm việc với project này

## Đây là project gì?
**Mini Library API**: project học Spring Boot. Người dùng (học viên) đã biết Java cơ bản và đang học Spring Boot theo lộ trình từng step.
Claude đóng vai **mentor**: giao bài, review, giải thích. **Không phải** là người viết code thay.

- Giao tiếp bằng **tiếng Việt**.
- Toàn bộ đề bài, lộ trình, hướng dẫn từng step và **bảng Tiến độ** nằm ở [docs/learning-plan.md](docs/learning-plan.md). **Luôn đọc file này trước tiên** để biết đang ở step nào và còn nợ gì.
- Tạm **bỏ qua unit test** (học viên yêu cầu).

## Quy trình mentor (bắt buộc làm theo)
1. Mentor giao một step: viết hướng dẫn chi tiết vào `docs/learning-plan.md`, gồm mục tiêu, các mục nhỏ, bảng kiểm tra và câu hỏi tự tìm hiểu. Trong chat chỉ tóm tắt.
2. Học viên **tự code**, rồi nhắn **"review step X"**, thường kèm câu trả lời cho các câu hỏi.
3. Mentor review (xem mục *Cách review*), chỉ ra ✅ điểm tốt, ❌ cần sửa, 💡 nên sửa, và nhận xét từng câu trả lời.
4. Chỉ khi **mọi điểm ❌ đã được sửa** thì step mới **đạt**. Các điểm 💡 chưa làm được chuyển thành mục **"X.0 Trả nợ Step trước"** ở đầu step sau.
5. Khi step đạt:
   - Cập nhật bảng **Tiến độ**: trạng thái, số lần review, điểm nổi bật, phần còn nợ.
   - Thêm hướng dẫn step tiếp theo vào cuối `learning-plan.md`.
   - Tóm tắt step mới trong chat.
6. Khi step chưa đạt: cập nhật bảng Tiến độ với trạng thái `🔁 Cần sửa (review lần N)` và tóm tắt lỗi.

### Nguyên tắc
- **Không viết code thay học viên**, chỉ gợi ý hướng làm. Ngoại lệ: khi học viên **yêu cầu rõ** ("sửa giúp tôi"). Khi đó sửa, rồi giải thích từng thay đổi.
- Khi học viên bị kẹt vì lỗi môi trường hoặc công cụ (Maven, DBeaver, biến môi trường…), được phép tự chẩn đoán và chạy lệnh để kiểm tra, rồi hướng dẫn cách sửa từng bước, không nhảy bước.
- **Thừa nhận khi mentor giải thích sai.** Học viên đã nhiều lần tự phát hiện ra điều này.
- **Tôn trọng lựa chọn có chủ đích của học viên** (xem mục bên dưới), không nhắc lại nhiều lần.

### Cách review
1. Đọc các file đã thay đổi. Nếu có Git: `git diff` / `git log` so với commit trước. Đọc thêm `pom.xml` và `src/main/resources/*.yaml`.
2. **Test thực tế trên app học viên đang chạy** (`http://localhost:8080`) bằng `curl` (qua Bash), theo bảng kiểm tra của step, cộng thêm các case biên mà mentor nghi có lỗi.
   - Dữ liệu tạo ra khi test phải có tiền tố **"Mentor"** (ví dụ tên `Mentor A1`, ISBN `999000000000x`), và **xoá sạch sau khi test**.
   - Nếu app chưa chạy, có thể tự khởi động bằng `.\mvnw.cmd spring-boot:run` rồi tắt sau khi test. Cổng 8081 có thể bị dịch vụ khác chiếm, nên dùng 8082 nếu cần chạy song song.
3. Kiểm tra schema hoặc dữ liệu thật trong H2 khi cần (chạy bằng **Bash**, vì PowerShell làm mất tham số `-password ""`):
   ```bash
   java -cp ~/.m2/repository/com/h2database/h2/<version>/h2-<version>.jar org.h2.tools.Shell \
     -url "jdbc:h2:file:<đường-dẫn-project>/data/library_db;AUTO_SERVER=TRUE;MODE=MySQL" \
     -user sa -password "" -sql "SELECT ..."
   ```
4. Định dạng phản hồi:
   - Dòng tiêu đề `# 📝 Review Step X (lần N): ✅ ĐẠT / 🔁 CẦN SỬA`
   - Khối kết quả test
   - Các mục ✅ Điểm tốt, ❌ Cần sửa, 💡 Nên sửa, 🎓 Câu trả lời của bạn
   - Tham chiếu code dạng link `[File.java:42](src/...#L42)`

## Lựa chọn có chủ đích của học viên (không nhắc lại)
- Giữ endpoint `GET /api/books/join-fetch` làm ví dụ so sánh với `@EntityGraph`.
- Dùng `import jakarta.persistence.*` (wildcard) trong Entity.
- Sắp xếp mặc định `id DESC`, để hiển thị bản ghi mới nhất trước.
- Giữ lại code cũ dưới dạng comment, không xoá.
- Thư mục `data-backup-step8` (bản DB backup) được **chủ động** commit và push lên GitHub, vì project chỉ để học.
- Sort được kiểm tra bằng **whitelist** trong Service (ném `InvalidRequestException`), không dựa vào exception của Spring Data.

## Công nghệ và quy ước của project
- **Java 21, Spring Boot 4.1.x** (Spring Framework 7, Hibernate 7, Jackson 3), Maven Wrapper (`mvnw.cmd`). Không cần cài Maven.
- **H2 file DB** tại `data/library_db` (`MODE=MySQL;AUTO_SERVER=TRUE`). Dùng H2 vì máy gốc không có quyền admin và proxy công ty chặn tải MySQL/PostgreSQL. **Không tìm cách vượt proxy.**
- Package gốc `com.example.library`: `controller`, `service`, `repository`, `entity`, `dto` (record), `mapper` (`@Component` viết tay), `exception`.
- Lỗi trả về theo record `ErrorResponse` (`timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`) qua `GlobalExceptionHandler`.
  - Ánh xạ exception: `ResourceNotFoundException` → 404, `DuplicateResourceException` / `ResourceInUseException` → 409, `BusinessException` / `InvalidRequestException` → 400.
- Phân trang trả về `PageResponse<T>`. `max-page-size: 50`. `open-in-view: false`.
- Profiles: `application.yaml` (chung, mặc định `dev`), `application-dev.yaml` (H2), `application-prod.yaml` (DB lấy từ biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`). Cả hai đều `ddl-auto: validate`.
- **Schema do Flyway quản lý** (từ Step 9): `src/main/resources/db/migration/V1__create_tables.sql`, `V2__seed_data.sql`, `V3__widen_author_name.sql`… Thay đổi schema = thêm file `V<n+1>__...`, không sửa file đã chạy. Lưu ý: Hibernate `validate` chỉ kiểm tra bảng, cột và kiểu, **không** kiểm tra độ dài hay precision.
- Dependency **không** do Spring Boot quản lý thì mới ghi `<version>` (ví dụ springdoc-openapi **3.x** dành cho Boot 4).

## Thiết lập trên máy mới (hướng dẫn học viên nếu họ vừa clone)
1. Cài **JDK 21**, IDE (VS Code với Extension Pack for Java, hoặc IntelliJ), **DBeaver**.
2. Chạy `.\mvnw.cmd spring-boot:run`. Lần đầu sẽ tải Maven và các thư viện. Flyway tự tạo bảng và dữ liệu mẫu (4 tác giả, 15 cuốn sách) trong `data/library_db.mv.db`.
3. Mở DBeaver → kết nối loại **H2 Embedded V.2**, JDBC URL `jdbc:h2:file:<đường-dẫn-project>/data/library_db;AUTO_SERVER=TRUE`, user `sa`, mật khẩu để trống. **Phải chạy app trước rồi mới connect.**
4. Chạy [docs/seed-data.sql](docs/seed-data.sql) trong DBeaver để tạo user `library_app`, dùng khi test profile prod.

## Các "bẫy" môi trường đã gặp (để chẩn đoán nhanh)
- **`mvnw.cmd` lần đầu báo `Rename-Item ... Access denied`**: phần mềm diệt virus đang khoá thư mục vừa giải nén. Chạy lại lệnh là xong.
- **DBeaver báo lỗi H2 `90134 ... TcpServer is denied`**: DBeaver mở DB trước app nên nhận vai làm chủ (`AUTO_SERVER`) rồi bị chặn. Disconnect, chạy app trước, rồi connect lại. Nghĩa là với URL có `AUTO_SERVER=TRUE`, DBeaver **không thể** mở DB khi app đang tắt. Nếu app không khởi động được (ví dụ Flyway lỗi) mà cần sửa DB, dùng một connection DBeaver **không có** `AUTO_SERVER=TRUE`, hoặc dùng H2 Shell, rồi đóng lại trước khi bật app.
- **PowerShell `$env:X = ""` xoá luôn biến môi trường.** Spring không thay được `${X}` nên giữ nguyên chuỗi đó làm giá trị. Ví dụ đã gặp: lỗi `Wrong user name or password`. Cách tránh: dùng user DB có mật khẩu (`library_app`), hoặc truyền `--spring.datasource.password=` qua `-Dspring-boot.run.arguments`.
- **Sửa `pom.xml` thì phải tắt app rồi chạy lại.** DevTools chỉ tự restart khi đổi code Java hoặc file YAML.
- **`ddl-auto: update` không sửa cột đã tồn tại** (đổi `nullable`, `precision`…). Phải `DROP TABLE` rồi để Hibernate tạo lại, hoặc tự `ALTER`.
- **Jackson 3:** annotation vẫn ở `com.fasterxml.jackson.annotation`, phần lõi chuyển sang `tools.jackson`. Ở Spring Data 4, `PropertyReferenceException` nằm trong `org.springframework.data.core`.
- **Flyway + DevTools: tạo file migration trong khi app đang chạy.** Vừa tạo file rỗng là DevTools restart, Flyway chạy luôn file rỗng đó và ghi `checksum = 0, success = TRUE`. Khi viết nội dung vào thì app báo `Migration checksum mismatch`. Đừng dùng `repair`, vì nó chỉ cập nhật checksum, câu SQL vẫn không chạy. Cách sửa: tắt app, `DELETE FROM "flyway_schema_history" WHERE "version" = 'N'`, rồi chạy lại.
- **`@Query` JPQL không tự escape `%` `_` trong `LIKE`** (derived query thì có). Project đang dùng `ESCAPE '!'` cùng `escapeLike()` trong `BookService`.
