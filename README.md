# CoreBridge Demo

> [CoreBridge-MSA](https://github.com/atimaby28/CoreBridge-MSA)의 주요 기능을 체험할 수 있는 데모 애플리케이션입니다.

MSA 13개 서비스를 단일 Spring Boot 앱으로 통합하여 별도 인프라 없이 로컬에서 바로 실행할 수 있습니다.

## 실행

```bash
./gradlew bootRun
# → http://localhost:8080
```

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

## 원본 프로젝트

실제 MSA 아키텍처, AI 파이프라인, K8s 인프라 코드는 아래에서 확인하세요.

👉 **[CoreBridge-MSA](https://github.com/atimaby28/CoreBridge-MSA)**

## License

[MIT](LICENSE)
