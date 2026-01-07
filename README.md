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

## Cây thư mục

```
bondy-server/
├─ config-server/
├─ discovery-server/
├─ gateway/
├─ common-web/
├─ services/
│  ├─ auth-service/
│  ├─ bondy-proxy/
│  ├─ bondy-recommendation-system/
│  ├─ communication-service/
│  ├─ interaction-service/
│  ├─ mail-service/
│  ├─ moderation-service/
│  ├─ notification-service/
│  └─ upload-service/
├─ .env.example
├─ .gitignore
├─ docker-compose.yml              # (Sắp triển khai)
├─ pom.xml                         # Parent Maven (cho Java modules)
└─ README.md
```

## Yêu cầu hệ thống

- **JDK 21** (cho các service Spring Boot)
- **Node.js 20+** & npm/yarn/pnpm (cho NestJS services)
- **Python 3.11+** (cho recommendation-system)
- **PostgreSQL 15** (khuyến nghị - phiên bản ổn định, hiệu suất cao, hỗ trợ tốt JSONB cho dữ liệu social)
- **Redis 7+** (cache, pub/sub real-time)
- **Maven Wrapper** (`./mvnw`)
- **Docker & Docker Compose** (khuyến khích cho môi trường dev/prod)

## Thiết lập biến môi trường (.env)

File `.env.example` đã được cung cấp ở root project. Copy thành `.env` và chỉnh sửa theo môi trường của bạn.

### Nội dung mẫu `.env.example` (đã cập nhật)

```dotenv
# Môi trường chạy
APP_ENV=local                  # local | dev | staging | production

# Config & Discovery
CONFIG_SERVER_URL=http://localhost:8888
DISCOVERY_URL=http://localhost:8761/eureka

# Gateway
GATEWAY_URL=http://localhost:8080

# Internal security
API_KEY_HEADER=X-Internal-Api-Key
INTERNAL_API_KEY=your-super-secret-internal-key

# Port
SERVER_PORT=8081
ACTUATOR_PORT=9081

# JWT
JWT_ISSUER=bondy-app
JWT_SECRET=your-very-strong-jwt-secret-key-min-256-bits

# Database - PostgreSQL 15 (khuyến nghị)
DB=postgresql
DB_HOST=localhost
DB_PORT=5432
DB_NAME=bondy_db
DB_USER=bondy_user
DB_PASSWORD=your_strong_db_password

# Mật khẩu mặc định
DEFAULT_PASSWORD_SUFFIX=!Bondy2026@

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# SMTP - Mail service
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password
SMTP_FROM=no-reply@bondy.app

# OAuth2 providers
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
DISCORD_CLIENT_ID=
DISCORD_CLIENT_SECRET=

# Upload service (S3)
AWS_S3_BUCKET=bondy-media
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=ap-southeast-1

# Recommendation system
RECOMMEND_SERVICE_URL=http://localhost:8000
```

### Hướng dẫn cấu hình PostgreSQL 15

1. Cài đặt PostgreSQL 15.
2. Tạo user và database:
   ```sql
   CREATE USER bondy_user WITH PASSWORD 'your_strong_db_password';
   CREATE DATABASE bondy_db OWNER bondy_user ENCODING 'UTF8' LC_COLLATE 'en_US.UTF-8' LC_CTYPE 'en_US.UTF-8' TEMPLATE template0;
   GRANT ALL PRIVILEGES ON DATABASE bondy_db TO bondy_user;
   ```

## Thiết lập & chạy dự án

1. Clone dự án + repo `bondy-config` (nếu dùng Config Server).
2. Copy `.env.example` → `.env` và cập nhật các giá trị.
3. Cài dependencies:
   ```bash
   # Java
   ./mvnw clean install -DskipTests

   # NestJS (trong từng thư mục service)
   cd services/communication-service && npm install
   cd services/bondy-proxy && npm install

   # Python
   cd services/bondy-recommendation-system
   pip install -r requirements.txt
   ```
4. Khởi động theo thứ tự: config-server → discovery-server → các service → gateway.

Truy cập:
- Eureka: http://localhost:8761
- Gateway: http://localhost:8080

## Troubleshooting

- Service không đăng ký Eureka → kiểm tra `eureka.client.service-url` trong config.
- JWT invalid → đảm bảo `JWT_SECRET` giống nhau ở auth và gateway.
- DB connection refused → kiểm tra PostgreSQL đang chạy và thông tin trong `.env`.

## Định hướng mở rộng

- Hoàn thiện Docker Compose.
- Thêm user-service, post-service, ads-service...
- Tích hợp Keycloak/Vault.
- Monitoring với Prometheus + Grafana.

Chào mừng góp code! 🚀