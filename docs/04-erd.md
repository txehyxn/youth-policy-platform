# ERD 설계

## 설계 목표

본 프로젝트의 핵심은 정책 정보를 저장하는 것이 아니라 사용자의 조건과 정책 조건을 비교하여 신청 가능 여부를 판별하는 Eligibility Engine 구현이다.

따라서 정책 조건을 데이터베이스에서 관리할 수 있도록 설계하며, 새로운 정책이 추가되더라도 코드 수정 없이 확장 가능하도록 구성한다.

---

# 주요 엔티티

## users

회원 정보

| 컬럼명        | 설명           |
| ---------- | ------------ |
| id         | 회원 PK        |
| email      | 이메일          |
| password   | 비밀번호         |
| role       | USER / ADMIN |
| created_at | 생성일          |

---

## user_profiles

정책 판별에 필요한 사용자 정보

| 컬럼명               | 설명       |
| ----------------- | -------- |
| id                | PK       |
| user_id           | 회원 FK    |
| age               | 나이       |
| region            | 거주 지역    |
| household_size    | 가구원 수    |
| monthly_income    | 월소득      |
| annual_income     | 연소득      |
| employment_status | 취업 여부    |
| student_status    | 학생 여부    |
| house_owner       | 주택 소유 여부 |

---

## benefits

정책 기본 정보

| 컬럼명             | 설명      |
| --------------- | ------- |
| id              | PK      |
| title           | 정책명     |
| description     | 정책 설명   |
| support_amount  | 지원 내용   |
| application_url | 신청 링크   |
| category_id     | 카테고리 FK |

---

## benefit_categories

정책 카테고리

| 컬럼명  | 설명    |
| ---- | ----- |
| id   | PK    |
| name | 카테고리명 |

예시

* 주거
* 금융
* 취업
* 교육

---

## benefit_conditions

정책 조건

Eligibility Engine 핵심 테이블

| 컬럼명        | 설명     |
| ---------- | ------ |
| id         | PK     |
| benefit_id | 정책 FK  |
| field_name | 비교 대상  |
| operator   | 비교 연산자 |
| value      | 기준값    |
| required   | 필수 여부  |

예시

| field_name     | operator | value |
| -------------- | -------- | ----- |
| age            | >=       | 19    |
| age            | <=       | 34    |
| house_owner    | =        | false |
| income_percent | <=       | 60    |

---

## benefit_schedules

정책 신청 일정

| 컬럼명        | 설명    |
| ---------- | ----- |
| id         | PK    |
| benefit_id | 정책 FK |
| start_date | 시작일   |
| end_date   | 종료일   |

---

## benefit_documents

필요 서류

| 컬럼명           | 설명    |
| ------------- | ----- |
| id            | PK    |
| benefit_id    | 정책 FK |
| document_name | 서류명   |

예시

* 주민등록등본
* 소득증빙서류
* 재직증명서

---

## bookmarks

관심 정책 저장

| 컬럼명        | 설명    |
| ---------- | ----- |
| id         | PK    |
| user_id    | 회원 FK |
| benefit_id | 정책 FK |

---

## eligibility_results

정책 판별 결과 로그

| 컬럼명        | 설명                                       |
| ---------- | ---------------------------------------- |
| id         | PK                                       |
| user_id    | 회원 FK                                    |
| benefit_id | 정책 FK                                    |
| result     | ELIGIBLE / NOT_ELIGIBLE / NEED_MORE_INFO |
| checked_at | 판별 시각                                    |

---

# ERD 설계 핵심

본 프로젝트는 정책 조건을 컬럼으로 직접 구현하지 않고 benefit_conditions 테이블로 분리하였다.

이를 통해 새로운 정책이 추가되더라도 데이터만 추가하면 되며, 코드 수정 없이 확장이 가능하다.

이는 본 프로젝트의 핵심 설계 철학인 Data Driven Eligibility Engine 구현을 위한 구조이다.
