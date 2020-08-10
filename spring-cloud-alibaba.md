# Spring Cloud Alibaba



## 1. Cloud Alibaba

- spring cloud netflix in maintenance mode
- integration with alibaba cloud
#### Naming
- import spring-cloud-starter-alibaba-nacos-discovery library
- declare nacos server in application.xml
```groovy
dependencies {
    implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery'
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```
#### Nacos CA & AP
![](image/spring-nacos.png)
- CP: support persistent instance, e.g. k8s or dns
- AP: week consistent
#### Configuration
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
- bootstrap.yml
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
- application.yml
```yaml
spring:
  profiles:
    active: dev
```
***

## 2. Sentinel
- going to replace hystrix for flow limit, circuit break and service fallback
- hystrix missing: console, limit request
![](image/sentinel-features-overview-en.png)
- init: java -jar sentinel-dashboard-1.7.2.jar
- Flow Limit
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
#### Circuit break
- no half open status, all time windows is second
+ RT
    + response time, if continue 5 request in 1s and over response time, will break circuit
    + after circuit break windows, open circuit
+ Exception Ratio
    + QPS is greater than 5 and exception ratio is over threshold, will break circuit
    + after circuit break windows, open circuit
+ Exception Number
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
#### Ribbon: Fallback & BlockHandler
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
#### OpenFeign
```groovy
feign:
  sentinel:
    enabled: true
```
#### Persistence
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

## 3. Seata
- Distributed transaction: one Id + three components
- three components
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
```properties
## file.conf
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


## registry.conf    
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