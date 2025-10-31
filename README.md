# PayCore Architecture

> 프로젝트 설계 과정에서의 다이어그램 진화 기록입니다.  
> (Sequence Diagram, Class Diagram, Module Architecture)

---

## Version History

| Version | Date | Description |
|----------|------|--------------|
| v1.0 | 2025-10-31 | 초기 Sequence Diagram 및 Class Diagram 설계 |

---

## Diagrams by Version

<details>
<summary> <b>v1.0 — 초기 설계 (2025-10-25)</b></summary>

####  [Class Diagram - PaymentService]
<img width="700" height="300" alt="PaymentService Class Diagram" src="https://github.com/user-attachments/assets/217b619b-399a-4cc5-a5de-7d43a5a3a736" />

####  [Class Diagram - OrderService]
<img width="700" height="300" alt="OrderService Class Diagram" src="https://github.com/user-attachments/assets/c2d06778-be90-4296-bc73-b2a37e5cf847" />

---

####  [Sequence Diagram - PaymentService]
<img width="700" height="400" alt="PaymentService Sequence Diagram" src="https://github.com/user-attachments/assets/991cb7c7-5a17-460e-97e2-fb0d0bd3d1ef" />

####  [Sequence Diagram - OrderService]
<img width="700" height="400" alt="OrderService Sequence Diagram" src="https://github.com/user-attachments/assets/e77f6bc4-9fec-45f1-b91b-f0449f875231" />

---

###  Architecture Note
> **멀티 모듈(Multi-Module) 기반으로 구성한 이유**
> - 도메인별 책임 분리를 통해 코드의 응집도를 높이고 결합도를 낮추기 위함  
> - Order, Payment, Settlement, Notification 등 각 기능을 독립적으로 개발/테스트 가능  
> - 공통 모듈(`common`)에 예외 처리, 공용 DTO, 유틸 클래스를 모아 중복을 최소화  

> **Outbox 테이블 구조**
> - Outbox DB에 `sagaId`와 엔티티의 **PK**를 함께 저장  
> - 이는 보상 트랜잭션(Saga Compensation) 수행 시 어떤 로직을 롤백해야 하는지를 추적하기 위함  
> - 또한 **Transaction Outbox Pattern**을 적용해 DB 트랜잭션과 이벤트 발행의 **원자성(Atomicity)** 을 보장

> **RabbitMQ를 선택한 이유 (Kafka 대신)**
> - 프로젝트의 이벤트 규모가 상대적으로 작고, **실시간 트랜잭션 처리 중심 구조**이기 때문  
> - RabbitMQ는 **Low Latency / High Throughput** 환경에서 메시지 순서 보장과 즉각적 ACK 관리가 용이  
> - 향후 대규모 트래픽(대용량 로그, 이벤트 스트리밍)으로 확장 시 Kafka로 전환 예정  
---

</details>
