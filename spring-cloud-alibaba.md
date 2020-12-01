# Spring Cloud Alibaba

## 0. 演进背景

**两三年前，因为阿里开源的dubbo曾经不怎么维护**，然后加上spring cloud完善的技术栈冲击进来，所以大部分中小型公司都开始拥抱spring cloud，dubbo的使用比例下降很多，所以最近两三年，国内微服务这块，其实大公司是以纯自研/dubbo+自研为主的，中小公司是以全面拥抱spring cloud netflix技术栈为主的

 

**但是最近一年多，情况产生了变化，因为阿里的dubbo重启活跃维护，同时阿里把自己的微服务技术栈融入进了spring cloud体系和标准**，形成了一个spring cloud alibaba微服务技术组件，也就是以nacos、dubbo、seata、sentinal、rocketmq等技术为核心的一套技术体系

| 组件           | Cloud Alibaba | Cloud Netflix |
| -------------- | ------------- | ------------- |
| 注册中心       | nacos         | eureka        |
| RPC框架        | dubbo         | feign+ribbon  |
| 分布式事务     | seata         | N/A           |
| 限流/熔断/降级 | sentinel      | hystrix       |
| API网关        | N/A           | zuul          |

**spring cloud netflix微服务技术组件，开始更新的非常不活跃**，netflix公司公开宣布他之前开源的一些微服务组件未来就不会怎么更新了，这就导致spring cloud netflix微服务技术组件的未来有点不太光明 

**spring cloud alibaba微服务技术组件，活跃的更新，社区也重启**，做的很好，宣讲，演讲，采访，开始比较活跃起来。所以最近一年其实很多公司也开始尝试用spring cloud alibaba的技术组件，再加上一些其他的开源组件，同时其他的开源组件里，其实国内前天互联网公司也开源了不少优秀的项目，**比如说携程开源的apollo（spring cloud config），大众点评开源的CAT（zipkin、slueth）**，加上其他国外的优秀开源项目，比如Prometheus，ELK，Spring Cloud Gateway，等等，可以组成一套全新的以国内开源技术为核心的微服务体系中心公司开始进行分化，有部分公司还是spring cloud netflix为主的一套技术栈，**有少部分公司开始尝试推行spring cloud alibaba技术栈+国内开源的组件（apollo、CAT）+ Prometheus + ELK + Spring Cloud Gateway（Nginx+lua、Kong、Zuul、API网关自研）**

 个人倾向于以及比较认可的，是这套技术体系，也认为会是未来国内的主流，因为netflix很多组件维护的都不够活跃了，所以衰退是必然的，加上国内的开源项目，都是中文文档，中文社区，交流也方便，也很活跃，所以我们的课程主要是以这套国内为主的微服务技术体系为主的，也是面向未来的一套技术体系



## 1. Nacos

- spring cloud netflix in maintenance mode
- integration with alibaba cloud
### Config
- import spring-cloud-starter-alibaba-nacos-discovery library
- declare nacos server in application.xml
- **namespace 用于不同环境的隔离**
```groovy
dependencies {
    implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery'
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```
```properties
spring.cloud.nacos.discovery.server-addr=192.168.1.102:8848,192.168.1.102:8849,192.168.1.102:8850
spring.cloud.nacos.discovery.namespace=dev
spring.cloud.inetutils.preferred-networks=192
```
```shell
sh startup.sh -m standalone
```

### 服务发现中心比较

![](image/spring-ali-01.png)

#### Zookeeper

leader+follower, leader写，同步到follower，follower可以读，保证顺序一致性，就是基本尽量保证到数据一致的，主动推送，典型的CP，leader崩溃的时候，为了保证数据一致性，尽量不要读到不一致的数据，此时要重新选举leader以及做数据同步，此时集群会短暂的不可用，

#### Eureka

peer-to-peer，AP, 大家都能写也都能读，每个节点都要同步给其他节点，但是是异步复制的，所以随时读任何一个节点，可能读到的数据都不一样，任何一个节点宕机，其他节点正常工作，可用性超高，但是数据一致性不行

#### Consul

也是基于raft算法的CP模型

#### Nacos

也是基于raft算法的CP模型，同时也支持配置成类似

![](image/spring-nacos.png)
- CP: support persistent instance
- AP: week consistent

