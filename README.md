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
둘다 같은 애드을 사용할수있다.
