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
import ru.test_qa.model.TodoModel

@ExperimentalKotest
class ToDoSpec : FreeSpec() {

    init {
        "Create test" - {
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
                    it.body() shouldBe Unit
                }
            }
            lateinit var getResponse: Response<List<TodoModel>>
            "When sent GET request /todos" {
                getResponse = toDoService.get().awaitResponse()
            }
            "Then received list of todos contains original todoModel" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.get(0).asClue { model ->
                        model?.id shouldBe todoModelModel.id
                        model?.text shouldBe todoModelModel.text
                        model?.completed shouldBe todoModelModel.completed
                    }
                }
            }
            "Delete test data" {
                RestUtil.delTodos(listOf(todoModelModel.id!!))
            }
        }

        "Get empty test" - {
            lateinit var getResponse: Response<List<TodoModel>>
            "When sent GET request /todos" {
                getResponse = toDoService.get().awaitResponse()
            }
            "Then received list of todos is empty" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 0
                }
            }
        }

        "Get test" - {
            lateinit var todos: List<TodoModel>
            "Given created todos model" {
                todos = RestUtil.createTodos(5)
            }
            lateinit var getResponse: Response<List<TodoModel>>
            "When sent GET request /todos" {
                getResponse = toDoService.get().awaitResponse()
            }
            "Then received list of todos contains original todos" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 5
                    it.body()?.asClue { receivedModels ->
                        receivedModels shouldContainExactly todos
                    }
                }
            }
            "Delete test data" {
                RestUtil.delTodos(todos.map{it.id!!})
            }
        }

        "Pagination test" - {
            lateinit var todos: List<TodoModel>
            "Given created 10 todo model" {
                todos = RestUtil.createTodos(10)
            }
            lateinit var getResponse: Response<List<TodoModel>>
            "When sent GET request /todos without params limit and offset" {
                getResponse = toDoService.get().awaitResponse()
            }
            "Then received full list of todos" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 10
                }
            }

            "When sent GET request /todos with param limit = 5" {
                getResponse = toDoService.get(limit = 5).awaitResponse()
            }
            "Then received list of 5 first todos" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 5
                    it.body()?.asClue { receivedModels ->
                        receivedModels shouldContainExactly todos.take(5)
                    }
                }
            }

            "When sent GET request /todos with param limit = 0" {
                getResponse = toDoService.get(limit = 0).awaitResponse()
            }
            "Then received empty list limit = 0" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 0
                }
            }

            "When sent GET request /todos with param offset = 5" {
                getResponse = toDoService.get(offset = 5).awaitResponse()
            }
            "Then received list of 5 last todos" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 5
                    it.body()?.asClue { receivedModels ->
                        receivedModels shouldContainExactly todos.takeLast(5)
                    }
                }
            }

            "When sent GET request /todos with param offset = 10" {
                getResponse = toDoService.get(offset = 10).awaitResponse()
            }
            "Then received empty list offset = 0" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 0
                }
            }

            "When sent GET request /todos with params offset = 2, limit = 6" {
                getResponse = toDoService.get(offset = 2, limit = 6).awaitResponse()
            }

            "Then received list of 6 todos" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 6
                    it.body()?.asClue { receivedModels ->
                        receivedModels shouldContainExactly todos.takeLast(8).take(6)
                    }
                }
            }
            "Delete test data" {
                RestUtil.delTodos(todos.map{it.id!!})
            }
        }

        "Update test" - {
            lateinit var todos: List<TodoModel>
            lateinit var updatedTodo: TodoModel
            "Given created todos model and one updated model" {
                todos = RestUtil.createTodos(3)
                updatedTodo = TodoModel(
                    id = todos[1].id,
                    text = Arb.string().next(),
                    completed = !todos[1].completed!!
                )
            }

            lateinit var putResponse: Response<Unit>
            "When sent PUT request /todos" {
                putResponse = toDoService.update(id = updatedTodo.id!!, todoModel = updatedTodo).awaitResponse()
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
            "Then received list of todos contains updatedTodo" {
                val updatedTodos = listOf(todos[0], updatedTodo, todos[2])
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 3
                    it.body()?.asClue { receivedModels ->
                        receivedModels shouldContainExactly updatedTodos
                    }
                }
            }
            "Delete test data" {
                RestUtil.delTodos(todos.map{it.id!!})
            }
        }

        "Delete test" - {
            lateinit var todos: List<TodoModel>
            "Given created todos model and one updated model" {
                todos = RestUtil.createTodos(3)
            }

            lateinit var delResponse: Response<Unit>
            "When sent DELETE request /todos" {
                delResponse = toDoService.del(id = todos.last().id!!, basic = basic).awaitResponse()
            }
            "Then received response has status 204 NO CONTENT" {
                delResponse.asClue {
                    it.code() shouldBe 204
                    it.body() shouldBe null
                }
            }
            lateinit var getResponse: Response<List<TodoModel>>
            "When sent GET request /todos" {
                getResponse = toDoService.get().awaitResponse()
            }
            "Then received list has not deleted todo" {
                getResponse.asClue {
                    it.code() shouldBe 200
                    it.body() shouldNotBe null
                    it.body()?.size shouldBe 2
                    it.body()?.asClue { receivedModels ->
                        receivedModels shouldContainExactly todos.take(2)
                    }
                }
            }
            "Delete test data" {
                RestUtil.delTodos(todos.take(2).map { it.id!! })
            }
        }
    }
}