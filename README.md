# Fashion Store E-Commerce

## Thiết lập thông tin bảo mật

Dự án này yêu cầu một số thông tin bảo mật như API keys, thông tin cơ sở dữ liệu, và thông tin đăng nhập email. Những thông tin này không nên được commit lên git repository.

### Hướng dẫn thiết lập thông tin bảo mật

1. Tạo file `application-private.properties` trong thư mục `src/main/resources/`:
   - Sao chép nội dung từ file `application-private.properties.example`
   - Cung cấp các giá trị thực tế cho các thuộc tính nhạy cảm

2. Các thông tin nhạy cảm cần được cấu hình:
   - Thông tin kết nối cơ sở dữ liệu
   - JWT Secret key
   - Thông tin tài khoản email
   - API keys cho các dịch vụ thanh toán (SePay)
   - Các thông tin bảo mật khác

3. File `.gitignore` đã được cấu hình để bỏ qua các file chứa thông tin bảo mật:
   - `application-private.properties`
   - `application-dev.properties`
   - `application-prod.properties`
   - Các file chứng chỉ và khóa bảo mật (*.jks, *.pem, *.key, *.p12)
   - Thư mục uploads

### Thiết lập profile cho các môi trường

Để chạy ứng dụng với profile cụ thể:

```bash
# Development
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Production
./mvnw spring-boot:run -Dspring.profiles.active=prod

# Sử dụng thông tin bảo mật
./mvnw spring-boot:run -Dspring.profiles.active=private
```

**Quan trọng**: Không bao giờ commit các thông tin nhạy cảm lên repository. Nếu vô tình làm vậy, hãy thay đổi các khóa/mật khẩu ngay lập tức.

## Hướng dẫn cài đặt và chạy dự án

[Thêm hướng dẫn cài đặt và chạy dự án ở đây] 