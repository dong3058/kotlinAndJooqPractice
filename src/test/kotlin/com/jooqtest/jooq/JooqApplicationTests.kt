package com.jooqtest.jooq

import com.jooqtest.jooq.tables.references.MESSAGES
import com.jooqtest.jooq.tables.references.USERINFO
import com.jooqtest.jooq.tables.references.USERS
import com.jooqtest.jooq.testDto.UserCount
import com.jooqtest.jooq.testDto.UserData
import com.jooqtest.jooq.testDto.UserDto
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.impl.DSL.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class JooqApplicationTests {



	@Autowired
	lateinit var dslContext:DSLContext;


	@BeforeEach
	fun setting(){
		val query=dslContext.insertInto(USERS)
			.set(USERS.AGE,12)
			.set(USERS.USERNAME,"황동근")
			.set(USERS.CREATED_AT,LocalDateTime.now())
			.set(USERS.EMAIL,"dong.3058@daum.net")
			.returningResult(USERS.ID)
			.fetchOneInto(Long::class.java)
		val query2=dslContext.insertInto(USERS)
			.set(USERS.AGE,13)
			.set(USERS.USERNAME,"알")
			.set(USERS.CREATED_AT,LocalDateTime.now())
			.set(USERS.EMAIL,"hwangdonggeun70@gmail.com")
			.returningResult(USERS.ID)
			.fetchOneInto(Long::class.java)
		val query3=dslContext.insertInto(USERINFO,USERINFO.SEX,USERINFO.USERID).values("남자",query)
		val query4=dslContext.insertInto(USERINFO,USERINFO.SEX,USERINFO.USERID).values("여자",query2)
		dslContext.batch(query3,query4).execute();
	}

	@AfterEach
	fun clearAfterTest(){
		dslContext.deleteFrom(USERINFO).execute();
		dslContext.deleteFrom(USERS).execute();
	}

	@Test
	fun contextLoads() {

		val query=dslContext.insertInto(USERS,USERS.AGE,USERS.USERNAME,USERS.EMAIL).values(12,"황동근","dong.3058@daum.net");
		val query2=dslContext.insertInto(USERS,USERS.AGE,USERS.USERNAME,USERS.EMAIL,USERS.CREATED_AT).values(13,"알","hwnagdonggeun70@gmail.com",
			LocalDateTime.now());
	 	dslContext.batch(query,query2).execute();
	}

	@Test
	fun testingSelect(){
		val data=dslContext.selectFrom(USERS).fetch()
		System.out.println("데이터 타입:${data.javaClass}")
		println("총갯수:${data.size}\n")
		data.stream().forEach { x->
			println("id값:${x.id}\n")
			println("나이 값:${x.age}\n")
			println("이름 값:${x.username}\n")
			println("이메일 값:${x.email}\n")
		 }
	}
	@Test
	fun testingStaticProcess() {
		//dto 매핑시에 순서는 상관없고 이름,데이터 타입만 맞춰추면됨.
		//as로 이름 변동시엔 이름이 같아야되고 보통의 경우 카멜케이스가 아니라 그냥 소문자로 지어주면된다.
 		val data = dslContext.select(USERS.USERNAME, USERS.AGE).from(USERS)
			.where(USERS.AGE.eq(12)).fetchInto(UserDto::class.java);
		data.stream().forEach { x -> println("데이터:${x.age}-${x.username}") }

		val data2 = dslContext.select(count()).from(USERS).fetch()
		data2.stream().forEach { x ->
			println("데이터:${x.value1()}")
		}
		val data3=dslContext.select(count().`as`("userCount"),USERS.AGE).from(USERS)
			.groupBy(USERS.AGE)
			.orderBy(USERS.AGE.desc())
			.fetchInto(UserCount::class.java)
		data3.stream().forEach { x-> println("나이:${x.age}-카운트:${x.userCount}")}


		//from절 서브쿼리
		val subQuery=dslContext.select(USERS.AGE).from(USERS)
			.where(USERS.AGE.gt(12))
			.asTable("subQuery")

		val data4=dslContext.select(subQuery.field(USERS.AGE)).from(subQuery)
			.where(subQuery.field(USERS.AGE)!!.gt(10))
			.fetch()

		data4.stream().forEach { x-> println("나이데이터:${x.value1()}") }

		//union test

		val unionQuery1=dslContext.select(USERS.ID).from(USERS)
		val unionQuery2=dslContext.select(USERINFO.USERID).from(USERINFO)

		val unionSubQuery=unionQuery1
			.union(unionQuery2)
			.asTable("unionSubQuery")
		//유니온 서브 쿼리에서 FIELD 0은  유니온시 가져온느 데이터를 순서대로 몇번인지를 포함. 0번부터 시작.
		val result=dslContext.select(unionSubQuery.field(0)).from(unionSubQuery).fetch()
		result.stream().forEach { x-> println("유니온 데이터:${x.value1()}") }

		//join test
		val data5=dslContext.select(USERS.USERNAME,USERINFO.SEX,USERS.ID).from(USERS)
			.innerJoin(USERINFO)
			.on(USERINFO.USERID.eq(USERS.ID))
			.fetchInto(UserData::class.java)

		data5.stream().forEach { x-> println("유저정보:${x.sex}-${x.username}-${x.id}") }

		//from 절 서브쿼리 테스트 2
		val subQuery2=dslContext.select(USERS.AGE,USERINFO.USERID).from(USERS)
			.innerJoin(USERINFO)
			.on(USERINFO.USERID.eq(USERS.ID))
			.where(USERS.AGE.gt(12))
			.asTable("subQuery2")

		val data6=dslContext.select(subQuery2.field(USERINFO.USERID)).from(subQuery2)
			.where(subQuery2.field(USERS.AGE)!!.gt(10))
			.fetch()

		data6.stream().forEach { x-> println("나이데이터:${x.value1()}") }

		//CTE  테스트
		//서브쿼리와 cte간의 차이라면 cte는 미리 구해놔서 메모리에 적재해두고 재사용하는거고
		//서브 쿼리는 쿼리문 진행하면서 그떄그때 구해서 사용하는것.
		//복잡한 케이스가 아니라면 서브쿼리를, 복잡하거나 재귀를 써야된다면 cte로 쿼리문이 점차 깊어지는걸 방지하자.
		val cte=name("cte").`as`(dslContext.select(USERS.AGE,USERINFO.USERID).from(USERS)
			.innerJoin(USERINFO)
			.on(USERINFO.USERID.eq(USERS.ID))
			.where(USERS.AGE.gt(12)))

		val data7=dslContext.with(cte)
			.select(cte.field(USERINFO.USERID))
				 .from(cte)
				 .where(cte.field(USERS.AGE)!!.gt(10))
				 .fetch()
		data7.stream().forEach { x-> println("나이데이터:${x.value1()}") }
	}


	@Test
	fun testDynamicQuery(){
		val result=dslContext.select(USERS).from(USERS)
			.where(nameQuery(null))
			.fetch()
		println("결과:${result.size}")

	}
	fun nameQuery(username:String?):Condition?{
		return USERS.USERNAME.equal(username)
	}

	@Test
	fun settingData(){

		val batchList: MutableList<Query> = mutableListOf()
		for(i in 1..20){
			if(i%2==0){
				val value:Long=2;
				val query=dslContext.insertInto(MESSAGES)
					.set(MESSAGES.ROOM_ID,value)
					.set(MESSAGES.MESSAGE,"hello")

				batchList.add(query)
			}
			else{
				val value:Long=1;
				val query=dslContext.insertInto(MESSAGES)
					.set(MESSAGES.ROOM_ID,value)
					.set(MESSAGES.MESSAGE,"hello")
				batchList.add(query)
			}
		}
		dslContext.batch(batchList).execute();
	}



}
