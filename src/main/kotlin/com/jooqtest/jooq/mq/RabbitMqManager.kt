package com.jooqtest.jooq.mq


import jakarta.annotation.PostConstruct
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.stereotype.Service


@Service
class RabbitMqManager(private val rabbitAdmin: RabbitAdmin) {



    //@PostConstruct
    fun createMasterParts(){
        val masterQueue=Queue("masterQueue",false,false,false)
        val masterExchnage=FanoutExchange("masterExchange",false,false)
        rabbitAdmin.declareQueue(masterQueue)
        rabbitAdmin.declareExchange(masterExchnage)
        createMasterBinding(masterQueue.name,masterExchnage.name)
    }

    //exchage든 queue든간에 같은 이름에 같은속성이면 멱등성 즉 중복된게 생성이안된다-->속성은 durable,exculsive,autodel등을 말함.
    //durable-->브로커 재시작시에도 유지가 되는가를 말함. 즉 서버껏다 켜도 다시 실행되는가.exculsive가 애보단 우선권이있는지라
    //excusive가 켜진 상황에서 durable이고 연결이 종료되면 걍삭제된다.
    //auto delete--> 컨슈머가 모두 연결이 종료되면 연결이끊키는가,exchange의 경우에는 binding 모두 사라지면 자동으로 삭제되는가를의미.
    //exclusive 큐에대한 접근에 큐를생성한 커낵션만 접근할수있게 할것인가를 의미.
    //rabbit mq를 외부 브로커로 사용한다면은 구독경로에온 이름을 바탕으로 queue와 연결하는 대 기본적으로 durable이 true인 queue를 요구한다.
    //그래서 createqueue에서 durable을 true로했다.
    //내부 브로커로 이요시 prefix뒤에오는 값이 queue의 이름값이된다.

   fun createQueue(name: String):Queue{
        //val properties= mapOf("x-message-ttl" to 300000,"x-single-active-consumer" to true)
        //val queue =Queue(name,true, false, false,properties)
        val queue =Queue(name,true, false, false)
        rabbitAdmin.declareQueue(queue);
        return queue;
    }
    fun createMasterQueue(name: String):Queue{
        val queue =Queue(name,false, false, false)
        rabbitAdmin.declareQueue(queue);
        return queue;
    }
    //fanout 방식->카프카 하고는 다르게 분산서버들에있는 컨슈머들은 1개의 큐를 바라볼때 모두가 메시지를 떙겨오는게 아니라 선점한
    //1개의 컨슈머만 메시지가 소비가 가능함.서버별 전용 큐가있어서 같은 exchange를 바라보면서 fanout 방식이여야만
    //카프카의 컨슈머 그룹처럼 작동할수있다.
    fun createExchange(name: String):FanoutExchange{
        val exchange=FanoutExchange(name,false,false);
        rabbitAdmin.declareExchange(exchange);
        return exchange;
    }

    fun createDLQ():Queue{
        //dlx는 dlq생성시에 dlq에 binding할 exchange를 알려주면서 dlx로 등록됨.
        val properties= mapOf("x-dead-letter-exchange" to "DLX")
        val queue=Queue("DLQ",false,false,false,properties)
        rabbitAdmin.declareQueue(queue)
        return queue
    }
    fun createDLX():FanoutExchange{
        val exchange=FanoutExchange("DLX",false,false)
        rabbitAdmin.declareExchange(exchange)
        return exchange
    }

    //큐를 삭제하면 애하고 연결된 binding은 삭제됨.
    fun delQueue(name: String){
      rabbitAdmin.deleteQueue(name)
    }
    //exchange를 삭제하면 애하고 연결된 binding은 삭제됨.
    fun delExchange(name: String){
        rabbitAdmin.deleteExchange(name)
    }
    fun createBinding(queueName: String,exchangeName: String){
        val binding=BindingBuilder
            .bind(createQueue(queueName))
            .to(createExchange(exchangeName))
        rabbitAdmin.declareBinding(binding)
    }

    fun createMasterBinding(queueName: String,exchangeName: String){
        val binding=BindingBuilder
            .bind(createMasterQueue(queueName))
            .to(createExchange(exchangeName))
        rabbitAdmin.declareBinding(binding)
    }
    fun createDLXBinding(){
        val binding=BindingBuilder
            .bind(createDLQ())
            .to(createDLX())
        rabbitAdmin.declareBinding(binding)
    }

    //애는 큐에 걸린 binding를 개별적으로 컨트롤시에 사용.
    fun deleteBinding(queueName:String,exchangeName:String){
        val binding=BindingBuilder
            .bind(createQueue(queueName))
            .to(createExchange(exchangeName))
        rabbitAdmin.removeBinding(binding);
    }

}