# Hulk
高性能分布式事务框架

该框架采用TCC模式，TCC请求异步化和无锁设计方案，极大提升了事务处理的性能。

## 特性

1. TCC异步化，同比性能高于其他开源产品(比如：tcc-transaction，hmily，easyTransaction)
2. 提供服务通讯方式扩展接口，可自定义通讯方式，如：dubbo, 异步消息模式
3. 支持事务执行超时回滚
4. 独立事务恢复进程，避免框架所在线程异常退出导致事务无法恢复
5. 支持事务日志存储读写分离
6. 事务ID生成策略可配置
7. 支持分支事务(事务嵌套)
8. 业务代码无侵入
9. 防悬挂
10. 支持MVCC，RU和RC隔离级别，默认RC
11. 支持AOF机制持久化事务日志

## 使用指南

### Getting Started

使用代码中请求serviceA，将调用serviceB和serviceC

1. 添加配置文件：/opt/hulk/hulk_global.properties，内容如下：
   eureka.serviceUrl.defaultZone=http://localhost:8761/eureka/
2. 添加配置：/opt/hulk/hulk.properties，内容详见下面的配置说明

### 通讯方式自定义

#### 实现远程通讯接口NetworkCommunication

以FeignClient为例，实现接口如下：

```java
/**
   * Return provider map like map <helloService, helloServiceBean>
   * @param ctx ApplicationContext
   * @return provider map
*/
public class FeignClientCommunication implements NetworkCommunication {

    @Override
    public Map<String, Object> getProviders(ApplicationContext ctx) {
        FeignClient annotation = null;
        Map<String, Object> providerMap = new HashMap<String, Object>();
        Map<String, Object> feignClientMap = ctx.getBeansWithAnnotation(FeignClient.class);
        for (Object client : feignClientMap.values()) {
            annotation = client.getClass().getInterfaces()[0].getAnnotation(FeignClient.class);
            providerMap.put(annotation.value(), client);
        }
        return providerMap;
    }

}
```
#### 注入Bean

以FeignClient为例，如下：
```java
@Bean
public NetworkCommunication communication() {
    return new FeignClientCommunication();
}
```

### 依赖与配置项
#### 工程依赖

发起方和参与方在工程中添加如下依赖：

```java
<dependency>
  <groupId>com.mtl.hulk</groupId>
  <artifactId>hulk-core</artifactId>
  <version>${hulk.version}</version>
</dependency>
```
当前版本：1.0.1

#### 工程属性配置

配置属性说明：

* mtl.hulk.loggerStorage：该属性指定事务日志存储介质，默认：mysql

* mtl.snapShotLogDir：该属性指定事务日志快照路径，默认：/data/hulk

* mtl.logScanPeriod：该属性设置事务日志快照扫描周期，默认：20s

* mtl.hulk.snapshot.rule：该属性设置本地快照分割策略，默认：increment，自然数自增分割

* mtl.hulk.snapshot.bufferSize：该属性指定读取/写入缓冲区大小，单位Byte，默认：20K

* mtl.hulk.snapshot.dir：该属性指定本地快照存储目录，默认：/data/hulk

* mtl.hulk.snapshot.perFileSize：该属性设置单个快照存储记录数，默认：1000

* mtl.hulk.logMasters：该属性指定事务日志存储master，例如：mysql定义如下：

```java
mtl.hulk.logMasters[0].driverClassName=com.mysql.jdbc.Driver

mtl.hulk.logMasters[0].url=jdbc:mysql://localhost:3306/test

mtl.hulk.logMasters[0].username=test

mtl.hulk.logMasters[0].password=test

mtl.hulk.logMasters[0].initialSize=100

mtl.hulk.logMasters[0].maxActive=1000

mtl.hulk.logMasters[0].maxIdle=10

mtl.hulk.logMasters[0].minIdle=5

mtl.hulk.logMasters[0].maxWait=60000

mtl.hulk.logMasters[0].removeAbandonedTimeout=180

mtl.hulk.logMasters[0].removeAbandoned=true

mtl.hulk.logMasters[0].testOnBorrow=true

mtl.hulk.logMasters[0].testOnReturn=true

mtl.hulk.logMasters[0].testWhileIdle=true

mtl.hulk.logMasters[0].validationQuery=SELECT 1

mtl.hulk.logMasters[0].timeBetweenEvictionRunsMillis=5000

mtl.hulk.logMasters[0].jdbcInterceptors=ConnectionState;StatementFinalizer
```

