package ru.test_qa.service

import retrofit2.Call
import retrofit2.http.*
import ru.test_qa.model.TodoModel

interface ToDoService {

    @GET("todos")
    fun get(
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): Call<List<TodoModel>>

    @POST("todos")
    fun create(
        @Body todoModel: TodoModel
    ): Call<Unit>

    @PUT("todos/{id}")
    fun update(
        @Path("id") id: Int,
        @Body todoModel: TodoModel
    ): Call<Unit>

    @DELETE("todos/{id}")
    fun del(
        @Path("id") id: Int,
        @Header("Authorization") basic: String?,
    ): Call<Unit>
}