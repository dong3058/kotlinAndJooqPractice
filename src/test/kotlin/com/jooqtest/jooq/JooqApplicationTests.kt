package com.jooqtest.jooq

import com.jooqtest.jooq.tables.records.UsersRecord
import com.jooqtest.jooq.tables.references.USERS
import org.assertj.core.api.Assertions
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JooqApplicationTests {


	@Autowired
	lateinit var dslContext:DSLContext;
	@Test
	fun contextLoads() {

	 var data=dslContext.selectFrom(USERS).fetch();
		Assertions.assertThat(data.isNotEmpty).isTrue();
	}

}
