# BE
더조은 2팀 마지막 프로젝트 백엔드 레포지토리입니다.

---

# 📦 BoxMon Backend
**화주와 차주를 잇는 스마트 화물 운송 플랫폼, BoxMon**

BoxMon은 화주가 화물을 등록하고, 기사가 최적의 화물을 수락하며, 운송 추적 및 정산까지 원스톱으로 관리할 수 있는 화물 운송 중개 플랫폼의 백엔드 서비스입니다.

---

## 🚀 Key Features

### 🚛 운송 관리 (Shipment)
- **화물 등록 및 매칭**: 화주의 화물 등록 및 기사들의 실시간 목록 조회/수락.
- **실시간 위치 추적**: Naver Maps API 및 JTS(Spatial) 기반의 실시간 위치 정보 처리.
- **상태 관리**: 대기 -> 배차 완료 -> 운송 중 -> 완료로 이어지는 엄격한 상태 머신.

### 💳 결제 및 정산 (Payment)
- **Toss Payments 연동**: 신뢰할 수 있는 결제 프로세스 제공.
- **수수료 관리**: 관리자에 의한 유연한 수수료 설정 및 변경 이력 추적.

### 🔐 보안 및 인증 (Security)
- **JWT 기반 인증**: Access Token(15분) 및 Refresh Token(14일) 기반의 보안 유지.
- **권한 관리**: 화주, 기사, 관리자별 차등화된 API 접근 제어.

### 🛡️ 관리자 포털 (Admin)
- **회원 승인**: 기사 가입 시 사업자 등록증 이미지 검증 및 승인.
- **분쟁 중재**: 고객 문의(Contact) 처리 및 페널티 부여 시스템.
- **시스템 설정**: 플랫폼 전반의 정책 및 수수료 설정.

---

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 25
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA (Querydsl)
- **Security**: Spring Security, JWT
- **External APIs**: Toss Payments, Naver Maps (Directions/Static Map)
- **Infrastructure**: Naver Cloud Platform (Object Storage), Firebase (FCM)

---

## 📂 Project Structure

```text
boxmon/
├── feature/
│   ├── shipment/    # 화물 등록, 배차, 상태 관리
│   ├── user/        # 회원가입, 프로필, 차주 승인 요청
│   ├── payment/     # Toss Payments 결제 처리
│   ├── admin/       # 관리자 전용 기능 (정산, 로그, 분쟁)
│   ├── chat/        # STOMP 기반 실시간 채팅
│   └── location/    # JTS 기반 위치 로그 처리
├── global/          # 공통 설정 (NCP Storage, Naver Client, Util)
├── security/        # JWT 필터 및 Security 설정
└── exception/       # 전역 예외 처리 (ExceotionController)
```

---

## ⚙️ Environment Variables

애플리케이션 실행을 위해 다음 환경 변수 설정이 필요합니다.

```env
JWT_SECRET=your_jwt_secret_key
TOSS_API_KEY=your_toss_api_key
NAVER_MAPS_CLIENT_ID=your_id
NAVER_MAPS_CLIENT_SECRET=your_secret
NCP_ACCESS_KEY=your_ncp_access_key
NCP_SECRET_KEY=your_ncp_secret_key
```

---

## 🏃 Quick Start

### Prerequisites
- Java 25 이상
- MySQL 8.0 이상

### Build & Run
```bash
# Repository Clone
git clone https://github.com/your-repo/boxmon.git

# Build
./gradlew build

# Run
./gradlew bootRun
```

---

## 📝 Conventions
- **Indentation**: 4 spaces.
- **Encoding**: UTF-8 (No BOM).
- **Branch Strategy**: Git Flow 기반 (feat/#이슈번호).
- **Testing**: JUnit 5 기반의 단위/통합 테스트 수행.

---

## 👨‍💻 Contributors
- **Backend Team**: [Your Name/Team Name]
- **Project Period**: 2026.02 - 2026.03 (1 Month)

---
