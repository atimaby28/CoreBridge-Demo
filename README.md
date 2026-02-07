# CoreBridge Demo

> [CoreBridge-MSA](https://github.com/atimaby28/CoreBridge-MSA)의 주요 기능을 체험할 수 있는 데모 애플리케이션입니다.

MSA 13개 서비스를 단일 Spring Boot 앱으로 통합하여 별도 인프라 없이 바로 체험할 수 있습니다.

## 🌐 Live Demo

**👉 [https://www.corebridge.cloud/home](https://www.corebridge.cloud/home)**

별도 설치 없이 위 링크에서 바로 체험할 수 있습니다. 아래 테스트 계정으로 로그인하세요.

## 테스트 계정

| 역할 | 이메일 | 비밀번호 |
|------|--------|----------|
| 관리자 | admin@demo.com | qwer1234 |
| 구직자 | user@demo.com | qwer1234 |
| 기업 | company@demo.com | qwer1234 |

## 주요 기능

- **채용공고** 등록/조회/검색 (좋아요, 댓글, 조회수)
- **이력서** 작성 및 지원
- **채용 프로세스** State Machine 기반 상태 관리
- **AI 분석** 이력서 요약, 스킬 추출, JD 매칭 (Mock)
- **실시간 알림** 상태 변경 시 즉시 알림
- **감사 로그** 모든 API 호출 기록
- **기업 대시보드** 지원자 통계, 칸반보드 관리

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.4, Java 21, Spring Security, JPA |
| Frontend | Vue 3, TypeScript, Tailwind CSS, Pinia |
| Database | H2 (In-Memory) |
| Auth | JWT (JJWT 0.12.6) |
| Infra | AWS (EC2, S3, CloudFront, ACM), Nginx, Let's Encrypt |

## 🏗️ AWS 배포 아키텍처

```
                        ┌─────────────────────────────────────────────┐
                        │              corebridge.cloud               │
                        │            (Gabia DNS 관리)                  │
                        └──────┬──────────────────┬───────────────────┘
                               │                  │
                     www.corebridge.cloud    api.corebridge.cloud
                               │                  │
                               ▼                  ▼
                     ┌──────────────────┐  ┌──────────────────┐
                     │   CloudFront     │  │   EC2 (t2.micro) │
                     │   (CDN + HTTPS)  │  │   ap-northeast-2 │
                     │                  │  │                  │
                     │  ACM 인증서        │  │ ┌──────────────┐ │
                     │  (us-east-1)     │  │ │    Nginx      ││
                     │                  │  │ │  (80 → 443)   ││
                     │  SPA 에러 페이지    │  │ │ Let's Encrypt ││
                     │  (403,404→200)   │  │ └──────┬───────┘ │
                     └────────┬─────────┘  │         │        │
                              │            │         ▼        │
                              ▼            │  ┌──────────────┐│
                     ┌──────────────────┐  │  │ Spring Boot  ││
                     │   S3 Bucket      │  │  │   (:8080)    ││
                     │ (Vue 정적 파일)    │  │  │  H2 In-Mem   ││
                     │  OAC 접근 제어     │  │  └──────────────┘│
                     └──────────────────┘  │  Elastic IP 고정  │
                                           └──────────────────┘
```

### 프론트엔드 (www.corebridge.cloud)

S3에 Vue 빌드 결과물을 호스팅하고, CloudFront를 통해 HTTPS와 CDN 캐싱을 적용했습니다. ACM 인증서는 CloudFront 요구사항에 따라 us-east-1 리전에서 발급하였으며, Vue Router의 History Mode를 위해 CloudFront 에러 페이지(403/404 → index.html, 200)를 설정했습니다.

### 백엔드 (api.corebridge.cloud)

EC2 인스턴스에서 Spring Boot JAR를 systemd 서비스로 운영하고, Nginx가 리버스 프록시로 앞단에서 요청을 처리합니다. Let's Encrypt(Certbot)로 SSL 인증서를 발급하여 HTTPS를 적용했으며, 프론트엔드 HTTPS 환경에서의 Mixed Content 차단 및 JWT Secure Cookie 전송을 위해 필수적입니다.

### 비용 최적화

포트폴리오 시연 목적에 맞게 최소 비용으로 구성했습니다. EC2 프리티어(t3.micro), S3/CloudFront 무료 티어, Let's Encrypt 무료 인증서를 활용하여 도메인 비용 외 월 ~$1 수준으로 운영하고 있습니다.

## 로컬 실행

```bash
./gradlew bootRun
# → http://localhost:8080
```

## 원본 프로젝트

실제 MSA 아키텍처, AI 파이프라인, K8s 인프라 코드는 아래에서 확인하세요.

👉 **[CoreBridge-MSA](https://github.com/atimaby28/CoreBridge-MSA)**

## License

[MIT](LICENSE)
