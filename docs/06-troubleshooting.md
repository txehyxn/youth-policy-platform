# Trouble Shooting

## 목적

프로젝트 개발 과정에서 발생한 문제와 해결 과정을 기록한다.

단순히 오류를 해결하는 것을 넘어 문제 분석 과정과 해결 방법을 문서화하여 유지보수성과 개발 경험을 축적하는 것을 목표로 한다.

---

# 작성 템플릿

## 문제

발생한 오류 또는 문제 상황 설명

### 예시

```text
로그인 요청 시 403 Forbidden 발생
```

---

## 원인 분석

문제가 발생한 원인 조사

### 예시

```text
Spring Security에서 CSRF 검증이 활성화되어 있었음
```

---

## 해결 방법

적용한 해결 방법

### 예시

```text
Security 설정에서 CSRF 정책 수정
```

---

## 결과

문제가 해결되었는지 여부

### 예시

```text
로그인 정상 동작 확인
```

---

# 개발 기록

## [예시] GitHub Repository 초기 구성

### 문제

프로젝트 구조와 문서 관리 방식이 정해져 있지 않아 향후 관리가 어려울 수 있음

### 원인 분석

프로젝트 규모가 커질수록 설계 문서와 개발 기록의 중요성이 증가함

### 해결 방법

docs 폴더를 생성하여 프로젝트 문서를 분리 관리

구성:

```text
docs/
├── 01-project-overview.md
├── 02-requirements.md
├── 03-tech-stack.md
├── 04-erd.md
├── 05-eligibility-engine.md
├── 06-troubleshooting.md
```

### 결과

프로젝트 설계 과정과 개발 과정을 체계적으로 기록할 수 있는 구조 확보

---

# 실제 개발 중 기록 예정

향후 기록 예시

* Spring Security 로그인 오류
* JPA 연관관계 매핑 문제
* N+1 문제 해결
* MySQL 데이터 타입 오류
* Eligibility Engine 판별 로직 오류
* 중위소득 계산 오류
* AWS 배포 오류
* Git 충돌 해결
* API 데이터 파싱 오류

---

# 작성 원칙

문제 발생 시 아래 순서로 기록한다.

1. 문제
2. 원인 분석
3. 해결 방법
4. 결과

문제 해결 과정이 명확하게 드러나도록 작성한다.

코드보다 문제 해결 과정과 의사결정 이유를 중심으로 기록한다.

---

# 실제 트러블슈팅 기록

## 1. Spring Boot DataSource 설정 오류

### 문제

Spring Boot 실행 시 다음 오류가 발생하였다.

```text
Failed to configure a DataSource
url attribute is not specified
```

### 원인 분석

Spring Data JPA와 MySQL Driver를 프로젝트에 추가했지만, 아직 MySQL 연결 정보를 설정하지 않은 상태였다.

### 해결 방법

