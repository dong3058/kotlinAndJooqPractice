package jooq

import com.jooqtest.jooq.tables.Messages
import com.jooqtest.jooq.tables.references.MESSAGES
import com.jooqtest.jooq.tables.references.USERINFO
import com.jooqtest.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.jooq.Query
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.stream.IntStream

@SpringBootTest
class CursorPagNationTest {


    @Autowired
    lateinit var dslContext: DSLContext;




}