* mtl.hulk.logSlaves：事务日志存储slave，例如：mysql定义如下：

```java
mtl.hulk.logSlaves[0].driverClassName=com.mysql.jdbc.Driver

mtl.hulk.logSlaves[0].url=jdbc:mysql://localhost:3306/test

mtl.hulk.logSlaves[0].username=test

mtl.hulk.logSlaves[0].password=test

mtl.hulk.logSlaves[0].initialSize=100

mtl.hulk.logSlaves[0].maxActive=1000

mtl.hulk.logSlaves[0].maxIdle=10

mtl.hulk.logSlaves[0].minIdle=5

mtl.hulk.logSlaves[0].maxWait=60000

mtl.hulk.logSlaves[0].removeAbandonedTimeout=180

mtl.hulk.logSlaves[0].removeAbandoned=true

mtl.hulk.logSlaves[0].testOnBorrow=true

mtl.hulk.logSlaves[0].testOnReturn=true

mtl.hulk.logSlaves[0].testWhileIdle=true

mtl.hulk.logSlaves[0].validationQuery=SELECT 1

mtl.hulk.logSlaves[0].timeBetweenEvictionRunsMillis=5000

mtl.hulk.logSlaves[0].jdbcInterceptors=ConnectionState;StatementFinalizer
```
* mtl.hulk.retryTransactionCount：该属性指定事务执行重试次数，默认：3


### 以转账场景举例

#### TCC参与者

TCC 参与者需要实现 3 个方法，分别是一阶段 Try 方法、二阶段 Confirm 方法以及二阶段 Cancel 方法。
在 TCC 参与者的接口中需要先加上 @MTLTwoPhaseAction 注解（需引入 com.mtl.hulk包），并定义这3个方法，如
下所示：

```java
@Component
class TransferMinusActionImpl implements TransferMinusAction {

    @Override
    @MTLTwoPhaseAction(confirmMethod = "confirm", cancelMethod = "cancel")
    public String prepareMinus(BusinessActivityContext businessActivityContext, String accountNo, double amount) {

    }

    @Override
    @MTLSuspendControl(BusinessActivityExecutionType.COMMIT) //防悬挂注解
    public boolean confirm(BusinessActionContext businessActionContext){
        DAO1; //实现扣钱
    }

    @Override
    @MTLSuspendControl(BusinessActivityExecutionType.ROLLBACK) //防悬挂注解
    public boolean cancel(BusinessActionContext businessActionContext){
	DAO2; //扣钱回滚
    }

}
```           
```java
@Component
class TransferAddActionImpl implements TransferAddAction {

    @Override
    @MTLTwoPhaseAction(confirmMethod = "confirm", cancelMethod = "cancel")
    public String prepareAdd(BusinessActivityContext businessActivityContext, String accountNo, double amount) {

    }

    @Override
    public boolean confirm(BusinessActionContext businessActionContext){
        DAO1; //实现加钱
    }

    @Override
    public boolean cancel(BusinessActionContext businessActionContext){
	DAO2; //加钱回滚
    }

}
```

@MTLTwoPhaseAction 注解属性说明：

```java
comfirmMethod：指定二阶段 Confirm 方法的名称，可自定义。
cancelMethod：指定二阶段 Cancel 方法的名称，可自定义。
```

##### TCC 方法参数说明：

