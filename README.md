# 동시성 제어 방식에 대한 분석 및 보고서

---

사용자가 포인트 충전 및 사용시 동시성 문제가 발생할 수 있기 때문에 
여러 쓰레드가 동시에 포인트를 충전하거나 사용할 때 동작에 대해 작업하였습니다. 

## 동시성 제어 
- 사용자가 포인트를 충전하고 사용할 경우, 여러 쓰레드가 동시에 접근하여 공유자원을 수정하는 상황이 발생할 수 있습니다. 
- 동시에 접근 후 수정하게 되면 데이터 무결성을 위반할 수 있습니다.
- 이를 해결하기 위해 쓰는 방법은 Lock입니다. Lock이란 공유 자원에 대한 동시접근 제한 기술입니다.

### 동시성 제어 종류 
- Java 에는 `synchronized`, `ConcurrentHashMap`, `ReentrantLock` 가 동시성을 관리하며 각각의 목적과 특징에 따라 장단점이 있습니다. 

1. `synchronized`
- 특징 
  - Java의 키워드로 메서드나 블록을 동기화하여 여러 쓰레드가 동시에 해당 코드를 실행하지 못하도록 제한합니다. 
  - JVM 수준에서 관리되면서 사용이 비교적 쉽습니다. 

- 장점
  - 간단하고 코드 작성이 쉽습니다. 
  - JVM에서 직접 관리하므로 안정성이 높습니다.
  - 코드가 직관적이기 때문에 유지보수가 용이합니다. 
- 단점
  - 여러 쓰레드가 대기상태일 경우 JVM이 락 관리와 상태 전환을 처리하는 방식 때문입니다.
  - 여러 쓰레드가 하나의 `synchronized` 블록이나 메소드에 접근하려고 하면 Lock 을 얻기 위해 경쟁합니다. 
    경쟁이 심화될수록 쓰레드가 대키 큐에 줄을 서고, 대기시간이 길어집니다. 많은 쓰레드가 대기하면 스케줄링 오버헤드가 증가하면서 성능 저하를 일으킵니다. 

2. `ConcurrentHashMap`
- 특징 
  - 동시성을 고려하여 설계된 `HashMap` 으로 내부적으로 세분화된 Lock 을 사용하여 성능을 높입니다. 
  - 특정 구역에만 Lock을 걸기 때문에 다중 쓰레드 환경에서 읽기/쓰기 작업을 효율적으로 수행할 수 있습니다.
- 장점 
  - 읽기 작업이 많은 경우 성능이 뛰어납니다. 
  - 동시성 문제가 발생하지 않도록 설계되어 있기 때문에 추가적인 동기화 코드가 필요하지 않습니다. 
  - 일부 메서드(get, containsKey, size 등)은 내부적으로 Lock을 사용하지 않으며 Lock 없이 안전하게 동작하도록 설계되어 있습니다. 
- 단점 
  - 복잡한 연산에서는 동기화가 필요한 경우가 있어 완벽한 동기화를 보장하지 않습니다. 
  - 설계가 복잡하여 잘못된 사용을 할 경우 동시성 문제가 생길 수 있습니다. 
3. `ReentrantLock`
- 동일한 쓰레드가 여러 번 연속해서 lock 을 획득할 수 있는 기능을 제공합니다. (재진입 가능)
- lock을 획득하는 순서를 제어할 수 있습니다. (공정설 설정 가능)
- 장점 
  - Lock 에 대해 명시적으로 제어할 수 있으며, 필요할 때 Lock 을 해제할 수 있습니다.
  - 조건 변수(Condition)를 사용하여 더 복잡한 동기화 작업이 가능합니다. 
- 단점 
  - try-finally 블록을 사용하여 락 해제를 반드시 명시해야 합니다.
  - 과도한 Lock 사용 시 데드락(Deadlock) 위험이 존재합니다.

### `ReentrantLock` 을 선택한 이유
- `lock()`, `unlock()` 으로 시작과 끝을 명시적으로 제어할 수 있습니다. 포인트 충전 및 사용이 완료된 후 lock 을 해제하여 동시성 문제를 해결할 수 있습니다.
- `synchronized` 와 다르게 `ReentrantLock`는 공정성을 지원합니다. 공정성이란 모든 쓰레드가 작업을 수행할 기회를 공평하게 갖는 것을 의미합니다. 
  `fair` 옵션을 여러 쓰레드가 동시에 작업을 처리할 때 오래 기다린 작업부터 접근권한을 주는 방식으로 진행합니다.

### 구현 방안 
- 사용자에 대한 포인트 충전 및 포인트 사용 메소드는 `ReentrantLock` 을 사용하여 동시성을 제어하도록 구현하였습니다. 
- 동시성 제어를 통해 여러 쓰레드가 동시에 포인트를 수정하는 것을 방지하도록 합니다. 

### 개선 방안
- 공통 로직은 추가적인 리팩토링을 통해 별도로 분리할 예정이며, validation 또한 domain 으로 분리할 예정입니다. 

### 정리 
공유 자원에 동시에 접근하게 되면 예상치 못한 값으로 업데이트 되거나 중복 처리가 이루어질 수 있습니다.
다수의 쓰레드가 동일한 데이터나 공유자원에 접근할 때, 무결성과 일관성을 보장하기 위해 동시성 제어가 필요하다는 것을 학습하였습니다.
업무에서 사용하지 않아 생소하였던 동시성 제어를 학습함으로써 예외적인 문제 발생에 대응할 수 있는 테스트가 필요하다는 것을 깨달았고,
상황에 따라 적합한 동시성 제어 방식을 선택하여야 한다는 것을 배울 수 있었습니다. 