zk作为注册中心是早期dubbo时代的标配；后续spring cloud进入国内市场，大家就都用eureka了，但是spring cloud也推荐了consul，所以consul也有不少人在用。zk，eureka， consul其实都有人用。但是未来还是建议大家用nacos，因为nacos的功能最为完善，包括了雪崩保护，自动注销实例，监听支持，多数据中心，跨注册中心同步，spring cloud集成，dubbo集成，k8s集成，这些都支持，其他的几个技术基本都支持部分罢了



### 架构原理

![](image/spring-ali-02.png)

服务通过nacos server内部的**open api进行服务注册**，nacos server内部有一个sevice服务的概念，里面有多个instance实例的概念，同时对**不同的service服务可以划归到不同的namespace命名空间下去**

namespace可以是一个技术团队，比如说一个技术团队，业务A的技术团队所有的服务都放在一个namespace命名空间下面，业务B的技术团队所有的服务都放在另外一个namespace命名空间。注册的时候就是在注册表里维护好每个服务的每个实例的服务器地址，包括ip地址和端口号。

注册成功之后，**服务就会跟nacos server进行定时的心**跳，保持心跳是很关键的，nacos server会定时检查服务各个实例的心跳，如果一定时间没心跳，就认为这个服务实例宕机了，就从注册表里摘除了

其他服务会从nacos server通过open api查询要调用的服务实例列表，而**且nacos客户端会启动一个定时任务，每隔10s就重新拉取一次服务实例列表，**这样如果调用的服务有上线或者下线，就能很快感知到了。此外还可以对要调用的服务进行监听，如果有异常变动会由nacos server反向通知他



#### 服务注册

nacos本身的话，其实是完全可以脱离spring cloud自己独立运作的，但是他目前是集成到spring cloud alibaba里去的，也就是在spring cloud的标准之下实现了一些东西，s**pring cloud自己是有一个接口，叫做ServiceRegistry**，也就是服务注册中心的概念，**nacos是实现了一个实现类的，也就是NacosServiceRegistry，实现了register、deregister、close、setStatus、getStatus之**类的方法。利用spring boot自动装配去调用NacosServiceRegistry的register方法去进行服务注册。

而且除了注册之外，还会通过schedule线程池去提交一个定时调度任务, 定时发送心跳给nacos server

```JAVA
this.exeutorService.schedule(new BeatReactor.BeatTask(beatInfo), beatInfo.getPeriod(), TimeUnit.MILLISECONDS)
```

接着会进行注册，注册的话是访问nacos server的open api，其实就是http接口

```SHELL
http://31.208.59.24:8848/nacos/v1/ns/instance?serviceName=xx&ip=xx&port=xx
```



#### 服务同步 

nacos server那里是基于一个ConcurrentHashMap作为注册表来放服务信息的，直接会构造一个Service放到map里，然后对Service去addInstance添加一个实例，本质里面就是在维护信息，同时还会建立定时检查实例心跳的机制。最后还会基于一致性协议，比如说raft协议，去把注册同步给其他节点

 

#### 服务发现

服务发现的本质其实也是一个http接口

```
http://31.208.59.24:8848/nacos/v1/ns/instance/list?serviceName=xx
```

**会启动定时任务，每隔10s拉取一次最新的实例列表**，然后服务端还会监听他监听服务的状态，**有异常就会基于UDP协议反向通知客户端这次服务异常变动**



### 配置
- namespace + group + id
   + namespace: env
   + group: logic group different service instance
   + service: service cluster
   + instance: instance
- Data Id
    - env control the config
    - name: ${prefix}-${spring.profile.active}.${file-extension}
    - prefix: default is spring.application.name or customized by spring.cloud.nacos.config.prefix
    - file-extension: spring.cloud.nacos.config.file-extension
- namespace contains group, group contains env
#### Server - HA

cluster.conf

```properties
#集群的ip地址
192.168.66.20:8847
192.168.66.50:8848
192.168.66.51:8848
```

application.properties

```properties
# 开启mysql的持久化
### If use MySQL as datasource:
spring.datasource.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://192.168.66.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user=root
db.password=root
```



#### Client

bootstrap.yml

```yaml
spring:
  application:
    name: nacos-config-client
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848 #Nacos naming service
      config:
        server-addr: localhost:8848 #Nacos config service
        file-extension: yaml
        group: DEV_GROUP
        namespace: 5d49b6d6-2d35-41fc-a075-95240c222d51 # namespace id
```
application.yml

```yaml
spring:
  profiles:
    active: dev
```

***

## 2. Dubbo 整合 Nacos

### API接口定义

定义API接口

```JAVA
public interface ServiceA {
    String greet(String name);
}
```

