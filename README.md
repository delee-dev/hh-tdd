# 동시성 제어

## 스프링에서 동시성 문제
스프링 MVC는 기본적으로 서블릿 기반의 **멀티스레드 모델**을 따릅니다. 
서블릿 컨테이너는 내부적으로 **스레드 풀**을 사용하여 다중 요청을 동시에 처리합니다. 
각 요청은 독립된 스레드에서 실행되므로 여러 사용자의 요청이 병렬로 처리됩니다.

스프링의 빈은 기본적으로 **싱글톤 범위**로 관리됩니다. 
즉, 애플리케이션 컨텍스트에서 모든 요청이 동일한 빈 인스턴스를 공유합니다. 
따라서 싱글톤 빈이 상태를 가지는 필드를 포함하는 경우, 여러 스레드가 동시에 해당 필드에 접근하면 데이터 충돌이나 예기치 않은 결과가 발생할 수 있습니다.

이러한 문제를 방지하기 위해, **스레드 안정성**을 고려하여 빈을 설계해야 하며, 빈은 가능한 한 **무상태**로 설계하는 것이 권장됩니다. 
하지만 빈이 상태를 가지는 필드를 포함해야만 하는 경우에는 스레드 동기화를 통해 문제를 해결할 수 있습니다.

해당 프로젝트에서 데이터베이스로 사용되는 `UserPointTable`과 `PointHistoryTable`은 상태를 가지는 빈이고, 해당 클래스를 수정할 수 없다는 제약사항이 존재합니다. 
따라서, 스레드 동기화를 통해 동시성을 제어하는 방법을 선택했습니다.

## 동시성 제어

### 주어진 요구사항

동시에 여러 요청이 들어오더라도 순서대로 (혹은 한 번에 하나의 요청만) 처리되어야 한다.

### 요구사항 분석

#### 한 번에 하나의 요청을 처리한다 vs 순서대로 처리한다

한 번에 하나의 요청만을 처리하는 경우 특정 요청이 계속해서 처리되지 않는 상황이 발생할 수 있습니다. 
또한, 공정성 측면에서도 한 번에 하나의 요청을 처리하는 것 보다 순서대로 처리하는 것이 바람직하다고 생각했습니다.

#### 모든 요청을 순서대로 (혹은 한 번에 하나의 요청만) 처리해야 할까?

리팩토링을 하는 이유는 같은 데이터에 동시에 접근하는 경우, 데이터 정합성을 보장할 수 없기 때문입니다. 
만약 서로 다른 사용자가 시스템에 요청을 보낸다면 각자의 포인트에 대한 조회 및 수정이 발생합니다. 
이는 서로 다른 데이터에 대한 접근이라고 할 수 있습니다.
따라서, 사용자가 같은 요청 간에만 동시성 제어가 필요하다고 생각했습니다.

### 사용 기술

#### 스레드 동기화: `ReentrantLock`

스레드 동기화를 위해 `synchronized`, `ReentrantLock`, `StampedLock`, `Semaphore` 등 여러 방법을 사용할 수 있습니다.

| **특징** | **synchronized**                                              | **ReentrantLock**                                             | **StampedLock**                                              | **Semaphore**                                                |
|-----------------------|-------------------------------------------------------------|-------------------------------------------------------------|-------------------------------------------------------------|-------------------------------------------------------------|
| **재진입 가능 여부**     | 가능                                                        | 가능                                                        | 불가능                                                      | 불가능                                                      |
| **타임아웃 지원**       | 지원하지 않음                                                | 지원                                       | `tryOptimisticRead()`로 낙관적 읽기                         | 지원                                   |
| **공정성 보장**         | 지원하지 않음                                                | 공정 모드 설정 가능              | 지원하지 않음                                                | 공정 모드 설정 가능       |
| **락 획득/해제 관리**   | JVM이 자동 관리                                              | 명시적 관리                    | 명시적 관리                     | 명시적 관리                    |
| **낙관적 읽기 지원**    | 지원하지 않음                                                | 지원하지 않음                                                | `tryOptimisticRead()`로 읽기 지원                           | 지원하지 않음                                                |
| **사용 사례**           | 간단한 임계 구역 보호                                        | 복잡한 동기화 및 공정성/타임아웃 요구 상황                   | 읽기 작업이 많고 쓰기 작업이 적은 데이터 동기화             | 제한된 자원(예: 연결, 쓰레드 풀)의 동시 접근 관리            |
| **주요 단점**           | 고급 기능 부족                                               | 명시적 관리 필요로 인해 코드 복잡                            | 재진입 불가, 사용법이 복잡                                   | 허가 누락 또는 초과 관리 시 데드락 위험                     |
| **성능 최적화**         | 최신 JVM에서 최적화됨 (경량 락, 바이어스 락)                 | 경합이 많을 경우 `synchronized`보다 효율적                   | 읽기 작업이 많은 경우 최적화 가능                            | 허가 수 기반으로 효율적 관리                                 |

- `synchronized`: 간단한 동기화가 필요할 때 적합.
- `ReentrantLock`: 고급 기능(공정성, 타임아웃 등)이 필요하거나 복잡한 동기화 로직에 적합.
- `StampedLock`: 읽기 작업이 많은 경우 성능을 최적화할 때 유용.
- `Semaphore`: 제한된 자원을 여러 스레드가 동시에 사용해야 하는 경우 적합.

요구사항 분석에 따라, 요청의 순서대로 lock을 얻을 수 있도록 공정성을 보장하고, 한 스레드의 접근을 관리하는데 적합한 `ReentrantLock`을 선택했습니다.

#### 사용자별 lock 관리: `ConcurrentHashMap`

사용자 별로 lock을 관리 해야하므로 `Map` 타입을 선택했고, 멀티스레드에서 사용되므로 스레드 세이프한 `ConcurrentHashMap`을 선택했습니다.

### 구현

```java
@Component
public class LockManager {
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public ReentrantLock getLock(Long id) {
        return lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
    }
}
```

`LockManager`에 사용자별 lock을 관리할 수 있는 `ConcurrentHashMap`을 두고, `getLock()` 메서드를 통해 `ReentrantLock`을 얻을 수 있도록 구현했습니다.

요청 순서대로 lock을 획득할 수 있도록 하기 위해 `ReentrantLock`에서 `FairSync`를 사용하도록 파라미터로 `true` 값을 넘겼습니다.

```java
@Service
public class PointService {
    ...
    
    public UserPoint charge(long id, PointChargeRequest request) {
        ReentrantLock lock = lockManager.getLock(id);
        lock.lock();

        try {
            UserPoint existingUserPoint = userPointRepository.selectById(id);

            long beforePoint = existingUserPoint.point();
            long pointToCharge = request.amount();
            long afterPoint = beforePoint + pointToCharge;

            // 정책 검증
            pointValidator.validateForCharge(beforePoint, pointToCharge);
            // 포인트 충전
            UserPoint result = userPointRepository.insertOrUpdate(id, afterPoint);
            // 충전 내역 등록
            pointHistoryRepository.insert(id, pointToCharge, TransactionType.CHARGE, result.updateMillis());

            return result;
        } finally {
            lock.unlock();
        }
    }
    
    ...
}
```

`PointService` 내 모든 메서드에서 비즈니스 로직을 수행하기 전 lock을 얻고, 비즈니스 로직 수행 후 lock을 반환하도록 하여 같은 사용자의 경우에는 스레드를 동기 처리하도록 구현 했습니다.
