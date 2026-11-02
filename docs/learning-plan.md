# 📚 Mini Library API: Kế hoạch học Spring Boot

> File này lưu đề bài, lộ trình và hướng dẫn từng step để tra cứu lại.
> Mỗi khi qua một step, mentor sẽ cập nhật phần **Tiến độ** và thêm hướng dẫn cho step tiếp theo.

## Đề bài

Xây một **REST API quản lý thư viện** gồm 2 đối tượng:

- **Author** (tác giả): `id`, `name`, `email`, `birthYear`
- **Book** (sách): `id`, `title`, `isbn`, `publishedYear`, `price`, thuộc về 1 Author

Quan hệ: **1 Author có nhiều Book**.

Khi xong, API phải làm được:
- Thêm, xem, sửa, xoá Author và Book
- Kiểm tra dữ liệu đầu vào (email đúng định dạng, `title` không để trống…)
- Trả lỗi rõ ràng và đồng nhất (404, 400…)
- Tìm sách theo tên, phân trang, sắp xếp
- Xem và test API qua giao diện Swagger

## Lộ trình

| Step | Nội dung | Bạn sẽ học |
|---|---|---|
| 0 | Chuẩn bị môi trường | IDE, Git, Postman |
| 1 | Tạo project + Hello API | Cấu trúc project, `@RestController`, `@GetMapping`, `@PathVariable`, `@RequestParam`, trả JSON |
| 2 | Kết nối DB (H2) + Entity `Author` | `application.yml`, datasource, JPA/Hibernate, `@Entity`, xem DB bằng DBeaver |
| 3 | CRUD Author | Kiến trúc Controller → Service → Repository, Dependency Injection |
| 4 | DTO + Validation | Tách Entity khỏi API, `@Valid`, `@NotBlank`, `@Email` |
| 5 | Xử lý lỗi tập trung | `@RestControllerAdvice`, custom exception, mã HTTP chuẩn |
| 6 | Entity `Book` + quan hệ | `@ManyToOne`, `@OneToMany`, lazy loading, lỗi N+1 |
| 7 | Truy vấn nâng cao | Derived query, `@Query`, phân trang, sắp xếp, tìm kiếm |
| 8 | Hoàn thiện | Profiles (dev/prod), logging, Swagger |
| 9 | *(Tuỳ chọn)* | Chuyển sang MySQL/PostgreSQL, Flyway, Spring Security/JWT, Docker |

**Quy trình:** mentor giao step → bạn tự code → nhắn **"review step X"** → mentor chỉ ra ✅ điểm tốt, ⚠️ điểm cần sửa, 💡 gợi ý → đạt thì sang step tiếp theo.

### Ghi chú về database
Máy không có quyền admin, và proxy công ty chặn tải PostgreSQL/MySQL. Vì vậy project dùng **H2** (database nhúng, lấy qua Maven Central, không cần cài đặt). Khi IT cài MySQL hoặc PostgreSQL thì chỉ cần đổi dependency và cấu hình (Step 9).

File DB (`data/`) **không** được đưa lên Git. Trên máy mới: chạy app một lần để Hibernate tạo bảng, rồi chạy [seed-data.sql](seed-data.sql) trong DBeaver. Hướng dẫn cho Claude nằm ở [CLAUDE.md](../CLAUDE.md).

---

## Tiến độ

| Step | Trạng thái | Ghi chú |
|---|---|---|
| 0 | ✅ Xong | Spring Boot 4.1.1, Maven Wrapper 3.10.0 |
| 1 | ✅ Đạt (review lần 2) | Điểm hay: `Instant` cho timestamp, switch expression, `toLowerCase()`. Còn nợ: thêm `server.port` vào yaml |
| 2 | ✅ Đạt (review lần 1) | Schema chuẩn, làm cả bonus `createdAt`. Còn nợ: thêm `getCreatedAt()`, format `@Column (`, bỏ `<scope>compile</scope>` thừa |
| 3 | ✅ Đạt (review lần 1) | Đủ 5 API, đúng status, có `Location`, check trùng email cả khi update. Lỗi chuyển sang Step 4: POST kèm `id` ghi đè author cũ; null-check chết trong controller |
| 4 | ✅ Đạt (review lần 2) | Đã sửa NPE, sửa PUT, PUT có kiểm tra năm sinh, bỏ null-check chết, format DTO. Còn nợ: tách `validateBirthYear()`, `update` chỉ lấy Entity một lần |
| 5 | ✅ Đạt (review lần 2) | Làm cả cách A lẫn B (`instanceof org.springframework.web.ErrorResponse`), `validateBirthYear()` đã tách. Còn nợ: chỉ `logger.error` khi thực sự là lỗi 500 |
| 6 | ✅ Đạt (review lần 3) | Quan hệ LAZY, sửa N+1 bằng `@EntityGraph`, tắt OSIV, 409 khi xoá author có sách, chuẩn hoá scale `price`. Giữ `/api/books/join-fetch` làm ví dụ (chủ động) |
| 7 | ✅ Đạt (review lần 2) | Tự đổi sang whitelist trường sort (tốt hơn cách mentor gợi ý). Mentor sửa giúp phần escape `%` `_` trong `LIKE`. Chủ động: sort mặc định `id DESC`, giữ code comment. Còn nợ: `throw ex` trong handler `InvalidDataAccessApiUsageException` |
| 8 | ✅ Đạt (review lần 3) | 3 profile, `LibraryProperties` + `@Validated`, log SQL kèm tham số, prod ghi log ra file và tắt Swagger. Mọi endpoint đều khai báo mã thành công/lỗi với schema `ErrorResponse`. Làm hết các điểm 💡 (bỏ email khỏi log, `@NotNull` cho `api`…). Không còn nợ |
| 9 | ⏳ Đang làm | Flyway: quản lý schema bằng migration |

---

## STEP 0: Chuẩn bị môi trường

1. **IDE**: IntelliJ IDEA Community (khuyên dùng), hoặc VS Code với "Extension Pack for Java" và "Spring Boot Extension Pack".
2. **Git** *(khuyến khích)*: sau mỗi step đạt review thì commit một lần.
3. **Công cụ test API**: Postman, `curl`, hoặc trình duyệt.
4. **DBeaver**: đã có sẵn, dùng từ Step 2.

---

## STEP 1: Tạo project và Hello API

### 1.1 Tạo project
Vào https://start.spring.io và chọn:
- Project: **Maven** · Language: **Java**
- Spring Boot: bản **stable mới nhất** (không chọn bản có chữ SNAPSHOT, M hay RC)
- Group: `com.<tên-bạn>` (ví dụ `com.sonnt`) · Artifact: `library`
- Packaging: **Jar** · Java: **21**
- Configuration: **YAML**
- Dependencies: chỉ chọn **Spring Web** và **Spring Boot DevTools**

> Chưa thêm JPA hay database vì nếu chưa cấu hình DB thì app sẽ không khởi động được. Step 2 bạn sẽ tự thêm dependency vào `pom.xml`.

