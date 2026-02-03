package halo.corebridge.demo.config;

import halo.corebridge.demo.common.snowflake.Snowflake;
import halo.corebridge.demo.domain.apply.entity.Apply;
import halo.corebridge.demo.domain.apply.entity.ProcessHistory;
import halo.corebridge.demo.domain.apply.entity.RecruitmentProcess;
import halo.corebridge.demo.domain.apply.enums.ProcessStep;
import halo.corebridge.demo.domain.apply.repository.ApplyRepository;
import halo.corebridge.demo.domain.apply.repository.ProcessHistoryRepository;
import halo.corebridge.demo.domain.apply.repository.RecruitmentProcessRepository;
import halo.corebridge.demo.domain.comment.entity.Comment;
import halo.corebridge.demo.domain.comment.repository.CommentRepository;
import halo.corebridge.demo.domain.jobposting.entity.Jobposting;
import halo.corebridge.demo.domain.jobposting.repository.JobpostingRepository;
import halo.corebridge.demo.domain.notification.entity.Notification;
import halo.corebridge.demo.domain.notification.enums.NotificationType;
import halo.corebridge.demo.domain.notification.repository.NotificationRepository;
import halo.corebridge.demo.domain.resume.entity.Resume;
import halo.corebridge.demo.domain.resume.repository.ResumeRepository;
import halo.corebridge.demo.domain.schedule.entity.Schedule;
import halo.corebridge.demo.domain.schedule.enums.ScheduleStatus;
import halo.corebridge.demo.domain.schedule.enums.ScheduleType;
import halo.corebridge.demo.domain.schedule.repository.ScheduleRepository;
import halo.corebridge.demo.domain.user.entity.User;
import halo.corebridge.demo.domain.user.enums.UserRole;
import halo.corebridge.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 데모 샘플 데이터 초기화
 *
 * 앱 시작 시 H2 인메모리 DB에 샘플 데이터를 자동 삽입합니다.
 * 모든 ID는 Snowflake로 생성하여 MSA 원본과 동일한 방식 유지.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final Snowflake snowflake;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final JobpostingRepository jobpostingRepository;
    private final ApplyRepository applyRepository;
    private final RecruitmentProcessRepository processRepository;
    private final ProcessHistoryRepository historyRepository;
    private final ResumeRepository resumeRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final halo.corebridge.demo.domain.mock.controller.MockJobpostingExtController mockExtController;

    // 고정 ID (프론트엔드 테스트 편의)
    private long userApplicant, userCompany, userAdmin;
    private long user2, user3, user4, user5; // 추가 지원자
    private long jp1, jp2, jp3, jp4, jp5;
    private long apply1, apply2, apply3;
    private long process1, process2, process3;
    private long resume1, resume2, resume3, resume4, resume5;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("샘플 데이터 이미 존재 - 초기화 건너뜀");
            return;
        }

        log.info("========== 샘플 데이터 초기화 시작 ==========");
        createUsers();
        createJobpostings();
        createResume();
        createApplies();
        createProcessHistory();
        createSchedules();
        createNotifications();
        createComments();
        initViewAndLikeCounts();
        log.info("========== 샘플 데이터 초기화 완료 ==========");
        log.info("  로그인 계정:");
        log.info("    구직자: user@demo.com / qwer1234");
        log.info("    기업:   company@demo.com / qwer1234");
        log.info("    관리자: admin@demo.com / qwer1234");
    }

    private void createUsers() {
        String encodedPw = passwordEncoder.encode("qwer1234");

        userApplicant = snowflake.nextId();
        userCompany = snowflake.nextId();
        userAdmin = snowflake.nextId();

        userRepository.save(User.create(userApplicant, "user@demo.com", "양승우", encodedPw, UserRole.ROLE_USER));
        userRepository.save(User.create(userCompany, "company@demo.com", "테크컴퍼니", encodedPw, UserRole.ROLE_COMPANY));
        userRepository.save(User.create(userAdmin, "admin@demo.com", "관리자", encodedPw, UserRole.ROLE_ADMIN));

        // 추가 지원자 4명
        user2 = snowflake.nextId();
        user3 = snowflake.nextId();
        user4 = snowflake.nextId();
        user5 = snowflake.nextId();

        userRepository.save(User.create(user2, "kim@demo.com", "김민수", encodedPw, UserRole.ROLE_USER));
        userRepository.save(User.create(user3, "lee@demo.com", "이서연", encodedPw, UserRole.ROLE_USER));
        userRepository.save(User.create(user4, "park@demo.com", "박지훈", encodedPw, UserRole.ROLE_USER));
        userRepository.save(User.create(user5, "choi@demo.com", "최유진", encodedPw, UserRole.ROLE_USER));

        log.info("사용자 7명 생성 완료 (기업1 + 관리자1 + 구직자5)");
    }

    private void createJobpostings() {
        jp1 = snowflake.nextId();
        jp2 = snowflake.nextId();
        jp3 = snowflake.nextId();
        jp4 = snowflake.nextId();
        jp5 = snowflake.nextId();

        jobpostingRepository.save(Jobposting.create(jp1,
                "[테크컴퍼니] 백엔드 개발자 (Spring Boot/MSA)",
                "## 채용 포지션\nSpring Boot 기반 MSA 백엔드 개발자를 모집합니다.\n\n"
                        + "### 주요 업무\n- Spring Boot 기반 MSA 서비스 개발 및 운영\n"
                        + "- RESTful API 설계 및 구현\n- 데이터베이스 설계 및 쿼리 최적화\n\n"
                        + "### 자격 요건\n- Java/Spring Boot 경력 2년 이상\n"
                        + "- JPA/Hibernate 활용 경험\n- Git 기반 협업 경험",
                1L, userCompany, "Java,Spring Boot,JPA,MSA,Docker", "Kubernetes,Kafka,Redis"));

        jobpostingRepository.save(Jobposting.create(jp2,
                "[테크컴퍼니] 프론트엔드 개발자 (React/TypeScript)",
                "## 채용 포지션\nReact 기반 프론트엔드 개발자를 모집합니다.\n\n"
                        + "### 주요 업무\n- React/TypeScript 기반 웹 애플리케이션 개발\n"
                        + "- 사용자 인터랙션 설계 및 구현\n\n"
                        + "### 자격 요건\n- React 경력 2년 이상\n- TypeScript 필수",
                1L, userCompany, "React,TypeScript,Next.js", "Vue.js,Tailwind CSS"));

        jobpostingRepository.save(Jobposting.create(jp3,
                "[테크컴퍼니] DevOps 엔지니어",
                "## 채용 포지션\nCI/CD 파이프라인 구축 및 인프라 관리 엔지니어를 모집합니다.\n\n"
                        + "### 주요 업무\n- Kubernetes 클러스터 관리\n"
                        + "- Jenkins/GitHub Actions CI/CD 파이프라인 구축\n"
                        + "- 모니터링 시스템 운영 (Prometheus, Grafana)\n\n"
                        + "### 자격 요건\n- Linux 시스템 관리 경험\n- Docker/Kubernetes 실무 경험",
                1L, userCompany, "Kubernetes,Docker,Jenkins,Linux", "Terraform,AWS,Prometheus"));

        jobpostingRepository.save(Jobposting.create(jp4,
                "[테크컴퍼니] 데이터 엔지니어",
                "## 채용 포지션\n데이터 파이프라인 설계 및 운영 엔지니어를 모집합니다.\n\n"
                        + "### 주요 업무\n- ETL 파이프라인 구축\n- 데이터 웨어하우스 설계\n\n"
                        + "### 자격 요건\n- Python/SQL 필수\n- Spark/Airflow 경험",
                2L, userCompany, "Python,SQL,Spark,Airflow", "Kafka,Flink"));

        jobpostingRepository.save(Jobposting.create(jp5,
                "[테크컴퍼니] AI/ML 엔지니어",
                "## 채용 포지션\nAI/ML 모델 개발 및 서빙 엔지니어를 모집합니다.\n\n"
                        + "### 주요 업무\n- LLM 파인튜닝 및 RAG 시스템 구축\n"
                        + "- 모델 서빙 파이프라인 구축\n\n"
                        + "### 자격 요건\n- Python, PyTorch/TensorFlow\n- NLP 또는 CV 관련 경험",
                2L, userCompany, "Python,PyTorch,NLP,LLM", "FastAPI,Docker,MLOps"));

        log.info("채용공고 5건 생성 완료");
    }

    private void createResume() {
        resume1 = snowflake.nextId();

        Resume resume = Resume.create(resume1, userApplicant,
                "양승우 - 백엔드 개발자 이력서",
                "## 양승우\n\n"
                        + "### 소개\n"
                        + "Spring Boot 기반 백엔드 개발자입니다. "
                        + "MSA 아키텍처 설계부터 인프라 구축까지 경험하였으며, "
                        + "데이터 일관성과 시스템 안정성을 중시합니다.\n\n"
                        + "### 학력\n"
                        + "- 전남대학교 컴퓨터공학 전공 / 해양학 부전공 (2015 ~ 2021, 학점 3.59)\n\n"
                        + "### 교육 및 훈련\n"
                        + "- 한화 BEYOND SW캠프 (2025.05 ~ 2025.11, 우수 수료)\n"
                        + "- 삼성 SSAFY 6기 (2021 ~ 2022)\n"
                        + "- 한국재정정보원(FIS) 디지털 인재양성 과정 (2023)\n\n"
                        + "### 경력\n"
                        + "- FIS 인턴 (2023.03 ~ 2023.12) — ERP-Dooray 연동 자동화, 30분→8초 (99.5% 단축)\n"
                        + "- NIA 청년인턴 (2022) — 스타트업 현장 실습\n\n"
                        + "### 기술 스택\n"
                        + "- Backend: Java, Spring Boot, JPA/Hibernate, MyBatis\n"
                        + "- Database: PostgreSQL, MySQL, Redis, H2\n"
                        + "- Infra: Docker, Kubernetes(K3s), Jenkins, GitHub Actions\n"
                        + "- AI Pipeline: FastAPI, Ollama, n8n\n"
                        + "- Monitoring: Prometheus, Grafana\n"
                        + "- Frontend: Vue.js, TypeScript\n\n"
                        + "### 자격증\n"
                        + "- AWS Solutions Architect Associate\n"
                        + "- 정보처리기사\n"
                        + "- 빅데이터분석기사\n"
                        + "- SQLD\n"
                        + "- TOEIC 815 / TOEIC Speaking IM3\n"
                        + "- 한국사 1급, 한자 2급\n\n"
                        + "### 프로젝트\n"
                        + "**CoreBridge** — 채용 플랫폼 MSA (2025)\n"
                        + "- 13개 마이크로서비스 설계 및 구현\n"
                        + "- Outbox Pattern으로 서비스 간 데이터 일관성 보장\n"
                        + "- Circuit Breaker로 장애 격리 및 Fallback 처리\n"
                        + "- CQRS + Spring Batch로 조회 성능 최적화\n"
                        + "- AI Pipeline (FastAPI + Ollama + n8n) 비동기 연동\n"
                        + "- API Gateway JWT 검증 중앙화\n"
                        + "- K3s + Prometheus + Grafana 모니터링 구축");

        resume.updateAiAnalysis(
                "[AI 분석] 백엔드 개발 역량이 우수합니다. "
                        + "Spring Boot 기반 MSA 설계 경험과 Outbox, Circuit Breaker 등 분산 시스템 패턴 적용 이력이 돋보입니다. "
                        + "AWS 자격증과 K8s 인프라 경험으로 DevOps 역량도 겸비하고 있습니다. "
                        + "FIS 인턴 기간 중 ERP 연동 자동화(99.5% 시간 단축)는 문제 해결 능력을 잘 보여줍니다. "
                        + "추천 포지션: 백엔드 개발자, 플랫폼 엔지니어, DevOps 엔지니어",
                "[\"Java\",\"Spring Boot\",\"JPA\",\"MSA\",\"Docker\",\"Kubernetes\",\"Redis\",\"PostgreSQL\",\"FastAPI\",\"Vue.js\"]");

        resumeRepository.save(resume);
        log.info("이력서 1건 생성 완료 (AI 분석 포함)");

        // === 추가 지원자 이력서 ===
        resume2 = snowflake.nextId();
        Resume r2 = Resume.create(resume2, user2, "김민수 - 풀스택 개발자 이력서",
                "## 김민수\n\n### 소개\n"
                        + "3년차 풀스택 개발자입니다. Spring Boot와 React를 주력으로 사용합니다.\n\n"
                        + "### 기술 스택\n- Backend: Java, Spring Boot, JPA, MyBatis\n"
                        + "- Frontend: React, TypeScript, Next.js\n"
                        + "- Database: MySQL, Redis\n- Infra: Docker, AWS EC2/S3\n\n"
                        + "### 경력\n- (주)스타트업A 백엔드 개발자 (2022 ~ 2024)\n"
                        + "- 사내 ERP 시스템 풀스택 개발");
        r2.updateAiAnalysis("[AI 분석] 풀스택 역량 보유. Spring Boot + React 조합이 강점.",
                "[\"Java\",\"Spring Boot\",\"React\",\"TypeScript\",\"MySQL\",\"Docker\"]");
        resumeRepository.save(r2);

        resume3 = snowflake.nextId();
        Resume r3 = Resume.create(resume3, user3, "이서연 - 프론트엔드 개발자 이력서",
                "## 이서연\n\n### 소개\n"
                        + "React/Vue 전문 프론트엔드 개발자입니다. UI/UX에 관심이 많습니다.\n\n"
                        + "### 기술 스택\n- Frontend: React, Vue.js, TypeScript, Tailwind CSS\n"
                        + "- Backend: Node.js, Express\n- Tool: Figma, Storybook\n\n"
                        + "### 경력\n- (주)웹에이전시B 프론트엔드 (2023 ~ 현재)\n"
                        + "- 반응형 웹 및 디자인 시스템 구축");
        r3.updateAiAnalysis("[AI 분석] 프론트엔드 전문가. React/Vue 모두 숙련. UI/UX 역량 우수.",
                "[\"React\",\"Vue.js\",\"TypeScript\",\"Tailwind CSS\",\"Node.js\"]");
        resumeRepository.save(r3);

        resume4 = snowflake.nextId();
        Resume r4 = Resume.create(resume4, user4, "박지훈 - DevOps 엔지니어 이력서",
                "## 박지훈\n\n### 소개\n"
                        + "인프라 자동화와 CI/CD 파이프라인 구축을 전문으로 합니다.\n\n"
                        + "### 기술 스택\n- Infra: Kubernetes, Docker, Terraform, Ansible\n"
                        + "- CI/CD: Jenkins, GitHub Actions, ArgoCD\n"
                        + "- Cloud: AWS, GCP\n- Monitoring: Prometheus, Grafana, ELK\n\n"
                        + "### 경력\n- (주)클라우드C DevOps 엔지니어 (2021 ~ 현재)\n"
                        + "- K8s 클러스터 운영 (50+ 노드)");
        r4.updateAiAnalysis("[AI 분석] DevOps 전문가. K8s 대규모 운영 경험. CI/CD 자동화 역량 탁월.",
                "[\"Kubernetes\",\"Docker\",\"Terraform\",\"Jenkins\",\"AWS\",\"Prometheus\"]");
        resumeRepository.save(r4);

        resume5 = snowflake.nextId();
        Resume r5 = Resume.create(resume5, user5, "최유진 - 데이터 엔지니어 이력서",
                "## 최유진\n\n### 소개\n"
                        + "데이터 파이프라인 설계와 분석 플랫폼 구축을 전문으로 합니다.\n\n"
                        + "### 기술 스택\n- Data: Python, Spark, Airflow, Kafka\n"
                        + "- Database: PostgreSQL, BigQuery, Snowflake\n"
                        + "- ML: PyTorch, Scikit-learn\n- Cloud: GCP, AWS\n\n"
                        + "### 경력\n- (주)데이터D 데이터 엔지니어 (2022 ~ 현재)\n"
                        + "- 일 1억건 ETL 파이프라인 운영");
        r5.updateAiAnalysis("[AI 분석] 데이터 엔지니어링 전문. 대용량 ETL 경험. ML 기초 역량 보유.",
                "[\"Python\",\"Spark\",\"Airflow\",\"Kafka\",\"PostgreSQL\",\"PyTorch\"]");
        resumeRepository.save(r5);

        log.info("추가 이력서 4건 생성 완료");
    }

    private void createApplies() {
        apply1 = snowflake.nextId();
        apply2 = snowflake.nextId();
        apply3 = snowflake.nextId();

        // 지원 1: 백엔드 공고 → 1차면접합격까지 진행
        applyRepository.save(Apply.create(apply1, jp1, userApplicant, resume1,
                "Spring Boot와 MSA 경험을 바탕으로 백엔드 개발자로 지원합니다."));

        // 지원 2: 프론트엔드 공고 → 서류검토중
        applyRepository.save(Apply.create(apply2, jp2, userApplicant, resume1,
                "프론트엔드 기술에도 관심이 있어 지원합니다."));

        // 지원 3: DevOps 공고 → 지원 완료
        applyRepository.save(Apply.create(apply3, jp3, userApplicant, resume1,
                "Kubernetes와 CI/CD 경험을 살려 DevOps 엔지니어로 지원합니다."));

        process1 = snowflake.nextId();
        process2 = snowflake.nextId();
        process3 = snowflake.nextId();

        // 프로세스 1: INTERVIEW_1_PASS 까지 진행 (State Machine 데모)
        RecruitmentProcess p1 = RecruitmentProcess.create(process1, apply1, jp1, userApplicant);
        p1.transition(ProcessStep.DOCUMENT_REVIEW);
        p1.transition(ProcessStep.DOCUMENT_PASS);
        p1.transition(ProcessStep.INTERVIEW_1);
        p1.transition(ProcessStep.INTERVIEW_1_PASS);
        processRepository.save(p1);

        // 프로세스 2: DOCUMENT_REVIEW (진행 중)
        RecruitmentProcess p2 = RecruitmentProcess.create(process2, apply2, jp2, userApplicant);
        p2.transition(ProcessStep.DOCUMENT_REVIEW);
        processRepository.save(p2);

        // 프로세스 3: APPLIED (최초 상태)
        processRepository.save(RecruitmentProcess.create(process3, apply3, jp3, userApplicant));

        // === 추가 지원자 지원 데이터 ===

        // 김민수: 백엔드, 프론트엔드 지원
        long applyKim1 = snowflake.nextId();
        applyRepository.save(Apply.create(applyKim1, jp1, user2, resume2,
                "풀스택 경험을 살려 백엔드 포지션에 지원합니다."));
        RecruitmentProcess pKim1 = RecruitmentProcess.create(snowflake.nextId(), applyKim1, jp1, user2);
        pKim1.transition(ProcessStep.DOCUMENT_REVIEW);
        pKim1.transition(ProcessStep.DOCUMENT_PASS);
        processRepository.save(pKim1);

        long applyKim2 = snowflake.nextId();
        applyRepository.save(Apply.create(applyKim2, jp2, user2, resume2,
                "React/TypeScript 경험이 있어 프론트엔드도 지원합니다."));
        RecruitmentProcess pKim2 = RecruitmentProcess.create(snowflake.nextId(), applyKim2, jp2, user2);
        pKim2.transition(ProcessStep.DOCUMENT_REVIEW);
        processRepository.save(pKim2);

        // 이서연: 프론트엔드 지원
        long applyLee = snowflake.nextId();
        applyRepository.save(Apply.create(applyLee, jp2, user3, resume3,
                "프론트엔드 전문 역량으로 지원합니다. React/Vue 모두 가능합니다."));
        RecruitmentProcess pLee = RecruitmentProcess.create(snowflake.nextId(), applyLee, jp2, user3);
        pLee.transition(ProcessStep.DOCUMENT_REVIEW);
        pLee.transition(ProcessStep.DOCUMENT_PASS);
        pLee.transition(ProcessStep.INTERVIEW_1);
        processRepository.save(pLee);

        // 박지훈: DevOps, 백엔드 지원
        long applyPark1 = snowflake.nextId();
        applyRepository.save(Apply.create(applyPark1, jp3, user4, resume4,
                "K8s 클러스터 대규모 운영 경험으로 DevOps 포지션에 지원합니다."));
        RecruitmentProcess pPark1 = RecruitmentProcess.create(snowflake.nextId(), applyPark1, jp3, user4);
        pPark1.transition(ProcessStep.DOCUMENT_REVIEW);
        pPark1.transition(ProcessStep.DOCUMENT_PASS);
        processRepository.save(pPark1);

        long applyPark2 = snowflake.nextId();
        applyRepository.save(Apply.create(applyPark2, jp1, user4, resume4,
                "인프라 경험을 바탕으로 백엔드 개발에도 도전하고 싶습니다."));
        processRepository.save(RecruitmentProcess.create(snowflake.nextId(), applyPark2, jp1, user4));

        // 최유진: 데이터 엔지니어, AI/ML 지원
        long applyChoi1 = snowflake.nextId();
        applyRepository.save(Apply.create(applyChoi1, jp4, user5, resume5,
                "대용량 ETL 파이프라인 운영 경험을 살려 지원합니다."));
        RecruitmentProcess pChoi1 = RecruitmentProcess.create(snowflake.nextId(), applyChoi1, jp4, user5);
        pChoi1.transition(ProcessStep.DOCUMENT_REVIEW);
        pChoi1.transition(ProcessStep.DOCUMENT_PASS);
        pChoi1.transition(ProcessStep.INTERVIEW_1);
        pChoi1.transition(ProcessStep.INTERVIEW_1_PASS);
        processRepository.save(pChoi1);

        long applyChoi2 = snowflake.nextId();
        applyRepository.save(Apply.create(applyChoi2, jp5, user5, resume5,
                "ML 경험을 바탕으로 AI/ML 엔지니어에도 지원합니다."));
        RecruitmentProcess pChoi2 = RecruitmentProcess.create(snowflake.nextId(), applyChoi2, jp5, user5);
        pChoi2.transition(ProcessStep.DOCUMENT_REVIEW);
        processRepository.save(pChoi2);

        log.info("지원 총 10건 + 프로세스 10건 생성 완료");
    }

    private void createProcessHistory() {
        // 프로세스 1의 상태 변경 이력
        historyRepository.save(ProcessHistory.create(snowflake.nextId(), process1, apply1,
                ProcessStep.APPLIED, ProcessStep.DOCUMENT_REVIEW,
                userCompany, "서류 검토를 시작합니다.", null));

        historyRepository.save(ProcessHistory.create(snowflake.nextId(), process1, apply1,
                ProcessStep.DOCUMENT_REVIEW, ProcessStep.DOCUMENT_PASS,
                userCompany, "서류 전형 합격. 기술 역량이 우수합니다.", "Spring Boot + MSA 경험 높이 평가"));

        historyRepository.save(ProcessHistory.create(snowflake.nextId(), process1, apply1,
                ProcessStep.DOCUMENT_PASS, ProcessStep.INTERVIEW_1,
                userCompany, "1차 기술 면접 진행 예정", null));

        historyRepository.save(ProcessHistory.create(snowflake.nextId(), process1, apply1,
                ProcessStep.INTERVIEW_1, ProcessStep.INTERVIEW_1_PASS,
                userCompany, "1차 면접 합격. 기술 이해도 높음.", "시스템 설계 질문에 명확한 답변"));

        // 프로세스 2의 이력
        historyRepository.save(ProcessHistory.create(snowflake.nextId(), process2, apply2,
                ProcessStep.APPLIED, ProcessStep.DOCUMENT_REVIEW,
                userCompany, "서류 검토 시작", null));

        log.info("프로세스 이력 5건 생성 완료");
    }

    private void createSchedules() {
        LocalDateTime upcoming = LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0);

        // 2차 면접 일정 (프로세스 1)
        scheduleRepository.save(Schedule.builder()
                .id(snowflake.nextId())
                .applyId(apply1).jobpostingId(jp1)
                .userId(userApplicant).companyId(userCompany)
                .type(ScheduleType.INTERVIEW_2)
                .title("2차 기술 면접 (백엔드)")
                .description("MSA 설계 경험 및 시스템 설계 질문 위주로 진행됩니다.")
                .location("테크컴퍼니 본사 5층 회의실 A")
                .startTime(upcoming)
                .endTime(upcoming.plusHours(1).plusMinutes(30))
                .interviewerId(userCompany)
                .status(ScheduleStatus.SCHEDULED)
                .build());

        // 코딩 테스트 일정
        scheduleRepository.save(Schedule.builder()
                .id(snowflake.nextId())
                .applyId(apply2).jobpostingId(jp2)
                .userId(userApplicant).companyId(userCompany)
                .type(ScheduleType.CODING_TEST)
                .title("온라인 코딩 테스트 (프론트엔드)")
                .description("React/TypeScript 기반 구현 과제. 3시간.")
                .location("온라인 (링크 별도 안내)")
                .startTime(upcoming.plusDays(2))
                .endTime(upcoming.plusDays(2).plusHours(3))
                .status(ScheduleStatus.SCHEDULED)
                .build());

        log.info("일정 2건 생성 완료");
    }

    private void createNotifications() {
        notificationRepository.save(Notification.builder()
                .id(snowflake.nextId()).userId(userApplicant)
                .type(NotificationType.INTERVIEW_PASS)
                .title("1차 면접 합격").message("축하합니다! 1차 기술 면접에 합격하셨습니다. 2차 면접 일정을 확인해주세요.")
                .relatedId(apply1).relatedType("APPLY")
                .build());

        notificationRepository.save(Notification.builder()
                .id(snowflake.nextId()).userId(userApplicant)
                .type(NotificationType.INTERVIEW_SCHEDULED)
                .title("2차 면접 일정 안내").message("2차 기술 면접이 예정되었습니다. 일정을 확인해주세요.")
                .relatedId(apply1).relatedType("SCHEDULE")
                .build());

        notificationRepository.save(Notification.builder()
                .id(snowflake.nextId()).userId(userApplicant)
                .type(NotificationType.PROCESS_UPDATE)
                .title("서류 검토 시작").message("프론트엔드 개발자 포지션 서류 검토가 시작되었습니다.")
                .relatedId(apply2).relatedType("APPLY").isRead(true)
                .build());

        notificationRepository.save(Notification.builder()
                .id(snowflake.nextId()).userId(userApplicant)
                .type(NotificationType.RESUME_ANALYSIS_COMPLETE)
                .title("이력서 AI 분석 완료").message("이력서 AI 분석이 완료되었습니다. 결과를 확인해보세요.")
                .relatedId(resume1).relatedType("RESUME").isRead(true)
                .build());

        notificationRepository.save(Notification.builder()
                .id(snowflake.nextId()).userId(userCompany)
                .type(NotificationType.APPLY_RECEIVED)
                .title("새 지원서 접수").message("백엔드 개발자 포지션에 새로운 지원서가 접수되었습니다.")
                .relatedId(apply1).relatedType("APPLY")
                .build());

        log.info("알림 5건 생성 완료");
    }

    private void createComments() {
        long c1 = snowflake.nextId();
        long c2 = snowflake.nextId();
        long c3 = snowflake.nextId();
        long c4 = snowflake.nextId();

        // 루트 댓글 (parentCommentId == commentId)
        commentRepository.save(Comment.create(c1,
                "Spring Boot와 MSA 경험을 쌓기 좋은 포지션이네요!",
                null, jp1, userApplicant));

        commentRepository.save(Comment.create(c2,
                "Docker/Kubernetes 필수인 건 좋은데 JD가 좀 더 구체적이면 좋겠네요.",
                null, jp1, userApplicant));

        // 대댓글 (c1에 대한 답글)
        commentRepository.save(Comment.create(c3,
                "감사합니다! 관련 기술 스택이 궁금하시면 문의해주세요.",
                c1, jp1, userCompany));

        // 루트 댓글 (공고 2)
        commentRepository.save(Comment.create(c4,
                "React + TypeScript 포지션 좋네요. 지원 마감일이 언제인가요?",
                null, jp2, userApplicant));

        log.info("댓글 4건 생성 완료 (대댓글 1건 포함)");
    }

    /**
     * 조회수/좋아요 초기값 세팅
     * 지원 수, 댓글 수를 기반으로 현실적인 비율로 산출
     */
    private void initViewAndLikeCounts() {
        long[] jpIds = {jp1, jp2, jp3, jp4, jp5};
        for (long jpId : jpIds) {
            long applyCount = applyRepository.countByJobpostingId(jpId);
            long commentCount = commentRepository.countByJobpostingId(jpId);

            // 조회수: 지원 1건당 평균 30~50회 조회 + 댓글당 10회 + 기본 트래픽
            long views = applyCount * 40 + commentCount * 10 + 50;
            // 좋아요: 조회수의 10~15%
            long likes = (long) (views * 0.12);

            mockExtController.setViewCount(jpId, views);
            mockExtController.setLikeCount(jpId, likes);
        }
        log.info("조회수/좋아요 초기값 세팅 완료 (지원수·댓글수 기반)");
    }
}