초기 개발 단계에서는 데이터베이스 연결보다 Spring Boot 실행 확인이 우선이므로 DataSource 자동 설정을 임시로 제외하였다.

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
```

### 결과

데이터베이스 연결 없이 Spring Boot 애플리케이션 실행에 성공하였다.

---

## 2. OneDrive 경로 사용으로 인한 Gradle 빌드 오류

### 문제

프로젝트 빌드 중 다음 오류가 발생하였다.

```text
Unable to delete directory 'build/classes/java/main'
```

### 원인 분석

프로젝트가 OneDrive 경로 내부에 위치해 있었으며 OneDrive 동기화 또는 실행 중인 Java 프로세스가 build 폴더를 점유하고 있었다.

### 해결 방법

프로젝트 위치를 다음 경로로 변경하였다.

```text
C:\youth-policy-platform
```

### 결과

Gradle 빌드 오류가 해결되었고 Spring Boot 애플리케이션이 정상 실행되었다.

---

## 3. GitHub Push 권한 오류

### 문제

GitHub Push 시 다음 오류가 발생하였다.

```text
Permission to txehyxn/youth-policy-platform.git denied to taetaeq
```

### 원인 분석

Windows에 저장된 GitHub 자격 증명이 현재 사용하려는 GitHub 계정과 달랐다.

### 해결 방법

Windows 자격 증명 관리자에서 GitHub 관련 자격 증명을 삭제한 후 올바른 GitHub 계정으로 다시 로그인하였다.

### 결과

GitHub 인증 문제가 해결되었으며 정상적으로 Push가 가능해졌다.

---

## 4. GitHub 원격 저장소 병합 충돌

### 문제

최초 Push 시 다음 오류가 발생하였다.

```text
Updates were rejected because the remote contains work that you do not have locally
```

이후 병합 과정에서 다음 충돌이 발생하였다.

```text
CONFLICT (add/add): Merge conflict in .gitignore
```

### 원인 분석

GitHub 저장소에는 README 및 문서 파일이 존재했고, 로컬 저장소에는 Spring Boot 프로젝트가 존재하여 서로 다른 Git 이력을 가지고 있었다.

### 해결 방법

```bash
git pull origin main --allow-unrelated-histories
```

명령어로 병합을 수행한 후 `.gitignore` 충돌을 해결하였다.

```bash
git checkout --ours .gitignore
git add .gitignore
git commit -m "merge: GitHub 원격 저장소 병합"
```

### 결과

로컬 프로젝트와 GitHub 저장소가 정상적으로 병합되었고 Push에 성공하였다.

# Trouble Shooting #3 - MySQL Server 초기화 실패

## 발생 일시

2026-06-13

---

## 문제 상황

Spring Boot 프로젝트에 MySQL을 연결하기 위해 MySQL Community Server를 설치하였다.

설치 과정 중 `Apply Configuration` 단계에서 아래 항목이 실패하였다.

```text
Initializing database (may take a long time)
```

MySQL Installer 로그에는 다음과 같은 메시지가 출력되었다.

```text
Attempting to run MySQL Server with --initialize-insecure option...
Starting process for MySQL Server 8.0.46...
Failed to start process for MySQL Server 8.0.46.
Database initialization failed.
```

---

## 원인 분석

처음에는 비밀번호 설정 문제로 예상하였으나 로그를 분석한 결과 MySQL 서비스(MySQL80)는 등록되어 있었지만 실제 실행 파일(`mysqld.exe`)이 정상적으로 동작하지 않는 상태였다.

서비스 상태 확인:

```cmd
sc query MySQL80
```

서비스 설정 확인:

```cmd
sc qc MySQL80
```

결과:

```text
BINARY_PATH_NAME :
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe"
```

기존 설치 과정이 비정상 종료되면서 서비스 정보만 남아 있는 상태였다.

---

## 해결 과정

### 1. 기존 MySQL 서비스 삭제

```cmd
sc delete MySQL80
```

추가적으로 남아 있는 서비스 레지스트리 제거

```cmd
reg delete "HKLM\SYSTEM\CurrentControlSet\Services\MySQL80" /f
```

재부팅 후 서비스 제거 여부 확인

```cmd
sc query MySQL80
```

결과:

```text
[SC] OpenService 실패 1060
지정된 서비스가 설치된 서비스로 존재하지 않습니다.
```

---

### 2. MySQL 제품 제거

MySQL Installer에서 기존 항목 제거

* MySQL Server
* MySQL Workbench
* MySQL Shell
* MySQL Router
* Samples and Examples

---

### 3. MySQL 재설치

MySQL Installer를 통해 다시 설치 진행

설치 완료 후 서비스 상태 확인

```cmd
sc query MySQL80
```

결과:

```text
STATE : 4 RUNNING
```

---

### 4. MySQL 접속 확인

```cmd
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

접속 성공 확인

---

### 5. 데이터베이스 생성

