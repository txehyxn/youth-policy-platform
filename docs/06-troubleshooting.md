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