### 服务端

导入组件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">


    <artifactId>demo-dubbo-nacos-ServiceA</artifactId>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <spring-cloud.version>Greenwich.SR2</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- 导入spring cloud dubbo组件 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-dubbo</artifactId>
            <version>2.1.2.RELEASE</version>
        </dependency>
       <!-- 导入自己定义的API -->
        <dependency>
            <groupId>com.zhss.demo</groupId>
            <artifactId>demo-dubbo-nacos-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
       <!-- 导入nacos的服务注册和发现 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-nacos-discovery</artifactId>
            <version>2.1.1.RELEASE</version>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-context</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-context</artifactId>
            <version>2.1.1.RELEASE</version>
        </dependency>
    </dependencies>
</project>
```

实现服务接口

```java
@Service(
        version = "1.0.0",
        interfaceClass = ServiceA.class,
        cluster = "failfast",
        loadbalance = "roundrobin"
)
public class ServiceAImpl implements ServiceA {

    @Override
    public String greet(String name) {
        return "hello, " + name;
    }
}
```

配置服务地址和nacos的注册信息

```properties
spring.application.name=demo-dubbo-nacos-ServiceA
dubbo.scan.base-packages=com.zhss.demo.dubbo.nacos
dubbo.protocol.name=dubbo
dubbo.protocol.port=20880
# 本地服务地址
dubbo.registry.address=spring-cloud://192.168.1.102
# nacos注册集群
spring.cloud.nacos.discovery.server-addr=192.168.1.102:8848,192.168.1.102:8849,192.168.1.102:8850
# 多个地址的前缀
spring.cloud.inetutils.preferred-networks=192
```

### 客户端

直接调用服务A

```JAVA
@RestController
public class TestController {

    @Reference(version = "1.0.0",
            interfaceClass = ServiceA.class,
            cluster = "failfast")
    private ServiceA serviceA;

    @GetMapping("/greet")
    public String greet(String name) {
        return serviceA.greet(name);
    }

}
```

```properties
spring.application.name=demo-dubbo-nacos-ServiceB
dubbo.cloud.subscribed-services=demo-dubbo-nacos-ServiceA
dubbo.scan.base-packages=com.zhss.demo.dubbo.nacos
spring.cloud.nacos.discovery.server-addr=192.168.1.102:8848,192.168.1.102:8849,192.168.1.102:8850
spring.cloud.inetutils.preferred-networks=192
```



## 3. Sentinel

going to replace hystrix for flow limit, circuit break and service fallback

hystrix missing: console, limit request
![](image/sentinel-features-overview-en.png)

init: java -jar sentinel-dashboard-1.7.2.jar

Rate Limit
+ Resource: URI
+ Type: 
    + QPS: number of request per second
    + Thread: number of thread handle, if thread is full, directly fail
+ Model
    + fast fail
    + related: e.g. related B,  if resource B reach limit, control limit for resource A
    + chain
+ Effect
    + direct fail
    + warm up: code factor is 3, start flow limit is (flow limit / code factor), eventually reach flow limit
    + queue: request flow in certain speed and set the timeout
+ Configuration
![](image/sentinel-fail-fast.png)
![](image/sentinel-warmup.png)
![](image/sentinel-queue.png)

### Circuit break
no half open status, all time windows is second

RT
+ response time, if continue 5 request in 1s and over response time, will break circuit
+ after circuit break windows, open circuit

Exception Ratio
+ QPS is greater than 5 and exception ratio is over threshold, will break circuit
+ after circuit break windows, open circuit

Exception Number
+ number of exception exceed threshold
![](image/senti-rt.png)

- Hot Key
    + set threshold for specific resource and parameter
    + @SentinelResource to set the fallback method and parameter
    + resource name in sentinel is @SentinelResource(value)
    + if blockHandler is not set, redirect to /error
    + advance optional able to set different threshold for specific value of parameter
```java
@RestController
@Slf4j
public class FlowLimitController  {
    @GetMapping("/testHotKey")
    @SentinelResource(value = "testHotKey",blockHandler = "deal_testHotKey")
    public String testHotKey(@RequestParam(value = "p1",required = false) String p1,
                             @RequestParam(value = "p2",required = false) String p2) {
        //int age = 10/0;
        return "------testHotKey";
    }

