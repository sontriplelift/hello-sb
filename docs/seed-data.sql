-- Dữ liệu mẫu cho Mini Library API (H2, chạy trong DBeaver).
-- Điều kiện: đã chạy app ít nhất một lần với profile dev, để Hibernate tạo sẵn bảng authors và books.
-- Không ghi id: để IDENTITY tự sinh. Sách tham chiếu tác giả qua email.
-- Chạy lại nhiều lần sẽ lỗi trùng email/ISBN: đó là dấu hiệu dữ liệu đã có sẵn.

INSERT INTO authors (name, email, birth_year, created_at) VALUES
  ('Nguyen Van An', 'an.nguyen@example.com', 1990, CURRENT_TIMESTAMP),
  ('Tran Thi Binh', 'binh.tran@example.com', 1985, CURRENT_TIMESTAMP),
  ('Le Quang Huy',  'huy.le@example.com',    1995, CURRENT_TIMESTAMP),
  ('Pham Thu Ha',   'ha.pham@example.com',   1988, CURRENT_TIMESTAMP);

INSERT INTO books (title, isbn, published_year, price, author_id, created_at) VALUES
  ('Java Fundamentals',          '9780134685991', 2020, 250000.00, (SELECT id FROM authors WHERE email = 'an.nguyen@example.com'), CURRENT_TIMESTAMP),
  ('Spring Boot in Practice',    '9781617298813', 2022, 320000.00, (SELECT id FROM authors WHERE email = 'an.nguyen@example.com'), CURRENT_TIMESTAMP),
  ('Clean Code',                 '9780132350884', 2008, 450000.00, (SELECT id FROM authors WHERE email = 'an.nguyen@example.com'), CURRENT_TIMESTAMP),
  ('Effective Java',             '9780134686097', 2018, 380000.00, (SELECT id FROM authors WHERE email = 'an.nguyen@example.com'), CURRENT_TIMESTAMP),
  ('Database Design Basics',     '9780321884497', 2019, 290000.00, (SELECT id FROM authors WHERE email = 'binh.tran@example.com'), CURRENT_TIMESTAMP),
  ('SQL for Beginners',          '9780134853987', 2021, 220000.00, (SELECT id FROM authors WHERE email = 'binh.tran@example.com'), CURRENT_TIMESTAMP),
  ('MySQL Cookbook',             '9781492077442', 2020, 410000.00, (SELECT id FROM authors WHERE email = 'binh.tran@example.com'), CURRENT_TIMESTAMP),
  ('Data Modeling Essentials',   '9780123821902', 2017, 350000.00, (SELECT id FROM authors WHERE email = 'binh.tran@example.com'), CURRENT_TIMESTAMP),
  ('Design Patterns',            '9780201633610', 1994, 520000.00, (SELECT id FROM authors WHERE email = 'huy.le@example.com'),    CURRENT_TIMESTAMP),
  ('Refactoring',                '9780134757599', 2018, 480000.00, (SELECT id FROM authors WHERE email = 'huy.le@example.com'),    CURRENT_TIMESTAMP),
  ('Head First Design Patterns', '9780596007126', 2004, 390000.00, (SELECT id FROM authors WHERE email = 'huy.le@example.com'),    CURRENT_TIMESTAMP),
  ('Test Driven Development',    '9780321146533', 2002, 300000.00, (SELECT id FROM authors WHERE email = 'huy.le@example.com'),    CURRENT_TIMESTAMP),
  ('REST API Design',            '9781492026921', 2020, 280000.00, (SELECT id FROM authors WHERE email = 'ha.pham@example.com'),   CURRENT_TIMESTAMP),
  ('Microservices Architecture', '9781617294549', 2018, 420000.00, (SELECT id FROM authors WHERE email = 'ha.pham@example.com'),   CURRENT_TIMESTAMP),
  ('Cloud Native Java',          '9781449374648', 2017, 460000.00, (SELECT id FROM authors WHERE email = 'ha.pham@example.com'),   CURRENT_TIMESTAMP);

-- User riêng cho profile prod (Step 8, "Cách A"). Chỉ dùng cho máy local.
-- Khi chạy prod: $env:DB_USERNAME="library_app"; $env:DB_PASSWORD="Library@123"
CREATE USER IF NOT EXISTS library_app PASSWORD 'Library@123' ADMIN;
