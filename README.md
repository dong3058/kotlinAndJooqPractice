rabbit mq 계정: rabbit mq에서 사용한 계정을 말함. 이는 곧 id,비밀번호의 형태로 접근하는것을 의미한다.
각 계정별로 모든 소스에 접근가능한 관리자, 혹은 한정적인 자원만 이용하는게 가능한 client과 같이 나눌수있다.


vhost:exchange,queue를  묶어서 접근 할수있게 만드는 논리적 단위를 말함.각각의 vhost는 서로의 vhost 안에있는 큐,ex에대한 내용을 알수가없다.
또한 각계정에서 접근가능한 vhost를 부여할수있음.
spring connection단계에서 어떤 vhost에 접근할지를 설정할수있다.

connection: rabbit mq와 직접적으로 연결되는 물리적 실체를 의미. 즉 스프링 어플리케이션과 rabbitmq가 가지는 tcp 연결을 의미한다.

channel: connection이라는 물리적 연결안에 존재한느 논리적 존재로 producer-broker, consumer-queue 간의 연결에 관여한다.
connection에 할당된 리소스에서 channel이라는 논리적 연결기반을 구현하고 이를 바탕으로 데이터를 주고받는다고 보면된다.
producer쪽 connection문제로 consumer쪽이 뻗는걸 막기위해서 2개의 connection으로 분리한다.
차피 queue,exchange,binbding은 rabbitmq 내부에 존재하는 데이터이므로 connection이 분리되어있어도 각 연결이 가지는 계정,vhost가 접근 가능하다면 
둘다 같은 애들을 사용할수있다.

rabbitmq port:webscoket config 에서 외부 브로커로써 rabbitmq와 연갈하는 포트와 rabbittemplate가 rabbit mq와 연결하는 포트 번호는 다르다,

rabbitmq를 외부브로커로 사용: 기존의 인메모리 브로커가아니라 rabbitmq를 외부의 메시지 브로커로 사용시 스프링의 메모리가 아닌 rabbit mq의
메시지 브로커를 이용하게된다. 이때 보통의 rabbit mq 포트가 아니라 stomp 통신을 위한 61613(맞나?)라는 포트로 연결 하게된다.
해당 포트로 연결이되면 simpmessagetemplate로 구독경로로 보낼시 자동으로 해당 큐를타고 구독한 클라이언트에게 메시지를 뿌린다.
rabbittemplate(일반 rabbit port하고 연결한)로 메시지를 보내도 알아서 큐를 찾은후 해당 큐에대해서 subcribe가 된애들이 있으면 그쪽으로
메시지를 알아서 뿌린다.


flywat랑 h2 db jooq 테스트 환경구성

