package ru.test_qa.spec

import io.kotest.assertions.asClue
import io.kotest.core.config.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bool
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.arbitrary.string
import okhttp3.Credentials
import retrofit2.Response
import retrofit2.awaitResponse
import ru.test_qa.RegistryAndProjectConfiguration.toDoService
import ru.test_qa.RestUtil
import ru.test_qa.RestUtil.basic
import ru.test_qa.RestUtil.generateTodos
import ru.test_qa.model.TodoModel
import kotlin.system.measureTimeMillis

@ExperimentalKotest
class PerfomanceTest : FreeSpec() {

    init {
        val checkCount = 10000
        "!Create $checkCount todo test" - {
            lateinit var todos: List<TodoModel>
            var timeInMillis: Long = 0
            "Given $checkCount todo models" {
                todos = generateTodos(checkCount)
            }

            "When sent POST request /todos with for all todos" {
                lateinit var response: Response<Unit>
                timeInMillis = measureTimeMillis {
                    todos.forEach { todo ->
                        response = toDoService.create(todo).awaitResponse()
                        response.code() shouldBe 201
                    }
                }
                print("Time: $timeInMillis")
            }
            "Then $checkCount item sent in ${timeInMillis/1000.0} sec" {

            }
            lateinit var getResponse: Response<List<TodoModel>>
            "When sent GET request /todos" {
                getResponse = toDoService.get().awaitResponse()
            }
            "Then received list of todos contains $checkCount item" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe checkCount
                    it.body()?.asClue { receivedModels ->
                        receivedModels shouldContainExactly todos
                    }

                }
            }
        }
    }
}

/*
100 000 105.32
--
TPS = 100 000/105.32 = 950
--
10000 10.31
10000 11.33
10000 10.4
10000 10.79
10000 11.53
--
TPS = 50 000/54.36 = 920
--
1000 1.54
1000 1.55
1000 1.52
1000 1.62
1000 1.53
--
TPS = 5 000/7.76 = 644
 */