Bấm **Generate**, rồi giải nén sao cho `pom.xml` nằm ngay trong `D:\Workspace\spring-boot\hello-sb\` (cùng cấp với thư mục `docs/`).

### 1.2 Chạy thử
```powershell
.\mvnw.cmd spring-boot:run
```
Mở http://localhost:8080. Nếu thấy trang "Whitelabel Error Page" là **đúng**: app đã chạy, chỉ là chưa có endpoint nào.

### 1.3 Nhiệm vụ
Tạo package `controller`, nằm cùng cấp với class `LibraryApplication`. Trong đó tạo class `HelloController` có 3 endpoint:

| Method | URL | Kết quả mong đợi |
|---|---|---|
| GET | `/api/hello` | Chuỗi `Hello Spring Boot!` |
| GET | `/api/hello/{name}` | `Hello, Son!` (lấy `name` từ đường dẫn) |
| GET | `/api/greet?name=Son&lang=vi` | JSON: `{"message": "Xin chào, Son!", "lang": "vi", "timestamp": "..."}` |

Yêu cầu cho endpoint `/api/greet`:
- `lang` **không bắt buộc**, mặc định là `en`. `vi` trả "Xin chào", `en` trả "Hello".
- Dữ liệu trả về là một **Java `record`** (ví dụ `GreetingResponse`), **không** tự ghép chuỗi JSON bằng tay.
- Dùng `@RequestMapping("/api")` ở mức class để không phải lặp lại `/api` ở từng method.

Thêm vào `application.yml`:
```yaml
spring:
  application:
    name: library
server:
  port: 8080
```

### 1.4 Câu hỏi tự tìm hiểu (trả lời ngắn khi gửi review)
1. `@SpringBootApplication` thực chất gồm những annotation nào?
2. `@RestController` khác `@Controller` ở điểm gì?
3. Vì sao bạn trả về một object `record` mà client nhận được JSON? Ai làm việc chuyển đổi đó?

### 💡 Gợi ý
- Tìm hiểu `@RequestParam(defaultValue = ...)`.
- `timestamp` có thể dùng `LocalDateTime.now()`.
- Có DevTools rồi thì sửa code xong chỉ cần build lại (Ctrl+F9 trong IntelliJ), app sẽ tự restart.

➡️ Làm xong thì nhắn **"review step 1"** kèm câu trả lời cho 3 câu hỏi.

---

## STEP 2: Kết nối database (H2) + Entity `Author`

**Mục tiêu:** app kết nối được database, Hibernate tự tạo bảng `authors` từ class Java, và bạn xem được bảng đó bằng DBeaver.

### 2.1 Thêm dependency
Mở `pom.xml` và thêm 2 dependency vào khối `<dependencies>`:
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `com.h2database:h2` với `<scope>runtime</scope>`

Không cần ghi `<version>`, vì `spring-boot-starter-parent` đã quản lý version cho bạn.
Sau đó reload Maven: trong VS Code là "Java: Reload Projects", hoặc chạy `.\mvnw.cmd compile`.

### 2.2 Cấu hình `application.yaml`
```yaml
spring:
  application:
    name: library
  datasource:
    url: jdbc:h2:file:./data/library_db;MODE=MySQL;AUTO_SERVER=TRUE
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
server:
  port: 8080
```
- `file:./data/library_db`: dữ liệu được lưu vào file `data/library_db.mv.db` trong project.
- `MODE=MySQL`: H2 hiểu cú pháp gần giống MySQL, giúp sau này chuyển sang MySQL dễ hơn.
- `AUTO_SERVER=TRUE`: cho phép DBeaver mở database **trong lúc app đang chạy**.

Nếu đã có Git, thêm dòng `data/` vào `.gitignore` để không commit file database.

### 2.3 Nhiệm vụ: tạo Entity `Author`
Tạo package `entity`, trong đó tạo class `Author` ánh xạ tới bảng **`authors`**:

| Field | Kiểu Java | Ràng buộc |
|---|---|---|
| `id` | `Long` | Khoá chính, tự tăng |
| `name` | `String` | Không null, tối đa 100 ký tự |
| `email` | `String` | Không null, **unique**, tối đa 150 ký tự |
| `birthYear` | `Integer` | Được phép null. Tên cột trong DB phải là `birth_year` |

Yêu cầu:
- Dùng các annotation `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`.
- Có **constructor không tham số**, cùng getter và setter. Viết tay hoặc để IDE sinh ra; **chưa dùng Lombok**.
- *(Bonus)* Thêm `createdAt` kiểu `Instant`, tự lưu thời điểm tạo bản ghi. Gợi ý: `@CreationTimestamp`.

⚠️ Import từ **`jakarta.persistence.*`**, **không phải** `javax.persistence.*`. Tutorial cũ (Spring Boot 2) dùng `javax`; từ Spring Boot 3 trở đi đã đổi sang `jakarta`.

### 2.4 Kiểm tra
1. Chạy app. Trong log console phải thấy câu `create table authors (...)` do Hibernate sinh ra.
2. Kiểm tra thư mục `data/` đã xuất hiện file `library_db.mv.db`.
3. Mở DBeaver → **New Database Connection** → **H2 Embedded V.2**:
   - JDBC URL: `jdbc:h2:file:D:/Workspace/spring-boot/hello-sb/data/library_db;AUTO_SERVER=TRUE`
   - User `sa`, password để trống. Lần đầu DBeaver sẽ hỏi tải driver, cứ đồng ý.
4. Mở bảng `AUTHORS` và kiểm tra các cột cùng ràng buộc (NOT NULL, UNIQUE).
5. Dùng SQL Editor `INSERT` thử 2 tác giả. Thử chèn **trùng email** và quan sát lỗi gì xảy ra.
6. Tắt app rồi chạy lại: dữ liệu vẫn còn chứ?

### 2.5 Câu hỏi tự tìm hiểu
1. `ddl-auto` có những giá trị nào (`none`, `validate`, `update`, `create`, `create-drop`)? Vì sao **không** nên dùng `update` trên production?
2. Vì sao JPA bắt buộc Entity phải có constructor không tham số?
3. **JPA**, **Hibernate** và **Spring Data JPA** khác nhau thế nào?
4. `GenerationType.IDENTITY` nghĩa là gì?

➡️ Làm xong thì nhắn **"review step 2"** kèm câu trả lời.

---

## STEP 3: CRUD Author (Controller → Service → Repository)

**Mục tiêu:** viết API thêm, xem, sửa, xoá Author theo kiến trúc 3 lớp, và hiểu Dependency Injection.

```
Client ──HTTP──▶ AuthorController ──▶ AuthorService ──▶ AuthorRepository ──▶ Database
                 (nhận request,       (logic nghiệp vụ,   (truy vấn DB,
                  trả response)        transaction)         Spring Data JPA)
