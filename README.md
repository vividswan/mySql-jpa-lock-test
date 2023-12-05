# Lock Test (MySQL, JPA)

## synchronized

- 트랜잭션 메소드에 `@Transactional`
  - 프록시 패턴에 의해 관련 메소드를 래핑한 클래스에서 트랜잭션을 끝내서 DB에 저장하기 전 다른 쓰레드가 synchronized 메서드에 접근 가능하기 때문에 @Transaction이 있으면 제대로 동작하지 않음
  - synchronized는 프로세스 단위로 lock을 제어하기 때문에 다중 서버에선 유효하지 않음

## 비관적 LOCK (LockModeType.PESSIMISTIC_WRITE)

- 비관적 락은 조회 시 쿼리문에서 "for update"

```sql
  org.hibernate.SQL   : select p1_0.id,p1_0.product_id,p1_0.quantity from product p1_0 where p1_0.product_id=? for update
```

- 장점
  - exclusiveLock(배타 Lock)에 의해 데이터 정합성 보장
  - 충돌이 빈번하게 일어날 때 낙관적 락보다 성능이 좋을 수 있음
- 단점
  - 별도의 Lock 잡기 때문에 성능 감소가 있을 수 있음

## 낙관적 LOCK (LockModeType.OPTIMISTIC)

- 낙관적 락은 조회 시 쿼리문에서 "version" 조회

```sql
  org.hibernate.SQL   : update product set product_id=?,quantity=?,version=? where id=? and version=?
```

- 장점
  - 별도의 Lock 잡지 않으므로 성능상 이점이 있을 수 있음 (충돌이 빈번하게 일어나지 않을 때)
- 단점
  - update 실패 시 재시도 로직을 개발자가 직접 작성해야 함

## 네임드 LOCK

- 실무에선 Product Entity 대신 별도의 JDBC 등을 사용해야 함 (데이터 소스를 많이 차지하기 때문)

```yml
     hikari:
       maximum-pool-size: 40 # 옵션 추가
```

- JPA에서 네임드락 구현
  - "Propagation.REQUIRES_NEW" 트랜잭션 사용
    - 해당 메소드를 새로운 트랜잭션으로 시작하도록 지시
    - lock -> 비즈니스 로직 -> unlock -> 비즈니스 로직 commit으로 되는 것을 방지하고, lock -> REQUIRES_NEW (비즈니스 로직 & commit) -> unlock을 하기 위해
  - 같은 클래스 파일에선 트랜잭션이 독립적으로 실행되지 않으므로 facade에서 호출
- 네임드락은 주로 분산락을 구현할 때 많이 사용
- 장점
  - 비관적 락보다 타임아웃을 손쉽게 구현, 데이터 삽입 시 정합성을 맞춰야 할 때도 사용
- 단점
  - 트랜잭션 종료 시 락 해제, 세션관리 등이 복잡