```sql
CREATE DATABASE youth_policy_platform
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

---

## 결과

Spring Boot와 MySQL 연동에 성공하였다.

Hibernate가 엔티티를 기반으로 아래 테이블을 자동 생성하였다.

* user
* user_profile
* benefit
* benefit_category
* benefit_condition
* benefit_document
* benefit_schedule
* bookmark

---

## 배운 점

설치 오류 발생 시 단순 재설치보다 로그를 먼저 확인해야 한다.

Windows 서비스와 레지스트리에 이전 설치 정보가 남아 있으면 MySQL 초기화가 실패할 수 있으며, 서비스 삭제 후 재설치가 가장 확실한 해결 방법임을 확인하였다.

---

# Trouble Shooting - Eligibility Engine 프로필 ID 오류

## 발생 일시

2026-06-19

---

## 문제 상황

Eligibility Engine 1차 구현 후 신청 가능 여부를 테스트하기 위해 아래 URL로 요청하였다.

```text
http://localhost:8080/eligibility/check?benefitId=1&profileId=2
```

하지만 다음과 같은 500 Internal Server Error가 발생하였다.

```text
Whitelabel Error Page
There was an unexpected error (type=Internal Server Error, status=500).

존재하지 않는 프로필입니다.
```

콘솔 및 에러 페이지에서는 다음 위치에서 예외가 발생한 것을 확인하였다.

```text
java.lang.IllegalArgumentException: 존재하지 않는 프로필입니다.
    at com.taehyun.youthpolicyplatform.eligibility.service.EligibilityService.check(EligibilityService.java:29)
    at com.taehyun.youthpolicyplatform.eligibility.controller.EligibilityController.check(EligibilityController.java:25)
```

---

## 원인 분석

Eligibility Engine 테스트 URL에는 두 개의 ID가 필요하다.

```text
benefitId  = 정책 ID
profileId  = 사용자 프로필 ID
```

하지만 테스트 과정에서 `profileId`를 사용자 ID와 혼동하였다.

현재 구조는 다음과 같다.

```text
users
- id: 회원 ID

user_profile
- id: 프로필 ID
- user_id: 회원 ID
```

즉, `userId`와 `profileId`는 서로 다른 값이다.

`/admin/user-profiles` 화면에서 실제 등록된 프로필 ID를 확인하지 않고 존재하지 않는 `profileId`로 요청하여 다음 코드에서 예외가 발생하였다.

```java
UserProfile profile = userProfileRepository.findById(profileId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));
```

---

## 해결 방법

`/admin/user-profiles` 화면에서 실제 등록된 프로필 ID를 확인하였다.

등록된 프로필 중 존재하는 ID인 `1` 또는 `3`을 사용하여 다시 요청하였다.

예시:

```text
http://localhost:8080/eligibility/check?benefitId=1&profileId=1
```

또는

```text
http://localhost:8080/eligibility/check?benefitId=1&profileId=3
```

---

## 결과

존재하는 프로필 ID로 요청하자 Eligibility Engine이 정상 동작하였다.

정책 조건과 사용자 프로필 정보가 비교되었고, 조건별 판별 결과가 화면에 출력되었다.

출력 예시:

```text
신청 가능 여부 결과

청년월세 특별지원

조건별 판별 결과
age >= 19 : 통과
age <= 34 : 통과
house_owner = false : 통과 또는 실패
middle_income_percent <= 60 : 통과
```

---

## 추가로 확인한 문제

프로필 등록 과정에서 `User ID`와 `UserProfile ID`가 혼동될 수 있음을 확인하였다.

현재 테스트 화면에서는 사용자가 직접 `userId`를 입력하도록 구성되어 있다.

```text
사용자 ID 입력
```

하지만 실제 서비스에서는 사용자가 직접 ID를 입력하지 않는다.

로그인 기능 구현 후에는 로그인한 사용자의 ID를 자동으로 사용하도록 변경할 예정이다.

개선 예정:

```text
현재:
관리자가 userId 직접 입력

