Free Point System API
1. 프로젝트 소개

본 프로젝트는 무료 포인트 시스템 API를 구현한 Spring Boot 기반 서비스입니다.

사용자는 포인트를 적립하고 주문 시 사용할 수 있으며,
적립 취소 및 사용 취소 기능을 제공합니다.

또한 다음 요구사항을 만족하도록 설계되었습니다.

포인트 적립

포인트 적립 취소

포인트 사용

포인트 사용 취소

적립된 포인트의 사용 추적

포인트 만료 관리

관리자 수기 지급 포인트 구분

정책 기반 포인트 제한 관리

포인트는 단순 잔액 방식이 아니라 적립 단위 기반(Bucket 기반) 으로 관리하여
어떤 포인트가 어떤 주문에 사용되었는지 정확하게 추적할 수 있도록 설계했습니다.

2. 기술 스택
   항목	기술
   Language	Java 21
   Framework	Spring Boot 3.x
   ORM	Spring Data JPA
   Database	H2 Database
   Build Tool	Gradle
   API Test	Postman
3. 실행 방법
   3.1 프로젝트 실행
   ./gradlew bootRun

또는 IntelliJ에서

Run → FreePointSystemApplication

실행 후 서버가 시작됩니다.

http://localhost:8080
4. H2 Console 접속

개발 및 테스트 편의를 위해 H2 In-Memory DB를 사용합니다.

접속 URL

http://localhost:8080/h2-console

접속 정보

항목	값
JDBC URL	jdbc:h2:mem:pointdb
User Name	sa
Password	(비워둠)
5. 주요 기능
   5.1 포인트 적립

사용자에게 포인트를 적립합니다.

제약 조건

최소 적립 포인트 : 1

최대 적립 포인트 : 100,000

만료일 : 1일 이상 ~ 5년 미만

기본 만료일 : 365일

관리자는 수기 지급 포인트를 지급할 수 있으며 일반 적립과 구분됩니다.

5.2 포인트 적립 취소

특정 적립 포인트를 취소할 수 있습니다.

제약 조건

해당 적립 포인트가 사용된 적이 없어야 취소 가능

5.3 포인트 사용

포인트는 주문 시에만 사용 가능합니다.

사용 정책

관리자 수기 지급 포인트 우선 사용

만료일이 가까운 포인트 우선 사용

포인트 사용 시 다음 정보가 기록됩니다.

주문번호

사용된 포인트 금액

어떤 적립 포인트에서 사용되었는지

5.4 포인트 사용 취소

포인트 사용은 전체 또는 일부 취소가 가능합니다.

취소 로직

원래 사용된 적립 포인트로 복구

단, 해당 적립 포인트가 이미 만료된 경우

복구 대신 신규 적립 포인트 생성

6. API 명세
   6.1 포인트 적립
   POST /api/points/earn
   Request
   {
   "userId": "user-001",
   "amount": 1000,
   "expireDays": 365,
   "manual": false,
   "description": "event reward"
   }
   6.2 포인트 적립 취소
   POST /api/points/earn-cancel
   Request
   {
   "userId": "user-001",
   "pointKey": "적립 pointKey"
   }
   6.3 포인트 사용
   POST /api/points/use
   Request
   {
   "userId": "user-001",
   "orderNo": "A1234",
   "amount": 1200
   }
   6.4 포인트 사용 취소
   POST /api/points/use-cancel
   Request
   {
   "userId": "user-001",
   "pointKey": "사용 pointKey",
   "cancelAmount": 1100
   }
   6.5 포인트 잔액 조회
   GET /api/points/balance/{userId}
7. 설계 개요

본 시스템은 포인트 추적을 위해 다음과 같은 구조로 설계되었습니다.

7.1 PointLedger

모든 포인트 활동을 기록하는 원장 테이블

적립

적립 취소

사용

사용 취소

만료 복구 신규 적립

모든 포인트 이벤트는 Ledger에 기록됩니다.

7.2 PointBucket

포인트 적립 단위 저장소

각 적립 건별로 다음 정보를 관리합니다.

최초 적립 금액

현재 잔액

만료일

포인트 유형

이를 통해 포인트 사용 시 어떤 적립 포인트를 사용했는지 추적할 수 있습니다.

7.3 PointUsageAllocation

포인트 사용 시 어떤 적립 포인트에서 얼마가 사용되었는지 기록합니다.

예시

1200 포인트 사용

A 적립 → 1000 사용
B 적립 → 200 사용

이 정보를 통해 사용 취소 시 정확하게 복구할 수 있습니다.

8. 정책 관리

포인트 정책은 하드코딩하지 않고 DB 정책 테이블로 관리합니다.

테이블

point_policy

관리 항목

정책	설명
maxEarnPerTxn	1회 최대 적립 가능 포인트
maxBalancePerUser	사용자 최대 보유 포인트
defaultExpireDays	기본 만료일
9. 사용 우선순위 정책

포인트 사용 시 다음 우선순위를 적용합니다.

관리자 수기 지급 포인트 우선

만료일이 빠른 포인트 우선

적립 순서 기준 사용

10. 테스트

테스트 실행

./gradlew test

테스트 주요 시나리오

정상 적립

수기 지급 포인트 우선 사용

포인트 사용

부분 사용 취소

이미 사용된 적립 취소 실패

11. ERD

ERD 파일 위치

src/main/resources/erd.png

ERD에는 다음 테이블이 포함됩니다.

users

point_policy

point_ledger

point_bucket

point_usage_allocation

12. 프로젝트 구조
    src
    ├─ controller
    ├─ service
    ├─ repository
    ├─ domain
    ├─ dto
    ├─ exception
    └─ resources
    └─ docs
    ├─ erd.png
    └─ aws-architecture.png
13. 개선 가능 사항

추가적으로 다음 기능을 개선할 수 있습니다.

동시성 처리

포인트 만료 배치 처리

Swagger API 문서

Redis 캐싱

대용량 데이터 최적화

14. 결론

본 프로젝트는 단순 잔액 방식이 아닌 적립 단위 기반 포인트 시스템을 구현하여

포인트 사용 추적

정확한 사용 취소

만료 포인트 처리

정책 기반 관리

가 가능하도록 설계되었습니다.