```java
Try(上例中的prepareMinus)：第一个参数类型必须是 com.mtl.hulk.context.BusinessActivityContext，后续参数的个数和类型可以自定义。
Confirm：有且仅有一个参数，参数类型必须是 com.mtl.hulk.context.BusinessActivityContext，后续为相应的参数名。
Cancel：有且仅有一个参数，参数类型必须是 com.mtl.hulk.context.BusinessActivityContext，后续为相应的参数名。

返回类型说明
Try方法的返回类型必须为 String 类型
Confirm 和 Cancel 这 2 个方法的返回类型必须为 boolean 类型。
```
  注意：同一个事务发起方try方法名不能重复。

##### TCC 参与者实现规范

实现两阶段接口：二阶段入参只包含事务数据，参与者处理业务数据之前需要根据事务数据关联到具体的业务数据。

控制业务幂等性：参与者需要支持同一笔事务的重复提交和重复回滚，保证一笔事务能且只能成功一次。
数据可见性控制：当一笔分布式事务正在处理中，此时如果有查询，则需要兼容未处理完的中间数据的可见性。一般通过文案展示告诉用户中间数据的存在，例如告诉用户当前冻结的金额有多少。
隔离性控制：对于状态类数据，需要提供隔离性控制来允许不同事务操作同一个业务资源。例如账户余额，不同事务操作的金额是隔离的。
允许空回滚，拒绝空提交：回滚请求处理时，如果对应的具体业务数据为空，则返回成功。提交请求处理时，如果对应的具体业务数据为空，则返回失败。

#### TCC发起方

##### 开启分布式事务

为需要开启分布式事务的接口增加分布式事务注解

@MTLDTActivity(businessDomain = "mtl", businessActivity = "transfer")，表明此方法内部需要开启分布式事务。

方法内部可以执行下列事务操作（无数量与先后顺序的限制）：
* 操作本地 DAO 操作。
* 调用基于 Spring Cloud 发布的跨服务 TCC 参与者的第一阶段 Try 方法。

业务方法正常返回(true)则分布式事务提交，业务方法抛出异常(false)则分布式事务回滚。

示例如下：

```java
@Component
public class TransferImpl implement Transfer {

	@MTLDTActivity(businessDomain = "mtl", businessActivity = "transfer")
	@MTLTwoPhaseAction(confirmMethod = "confirm", cancelMethod = "cancel")
	public String transferByTcc(String from, String to, double amount) {
		try{
			//第一个参与者
			String ret = firstTccActionRef.prepareMinus(null, from, amount);
			if(ret.isEmpty()){
				//事务回滚
				return false;
			}
			//第二个参与者
			ret = secondTccActionRef.prepareAdd(null, to, amount);
			if(ret.isEmpty()) {
				//事务回滚
				return false;
			}
			return ret;
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
    	public boolean confirm(BusinessActionContext businessActionContext){

    	}

    	@Override
    	public boolean cancel(BusinessActionContext businessActionContext){

    	}

}
```

@MTLDTActivity

注解属性说明：

```java
businessDomain：必选项。该属性指定业务类型，由用户根据自己业务场景自定义
businessActivity：必选项。该属性指定事务名，由用户根据自己业务场景自定义，例如：转账定义为transfer
timeout：该属性指定事务超时时间，默认为 5 秒，用户可以根据自身需要自定义，单位为秒。
```

注意：超时会自动触发当前事务回滚。

##### FeignClient接口

为收集远程调用接口返回的上下文增加Broker注解

@MTLDTBroker
示例如下：

```java
@FeignClient("transferAddAction")
public interface TransferAddActionClient {

    @MTLDTBroker
    @RequestMapping("/transfer/add")
    String transferAdd(@RequestParam("accountNo") String accountNo, @RequestParam("amount") double amount);

}
```
## 未来规划
* 实现MVCC之RR，Serializable隔离级别
* 分布式事务日志恢复
* 事务执行监控
* SPI
