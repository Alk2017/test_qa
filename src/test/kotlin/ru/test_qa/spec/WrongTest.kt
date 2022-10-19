package ru.test_qa.spec

import io.kotest.assertions.asClue
import io.kotest.core.config.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bool
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.arbitrary.string
import retrofit2.Response
import retrofit2.awaitResponse
import ru.test_qa.RegistryAndProjectConfiguration.toDoService
import ru.test_qa.RestUtil
import ru.test_qa.model.TodoModel

@ExperimentalKotest
class WrongTest : FreeSpec() {
    init {
        "Update other todo test" - {
            lateinit var todos: List<TodoModel>
            lateinit var updatedTodo: TodoModel
            "Given created todos model and updated model" {
                todos = RestUtil.createTodos(2)
                updatedTodo = TodoModel(
                    id = todos[1].id,
                    text = Arb.string().next(),
                    completed = !todos[1].completed!!
                )
            }

            lateinit var putResponse: Response<Unit>
            "When sent PUT request /todos for todo with wrong id" {
                putResponse = toDoService.update(id = todos[0].id!!, todoModel = updatedTodo).awaitResponse()
            }
            "Then received response has status 200 OK" {
                putResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldBe Unit
                }
            }
            lateinit var getResponse: Response<List<TodoModel>>
            "When sent GET request /todos" {
                getResponse = toDoService.get().awaitResponse()
            }
            "Then received list of todos contains two todo with equal id" {
                val updatedTodos = listOf(updatedTodo, todos[1])
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 2
                    it.body()?.asClue { receivedModels ->
                        receivedModels shouldContainExactly updatedTodos
                        receivedModels[0].id shouldBe receivedModels[1].id
                    }
                }
            }
            "Delete test data" {
                RestUtil.delTodos(listOf(updatedTodo.id!!))
            }
        }

        forAll(
            row("id", TodoModel(text = Arb.string().next(),completed = Arb.bool().next())),
            row("text", TodoModel(id = Arb.positiveInts(100).next(),completed = Arb.bool().next())),
            row("completed", TodoModel(id = Arb.positiveInts(100).next(),text = Arb.string().next()))
        ) { nullParameter, todoModel ->
            "Update without $nullParameter parameter test" - {
                lateinit var putResponse: Response<Unit>
                "When sent PUT request /todos for todo with wrong id" {
                    putResponse = toDoService.update(
                        id = Arb.positiveInts(100).next(),
                        todoModel = todoModel
                    ).awaitResponse()
                }
                "Then received response has status 401 UNAUTHORIZED" {
                    putResponse.asClue {
                        it.code() shouldBe 401
                    }
                }
            }
        }
    }
}