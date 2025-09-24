# Bondy Server — Monorepo Microservices (Spring Boot)

## Kiến trúc & thành phần

* **config-server**
  Quản lý cấu hình tập trung (Spring Cloud Config), lấy từ repo `bondy-config`.

* **discovery-server**
  Eureka Server để tất cả service đăng ký/tra cứu lẫn nhau.

* **gateway**
  Spring Cloud Gateway, làm entrypoint duy nhất: định tuyến request, xác thực JWT, CORS, lọc header.

* **services/**

    * **auth-service**
      Xử lý xác thực/ủy quyền: đăng ký, đăng nhập, phát hành & refresh JWT, hỗ trợ OAuth2 (Google/Discord).
    * **mail-service**
      Gửi email (OTP, template), kết nối SMTP với TLS.

* **common-web**
  Module thư viện chung: DTO, exception, JWT util, filter, constants. Được import vào các service để tránh code lặp lại.

* **libs/**
  Thư mục để mở rộng/thêm library nội bộ.

---

## Cây thư mục

```
bondy-server/
├─ config-server/         # Spring Cloud Config Server
├─ discovery-server/      # Eureka Discovery
├─ gateway/               # API Gateway
├─ common-web/            # Shared library
├─ services/
│  ├─ auth-service/       # Auth & JWT
│  └─ mail-service/       # Mail/OTP
├─ .env.example           # Biến môi trường mẫu
├─ pom.xml                # Parent pom (aggregator)
└─ README.md
```

---

## Yêu cầu hệ thống

* **JDK 21**
* **Maven Wrapper** (có sẵn `./mvnw`)
* **PostgreSQL/MySQL** cho `auth-service`
* **SMTP server** (ví dụ Gmail SMTP) cho `mail-service`

---

## Thiết lập nhanh

1. Clone dự án `bondy-server` và repo config riêng `bondy-config`.

2. Copy `.env.example` thành `.env`, cập nhật giá trị (DB, SMTP, JWT\_SECRET, …).

3. Build toàn bộ dự án:

   ```bash
   ./mvnw clean install -DskipTests
   ```

4. Chạy theo thứ tự:

    * `config-server`
    * `discovery-server`
    * `auth-service` + `mail-service`
    * `gateway`

5. Truy cập:

    * Eureka dashboard: [http://localhost:8761](http://localhost:8761)
    * Gateway entrypoint: [http://localhost:8080](http://localhost:8080)

---

## Luồng hoạt động

1. **Client** gửi request → **Gateway**.
2. **Gateway** kiểm tra JWT:

    * Nếu hợp lệ → forward đến service tương ứng.
    * Nếu không hợp lệ → trả 401.
3. **Auth-service** phát hành JWT & refresh token.
4. **Mail-service** gửi OTP qua email (ví dụ khi đăng ký).
5. **Common-web** cung cấp DTO/logic chung cho các service.

---

## Troubleshooting

* **Không push được code** → cần `git pull` trước rồi mới push.
* **Mail lỗi STARTTLS** → bật `mail.smtp.starttls.enable=true`.
* **JWT invalid** → kiểm tra `JWT_SECRET` đồng bộ ở `gateway` và `auth-service`.

---

## Định hướng mở rộng

* Thêm service: `user-service`, …
* Triển khai **Docker Compose** để chạy toàn bộ stack.
* Tích hợp **Keycloak** hoặc **Vault** cho quản lý secret nâng cao.
* CI/CD pipeline (GitHub Actions/GitLab CI).

---