    public String deal_testHotKey (String p1, String p2, BlockException exception) {
        return "fail to testHotKey";
    }
}
```
![](image/sentinel-hotkey.png)
![](image/sentinel-hotkey-exception.png)
- System Load auto adjustment for global settings, all the service
    + Load: maxQps * minRT
    + CPU usage
    + Average RT
    + Entry QPS
- SentinelResource
    + Global Exception Handler
        + @SentinelResource.blockHandlerClass is exception handler class
        + @SentinelResource.blockHandler is exception handler class's method
```java
@RestController
public class RateLimitController {
    @GetMapping("/rateLimit/customerBlockHandler")
    @SentinelResource(value = "customerBlockHandler",
            blockHandlerClass = CustomerBlockHandler.class,
            blockHandler = "handlerException2")
    public CommonResult customerBlockHandler() {
        return new CommonResult(200,"customized",new Payment(2020L,"serial003"));
    }
}

public class CustomerBlockHandler {

    public static CommonResult handlerException(BlockException exception) {
        return new CommonResult(445,"customized global handlerException----1");
    }
}
```
### Ribbon: Fallback & BlockHandler
    + fallback: business exception
    + blockHandler: sentinel flow control
    + if configure both, flow reach threshold will handle by blockHandler
    + exceptionsToIgnore will ignore the exception and will not go to fallback metho
```java
public class CircleBreakerController {
    public static final String SERVICE_URL = "http://nacos-payment-provider";

    @Resource
    private RestTemplate restTemplate;

    @RequestMapping("/consumer/fallback/{id}")
    @SentinelResource(value = "fallback",fallback = "handlerFallback",blockHandler = "blockHandler",
            exceptionsToIgnore = {IllegalArgumentException.class})
    public CommonResult<Payment> fallback(@PathVariable Long id) {
        CommonResult<Payment> result = restTemplate.getForObject(SERVICE_URL + "/paymentSQL/"+id,CommonResult.class,id);

        if (id == 4) {
            throw new IllegalArgumentException ("IllegalArgumentException");
        }else if (result.getData() == null) {
            throw new NullPointerException ("NullPointerException, no records");
        }

        return result;
    }
}
```
### OpenFeign
```groovy
feign:
  sentinel:
    enabled: true
```
### Persistence
- save rule into nacos in json format
- dataId in nacos is spring.cloud.sentinel.datasource.ds1.nacos.dataId
```groovy
spring:
  application:
    name: cloudalibaba-sentinel-service
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
    sentinel:
      transport:
        dashboard: localhost:8080
        port: 8719
      datasource:
        ds1:
          nacos:
            server-addr: localhost:8848
            dataId: cloudalibaba-sentinel-service
            groupId: DEFAULT_GROUP
            data-type: json
            rule-type: flow
```
```json
[
      {
        "resource": "/rateLimit/byUrl",
        "limitApp": "default",
        "grade": 1,
        "count": 1.0,
        "strategy": 0,
        "refResource": null,
        "controlBehavior": 0,
        "warmUpPeriodSec": 10,
        "maxQueueingTimeMs": 500,
        "clusterMode": false,
        "clusterConfig": {
          "flowId": null,
          "thresholdType": 0,
          "fallbackToLocalWhenFail": true,
          "strategy": 0,
          "sampleCount": 10,
          "windowIntervalMs": 1000
        }
      }
    ]
```
***

## 4. Seata
Distributed transaction: one Id + three components

**Components**

+ Transaction Coordinator: seata server, maintain transaction status, coordinate commit and rollback
+ Transaction Manager: transaction initializer with @GlobalTransaction
+ Resource Manager: transaction participant, register transcation status and trigger commit and rollback

![](image/spring-seata.png)

- step
    + TM ask TC open distributed transaction and assign unique XID, XID is same in different service
    + RM register distributed transaction in TC
    + TM close transaction for pharse I
    + TC decides to commit or rollback
    + TC info RM to commit or rollback to complete pharse II
- pharse I - loading
    + seata intercept SQL, parse SQL to create "before image"
    + execute SQL
    + create "after image", create row lock
- pharse II - commit
    + delete "before image"
    + delete "after image"
    + delete row lock
- pharse II - rollback
    + validate dirty write: compare "after image" and database current data
    + if no dirty write, generate revert sql from "after image", otherwise need manually intervention
    + delete "before/after image", row lock
- config file.conf and registry.conf

```shell
sh seata-server.sh -p 8091 -h 127.0.0.1 -m file
```

**File.conf**

```json
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  #thread factory for netty
  threadFactory {
    bossThreadPrefix = "NettyBoss"
    workerThreadPrefix = "NettyServerNIOWorker"
    serverExecutorThreadPrefix = "NettyServerBizHandler"
    shareBossWorker = false
    clientSelectorThreadPrefix = "NettyClientSelector"
    clientSelectorThreadSize = 1
    clientWorkerThreadPrefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    bossThreadSize = 1
    #auto default pin or 8
    workerThreadSize = "default"
  }
  shutdown {
    # when destroy server, wait seconds
    wait = 3
  }
  serialization = "seata"
  compressor = "none"
}

