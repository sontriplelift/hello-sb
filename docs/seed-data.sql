-- User riêng cho profile prod (Step 8, "Cách A"). Chỉ dùng cho máy local.
-- Khi chạy prod: $env:DB_USERNAME="library_app"; $env:DB_PASSWORD="Library@123"
CREATE USER IF NOT EXISTS library_app PASSWORD 'Library@123' ADMIN;
