package ru.test_qa

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.config.ExperimentalKotest
import io.kotest.core.listeners.Listener
import io.kotest.extensions.testcontainers.perProject
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import retrofit2.Retrofit
import retrofit2.create
import ru.test_qa.http.HttpUtil
import ru.test_qa.http.HttpUtil.jsonConverter
import ru.test_qa.http.HttpUtil.slf4jInterceptor
import ru.test_qa.service.ToDoService
import sun.misc.Unsafe
import java.lang.reflect.Field

@ExperimentalKotest
@Suppress("MemberVisibilityCanBePrivate")
object RegistryAndProjectConfiguration : AbstractProjectConfig() {

    val GenericContainer<*>.containerUrl: String
        get() = "http://$containerIpAddress:$firstMappedPort"

    val todoContainer = GenericContainer<Nothing>(
        "todo-app"
    ).apply {
        withExposedPorts(4242)
        waitingFor(Wait.forHttp("/todos"));
    }

    val retrofitJson: Retrofit by lazy {
        HttpUtil.createRetrofit(
            todoContainer.containerUrl, jsonConverter,
            listOf(slf4jInterceptor)
        )
    }

    val toDoService: ToDoService by lazy { retrofitJson.create() }

    override fun listeners(): List<Listener> = listOf(
        todoContainer.perProject("todo-app")
    )

    init {
        /* Suppress 'Illegal reflective access WARNING' */
//        val theUnsafe: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
//        theUnsafe.isAccessible = true
//        val u: Unsafe = theUnsafe.get(null) as Unsafe
//        val cls = Class.forName("jdk.internal.module.IllegalAccessLogger")
//        val logger: Field = cls.getDeclaredField("logger")
//        u.putObjectVolatile(cls, u.staticFieldOffset(logger), null)
    }
}