개선 후:
로그인한 사용자 정보에서 userId 자동 추출
```

---

## 배운 점

1. 1:1 관계에서는 `id`와 `FK`를 명확히 구분해야 한다.
2. `User ID`와 `UserProfile ID`는 서로 다른 값이다.
3. 테스트 URL을 직접 입력할 때는 실제 DB에 존재하는 PK 값을 확인해야 한다.
4. 사용자 화면에서는 ID를 직접 입력받지 않고 로그인 세션에서 자동으로 처리하는 구조가 필요하다.
5. Whitelabel Error Page가 발생했을 때는 에러 메시지보다 실제 예외 발생 위치를 먼저 확인해야 한다.

---

## 향후 개선 방향

현재는 예외 발생 시 Whitelabel Error Page가 출력된다.

향후에는 예외 처리 기능을 추가하여 사용자에게 더 명확한 오류 메시지를 제공할 예정이다.

예시:

```text
존재하지 않는 프로필입니다.
프로필을 먼저 등록한 후 다시 시도해주세요.
```

또한 Eligibility Engine 테스트 화면을 별도로 만들어 사용자가 직접 URL에 `benefitId`, `profileId`를 입력하지 않도록 개선할 예정이다.

# Trouble Shooting - 정책 삭제 시 외래키(Foreign Key) 제약 오류

## 발생 일시

2026-06-24

---

## 문제 상황

정책 삭제 기능 구현 후 `/admin/benefits` 화면에서 정책 삭제 버튼을 클릭하였으나 삭제가 정상적으로 수행되지 않았다.

다음과 같은 오류가 발생하였다.

```text
Cannot delete or update a parent row:
a foreign key constraint fails
(benefit_condition ...)
```

---

## 원인 분석

현재 데이터베이스 구조는 다음과 같다.

```text
Benefit (정책)
    ↓
BenefitCondition (정책 조건)
```

BenefitCondition 테이블은 Benefit의 ID를 외래키(FK)로 참조하고 있다.

예시

청년월세 특별지원

* age >= 19
* age <= 34
* house_owner = false
* middle_income_percent <= 60

위와 같이 조건이 연결된 상태에서 정책을 먼저 삭제하려고 하면 데이터 무결성 문제가 발생한다.

MySQL은 참조 중인 부모 데이터를 삭제하지 못하도록 외래키 제약을 적용하기 때문에 삭제가 차단되었다.

---

## 해결 방법

정책 삭제 전에 연결된 조건을 먼저 삭제하도록 처리하였다.

실행 순서

```text
1. /admin/conditions 이동

2. 해당 정책의 조건 삭제

3. /admin/benefits 이동

4. 정책 삭제
```

---

## 결과

정책 조건 삭제 후 정책 삭제가 정상적으로 수행되었다.

현재 관리자 화면에서는 다음 순서로 삭제하도록 운영한다.

```text
조건 삭제
    ↓
정책 삭제
    ↓
카테고리 삭제
```

---

## 배운 점

1. JPA 엔티티 간 연관관계에서는 외래키 제약을 반드시 고려해야 한다.
2. 부모 데이터를 삭제하기 전에 자식 데이터를 먼저 정리해야 한다.
3. CRUD 기능 구현 시 생성(Create)보다 삭제(Delete)가 더 많은 제약을 가진다.
4. 향후 Cascade 옵션을 사용할 경우 연쇄 삭제에 대한 충분한 검토가 필요하다.

---

## 향후 개선 방향

현재는 관리자가 직접 순서대로 삭제해야 한다.

향후에는 다음 중 하나를 적용할 예정이다.

```text
방법 1
정책 삭제 시 연결된 조건 자동 삭제

방법 2
조건이 존재하면 삭제 불가 메시지 출력

