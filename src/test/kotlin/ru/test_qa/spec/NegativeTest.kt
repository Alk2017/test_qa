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
import retrofit2.Response
import retrofit2.awaitResponse
import ru.test_qa.RegistryAndProjectConfiguration.toDoService
import ru.test_qa.RestUtil
import ru.test_qa.model.TodoModel

@ExperimentalKotest
class NegativeTest : FreeSpec() {
    init {
        "Create double test" - {
            lateinit var todoModelModel: TodoModel
            "Given todo model" {
                todoModelModel = TodoModel(
                    id = Arb.positiveInts(100).next(),
                    text = Arb.string().next(),
                    completed = Arb.bool().next()
                )
            }
            lateinit var response: Response<Unit>
            "When sent POST request /todos with id = ${todoModelModel.id}" {
                response = toDoService.create(todoModel = todoModelModel).awaitResponse()
            }
            "Then received response has status 201 CREATED" {
                response.asClue {
                    it.code() shouldBe 201
                }
            }
            "When sent POST request /todos with id = ${todoModelModel.id} again" {
                response = toDoService.create(todoModel = todoModelModel).awaitResponse()
            }
            "Then received response has status 400 BAD_REQUEST" {
                response.asClue {
                    it.code() shouldBe 400
                }
            }
            "Delete test data" {
                RestUtil.delTodos(listOf(todoModelModel.id!!))
            }
        }

        "Update unexisting todo test" - {
            lateinit var updatedTodo: TodoModel
            "Given updated model" {
                updatedTodo = TodoModel(
                    id = Arb.positiveInts(100).next(),
                    text = Arb.string().next(),
                    completed = Arb.bool().next()
                )
            }

            lateinit var putResponse: Response<Unit>
            "When sent PUT request /todos" {
                putResponse = toDoService.update(id = updatedTodo.id!!, todoModel = updatedTodo).awaitResponse()
            }
            "Then received response has status 404 NOT_FOUND" {
                putResponse.asClue {
                    it.code() shouldBe 404
                }
            }
        }

        "Delete unexisting test" - {
            lateinit var delResponse: Response<Unit>
            "When sent DELETE request /todos" {
                delResponse = toDoService.del(id = Arb.positiveInts(100).next(), basic = RestUtil.basic).awaitResponse()
            }
            "Then received response has status 404 NOT_FOUND" {
                delResponse.asClue {
                    it.code() shouldBe 404
                }
            }
        }

        "Delete unauthorized test" - {
            lateinit var delResponse: Response<Unit>
            "When sent DELETE request /todos" {
                delResponse = toDoService.del(id = Arb.positiveInts(100).next(), basic = null).awaitResponse()
            }
            "Then received response has status 401 UNAUTHORIZED" {
                delResponse.asClue {
                    it.code() shouldBe 401
                }
            }
        }
    }
}
