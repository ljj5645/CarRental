# CarRental - 자동차 렌탈 시스템

본 과제는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성하였습니다.  
이는 클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트들을 통과하기 위한 Project CarRental 수행 결과입니다.


# Table of contents

- [CarRental - 자동차 렌탈 시스템](#CarRental---자동차-렌탈-시스템)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#DDD-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일아웃 (HPA)](#오토스케일아웃_(HPA))
    - [ConfigMap](#ConfigMap)
    - [Zero-downtime deploy (Readiness Probe)](#Zero-downtime_deploy_(Readiness_Probe))
    - [Self-healing (Liveness Probe)](#Self-healing_(Liveness_Probe))
    

# 서비스 시나리오


기능적 요구사항
1. 자동차 관리자는 자동차를 등록한다.
2. 고객이 렌탈을 신청한다.
3. 고객이 렌탈 비용을 결제한다.
4. 신청이 되면 자동차가 렌탈되고 현황이 업데이트 된다. (RENTALED)
6. 고객이 신청을 취소할 수 있다.
7. 신청이 취소되면 렌탈이 취소되고 현황이 업데이트 된다. (AVAILABLE)
8. 자동차가 고장 났을 경우 수리를 신청한다.
9. 수리가 신청/완료될 경우 현황이 업데이트 된다. (REPAIR APPLY, REPAIR COMPLETE)
10. 접수된 수리 내역을 확인하고 수리를 진행한다. 
11. 렌탈 현황은 언제나 확인할 수 있다.

비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않은 신청건은 아예 거래가 성립되지 않아야 한다. `Sync 호출` 
1. 장애격리
    1. 자동차 관리 기능이 수행되지 않더라도 렌탈 신청은 365일 24시간 가능해야 한다. `Async (event-driven)`, `Eventual Consistency`
    1. 결제시스템이 과중되면 신청을 잠시동안 받지 않고 잠시후에 신청하도록 유도한다. `Circuit breaker`, `fallback`
1. 성능
    1. 고객은 렌탈 현황을 언제든지 확인할 수 있어야 한다. `CQRS`


# 체크포인트
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW
1. Saga
1. CQRS
1. Correlation
1. Req/Resp
1. Gateway
1. Deploy/ Pipeline
1. Circuit Breaker
1. Autoscale (HPA)
1. Zero-downtime deploy (Readiness Probe)
1. Config Map/ Persistence Volume
1. Polyglot
1. Self-healing (Liveness Probe)


# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)

## TO-BE 조직 (Vertically-Aligned)
  <img width="700" alt="TO-BE" src="https://user-images.githubusercontent.com/80210609/122098396-c0f3bc00-ce4b-11eb-8ba8-a53d96d74b2e.PNG">



## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://msaez.io/#/storming/nZJ2QhwVc4NlVJPbtTkZ8x9jclF2/every/a77281d704710b0c2e6a823b6e6d973a/-M5AV2z--su_i4BfQfeF


### 이벤트 도출
<img width="700" alt="이벤트 도출" src="https://user-images.githubusercontent.com/80210609/123116430-de440e00-d47b-11eb-9405-94758bf365ef.PNG">

### 부적격 이벤트 탈락
<img width="700" alt="이벤트 탈락" src="https://user-images.githubusercontent.com/80210609/123116526-f2880b00-d47b-11eb-83cd-46c84783fdd3.PNG">

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - 신청버튼 클릭, 결제버튼 클릭은 UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외

### 액터, 커맨드 부착하여 읽기 좋게
<img width="800" alt="액터, 커맨드" src="https://user-images.githubusercontent.com/80210609/123121380-151c2300-d480-11eb-9cb1-dc2134cc22bb.PNG">

### 어그리게잇으로 묶기
<img width="800" alt="어그리게잇" src="https://user-images.githubusercontent.com/80210609/123121308-07ff3400-d480-11eb-9ecb-f30c2815cd84.PNG">

    - 신청 관리의 렌트, 결제의 결제이력, 렌터카 관리는 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌

### 바운디드 컨텍스트로 묶기

<img width="800" alt="바운디드 컨텍스트" src="https://user-images.githubusercontent.com/80210609/123122009-9c699680-d480-11eb-9a9a-154bf0e98e9f.PNG">

    - 도메인 서열 분리 
        - Core Domain:  rent(front), management : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 rent 의 경우 1주일 1회 미만, management 의 경우 1개월 1회 미만
        - General Domain:   pay : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (핑크색으로 이후 전환할 예정)

### 폴리시 부착 (괄호는 수행주체)

<img width="850" alt="폴리시 부착" src="https://user-images.githubusercontent.com/80210609/123123640-f28b0980-d481-11eb-90cb-2d094aeea2de.PNG">

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

<img width="850" alt="폴리시 이동" src="https://user-images.githubusercontent.com/80210609/123125445-7b567500-d483-11eb-80bd-a60c18afe1e7.PNG">


### 완성된 1차 모형

<img width="900" alt="1차 모형" src="https://user-images.githubusercontent.com/80210609/123126630-7fcf5d80-d484-11eb-89e2-705fb2051a4b.PNG">


    - View Model 추가

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

<img width="800" alt="검증-1" src="https://user-images.githubusercontent.com/80210609/123129281-cc1b9d00-d486-11eb-94e3-f3b950874af5.PNG">)

    - 자동차 관리자는 자동차를 등록한다. (ok)
    - 고객이 렌탈을 신청한다. (ok)
    - 고객이 렌탈 비용을 결제한다. (ok)
    - 신청이 되면 자동차가 렌탈되고 현황이 업데이트 된다. (ok)

<img width="800" alt="검증-2" src="https://user-images.githubusercontent.com/80210609/123129846-37fe0580-d487-11eb-8a24-a7f74c6fccf7.PNG">
    
    - 고객이 신청을 취소할 수 있다. (ok)
    - 신청이 취소되면 렌탈이 취소되고 현황이 업데이트 된다. (ok)
    
<img width="800" alt="검증-3" src="https://user-images.githubusercontent.com/80210609/123130478-bfe40f80-d487-11eb-9839-5849585ab131.PNG">
    
    - 자동차가 고장 났을 경우 수리를 신청한다. (ok)
    - 수리가 신청/완료될 경우 현황이 업데이트 된다. (ok)
    - 접수된 수리 내역을 확인하고 수리를 진행한다. (ok) 


### 비기능 요구사항에 대한 검증

<img width="800" alt="비기능" src="https://user-images.githubusercontent.com/80210609/123131992-32a1ba80-d489-11eb-8d80-748a73737031.PNG">

        - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 고객 렌탈 신청시 결제처리:  결제가 완료되지 않은 신청은 절대 받지 않는다는 경영자의 오랜 신념(?) 에 따라, ACID 트랜잭션 적용. 신청 완료시 결제처리에 대해서는 Request-Response 방식 처리
        - 결제 완료시 렌터카 할당 처리:  rental(front) 에서 management 마이크로서비스로 신청건이 전달되는 과정에 있어서 management 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
        - 나머지 모든 inter-microservice 트랜잭션: 렌탈 신청 상태, 렌터카 상태 등 모든 이벤트에 대해 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.




## 헥사고날 아키텍처 다이어그램 도출
    
<img width="800" alt="헥사고날" src="https://user-images.githubusercontent.com/80210609/123132791-12bec680-d48a-11eb-9d42-50873d6fb997.PNG">


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트와 파이선으로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각 서비스의 포트넘버는 8081 ~ 8084이다)

```
cd rental
mvn spring-boot:run

cd pay
mvn spring-boot:run 

cd management
mvn spring-boot:run  

cd customerCenter
mvn spring-boot:run 
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다.

```
package carrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Rent_table")
public class Rent {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String status;
    private Long carId;

    @PostPersist
    public void onPostPersist(){
       
        carrental.external.Pay pay = new carrental.external.Pay();
     
        pay.setRentId(this.getId());
        pay.setStatus("PAID");
        pay.setCarId(this.getCarId());
        RentalApplication.applicationContext.getBean(carrental.external.PayService.class)
            .pay(pay);

        Rented rented = new Rented();
        BeanUtils.copyProperties(this, rented);
        rented.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        RepairApplied repairApplied = new RepairApplied();
        BeanUtils.copyProperties(this, repairApplied);
        repairApplied.publishAfterCommit();
    }

    @PreRemove
    public void onPreRemove(){
        RentCanceled rentCanceled = new RentCanceled();
        BeanUtils.copyProperties(this, rentCanceled);
        this.setStatus("RENTAL CANCELED");
        rentCanceled.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

}


```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package carrental;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="pays", path="pays")
public interface PayRepository extends PagingAndSortingRepository<Pay, Long>{

    Pay findByRentId(Long rentId);
}

```
- 적용 후 REST API 의 테스트
```
# rental 서비스의 렌탈 신청
http POST localhost:8082/rents carId=1234 status=RENT

# management 서비스의 렌터카 등록 
http POST localhost:8081/managements carId=1234 carName=car01 status=AVAILABLE

# 주문 상태 확인
http GET localhost:8081/managements
```
<img width="500" alt="restapi테스트" src="https://user-images.githubusercontent.com/80210609/123187091-6fdf6a00-d4d4-11eb-9f02-98cb2de86073.PNG">



## 폴리글랏 퍼시스턴스

rental는  데이터베이스는 HSQL로 구현하여 MSA의 서비스간 서로 다른 종류의 DB에도 문제없이 동작하여 다형성을 만족하는지 확인하였다.

> rental의 application.yml

```xml
  <dependency>
        <groupId>org.hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
        <scope>runtime</scope>
    </dependency>
```


## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 렌탈 신청 관리(rental)->결제(pay) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (rental) PayService.java

package carrental.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="pay", url="http://localhost:8083")
public interface PayService {

    @RequestMapping(method= RequestMethod.POST, path="/pays")
    public void pay(@RequestBody Pay pay);

}
```

- 렌탈 신청을 받은 직후(@PostPersist) 결제를 요청하도록 처리
```
# Rent.java (Entity)

   @PostPersist
    public void onPostPersist(){
       
        carrental.external.Pay pay = new carrental.external.Pay();
     
        pay.setRentId(this.getId());
        pay.setStatus("PAID");
        pay.setCarId(this.getCarId());
        RentalApplication.applicationContext.getBean(carrental.external.PayService.class)
            .pay(pay);

        Rented rented = new Rented();
        BeanUtils.copyProperties(this, rented);
        rented.publishAfterCommit();
    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


```
# 결제 (pay) 서비스를 잠시 내려놓음 (ctrl+c)

#렌탈 신청 처리
http POST localhost:8082/rents carId=1234 status=RENT   #Fail
http POST localhost:8082/rents carId=2345 status=RENT   #Fail

#결제서비스 재기동
cd 결제
mvn spring-boot:run

#렌탈 신청 처리
http POST localhost:8082/rents carId=1234 status=RENT   #Success
http POST localhost:8082/rents carId=2345 status=RENT   #Success
```
<img width="500" alt="동기식에러확인" src="https://user-images.githubusercontent.com/80210609/123187415-175c9c80-d4d5-11eb-82ff-d98a0adbe4a5.PNG">

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


결제가 이루어진 후에 렌터카 관리 시스템으로 이를 알려주는 행위는 동기식이 아니라 비동기식으로 처리하여 렌터카 관리 시스템의 처리를 위하여 결제주문이 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 결제이력에 기록을 남긴 후에 곧바로 결제승인이 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package carrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Pay_table")
public class Pay {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long rentId;
    private Long carId;
    private String status;

    @PostPersist
    public void onPostPersist(){
        Paid paid = new Paid();
        BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();

    }
}
```
- 렌터카 관리 서비스에서는 결제 승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package carrental;

...

@Service
public class PolicyHandler{
    @Autowired ManagementRepository managementRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_UpdateStatus(@Payload Paid paid){

        if(!paid.validate()) return;

        Management management = managementRepository.findByCarId(paid.getCarId());
        management.setStatus("RENTED");
        management.setRentId(paid.getRentId()); 

        managementRepository.save(management);  
    }

}

```
렌터카 관리 서비스는 렌탈 신청 관리/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 렌터카 관리 서비스이 유지보수로 인해 잠시 내려간 상태라도 신청을 받는데 문제가 없다. 

```
# 렌터카 관리 서비스 (management) 를 잠시 내려놓음 (ctrl+c)

#렌탈 신청 처리
http POST localhost:8082/rents carId=1234 status=RENT   #Success
http POST localhost:8082/rents carId=2345 status=RENT   #Success

#렌터카 관리 서비스 기동
cd 상점
mvn spring-boot:run

#렌터카 할당 상태 확인
http localhost:8081/managements      # 렌터카의 상태가 "RENTED"으로 확인
```
<img width="500" alt="restapi테스트" src="https://user-images.githubusercontent.com/80210609/123187091-6fdf6a00-d4d4-11eb-9f02-98cb2de86073.PNG">


## CQRS
Materialized View 구현을 통해 다른 마이크로서비스의 데이터 원본에 접근없이 내 서비스의 화면 구성과 잦은 조회가 가능하게 하였습니다. 본 과제에서 View 서비스는 CustomerCenter 서비스가 수행하며 렌탈 신청 상태를 보여준다.

> 신청 완료 후 customercenter 결과
<img width="500" alt="CQRS" src="https://user-images.githubusercontent.com/80210609/123208570-5224fb80-d4fa-11eb-9b01-4c7e2b963e49.PNG">


## Gateway 적용
API Gateway를 통하여 마이크로서비스들의 진입점을 단일화하였습니다.

> gateway > application.xml 설정
```
spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: management
          uri: http://management:8080
          predicates:
            - Path=/managements/** 
        - id: rental
          uri: http://rental:8080
          predicates:
            - Path=/rents/** 
        - id: pay
          uri: http://pay:8080
          predicates:
            - Path=/pays/** 
        - id: customerCenter
          uri: http://customerCenter:8080
          predicates:
            - Path= /customerCenters/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```


# 운영

## CI/CD 설정


각 구현체들은 각자의 source repository 에 구성되었고, 도커라이징, deploy 및 서비스 생성을 진행하였다.

- git에서 소스 가져오기
```
git clone https://github.com/ljj5645/CarRental.git
```
- Build 하기
```
cd hifive
cd management
mvn package
```
- 도커라이징 : Azure 레지스트리에 도커 이미지 푸시하기
```
az acr build --registry skccuser19 --image skccuser19.azurecr.io/management:latest .
```
- 컨테이너라이징 : 디플로이 생성 확인
```
kubectl create deploy management --image=skccuser19.azurecr.io/management:latest
```
- 컨테이너라이징 : 서비스 생성
```
kubectl expose deploy management --port=8080
```
> rental, pay, customercenter, gateway 서비스도 동일한 배포 작업 반복


## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 렌탈 신청 관리(rental)--> 결제(pay) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml

feign:
  hystrix:
    enabled: truekubectl run siege --image=apexacme/siege-nginx
hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
$ siege -c100 -t60S -r10 -v --content-type "application/json" 'http://gateway:8080/rents POST {"carId":1234, "status":"RENT"}'
```

- 부하가 발생하고 서킷브레이커가 발동하여 요청 실패하였고, 밀린 부하가 다시 처리되면서 렌탈 신청을 받기 시작

<img width="500" alt="서킷브레이커" src="https://user-images.githubusercontent.com/80210609/123289912-d05dbe00-d54b-11eb-9fa2-8ae286aca3ec.PNG">

- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여준다. 하지만, 7.30% 가 성공하였고, 92.70%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요하다.

<img width="500" alt="서킷" src="https://user-images.githubusercontent.com/80210609/123289017-08b0cc80-d54b-11eb-84b9-e490952df896.PNG">


### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

- conference의 deployment.yaml 파일 설정
```
resources:
  limits:
    cpu: 500m
  requests:
    cpu: 200m 
```

- 결제 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:

```
kubectl autoscale deploy pay --min=1 --max=10 --cpu-percent=15
```
<img width="600" alt="hpa4" src="https://user-images.githubusercontent.com/80210609/123291080-bffa1300-d54c-11eb-959f-55ea62e0f653.PNG">

- CB 에서 했던 방식대로 워크로드를 60초 동안 걸어준다.
```
$ siege -c100 -t60S -r10 -v --content-type "application/json" 'http://gateway:8080/rents POST {"carId":1234, "status":"RENT"}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy rental -w
```
- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:
<img width="500" alt="hpa3" src="https://user-images.githubusercontent.com/80210609/123290497-4d893300-d54c-11eb-8ee5-870183da6841.PNG">
<img width="600" alt="hpa3" src="https://user-images.githubusercontent.com/80210609/123290623-642f8a00-d54c-11eb-9451-31a63908a5f6.PNG">

- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다. 
<img width="400" alt="hpa2" src="https://user-images.githubusercontent.com/80210609/123290546-57129b00-d54c-11eb-8adb-75317f601b11.PNG">


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
siege -c100 -t120S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'

** SIEGE 4.0.5
** Preparing 100 concurrent users for battle.
The server is now under siege...

HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
:

```

- 새버전으로의 배포 시작
```
kubectl set image ...
```

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인
```
Transactions:		        3078 hits
Availability:		       70.45 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```
배포기간중 Availability 가 평소 100%에서 70% 대로 떨어지는 것을 확인. 원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

```
# deployment.yaml 의 readiness probe 의 설정:


kubectl apply -f kubernetes/deployment.yaml
```

- 동일한 시나리오로 재배포 한 후 Availability 확인:
```
Transactions:		        3078 hits
Availability:		       100 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```
<img width="500" alt="CM2" src="https://user-images.githubusercontent.com/80210609/123247441-de96e480-d521-11eb-9222-9462d4072c36.PNG">

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.