"연결된 조건이 존재하여 삭제할 수 없습니다."
```

실무 서비스에서는 데이터 보호를 위해 방법 2가 더 안전할 것으로 판단된다.

# Trouble Shooting - Eligibility Engine 안내 메시지 미출력 문제

## 발생 일시

2026-06-26

---

## 문제 상황

Eligibility Engine 개선 작업 중 조건별 안내 메시지 기능을 추가하였다.

예상 결과

```text
중위소득 기준을 충족하지 않습니다.
```

실제 결과

```text
조건을 충족하지 않습니다.
```

기본 메시지만 출력되었다.

---

## 원인 분석

EligibilityService의 createMessage() 메서드에서는 fieldName 값을 기준으로 안내 메시지를 생성하고 있었다.

```java
if (fieldName.equals("middle_income_percent")) {
    return "중위소득 기준을 충족하지 않습니다.";
}
```

하지만 DB에 저장된 필드명에 공백이 포함되거나 문자열 비교 과정에서 정확하게 일치하지 않아 조건문이 실행되지 않았다.

그 결과 마지막 기본 메시지가 반환되었다.

---

## 해결 방법

문자열 비교 전 trim()을 적용하였다.

```java
String fieldName = condition.getFieldName().trim();
```

또한 checkCondition()에서도 동일하게 적용하였다.

```java
String fieldName = condition.getFieldName().trim();
```

---

## 결과

정책 판별 결과 화면에서 조건별 맞춤 안내 메시지가 정상적으로 출력되었다.

```text
중위소득 기준을 충족하지 않습니다.
주택 소유 여부 조건을 충족하지 않습니다.
```

---

## 배운 점

1. 문자열 비교 시 공백 문제를 항상 고려해야 한다.
2. 사용자 입력 데이터는 trim()을 적용하는 것이 안전하다.
3. 기능 자체보다 데이터 정합성 문제가 더 많은 오류를 유발할 수 있다.

# Trouble Shooting - 정책 상세 페이지에서 조건 관리 분산 문제

## 발생 일시

2026-06-26

---

## 문제 상황

초기 설계에서는 정책 조건 등록 및 삭제를 별도의 화면에서 수행하였다.

```text
/admin/conditions
```

관리자는 정책 상세 페이지에서 조건을 확인한 후 다시 조건 관리 화면으로 이동해야 했다.

---

## 원인 분석

정책과 정책 조건은 강하게 연관된 데이터임에도 관리 화면이 분리되어 있었다.

작업 흐름

```text
정책 상세 조회
→ 조건 관리 화면 이동
→ 조건 등록
→ 정책 상세 화면 이동
```

관리 효율이 떨어지고 사용성이 좋지 않았다.

---

## 해결 방법

정책 상세 페이지에 다음 기능을 추가하였다.

```text
조건 등록
조건 목록 조회
조건 삭제
```

구현 내용

```text
POST /admin/benefits/{benefitId}/conditions
POST /admin/benefits/{benefitId}/conditions/delete/{conditionId}
```

---

## 결과

관리자가 정책 상세 페이지 하나에서 정책 조건을 모두 관리할 수 있게 되었다.

작업 흐름

```text
정책 상세
→ 조건 등록
→ 조건 확인
→ 조건 삭제
```

---

## 배운 점

1. 관련 데이터는 가능한 한 동일한 화면에서 관리하는 것이 효율적이다.
2. CRUD 구현 후 사용자 흐름(UX)을 점검하는 과정이 중요하다.
3. 기능 구현뿐 아니라 관리 편의성도 고려해야 한다.

# Trouble Shooting - Spring Security 로그인 기능 구현 및 회원가입 검증

## 발생 일시

2026-06-29

---

## 문제 상황

회원가입 기능을 구현한 후 로그인을 시도했지만 정상적으로 로그인되지 않았다.

로그인 요청 시 다음과 같은 오류가 발생하였다.

```text
405 Method Not Allowed
Method 'POST' is not supported.
```

또한 회원가입 화면에서는 비밀번호와 비밀번호 확인이 서로 달라도 사용자가 즉시 확인할 수 없어 일반적인 웹 서비스보다 사용자 경험이 좋지 않았다.

---

## 원인 분석

Spring Security에서 로그인 화면만 구현되어 있었고 실제 로그인 요청을 처리하는 설정이 존재하지 않았다.

작업 흐름

```text
로그인 화면
→ POST /login 요청
→ 로그인 처리 기능 없음
→ 405 오류 발생
```

또한 회원가입은 서버에서만 비밀번호를 검증하고 있어 회원가입 버튼을 누르기 전에는 오류를 확인할 수 없었다.

---

## 해결 방법

Spring Security 로그인 기능을 추가하였다.

구현 내용

```text
CustomUserDetails 구현
CustomUserDetailsService 구현
BCryptPasswordEncoder 적용
formLogin() 설정
로그인 성공 시 메인 페이지 이동
```

회원가입 화면도 함께 개선하였다.

```text
비밀번호 실시간 일치 검사(JavaScript)
비밀번호 일치 시 초록색 안내 메시지 출력
비밀번호 불일치 시 빨간색 안내 메시지 출력
비밀번호가 일치할 경우에만 회원가입 버튼 활성화
```

또한 서버에서도 이메일 중복 여부와 비밀번호 일치 여부를 다시 검증하도록 구현하였다.

---

## 결과

회원가입부터 로그인까지 정상적으로 동작하는 인증 기능을 구현하였다.

작업 흐름

```text
회원가입
→ 비밀번호 암호화 저장
→ 로그인
→ 메인 페이지 이동
→ 로그인 사용자 정보 표시
```

회원가입 화면에서도 비밀번호 일치 여부를 실시간으로 확인할 수 있도록 개선하였다.

---

## 배운 점

1. Spring Security는 로그인 화면뿐만 아니라 인증 처리(formLogin) 설정까지 함께 구성해야 정상적으로 동작한다.
2. 사용자 편의를 위한 클라이언트(JavaScript) 검증과 서버 검증은 함께 구현해야 한다.
3. 비밀번호는 BCrypt를 이용하여 암호화한 후 저장해야 안전한 인증 시스템을 구축할 수 있다.
# Trouble Shooting - 로그인 사용자 기반 마이페이지 프로필 관리 구현

## 발생 일시

2026-07-04

---

## 문제 상황

기존 Eligibility Engine은 테스트를 위해 profileId를 직접 전달하는 방식으로 구현되어 있었다.

```text
/eligibility/check?benefitId=1&profileId=4
```

이 구조는 로그인한 사용자와 관계없이 특정 프로필을 조회하기 때문에 실제 서비스 구조와 맞지 않았다.

또한 마이페이지 기능이 없어 사용자가 직접 자신의 프로필을 등록하거나 수정할 수 없었으며, UserProfile 저장 시 기존 프로필이 있어도 새로운 프로필이 계속 생성되는 문제가 있었다.

---

## 원인 분석

1. Eligibility Engine이 Authentication이 아닌 profileId를 기준으로 동작하고 있었다.
2. UserProfileService에서 로그인 사용자의 기존 프로필 존재 여부를 확인하지 않고 항상 새로운 UserProfile 객체를 생성하였다.
3. 사용자 프로필을 등록·조회·수정할 수 있는 화면이 구현되어 있지 않았다.

---

## 해결 방법

1. Authentication을 이용하여 로그인한 사용자의 이메일을 조회하도록 변경하였다.
2. 이메일을 통해 User를 조회한 후 UserProfile을 조회하도록 구조를 변경하였다.
3. UserProfileService에 saveForLoggedInUser() 메서드를 구현하여 기존 프로필이 존재하면 update()를 수행하고, 없으면 신규 등록하도록 수정하였다.
4. UserProfile 엔티티에 update() 메서드를 추가하여 기존 데이터를 수정할 수 있도록 개선하였다.
5. /my/profile 화면을 구현하여 로그인한 사용자가 자신의 프로필을 등록, 조회, 수정할 수 있도록 변경하였다.
6. Benefit 화면에서도 테스트용 profileId 선택 기능을 제거하고 로그인 사용자 기준으로 Eligibility Engine이 동작하도록 변경하였다.

---

## 결과

- 로그인한 사용자 기준으로 프로필 등록, 조회, 수정이 가능해졌다.
- 사용자당 하나의 프로필만 유지되는 구조로 개선되었다.
- Eligibility Engine이 로그인한 사용자의 프로필을 자동으로 조회하도록 변경되었다.
- 테스트용 profileId 전달 방식을 제거하여 실제 서비스 구조에 맞는 로직으로 개선하였다.

---

## 배운 점

1. 테스트를 위해 사용한 하드코드는 실제 서비스 전환 시 반드시 제거해야 한다.
2. OneToOne 관계에서는 기존 데이터 존재 여부를 먼저 확인한 후 등록 또는 수정을 수행해야 한다.
3. Authentication을 활용하면 로그인 사용자 정보를 안전하게 조회할 수 있으며, 실제 서비스 구조와도 자연스럽게 연결할 수 있다.
4. 기능 구현뿐 아니라 사용자 스스로 데이터를 관리할 수 있는 화면을 함께 제공해야 서비스 완성도가 높아진다.