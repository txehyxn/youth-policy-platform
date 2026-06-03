# 청년 맞춤형 정부 혜택·제도 통합 플랫폼

개인 조건 기반으로 청년 정책 및 정부 혜택의 신청 가능 여부를 자동 판별하고, 정책 정보를 효율적으로 관리할 수 있는 플랫폼입니다.

---

## 프로젝트 소개

정부 혜택과 지원 사업은 다양한 사이트에 분산되어 있으며, 신청 조건 또한 복잡하여 많은 청년들이 혜택을 놓치고 있습니다.

본 프로젝트는 사용자의 정보를 기반으로 정책 신청 가능 여부를 자동 판별하는 Eligibility Engine을 구현하여 이러한 문제를 해결하는 것을 목표로 합니다.

### 핵심 가치

> 몰라서 놓치는 혜택을 줄인다.

---

## 문제 정의

현재 청년 정책 이용 과정에는 다음과 같은 문제점이 존재합니다.

* 정부 혜택 정보가 여러 사이트에 분산되어 있음
* 내가 대상자인지 직접 확인해야 함
* 중위소득 및 복잡한 자격 조건 확인이 어려움
* 신청 기간을 놓치는 경우가 많음

---

## 해결 방안

사용자가 정보를 한 번 입력하면:

* 신청 가능
* 신청 불가능
* 추가 정보 필요

를 자동 판별하고,

판별 결과에 대한 사유까지 제공하는 Eligibility Engine 기반 플랫폼을 구축한다.

예시:

```text
청년월세지원

신청 가능

사유
- 나이 조건 충족
- 지역 조건 충족
- 무주택 조건 충족
- 중위소득 기준 충족
```

---

## 주요 기능

### 사용자 기능

* 회원가입 / 로그인
* 개인정보 입력
* 중위소득 자동 계산
* 정책 조회
* 정책 상세 조회
* 정책 자격 판별
* 관심 정책 저장

### 관리자 기능

* 정책 등록
* 정책 조건 등록
* 일정 등록
* 필요 서류 등록

---

## 핵심 기능

### Eligibility Engine

사용자 조건과 정책 조건을 비교하여 신청 가능 여부를 자동 판별한다.

결과:

* ELIGIBLE
* NOT_ELIGIBLE
* NEED_MORE_INFO

---

## 기술 스택

### Backend

* Java 25
* Spring Boot
* Spring Security
* Spring Data JPA

### Database

* MySQL

### Frontend

* Thymeleaf
* Bootstrap
* JavaScript
* FullCalendar

### Tools

* IntelliJ IDEA Community
* GitHub
* Postman
* MySQL Workbench

### Deployment

* AWS EC2
* AWS RDS

---

## 프로젝트 문서

| 문서                    | 설명                    |
| --------------------- | --------------------- |
| 01-project-overview   | 프로젝트 개요               |
| 02-requirements       | 요구사항 정의               |
| 03-tech-stack         | 기술 스택 선정 이유           |
| 04-erd                | ERD 설계                |
| 05-eligibility-engine | Eligibility Engine 설계 |
| 06-troubleshooting    | 문제 해결 기록              |

---

## 개발 목표

단순 CRUD 프로젝트가 아닌 다음 역량을 보여주는 것을 목표로 한다.

* 정책 조건 모델링
* 관계형 데이터베이스 설계
* Eligibility Engine 구현
* Spring Boot 기반 웹 서비스 개발
* GitHub 문서화 및 협업 방식 적용
* AWS 배포 경험

---

## 향후 확장 계획

* 정책 캘린더
* 공공 API 자동 수집
* 카카오 로그인
* 데이터 분석 대시보드
* 정책 추천 시스템

---

## 개발 현황

### 완료

* 프로젝트 기획
* 요구사항 정의
* 기술 스택 선정
* ERD 설계
* Eligibility Engine 설계

### 진행 예정

* 프로젝트 초기 세팅
* 엔티티 설계
* Spring Security 적용
* 관리자 기능 구현
* Eligibility Engine 구현
