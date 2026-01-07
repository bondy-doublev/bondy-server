# Bondy Server — Monorepo Microservices Architecture

Bondy là một hệ thống backend **microservices** hiện đại cho nền tảng mạng xã hội, kết hợp nhiều công nghệ để tối ưu hiệu suất và khả năng mở rộng.

### Công nghệ chính theo service

- **Java Spring Boot** (phần lớn services): auth, interaction, notification, upload, mail, moderation, ...
- **NestJS** (TypeScript/Node.js): communication-service, bondy-proxy
- **Python** (FastAPI + Uvicorn): recommendation-system

## Thành phần hệ thống

* **config-server** (Spring Boot)  
  Quản lý cấu hình tập trung (Spring Cloud Config), lấy từ repo riêng `bondy-config`.

* **discovery-server** (Spring Boot)  
  Eureka Server để các service đăng ký và khám phá lẫn nhau.

* **gateway** (Spring Boot)  
  Spring Cloud Gateway – entrypoint duy nhất: định tuyến request, xác thực JWT, CORS, rate limiting, lọc header.

* **services/**

    * **auth-service** (Spring Boot)  
      Quản lý tài khoản người dùng, API key, đăng ký/đăng nhập, phát hành & refresh JWT, hỗ trợ OAuth2 (Google, Discord,...).

    * **bondy-proxy** (NestJS)  
      Proxy server xử lý các request proxy (reverse proxy, caching, load balancing tùy nhu cầu).

    * **bondy-recommendation-system** (Python FastAPI + Uvicorn)  
      Hệ thống gợi ý bài viết, reel, nội dung dựa trên machine learning/user behavior.

    * **communication-service** (NestJS)  
      Quản lý chat thời gian thực, voice/video call, quảng cáo trong call/chat, tích hợp chatbot.

    * **interaction-service** (Spring Boot)  
      Quản lý tương tác mạng xã hội: like, comment, share, view post/reel/story, follow/unfollow,...

    * **mail-service** (Spring Boot)  
      Gửi email (OTP, thông báo, newsletter), hỗ trợ template và SMTP với TLS.

    * **moderation-service** (Spring Boot)  
      Kiểm duyệt nội dung: phát hiện spam, toxic content, báo cáo vi phạm (bug/report), tự động/mod manual.

    * **notification-service** (Spring Boot)  
      Gửi thông báo push/real-time (WebSocket, Firebase Cloud Messaging,...).

    * **upload-service** (Spring Boot)  
      Xử lý upload file/media (image, video, reel), lưu trữ (local/S3), resize/thumbnail, virus scan.

* **common-web** (Java)  
  Module thư viện chung cho các service Spring Boot: DTO, exception handler, JWT util, filter, constants.

* **libs/**  
  Thư mục chứa các thư viện nội bộ mở rộng (nếu cần).

## Cây thư mục (cập nhật)

```
bondy-server/
├─ config-server/                  # Spring Cloud Config
├─ discovery-server/               # Eureka
├─ gateway/                        # API Gateway
├─ common-web/                     # Shared Java lib
├─ services/
│  ├─ auth-service/                # User & Auth (Spring Boot)
│  ├─ bondy-proxy/                 # Proxy (NestJS)
│  ├─ bondy-recommendation-system/ # Recommendation (Python FastAPI)
│  ├─ communication-service/       # Chat/Call/Chatbot (NestJS)
│  ├─ interaction-service/         # Social interactions (Spring Boot)
│  ├─ mail-service/                # Email (Spring Boot)
│  ├─ moderation-service/          # Content moderation (Spring Boot)
│  ├─ notification-service/        # Notifications (Spring Boot)
│  └─ upload-service/              # File upload (Spring Boot)
├─ .env.example
├─ .gitignore
├─ docker-compose.yml              # (Sắp triển khai)
├─ pom.xml                         # Parent Maven (cho Java modules)
└─ README.md
```

## Yêu cầu hệ thống

- **JDK 21** (cho các service Spring Boot)
- **Node.js 18+ & npm/yarn/pnpm** (cho NestJS services)
- **Python 3.11+** (cho recommendation-system)
- **Maven Wrapper** (`./mvnw`) cho Java
- **PostgreSQL/MySQL** (cho auth, interaction,...)
- **Redis** (cache, real-time nếu cần)
- **SMTP server** cho mail-service
- **Docker & Docker Compose** (khuyến khích cho dev/prod)

## Thiết lập & biến môi trường

1. Clone dự án và repo config `bondy-config` (nếu dùng Config Server).

2. Copy `.env.example` → `.env`, chỉnh sửa các biến:
    - DB_URL, DB_USERNAME, DB_PASSWORD
    - JWT_SECRET (phải đồng bộ giữa auth-service và gateway)
    - SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS
    - REDIS_HOST
    - Các secret khác: OAuth client ID/secret, API keys,...

3. Cài dependencies:
   ```bash
   # Java modules
   ./mvnw clean install -DskipTests

   # NestJS services (communication & proxy)
   cd services/communication-service && npm install
   cd services/bondy-proxy && npm install

   # Python recommendation
   cd services/bondy-recommendation-system
   pip install -r requirements.txt
   ```

## Chạy hệ thống

Thứ tự khởi động quan trọng:

1. **config-server**
2. **discovery-server**
3. Các service khác (có thể song song):
    - Spring Boot: `./mvnw spring-boot:run` trong từng module hoặc dùng IDE.
    - NestJS: `npm run start:dev` (hoặc `nest start`)
    - Python recommend: `uvicorn main:app --reload --port <port>`

4. Cuối cùng: **gateway**

Truy cập:
- Eureka dashboard: http://localhost:8761
- API Gateway: http://localhost:8080 (hoặc port đã config)

## Luồng hoạt động cơ bản

1. Client → **gateway** (xác thực JWT).
2. Gateway route đến service phù hợp qua Eureka discovery.
3. Auth-service xử lý login → trả JWT.
4. Interaction/upload/notification... xử lý nghiệp vụ social.
5. Communication (NestJS) xử lý real-time chat/call.
6. Recommendation (Python) cung cấp gợi ý cá nhân hóa.

## Troubleshooting thường gặp

- Service không đăng ký trên Eureka → kiểm tra `application.yml` có `eureka.client.service-url`.
- JWT invalid → đảm bảo `JWT_SECRET` giống nhau.
- Mail lỗi TLS → bật `mail.smtp.starttls.enable=true`.
- Port conflict → chỉnh trong `.env` hoặc `application.yml`.

## Định hướng mở rộng

- Hoàn thiện **docker-compose.yml** để chạy toàn bộ stack một lệnh.
- Thêm **user-service**, **post-service**, **ads-service**,...
- Tích hợp **Keycloak** cho auth nâng cao hoặc **Hashicorp Vault** cho secret.
- CI/CD với GitHub Actions.
- Monitoring: Prometheus + Grafana, ELK stack.

Chào mừng góp code! 🚀