```

Mỗi lớp chỉ gọi lớp ngay bên dưới nó. Controller **không** gọi thẳng Repository.

### 3.0 Trả nợ Step 2
- Thêm `getCreatedAt()` vào `Author`. Không cần setter, vì giá trị này do Hibernate tự gán.
- Format code, và bỏ `<scope>compile</scope>` trong `pom.xml`.

### 3.1 Repository
Tạo package `repository`, trong đó tạo **interface** `AuthorRepository` kế thừa `JpaRepository<Author, Long>`.
- Thêm một method dùng để kiểm tra email đã tồn tại chưa. Gợi ý: tìm hiểu **derived query**, tức là Spring tự sinh câu truy vấn từ tên method, ví dụ `existsBy...`.
- Bạn **không** viết class nào implement interface này.

### 3.2 Service
Tạo package `service`, trong đó tạo class `AuthorService` (đánh dấu `@Service`) với các method:

| Method | Việc cần làm |
|---|---|
| `findAll()` | Lấy tất cả tác giả |
| `findById(Long id)` | Lấy một tác giả. Nếu không tồn tại thì controller phải trả **404** |
| `create(Author author)` | Tạo mới. **Email trùng → không cho tạo** |
| `update(Long id, Author author)` | Sửa `name`, `email`, `birthYear`. **Không** được đổi `id` và `createdAt` |
| `delete(Long id)` | Xoá. Không tồn tại → 404 |

Yêu cầu:
- Inject Repository qua **constructor**, **không** dùng `@Autowired` trên field. Field nên là `private final`.
- Dùng `@Transactional` cho các method ghi dữ liệu, và `@Transactional(readOnly = true)` cho các method chỉ đọc.
- Báo lỗi tạm thời: ném `ResponseStatusException` với mã `NOT_FOUND` (404) hoặc `CONFLICT` (409, khi email trùng).
  > Đây là cách **tạm** cho dễ. Ném exception "của tầng web" từ Service là chưa đẹp, Step 5 sẽ thay bằng custom exception + `@RestControllerAdvice`.

### 3.3 Controller
Tạo `controller/AuthorController` với `@RequestMapping("/api/authors")`:

| Method | URL | Thành công | Lỗi |
|---|---|---|---|
| GET | `/api/authors` | 200 + danh sách | |
| GET | `/api/authors/{id}` | 200 + author | 404 |
| POST | `/api/authors` | **201 Created** + author vừa tạo (có `id`) | 409 nếu trùng email |
| PUT | `/api/authors/{id}` | 200 + author sau khi sửa | 404, 409 |
| DELETE | `/api/authors/{id}` | **204 No Content** | 404 |

Gợi ý: tìm hiểu `@RequestBody`, `ResponseEntity`, `@ResponseStatus`.
*(Bonus)* POST trả thêm header `Location: /api/authors/{id}`. Gợi ý: `ResponseEntity.created(...)`.

> Ở step này Controller nhận và trả **thẳng Entity** `Author`. Step 4 sẽ thay bằng DTO, và bạn sẽ hiểu vì sao cách hiện tại chưa tốt.

### 3.4 Kiểm tra bằng Postman/curl
1. `POST /api/authors` với body `{"name":"Nam Cao","email":"namcao@example.com","birthYear":1915}` → 201
2. POST lại **đúng body đó** → 409
3. `GET /api/authors` → thấy cả dữ liệu cũ bạn đã INSERT bằng DBeaver
4. `GET /api/authors/999` → 404
5. `PUT /api/authors/{id}` đổi `name` → 200. Sau đó GET lại xem `createdAt` có bị đổi không (không được đổi)
6. `DELETE /api/authors/{id}` → 204. DELETE lần nữa → 404
7. **Thử nghiệm:** POST với body `{"name": null, "email": "x@y.com"}` thì chuyện gì xảy ra? Ghi lại status code và message. Step 4 và 5 sẽ xử lý trường hợp này.

Xem log SQL trong console để thấy Hibernate sinh ra câu `insert`, `select`, `update`, `delete` thế nào.

### 3.5 Câu hỏi tự tìm hiểu
1. Vì sao nên inject qua **constructor** thay vì `@Autowired` trên field?
2. `AuthorRepository` chỉ là interface, bạn không hề viết class implement nó. Vậy ai tạo ra object đó lúc chạy?
3. `@Transactional` làm gì? `readOnly = true` có lợi ích gì?
4. Vì sao POST nên trả 201 còn DELETE trả 204? `PUT` khác `PATCH` thế nào?

➡️ Làm xong thì nhắn **"review step 3"** kèm câu trả lời.

---

## STEP 4: DTO + Validation

**Mục tiêu:** API không nhận và trả thẳng Entity nữa mà dùng DTO; dữ liệu sai bị chặn ngay ở cửa vào và trả **400**, thay vì 500.

**Hai lỗi thật ở Step 3 mà step này sẽ sửa:**
1. `POST /api/authors` với body có `"id": 8` → **ghi đè** author số 8 nhưng vẫn trả 201. Lý do: Jackson gán `id` vào Entity, rồi `save()` thấy có id nên coi đó là lệnh update.
2. `POST` với `"name": null` → **500**. Lỗi bị database chặn chứ không phải được kiểm tra ở cửa vào. Đúng ra đây là lỗi của client và phải trả 400.

```
Request JSON ──▶ AuthorRequest (DTO vào) ──@Valid──▶ Service ──▶ Author (Entity) ──▶ DB
Response JSON ◀── AuthorResponse (DTO ra) ◀──────── Service ◀── Author (Entity)
```

### 4.1 Thêm dependency
Thêm `spring-boot-starter-validation` vào `pom.xml` (không ghi version). Nhớ **chạy lại app**, vì `pom.xml` đã đổi.

### 4.2 DTO (dùng `record`, đặt trong package `dto`)
**`AuthorRequest`**: dữ liệu client gửi lên, gồm `name`, `email`, `birthYear`. **Không có** `id` và `createdAt`.

| Field | Ràng buộc |
|---|---|
| `name` | Bắt buộc, không được chỉ toàn dấu cách, tối đa 100 ký tự |
| `email` | Bắt buộc, đúng định dạng email, tối đa 150 ký tự |
| `birthYear` | Không bắt buộc. Nếu có thì ≥ 1000 |

Gợi ý annotation: `@NotBlank`, `@Email`, `@Size`, `@Min`. Ghi kèm `message` tiếng Việt dễ hiểu, ví dụ `message = "Tên không được để trống"`.

**`AuthorResponse`**: dữ liệu trả về cho client, gồm `id`, `name`, `email`, `birthYear`, `createdAt`.

### 4.3 Mapper
Tạo package `mapper`, trong đó tạo class `AuthorMapper` viết tay (chưa dùng MapStruct), gồm 3 việc:
- `AuthorRequest` → `Author` mới (dùng khi tạo)
- Cập nhật một `Author` có sẵn từ `AuthorRequest` (dùng khi sửa)
- `Author` → `AuthorResponse`

Tự chọn giữa `static` method và `@Component` được inject vào Service, rồi giải thích lý do khi gửi review.

### 4.4 Sửa Service, Controller, Entity
- **Service** nhận `AuthorRequest` và trả `AuthorResponse` (hoặc `List<AuthorResponse>`). Entity chỉ dùng **bên trong** Service, không lọt ra Controller.
- **Business validation:** nếu `birthYear` **lớn hơn năm hiện tại** thì trả 400. Gợi ý: `Year.now()`, ném `ResponseStatusException` với `BAD_REQUEST`.
  > Vì sao kiểm tra trong Service mà không dùng annotation? Vì "năm hiện tại" thay đổi theo thời gian, còn annotation chỉ nhận hằng số. Đây là ví dụ về sự khác nhau giữa **kiểm tra định dạng** (annotation) và **kiểm tra nghiệp vụ** (code trong Service).
- **Controller** nhận `@Valid @RequestBody AuthorRequest`, và **bỏ các đoạn `if (... == null)`** không bao giờ chạy tới (xem review Step 3).
- **Entity:** bỏ `setId()`, vì không còn ai cần gán id bằng tay. Đổi constructor rỗng thành `protected`, và có thể thêm một constructor nhận `name`, `email`, `birthYear`.

### 4.5 Kiểm tra
| # | Request | Kết quả mong đợi |
|---|---|---|
| 1 | POST body hợp lệ | 201, JSON có đủ 5 field |
| 2 | POST body có `"id": <id của author đang có>` | 201 với **id mới**, author cũ **không bị đổi** |
| 3 | POST body có `"createdAt": "2000-01-01T00:00:00Z"` | Giá trị đó bị bỏ qua |
| 4 | POST `"name": null` hoặc `"name": "   "` | **400** |
| 5 | POST `"email": "abc"` | **400** |
| 6 | POST `name` dài 101 ký tự | **400** |
| 7 | POST `"birthYear": 3000` | **400** |
| 8 | PUT với body sai | **400** |
| 9 | Các case 404/409 của Step 3 | Vẫn đúng |

Quan sát body của response 400 ở case 4 và 5: nó có cho client biết **field nào** sai không? Ghi lại nhận xét, Step 5 sẽ xử lý.

### 4.6 Câu hỏi tự tìm hiểu
1. Nêu ít nhất 3 lý do **không nên** trả thẳng Entity ra API. Lỗi ghi đè ở Step 3 là một lý do; hãy tìm thêm, ví dụ về bảo mật, về lazy loading (sẽ gặp ở Step 6), và về việc thay đổi DB làm ảnh hưởng tới API.
2. `@NotNull`, `@NotEmpty` và `@NotBlank` khác nhau thế nào?
3. `@Valid` khác `@Validated` thế nào?
4. Vì sao `record` rất hợp làm DTO nhưng **không** dùng được làm JPA Entity?

➡️ Làm xong thì nhắn **"review step 4"** kèm câu trả lời.

---

## STEP 5: Xử lý lỗi tập trung (Global Exception Handling)

**Mục tiêu:** mọi lỗi đều trả về **cùng một format JSON**, gọn và dễ hiểu, không lộ stack trace. Service không còn phụ thuộc vào tầng web.

**Tình trạng hiện tại:**
- Response lỗi rất dài, có cả `trace` (stack trace) lộ ra ngoài.
- Lỗi validation có thông tin field sai, nhưng bị chôn trong đống dữ liệu thừa.
- Service ném `ResponseStatusException`, tức là tầng nghiệp vụ đang "biết" về HTTP.
- Tình huống hai request trùng email đến cùng lúc (race condition) vẫn trả 500.

**Format lỗi mong muốn:**
```json
{
  "timestamp": "2026-10-14T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Dữ liệu không hợp lệ",
  "path": "/api/authors",
  "fieldErrors": {
    "name": "Name is required",
    "email": "Invalid email format"
  }
}
```
`fieldErrors` chỉ xuất hiện khi lỗi là lỗi validation. Các lỗi khác không có field này; gợi ý: `@JsonInclude`.

### 5.0 Trả nợ Step 4
- Tách phần kiểm tra năm sinh thành `private void validateBirthYear(Integer birthYear)`, dùng chung cho `create` và `update`.
- Trong `update`, chỉ lấy **Entity một lần** qua `getAuthorById(id)` rồi dùng nó để kiểm tra email. Bỏ lời gọi `findById(id)` trả DTO. Xem giải thích trong review.

### 5.1 Custom exception
Tạo package `exception`, trong đó tạo các class kế thừa **`RuntimeException`**:

| Exception | Dùng khi | HTTP |
|---|---|---|
| `ResourceNotFoundException` | Không tìm thấy dữ liệu | 404 |
| `DuplicateResourceException` | Dữ liệu bị trùng (email) | 409 |
| `BusinessException` | Vi phạm quy tắc nghiệp vụ (năm sinh trong tương lai) | 400 |

*(Bonus)* Thiết kế `ResourceNotFoundException` sao cho tái sử dụng được cho Book ở Step 6. Ví dụ, constructor nhận tên resource và id, rồi tự ghép thành message `"Author not found with id: 5"`.

### 5.2 `ErrorResponse`
Tạo record `ErrorResponse` trong package `dto` (hoặc `exception`) theo format ở trên.

### 5.3 `GlobalExceptionHandler`
Tạo class `GlobalExceptionHandler` có annotation `@RestControllerAdvice`, xử lý các exception sau:

| Exception | HTTP | Ghi chú |
|---|---|---|
| 3 custom exception ở 5.1 | 404 / 409 / 400 | |
| `MethodArgumentNotValidException` | 400 | Lấy danh sách field lỗi đưa vào `fieldErrors` |
| `HttpMessageNotReadableException` | 400 | JSON sai cú pháp, hoặc `birthYear: "abc"` |
| `MethodArgumentTypeMismatchException` | 400 | Ví dụ `GET /api/authors/abc` |
| `DataIntegrityViolationException` | 409 | Chốt chặn cuối cho race condition trùng email |
| `Exception` (mọi lỗi còn lại) | 500 | Message chung chung như `"Đã có lỗi xảy ra"`. **Không** trả `e.getMessage()` ra ngoài, nhưng **phải ghi log** đầy đủ |

Gợi ý:
- Dùng `@ExceptionHandler(...)`.
- Lấy `path` bằng cách nhận tham số `HttpServletRequest request` trong method handler.
- Ghi log bằng SLF4J: `private static final Logger log = LoggerFactory.getLogger(...)`.
- Viết một method `private` để dựng `ErrorResponse`, tránh lặp code ở mọi handler.

⚠️ **Cái bẫy:** sau khi thêm handler cho `Exception`, hãy thử `PATCH /api/authors/3` và `GET /api/xyz`. Đúng ra phải ra **405** và **404**. Nếu bạn thấy **500** thì nghĩa là handler `Exception` đang "nuốt" mất các exception mà Spring vốn tự xử lý. Hãy tìm hiểu `ResponseEntityExceptionHandler` hoặc tự xử lý riêng từng exception đó.

### 5.4 Sửa Service
- Thay toàn bộ `ResponseStatusException` bằng custom exception.
- Sau khi sửa, Service **không còn import gì** từ `org.springframework.http` hay `org.springframework.web`.

### 5.5 Kiểm tra
| # | Request | Kết quả mong đợi |
|---|---|---|
| 1 | `GET /api/authors/999` | 404, đúng format, **không có `trace`** |
| 2 | POST email trùng | 409 |
| 3 | POST `{"name":"","email":"abc"}` | 400, `fieldErrors` có **cả** `name` và `email` |
| 4 | POST `birthYear: 3000` | 400 |
| 5 | POST body `{"name": "A",` (JSON hỏng) | 400 |
| 6 | POST `"birthYear": "abc"` | 400 |
| 7 | `GET /api/authors/abc` | 400 |
| 8 | `PATCH /api/authors/3` | **405** (không được ra 500) |
| 9 | `GET /api/xyz` | **404** (không được ra 500) |
| 10 | Các request thành công | Vẫn như cũ |

### 5.6 Câu hỏi tự tìm hiểu
1. `@ControllerAdvice` khác `@RestControllerAdvice` thế nào?
2. Nếu một exception khớp với nhiều `@ExceptionHandler` (ví dụ `ResourceNotFoundException` vừa khớp handler riêng vừa khớp handler `Exception`), Spring chọn handler nào?
3. Vì sao với lỗi 500 thì **không** nên trả `e.getMessage()` hoặc stack trace cho client?
4. **`ProblemDetail`** (chuẩn RFC 9457) trong Spring là gì? Nó khác `ErrorResponse` bạn tự viết ở điểm nào? Ưu, nhược điểm của mỗi cách?

➡️ Làm xong thì nhắn **"review step 5"** kèm câu trả lời.

---

## STEP 6: Entity `Book` + quan hệ `@ManyToOne` / `@OneToMany`

**Mục tiêu:** tạo bảng `books` có khoá ngoại tới `authors`; hiểu lazy loading, **lỗi N+1** và cách sửa; hiểu `open-in-view`.

```
authors (1) ──────< books (N)
  id                  id
  name                title, isbn, published_year, price
  ...                 author_id  ──FK──▶ authors.id
```

### 6.0 Trả nợ Step 5
Trong handler `Exception`, chỉ gọi `logger.error(...)` khi lỗi **thực sự là 500**, tức là không phải exception Spring loại `ErrorResponse`. Hiện tại một request thiếu tham số (400) cũng bị ghi log ERROR kèm stack trace, làm rác log.

### 6.1 Entity `Book` (bảng `books`)
| Field | Kiểu Java | Ràng buộc |
|---|---|---|
| `id` | `Long` | Khoá chính, tự tăng |
| `title` | `String` | Không null, tối đa 200 ký tự |
| `isbn` | `String` | Không null, **unique**, tối đa 13 ký tự |
| `publishedYear` | `Integer` | Được phép null, tên cột `published_year` |
| `price` | **`BigDecimal`** | Không null, `precision = 10, scale = 2` |
| `author` | `Author` | `@ManyToOne(fetch = FetchType.LAZY, optional = false)` + `@JoinColumn(name = "author_id", nullable = false)` |
| `createdAt` | `Instant` | Giống `Author` |

Trong `Author`, thêm quan hệ ngược lại:
```java
@OneToMany(mappedBy = "author")
private List<Book> books = new ArrayList<>();
```
**Không** dùng `cascade` và **không** đưa `books` vào `AuthorResponse`.

### 6.2 DTO
**`BookRequest`**: gồm `title`, `isbn`, `publishedYear`, `price`, `authorId`.
| Field | Ràng buộc |
|---|---|
| `title` | Bắt buộc, tối đa 200 ký tự |
| `isbn` | Bắt buộc, gồm **đúng 10 hoặc 13 chữ số**. Gợi ý: `@Pattern(regexp = ...)` |
| `publishedYear` | Không bắt buộc, ≥ 1000, không được ở tương lai (kiểm tra nghiệp vụ như Author) |
| `price` | Bắt buộc, ≥ 0, tối đa 2 chữ số thập phân. Gợi ý: `@DecimalMin`, `@Digits` |
| `authorId` | Bắt buộc |

**`BookResponse`**: gồm `id`, `title`, `isbn`, `publishedYear`, `price`, `createdAt`, và **`author` là một object lồng nhau** chỉ có `id` + `name`:
```json
{ "id": 1, "title": "Chí Phèo", "isbn": "9786041234567", "publishedYear": 1941,
  "price": 85000.00, "author": { "id": 6, "name": "Nam Cao" }, "createdAt": "..." }
```
Gợi ý: tạo thêm record `AuthorSummary(Long id, String name)`.

### 6.3 Repository, Mapper, Service, Controller cho Book
Làm theo đúng khuôn của Author. Phần mới:

| Method | URL | Thành công | Lỗi |
|---|---|---|---|
| GET | `/api/books` | 200 | |
| GET | `/api/books/{id}` | 200 | 404 |
| POST | `/api/books` | 201 + `Location` | 400, **404 nếu `authorId` không tồn tại**, 409 nếu trùng ISBN |
| PUT | `/api/books/{id}` | 200 (cho phép đổi tác giả) | 400, 404, 409 |
| DELETE | `/api/books/{id}` | 204 | 404 |
| GET | **`/api/authors/{id}/books`** | 200 + danh sách sách của tác giả | 404 nếu tác giả không tồn tại |

Quy tắc mới cho Author:
- **Không cho xoá tác giả đang có sách** → 409, với message rõ ràng như `"Cannot delete author 6: author has 3 book(s)"`.
- Gợi ý: thêm method đếm hoặc kiểm tra sách theo author trong `BookRepository`, dùng derived query như `countByAuthorId`.
- Nghĩ xem: `AuthorService` cần dùng `BookRepository`. Như vậy có ổn không? Ghi ý kiến của bạn vào phần câu trả lời.

### 6.4 Thí nghiệm N+1 (quan trọng nhất của step này)
1. Tạo ít nhất **3 tác giả** và **6 cuốn sách** (rải đều các tác giả) qua API.
2. Gọi `GET /api/books`, rồi **đếm số câu `select`** trong log console.
   - Bạn sẽ thấy: 1 câu lấy danh sách sách, **cộng thêm 1 câu cho mỗi tác giả**. Đó chính là **N+1**.
3. Sửa trong `BookRepository` để toàn bộ chỉ còn **1 câu select** (có JOIN). Chọn **một** trong hai cách:
   - Override `findAll()` và thêm `@EntityGraph(attributePaths = "author")`
   - Viết `@Query("select b from Book b join fetch b.author")`
4. Đếm lại số câu select. Ghi số **trước và sau** vào phần trả lời.
5. Làm tương tự cho `GET /api/authors/{id}/books`.

### 6.5 Tắt `open-in-view`
Khi khởi động app, log có một dòng `WARN ... spring.jpa.open-in-view is enabled by default...`. Thêm vào `application.yaml`:
```yaml
spring:
  jpa:
    open-in-view: false
```
Chạy lại app và kiểm tra **mọi API vẫn chạy bình thường**. Nếu có API bị lỗi `LazyInitializationException`, nghĩa là code đang truy cập quan hệ lazy **bên ngoài transaction**. Hãy tìm và sửa.

### 6.6 Kiểm tra
| # | Request | Kết quả mong đợi |
|---|---|---|
| 1 | POST book hợp lệ | 201, có object `author` lồng bên trong |
| 2 | POST với `authorId: 999` | 404 |
| 3 | POST trùng ISBN | 409 |
| 4 | POST `isbn: "123"` hoặc `"abc1234567"` | 400 |
| 5 | POST `price: -1` hoặc `price: 10.999` | 400 |
| 6 | PUT đổi `authorId` sang tác giả khác | 200, `author` trong response đã đổi |
| 7 | `GET /api/authors/{id}/books` | Chỉ ra sách của tác giả đó |
| 8 | `GET /api/authors/999/books` | 404 |
| 9 | DELETE tác giả đang có sách | 409 |
| 10 | Xoá hết sách của tác giả đó, rồi DELETE tác giả | 204 |
| 11 | `GET /api/books` | Chỉ **1** câu select trong log |

### 6.7 Câu hỏi tự tìm hiểu
1. Trong quan hệ hai chiều Author ↔ Book, phía nào là **owning side**? `mappedBy` có ý nghĩa gì? Cột `author_id` nằm ở bảng nào, và vì sao?
2. Kiểu fetch mặc định của `@ManyToOne` là gì? Vì sao người ta khuyên luôn đặt `LAZY`?
3. **N+1** là gì? Vì sao trên production nó nguy hiểm, dù với 6 cuốn sách thì không thấy chậm?
4. `open-in-view` là gì? Vì sao Spring cảnh báo về nó, và vì sao nên tắt?
5. Vì sao `price` dùng `BigDecimal` mà không dùng `double`? (Thử chạy `System.out.println(0.1 + 0.2);`)

➡️ Làm xong thì nhắn **"review step 6"** kèm câu trả lời.

---

## STEP 7: Tìm kiếm, phân trang, sắp xếp

**Mục tiêu:** API danh sách không trả "tất cả" nữa mà trả **theo trang**, có **lọc** và **sắp xếp** theo tham số từ client. Bạn sẽ học `Pageable`, `Page`, JPQL `@Query` với tham số tuỳ chọn.

### 7.0 Chuẩn bị dữ liệu
Cần ít nhất **4 tác giả** và **15 cuốn sách** với giá và năm khác nhau thì mới thấy rõ phân trang. Có thể tạo qua API, hoặc `INSERT` bằng SQL trong DBeaver (nhớ tự điền `created_at`).

### 7.1 `PageResponse<T>`
Tạo record generic `PageResponse<T>` trong package `dto`:
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 10,
  "totalElements": 15,
  "totalPages": 2,
  "first": true,
  "last": false
}
```
Gợi ý: viết thêm một static method `PageResponse.from(Page<T> page)`.
> Vì sao không trả thẳng `Page<T>` của Spring? Hãy thử trả thẳng một lần và xem log có cảnh báo gì (câu hỏi 2).

### 7.2 Tìm kiếm sách: `GET /api/books`
Endpoint `GET /api/books` hiện có sẽ nhận thêm các tham số, **tất cả đều không bắt buộc**:

| Tham số | Ý nghĩa | Ví dụ |
|---|---|---|
| `title` | Tên sách **chứa** chuỗi này, **không phân biệt hoa thường** | `title=chi` khớp với "Chí Phèo"? (thử xem!) |
| `authorId` | Lọc theo tác giả | `authorId=6` |
| `minPrice`, `maxPrice` | Lọc theo khoảng giá (tính cả hai đầu) | `minPrice=50000&maxPrice=100000` |
| `page`, `size` | Phân trang. Mặc định `page=0`, `size=10` | `page=1&size=5` |
| `sort` | Sắp xếp. Mặc định theo `id` tăng dần | `sort=price,desc` hoặc `sort=title,asc&sort=price,desc` |

Yêu cầu:
- Controller nhận `Pageable`, kèm `@PageableDefault(size = 10, sort = "id")`.
- Gom các tham số lọc vào một record `BookSearchCriteria(title, authorId, minPrice, maxPrice)`.
  > Gợi ý: Spring tự gán query param vào record nếu bạn khai báo tham số kiểu record trong method controller **mà không có annotation**.
- Repository: viết **một** `@Query` JPQL xử lý được mọi tổ hợp tham số tuỳ chọn. Gợi ý mẫu: `(:title is null or lower(b.title) like lower(concat('%', :title, '%')))`.
- Query này **không được bị N+1**: thêm `@EntityGraph(attributePaths = "author")` vào method có `Pageable`.
- `minPrice > maxPrice` → 400 (kiểm tra nghiệp vụ trong Service).
- Trả về `PageResponse<BookResponse>`.

### 7.3 Tìm kiếm tác giả: `GET /api/authors`
- Tham số `name` (chứa chuỗi, không phân biệt hoa thường, không bắt buộc), cùng `page`, `size`, `sort`.
- Lần này dùng **derived query** thay vì `@Query`. Gợi ý: `findByNameContainingIgnoreCase(String name, Pageable pageable)`. Nghĩ cách xử lý khi `name` là null.
- Trả về `PageResponse<AuthorResponse>`.

### 7.4 Bảo vệ API
- **Giới hạn `size` tối đa là 50**, để client không gọi được `size=1000000`. Gợi ý: tìm thuộc tính `spring.data.web.pageable.max-page-size`.
- **`sort` theo field không tồn tại** (ví dụ `sort=abc`) hiện sẽ ra **500**. Hãy biến nó thành **400**. Gợi ý: xem log để biết đó là exception gì, rồi thêm handler.

### 7.5 Kiểm tra
| # | Request | Kết quả mong đợi |
|---|---|---|
| 1 | `GET /api/books` | 10 cuốn đầu tiên theo `id`, đủ các field của `PageResponse` |
| 2 | `?page=1&size=5` | Cuốn thứ 6 → 10, `first=false` |
| 3 | `?sort=price,desc` | Giá giảm dần |
| 4 | `?sort=title,asc&sort=price,desc` | Sắp theo tên, cùng tên thì theo giá |
| 5 | `?title=CHI` | Không phân biệt hoa thường |
| 6 | `?authorId=X` | Chỉ sách của tác giả X |
| 7 | `?minPrice=50000&maxPrice=100000` | Đúng khoảng giá, tính cả hai đầu |
| 8 | Kết hợp `title` + `authorId` + `sort` + `page` | Đúng cả bốn điều kiện |
| 9 | `?page=999` | 200, `content` rỗng |
| 10 | `?size=1000` | `size` trong response là **50** |
| 11 | `?sort=abc` | **400** (không được 500) |
| 12 | `?minPrice=100&maxPrice=10` | **400** |
| 13 | `GET /api/authors?name=nam&size=2` | Phân trang tác giả đúng |
| 14 | Xem log khi gọi #8 | Chỉ có **1 câu select có join + 1 câu count**, không N+1 |

### 7.6 Câu hỏi tự tìm hiểu
1. `Page`, `Slice` và `List` khác nhau thế nào? Vì sao `Page` cần thêm một câu `count`? Khi nào nên dùng `Slice`?
2. Vì sao Spring Data cảnh báo khi trả thẳng `Page<T>` (cụ thể là `PageImpl`) ra JSON?
3. Vì sao phải giới hạn `size` tối đa?
4. Với tiếng Việt có dấu, `title=chi` có khớp với "Chí Phèo" không? Vì sao? Nếu muốn khớp thì cần làm gì? (chỉ cần nêu hướng, không cần code)
5. Phân trang kiểu `page` (offset) chậm thế nào khi `page=100000`? **Keyset pagination** (cursor) là gì?

➡️ Làm xong thì nhắn **"review step 7"** kèm câu trả lời.

---

## STEP 8: Profiles, cấu hình, logging, Swagger

**Mục tiêu:** app chạy được ở nhiều môi trường (dev/prod) với cấu hình khác nhau, không để lộ mật khẩu trong code; log chuyên nghiệp; có tài liệu API tự sinh (Swagger UI).

### 8.0 Trả nợ Step 7
Trong handler `InvalidDataAccessApiUsageException`, câu `throw ex;` **không** chuyển sang handler `Exception` như comment đang ghi. Exception ném ra từ một `@ExceptionHandler` sẽ thoát khỏi `@RestControllerAdvice`, và Spring trả về trang lỗi mặc định (sai format, có thể kèm `trace`). Thêm nữa, `ex.getCause().getMessage()` sẽ văng NPE nếu `cause` là null. Chọn một trong hai:
- **Xoá hẳn handler này.** Whitelist đã chặn mọi lỗi sort từ trước rồi.
- **Hoặc thay `throw ex;` bằng `return handleUnexpectedException(ex, request);`** và bỏ dòng `logger.error` ở đầu handler.

### 8.1 Profiles
Tách cấu hình thành 3 file trong `src/main/resources`:

| File | Chứa gì |
|---|---|
| `application.yaml` | Cấu hình **chung**: tên app, `open-in-view`, `max-page-size`… và `spring.profiles.active: dev` (profile mặc định) |
| `application-dev.yaml` | H2 file như hiện tại, `ddl-auto: update`, log SQL bật |
| `application-prod.yaml` | `ddl-auto: validate`, log SQL tắt, **datasource lấy từ biến môi trường**: `url: ${DB_URL}`, `username: ${DB_USERNAME}`, `password: ${DB_PASSWORD}` |

Thử chạy profile prod. Trước hết **không đặt** biến môi trường nào, rồi chạy:
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```
Quan sát lỗi khởi động. Sau đó đặt biến môi trường trỏ tới **chính file H2 hiện tại** rồi chạy lại:
```powershell
$env:DB_URL="jdbc:h2:file:./data/library_db;MODE=MySQL;AUTO_SERVER=TRUE"; $env:DB_USERNAME="sa"; $env:DB_PASSWORD=""
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```
Với `ddl-auto: validate`, Hibernate sẽ **kiểm tra Entity có khớp với schema không**. Thử đổi tạm `length` của một `@Column` để xem nó báo lỗi thế nào, rồi đổi lại.

### 8.2 Cấu hình riêng với `@ConfigurationProperties`
Tạo record `LibraryProperties` với prefix `library`, dùng để chứa thông tin cho Swagger:
```yaml
library:
  api:
    title: Mini Library API
    version: 1.0.0
    description: REST API quản lý tác giả và sách
```
- Gợi ý: `@ConfigurationProperties(prefix = "library")`, kết hợp với `@ConfigurationPropertiesScan` (hoặc `@EnableConfigurationProperties`) ở class main.
- Thêm `@Validated` cùng `@NotBlank` cho `title`. Thử xoá `title` khỏi YAML: app phải **không khởi động được** và báo lỗi rõ ràng.

### 8.3 Logging
- **Bỏ `show-sql: true` và `format_sql`.** Thay bằng log level trong `application-dev.yaml`:
  ```yaml
  logging:
    level:
      com.example.library: debug
      org.hibernate.SQL: debug
      org.hibernate.orm.jdbc.bind: trace   # in ra giá trị tham số của câu SQL
  ```
- Trong `application-prod.yaml`: `com.example.library: info`, không log SQL, và ghi log ra file `logs/library.log` (gợi ý: `logging.file.name`). Thêm `logs/` vào `.gitignore` nếu có Git.
- Thêm log vào `AuthorService` và `BookService`:
  - `log.info` khi **tạo, sửa, xoá** thành công, ghi kèm `id`. Ví dụ: `"Created book id={} isbn={}"`.
  - `log.debug` cho các thông tin chi tiết hơn, ví dụ điều kiện tìm kiếm.
  - Dùng placeholder `{}`, **không** nối chuỗi bằng `+`.

### 8.4 Swagger (springdoc-openapi)
Thêm dependency:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.1.1</version>
</dependency>
```
> ⚠️ Lần này **phải** ghi `<version>`, vì springdoc không do Spring Boot quản lý. springdoc 3.x là dòng dành cho Spring Boot 4.

1. Chạy app rồi mở http://localhost:8080/swagger-ui.html, và xem JSON ở http://localhost:8080/v3/api-docs.
2. Tạo class `OpenApiConfig` có `@Bean OpenAPI`, lấy `title`, `version`, `description` từ `LibraryProperties`.
3. Làm tài liệu API đẹp hơn:
   - `@Tag(name = "Books")` trên controller; `@Operation(summary = "...")` trên mỗi endpoint.
   - `@ApiResponse` cho các mã lỗi chính (400, 404, 409), với schema là `ErrorResponse`.
   - `@Schema(example = "...")` trên các field của `BookRequest` và `AuthorRequest`.
   - Đặt **`@ParameterObject`** trước `Pageable` và `BookSearchCriteria`. Thử bỏ đi để thấy sự khác biệt: không có annotation này thì Swagger hiển thị `pageable` như một object JSON khó dùng.
4. **Tắt Swagger ở profile prod**: `springdoc.api-docs.enabled: false` và `springdoc.swagger-ui.enabled: false`.

### 8.5 Kiểm tra
| # | Việc | Kết quả mong đợi |
|---|---|---|
| 1 | Chạy mặc định (không chỉ định profile) | Log có `The following 1 profile is active: "dev"` |
| 2 | Profile prod, thiếu biến môi trường | Không khởi động được. Lỗi là `'url' must start with "jdbc"`: Spring giữ nguyên chuỗi `${DB_URL}` làm giá trị, chứ không báo thẳng là thiếu biến |
| 3 | Profile prod, có biến môi trường | Chạy được, schema được validate, không log SQL, có file `logs/library.log` |
| 4 | Đổi `length` của một cột rồi chạy prod | Lỗi `Schema-validation: ...` |
| 5 | Xoá `library.api.title` | Không khởi động được, báo rõ field bị thiếu |
| 6 | Dev: gọi `GET /api/books?title=java` | Log SQL qua logger `org.hibernate.SQL`, có cả giá trị tham số |
| 7 | Tạo/sửa/xoá sách | Có dòng log `INFO` kèm id |
| 8 | Dev: mở `/swagger-ui.html` | Thấy 2 nhóm Authors/Books, có ví dụ request, có mã lỗi |
| 9 | Trên Swagger UI: bấm "Try it out" gọi `GET /api/books` với `page`, `size`, `sort` | Chạy được (nhờ `@ParameterObject`) |
| 10 | Prod: mở `/swagger-ui.html` | 404 |

### 8.6 Câu hỏi tự tìm hiểu
1. Thứ tự ưu tiên khi cùng một thuộc tính được khai báo ở `application.yaml`, `application-dev.yaml`, biến môi trường và tham số dòng lệnh: cái nào thắng?
2. Vì sao không được ghi mật khẩu DB thật vào `application-prod.yaml` rồi commit lên Git? Cú pháp `${DB_PASSWORD:default}` hoạt động thế nào, và vì sao **không** nên đặt default cho mật khẩu?
3. `show-sql: true` khác `logging.level.org.hibernate.SQL: debug` thế nào? Vì sao cách thứ hai tốt hơn?
4. Các log level `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` dùng khi nào? Những thông tin nào **không bao giờ** được ghi ra log?
5. `@Value` khác `@ConfigurationProperties` thế nào? Khi nào dùng cái nào?
6. Vì sao nên tắt (hoặc bảo vệ) Swagger trên production?

➡️ Làm xong thì nhắn **"review step 8"** kèm câu trả lời.

---

## STEP 9: Quản lý schema bằng Flyway

**Mục tiêu:** từ nay schema database do **file migration SQL** (có version, nằm trong Git) quyết định, chứ không để Hibernate tự sinh nữa. Mọi thay đổi schema đều được ghi lại, chạy theo đúng thứ tự, và giống nhau trên mọi máy và mọi môi trường.

> Vì sao chọn Flyway cho Step 9? Trong các mục tuỳ chọn ở Lộ trình, MySQL/PostgreSQL và Docker đang bị chặn bởi proxy và quyền admin. Flyway chạy được ngay với H2. Khi sau này đổi sang MySQL/PostgreSQL thì Flyway cũng là công cụ dùng để tạo schema trên DB mới.

### 9.0 Trả nợ Step 8
Không còn nợ 🎉

### 9.1 Thêm dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
```
- **Không** ghi `<version>`, vì Spring Boot 4.1.1 đã quản lý (Flyway 12.4). H2 được hỗ trợ sẵn trong `flyway-core`, không cần thêm module.
- ⚠️ Ở Boot 4, phải dùng **starter**. Nếu chỉ thêm `flyway-core` như các bài hướng dẫn cũ thì Flyway sẽ **không tự chạy**.
- Nhớ: sửa `pom.xml` thì phải tắt app rồi chạy lại.

### 9.2 Bắt đầu với database trống
DB hiện tại do Hibernate tạo (`ddl-auto: update`). Để Flyway dựng lại từ đầu:
1. Tắt app và **Disconnect DBeaver**.
2. Đổi tên thư mục `data` thành `data-backup-step8`. Đây là bản backup: nếu hỏng thì đổi tên lại là có DB cũ.
3. Chưa chạy app vội. Làm tiếp 9.3 và 9.4 trước.

### 9.3 Migration đầu tiên: `V1__create_tables.sql`
Tạo thư mục `src/main/resources/db/migration` (thư mục mặc định Flyway đọc), rồi tạo file `V1__create_tables.sql` chứa lệnh `CREATE TABLE` cho `authors` và `books`: khoá chính, `NOT NULL`, unique email, unique ISBN `uk_books_isbn`, khoá ngoại `author_id`.

- Tên file: `V` + version + **hai** dấu gạch dưới + mô tả.
- **Kiểu cột phải khớp Entity**, vì Hibernate sẽ `validate`. Đừng đoán kiểu, hãy copy từ DDL mà Hibernate đã sinh:
  - mở DB backup trong DBeaver và chạy lệnh `SCRIPT NO DATA`;
  - hoặc chuột phải vào bảng, chọn *Generate SQL → DDL*.
- Chú ý kiểu của cột `created_at` (`Instant`) và `price`. Đây là hai chỗ dễ sai nhất.

### 9.4 Dữ liệu mẫu: `V2__seed_data.sql`
- Chuyển phần `INSERT` tác giả và sách từ [seed-data.sql](seed-data.sql) vào `V2__seed_data.sql`.
- Trong `docs/seed-data.sql` chỉ giữ lại lệnh `CREATE USER library_app ...` (user DB là việc của người quản trị, không phải của app), và sửa comment đầu file cho đúng.
- Trong project này, dev và prod dùng chung một file H2, nên để seed trong migration chung cho đơn giản. Thực tế sẽ khác: xem câu hỏi 5.

### 9.5 Giao schema cho Flyway
- `application-dev.yaml`: đổi `ddl-auto: update` thành **`validate`**. Hibernate chỉ còn **kiểm tra**, không tạo hay sửa bảng nữa.
- Chạy app. Đọc log để thấy Flyway chạy V1, V2, rồi Hibernate validate thành công.
- Mở DBeaver và xem bảng mới `flyway_schema_history`.
- Chạy lại lệnh `CREATE USER` trong `docs/seed-data.sql`, vì DB mới chưa có user `library_app` dùng cho prod.

### 9.6 Sửa một lỗi schema thật bằng migration: `V3__...`
Mentor phát hiện: `AuthorRequest` cho phép `name` dài tối đa **100** ký tự (`@Size(max = 100)`), nhưng cột `authors.name` chỉ có **90** (`@Column(length = 90)`).
1. **Trước khi sửa**, tạo một author có tên dài khoảng 95 ký tự. Ghi lại status và message mà API trả về. Vì sao lại ra mã đó?
2. Tạo `V3__widen_author_name.sql` để nới cột lên 100. Gợi ý: tìm cú pháp `ALTER TABLE ... ALTER COLUMN ...` của H2.
3. Sửa `length` trong `Author` cho khớp.
4. Chạy lại: Flyway chỉ chạy **V3** (V1 và V2 đã chạy rồi), validate qua, và tạo lại author tên 95 ký tự thì thành công.

> Nhắc lại bẫy đã gặp: `ddl-auto: update` **không** sửa cột đã tồn tại. Đây chính là việc Flyway làm được.

### 9.7 Thí nghiệm: sửa migration đã chạy
Thêm một dòng comment `-- test` vào `V1__create_tables.sql` rồi chạy lại app. Quan sát lỗi. Sau đó **xoá dòng vừa thêm** để app chạy lại bình thường.

### 9.8 Kiểm tra
| # | Việc | Kết quả mong đợi |
|---|---|---|
| 1 | Chạy dev lần đầu với DB trống | Log Flyway áp dụng V1, V2, V3; `flyway_schema_history` có 3 dòng thành công |
| 2 | Chạy lại lần nữa | Log `Schema "PUBLIC" is up to date. No migration necessary.` |
| 3 | `GET /api/books` | 15 cuốn từ V2 |
| 4 | Tên author 95 ký tự, **trước** V3 | Ghi lại kết quả (đoán trước được không?) |
| 5 | Tên author 95 ký tự, **sau** V3 | 201 |
| 6 | Tên author 101 ký tự | 400 do validation (không phải lỗi DB) |
| 7 | Sửa V1 sau khi đã chạy | Không khởi động được, lỗi **checksum mismatch** |
| 8 | Profile prod với `library_app` | Chạy được: Flyway báo up to date, Hibernate validate qua |
| 9 | Đổi tạm `length` của một `@Column` (ví dụ `isbn`) | Hibernate validate báo lỗi, cả ở **dev**. Nhớ đổi lại |

### 9.9 Câu hỏi tự tìm hiểu
1. Flyway khác `ddl-auto: update` thế nào? Vì sao gần như không ai dùng `update` (hay `create`) trên production?
2. Vì sao **không được sửa** file migration đã chạy? Checksum dùng để làm gì? Nếu migration đã chạy bị sai thì phải làm gì?
3. Khi khởi động, Flyway chạy **trước** hay **sau** Hibernate? Vì sao thứ tự này quan trọng khi dùng `ddl-auto: validate`?
4. `spring.flyway.baseline-on-migrate` dùng trong trường hợp nào? (Gợi ý: nếu ở 9.2 ta **giữ** DB cũ, đã có bảng sẵn, thì sao?)
5. Dữ liệu mẫu chỉ dành cho dev thì nên để ở đâu để prod không chạy nó? (Gợi ý: `spring.flyway.locations` theo profile.) Nếu dev và prod dùng **chung một DB** như project này thì cách đó vướng gì?
6. Migration **versioned** (`V...`) và **repeatable** (`R__...`) khác nhau thế nào? Nêu một ví dụ nên dùng repeatable.

➡️ Làm xong thì nhắn **"review step 9"** kèm câu trả lời.
