# ERD 설계

## 설계 목표

본 프로젝트의 핵심은 정책 정보를 저장하는 것이 아니라 사용자의 조건과 정책 조건을 비교하여 신청 가능 여부를 판별하는 Eligibility Engine 구현이다.

따라서 정책 조건을 데이터베이스에서 관리할 수 있도록 설계하며, 새로운 정책이 추가되더라도 코드 수정 없이 확장 가능하도록 구성한다.

---

# 주요 엔티티

## users

회원 계정 정보

| 컬럼명        | 설명                           |
| ---------- | ---------------------------- |
| id         | 회원 PK                        |
| email      | 이메일                          |
| password   | 비밀번호                         |
| role       | USER / ADMIN / SUPER_ADMIN   |
| created_at | 생성일, 추후 BaseTimeEntity 적용 예정 |
| updated_at | 수정일, 추후 BaseTimeEntity 적용 예정 |

### role 설계

| role        | 설명                      |
| ----------- | ----------------------- |
| USER        | 일반 사용자                  |
| ADMIN       | 정책 등록 및 수정 관리자          |
| SUPER_ADMIN | 관리자 권한 부여 및 전체 관리 권한 보유 |

설계 이유:

* 일반 사용자와 관리자의 기능 접근 범위를 분리하기 위함
* 최고 관리자가 관리자 권한을 부여하거나 회수할 수 있는 구조를 고려
* 향후 Spring Security 권한 제어에 활용 가능

---

## user_profiles

정책 판별에 공통적으로 사용되는 사용자 기본 조건 정보

| 컬럼명                   | 설명      |
| --------------------- | ------- |
| id                    | 프로필 PK  |
| user_id               | 회원 FK   |
| age                   | 나이      |
| region                | 거주 지역   |
| household_size        | 가구원 수   |
| monthly_income        | 월소득     |
| annual_income         | 연소득     |
| middle_income_percent | 중위소득 비율 |
| employed              | 취업 여부   |
| student               | 학생 여부   |
| house_owner           | 무주택 여부  |

### users - user_profiles 관계

```text
users 1 : 1 user_profiles
```

설계 이유:

* 로그인 계정 정보와 정책 판별용 정보를 분리하기 위함
* User는 인증 및 권한 관리에 집중
* UserProfile은 나이, 지역, 소득, 무주택 여부 등 정책 판별 조건에 집중
* 회원가입 후 사용자가 프로필을 작성하는 흐름을 지원
* 정책별 추가 조건은 UserProfile에 저장하지 않고 정책 상세 화면에서 별도 입력받도록 설계

---

## benefit_categories

정책 카테고리

| 컬럼명  | 설명    |
| ---- | ----- |
| id   | PK    |
| name | 카테고리명 |

예시:

* 주거
* 금융
* 취업
* 교육
* 창업
* 복지

---

## benefits

정책 기본 정보

| 컬럼명             | 설명                           |
| --------------- | ---------------------------- |
| id              | PK                           |
| title           | 정책명                          |
| description     | 정책 설명                        |
| support_amount  | 지원 내용                        |
| application_url | 신청 링크                        |
| category_id     | 카테고리 FK                      |
| created_at      | 생성일, 추후 BaseTimeEntity 적용 예정 |
| updated_at      | 수정일, 추후 BaseTimeEntity 적용 예정 |

### benefit_categories - benefits 관계

```text
benefit_categories 1 : N benefits
```

설계 이유:

* 정책을 주거, 금융, 취업, 교육, 창업, 복지 등으로 분류하기 위함
* 사용자에게 카테고리별 정책 조회 기능을 제공하기 위함
* 관리자 정책 등록 시 정책 분류를 명확히 하기 위함

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

예시:

| field_name            | operator | value    |
| --------------------- | -------- | -------- |
| age                   | >=       | 19       |
| age                   | <=       | 34       |
| region                | IN       | 수원,서울,경기 |
| house_owner           | =        | false    |
| middle_income_percent | <=       | 60       |

### benefits - benefit_conditions 관계

```text
benefits 1 : N benefit_conditions
```

설계 이유:

* 하나의 정책은 여러 개의 신청 조건을 가질 수 있음
* 정책마다 필요한 조건이 다르기 때문에 조건을 코드에 직접 작성하지 않고 데이터베이스에서 관리
* 새로운 정책 추가 시 코드 수정이 아닌 데이터 추가로 확장 가능

예를 들어 청년월세지원과 청년도약계좌는 각각 다른 조건을 가진다.

```text
청년월세지원
- age >= 19
- age <= 34
- house_owner = false
- middle_income_percent <= 60
```

```text
청년도약계좌
- age >= 19
- age <= 34
- annual_income <= 75000000
```

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

예시:

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
| created_at | 생성일   |

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

본 프로젝트는 정책 조건을 컬럼으로 직접 구현하지 않고 `benefit_conditions` 테이블로 분리하였다.

이를 통해 새로운 정책이 추가되더라도 데이터만 추가하면 되며, 코드 수정 없이 확장이 가능하다.

또한 사용자 정보는 `users`와 `user_profiles`로 분리하여 인증 정보와 정책 판별 정보를 독립적으로 관리하도록 설계하였다.

정책 정보는 `benefit_categories`, `benefits`, `benefit_conditions`로 분리하여 카테고리 분류, 정책 기본 정보, 정책 조건을 각각 독립적으로 관리한다.

이는 본 프로젝트의 핵심 설계 철학인 Data Driven Eligibility Engine 구현을 위한 구조이다.