service {

  vgroup_mapping.fsp_tx_group = "default" #修改自定义事务组名称

  default.grouplist = "127.0.0.1:8091"
  #degrade, current not support
  enableDegrade = false
  #disable seata
  disableGlobalTransaction = false
}
#client transaction configuration, only used in client side
client {
  rm {
    asyncCommitBufferLimit = 10000
    lock {
      retryInterval = 10
      retryTimes = 30
      retryPolicyBranchRollbackOnConflict = true
    }
    reportRetryCount = 5
    tableMetaCheckEnable = false
    reportSuccessEnable = false
    sqlParserType = druid
  }
  tm {
    commitRetryCount = 5
    rollbackRetryCount = 5
  }
  undo {
    dataValidation = true
    logSerialization = "jackson"
    logTable = "undo_log"
  }
  log {
    exceptionRate = 100
  }
}

## transaction log store, only used in server side
store {
  ## store mode: file、db
  mode = "db"
  ## file store property
  file {
    ## store location dir
    dir = "sessionStore"
    # branch session size , if exceeded first try compress lockkey, still exceeded throws exceptions
    maxBranchSessionSize = 16384
    # globe session size , if exceeded throws exceptions
    maxGlobalSessionSize = 512
    # file buffer size , if exceeded allocate new buffer
    fileWriteBufferCacheSize = 16384
    # when recover batch read size
    sessionReloadReadSize = 100
    # async, sync
    flushDiskMode = async
  }

  ## database store property
  db {
    ## the implement of javax.sql.DataSource, such as DruidDataSource(druid)/BasicDataSource(dbcp) etc.
    datasource = "dbcp"
    ## mysql/oracle/h2/oceanbase etc.
    dbType = "mysql"
    driverClassName = "com.mysql.jdbc.Driver"
    url = "jdbc:mysql://127.0.0.1:3306/seata"
    user = "root"
    password = "password"
    minConn = 1
    maxConn = 10
    globalTable = "global_table"
    branchTable = "branch_table"
    lockTable = "lock_table"
    queryLimit = 100
  }
}
## server configuration, only used in server side
server {
  recovery {
    #schedule committing retry period in milliseconds
    committingRetryPeriod = 1000
    #schedule asyn committing retry period in milliseconds
    asynCommittingRetryPeriod = 1000
    #schedule rollbacking retry period in milliseconds
    rollbackingRetryPeriod = 1000
    #schedule timeout retry period in milliseconds
    timeoutRetryPeriod = 1000
  }
  undo {
    logSaveDays = 7
    #schedule delete expired undo_log in milliseconds
    logDeletePeriod = 86400000
  }
  #unit ms,s,m,h,d represents milliseconds, seconds, minutes, hours, days, default permanent
  maxCommitRetryTimeout = "-1"
  maxRollbackRetryTimeout = "-1"
  rollbackRetryTimeoutUnlockEnable = false
}

## metrics configuration, only used in server side
metrics {
  enabled = false
  registryType = "compact"
  # multi exporters use comma divided
  exporterList = "prometheus"
  exporterPrometheusPort = 9898
}
```
**registory.conf**

```json
registry {
  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
  type = "nacos"

  nacos {
    serverAddr = "localhost:8848"
    namespace = ""
    cluster = "default"
  }
}
```

- copy and paste file.conf and registry config to resource folder
- @GlobalTranscation to open distributed transaction
```java
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderDao orderDao;

    @Resource
    private StorageService storageService;

    @Resource
    private AccountService accountService;

    @Override
    @GlobalTransactional(name = "fsp-create-order",rollbackFor = Exception.class)
    public void create(Order order) {

        log.info("create new order");
        orderDao.create(order);

        log.info("decrease inventory");
        storageService.decrease(order.getProductId(), order.getCount());

        log.info("decrease amount");
        accountService.decrease(order.getUserId(), order.getMoney());

        log.info("update order");
        orderDao.update(order.getUserId(), 0);

        log.info("Done!");
    }
}
```