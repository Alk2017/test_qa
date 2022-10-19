package ru.test_qa

import io.kotest.core.config.ExperimentalKotest
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bool
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.arbitrary.string
import okhttp3.Credentials
import retrofit2.Response
import retrofit2.awaitResponse
import ru.test_qa.RegistryAndProjectConfiguration.toDoService
import ru.test_qa.model.TodoModel

object RestUtil {

    val basic = Credentials.basic("admin", "admin")

    @OptIn(ExperimentalKotest::class)
    suspend fun delTodos(ids: List<Int>): Unit {
        lateinit var response: Response<Unit>
        ids.forEach { id ->
            response = toDoService.del(id, basic).awaitResponse()
            response.code() shouldBe 204
        }
    }

    fun generateTodos(count: Int): List<TodoModel> {
        var id = 0
        val todos = List(count) {
            id += Arb.positiveInts(100).next()
            TodoModel(
                id = id,
                text = Arb.string(10).next(),
                completed = Arb.bool().next()
            )
        }
        return todos
    }

    suspend fun createTodos(count: Int): List<TodoModel> {
        val todos = generateTodos(count)
        lateinit var response: Response<Unit>
        todos.forEach { todo ->
            response = toDoService.create(todo).awaitResponse()
            response.code() shouldBe 201
        }
        return todos
    }
}