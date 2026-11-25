# Spring Boot 백엔드 프로젝트 템플릿

팀 프로젝트 첫날 바로 쓸 수 있는 초기 구성입니다.
HRBank의 **commitlint · Spotless · compose** 세트에 MoNew의 **빌드 · 테스트 CI**를 합치고,
**훅 자동 설치(`prepare`)** 와 **프로필 분리(`local` / `test` / `prod`)** 를 더했습니다.

| 구분 | 내용 |
| --- | --- |
| 런타임 | Java 17, Spring Boot 4.1.1, Gradle 9.7.1(wrapper) |
| DB | PostgreSQL 17 (로컬: Docker Compose, 테스트: Testcontainers) |
| 코드 스타일 | Spotless + Google Java Format |
| 커밋 규칙 | commitlint (`feat`, `fix`, `refactor`, `test`, `docs`, `style`, `chore`, `infra`) |
| 품질 게이트 | pre-commit(자동 포맷) → pre-push(전체 빌드) → CI(commitlint + build + 커버리지) |

---

## 1. 처음 받았을 때 (팀원 전원, 1회)

```bash
npm ci                    # commitlint 설치 + Git 훅 자동 등록(prepare 스크립트)
cp .env.example .env      # 로컬 DB 값 (기본값 그대로 써도 됨)
./gradlew build           # Docker Desktop 실행 상태에서
```

`npm ci`가 끝나면 `git config core.hooksPath .githooks`가 자동으로 설정됩니다.
확인: `git config core.hooksPath` → `.githooks`

> 필요 도구: JDK 17, Node.js 18+, Docker Desktop

## 2. 실행

```bash
./gradlew bootRun         # 기본 프로필 local → compose.yaml 의 Postgres 자동 기동
```

- 헬스 체크: http://localhost:8080/actuator/health
- 운영: `SPRING_PROFILES_ACTIVE=prod` + 아래 환경 변수 주입

## 3. 프로필

| 프로필 | 파일 | DB | `ddl-auto` | 용도 |
| --- | --- | --- | --- | --- |
| `local` (기본) | `application-local.yml` | compose.yaml Postgres | `update` | 개발 |
| `test` | `src/test/resources/application-test.yml` | Testcontainers | `create-drop` | 통합 테스트 |
| `prod` | `application-prod.yml` | 환경 변수 | `validate` | 운영 |

공통 설정은 `application.yml`에 둡니다(`open-in-view: false`, batch fetch size, 시간대 등).

`prod` 필수 환경 변수: `DB_HOST`, `DB_PORT`(기본 5432), `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
기본값이 없어서 하나라도 빠지면 기동이 실패합니다. 의도된 동작입니다.

## 4. 품질 게이트

| 시점 | 장치 | 하는 일 |
| --- | --- | --- |
| `git commit` | `.githooks/pre-commit` | 스테이징된 `.java`가 있으면 `spotlessApply` 후 다시 스테이징 |
| `git commit` | `.githooks/commit-msg` | commitlint로 메시지 검사 |
| `git push` | `.githooks/pre-push` | `./gradlew build`(포맷 검사 · 컴파일 · 테스트). 급하면 `--no-verify` |
| PR / push | `.github/workflows/ci.yml` | `commitlint` 잡 + `build` 잡 + JaCoCo 커버리지 PR 코멘트 |

커버리지 목표는 라인 80%입니다. 초반에는 PR 코멘트로만 알려 주고,
기능이 쌓이면 CI의 빌드 명령을 `./gradlew build -PenforceCoverage`로 바꿔 강제하세요.

### 테스트 이름 규칙

메서드 이름은 영어로, 한글 설명은 `@DisplayName`에 씁니다.
한글 메서드 이름은 테스트 리포트 파일명으로 쓰이는데, UTF-8이 아닌 환경(Windows 일부, 로케일 미설정 CI)에서는 리포트 생성이 실패합니다.

```java
@Test
@DisplayName("검증 실패는 400과 필드 에러 목록으로 응답한다")
void validationFailure_respondsWith400AndFieldErrors() { ... }
```

### 커밋 메시지

```
<type>: <요약> #이슈번호
feat: 회원가입 이메일 중복 검사 #12
```

## 5. 구조

```
.
├── .github/
│   ├── workflows/ci.yml          # commitlint + build + 커버리지
│   ├── pull_request_template.md
│   └── ISSUE_TEMPLATE/task.md
├── .githooks/                    # pre-commit, commit-msg, pre-push
├── compose.yaml                  # 로컬 Postgres
├── package.json                  # commitlint + prepare(훅 설치)
├── commitlint.config.cjs
└── src/
    ├── main/java/com/example/app/
    │   ├── Application.java
    │   └── common/exception/     # ErrorCode, BusinessException, ErrorResponse, GlobalExceptionHandler
    └── test/java/com/example/app/
        ├── support/              # @IntegrationTest, TestcontainersConfig
        └── common/exception/     # 전역 예외 처리 테스트(컨텍스트 없이 빠르게)
```

기능 패키지는 `com.example.app.<도메인>` 아래에 추가합니다. 아키텍처(레이어드/헥사고날)를 정했다면
**기능 개발 전에** 예시 도메인 하나로 패키지 골격을 먼저 커밋해 두세요. 나중에 패키지를 옮기면 충돌이 커집니다.

## 6. 새 프로젝트로 가져갈 때 바꿀 것

1. `settings.gradle`의 `rootProject.name`
2. `build.gradle`의 `group`, `description`
3. 패키지 `com.example.app` → 실제 패키지명 (IntelliJ: 패키지 우클릭 → Refactor → Rename)
4. `application.yml`의 `spring.application.name`, `application-local.yml`의 `logging.level` 패키지명
5. `compose.yaml`의 `container_name`, `.env.example`의 DB 이름
6. 필요 시 추가: QueryDSL, springdoc(Swagger), Spring Batch, Spring Security

## 7. GitHub 저장소 설정 (팀장 1회)

- **Settings → Branches → Branch protection** (`main`, `dev`):
  Require a pull request, Require status checks → `build`, `commitlint` 체크
- 훅은 로컬 우회(`--no-verify`)가 가능하므로, **최종 방어선은 CI + 브랜치 보호**입니다.
