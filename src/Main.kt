
// Кажется, сделано не по ТЗ, так как в итоге в andExpect, andDo, status и body
// можно в итоге обращаться к любым методам с соответствующей сигнатурой.
// А как сделать так, чтобы можно было обращаться только к методам соответствующего класса,
// сколько не пыталась понять - не поняла(((

val repeatFun: String.(Int) -> String = { times -> this.repeat(times) }
val twoParameters: (String, Int) -> String = repeatFun // OK

data class Response (
    val code: Int,
    val body: String?
)

class Client {
    fun perform(code: Int, body: String?) = ResponseActions(code, body)
}

class ResponseActions(code: Int, body: String?) {
    val response: Response = Response(code, body)

    fun andDo(responseFunction: (Response) -> Unit): ResponseActions {
        responseFunction(response)
        return this
    }

    fun andExpect(responseMatcher: () -> Unit) : ResponseActions {
        return this.apply {
            responseMatcher()
        }
    }

    fun status(responseFunction: ResponseActions.StatusResponseActions.() -> Unit) {
        this.StatusResponseActions().responseFunction()
    }

    fun body(responseFunction: ResponseActions.BodyResponseActions.() -> Unit) {
        this.BodyResponseActions().responseFunction()
    }

    inner class StatusResponseActions {
        fun isOk() {
            if (response.code != 200) throw StatusResponseMatchersException()
        } // если статус не 200, то выбросить исключение

        fun isBadRequest() {
            if (response.code != 400) throw StatusResponseMatchersException()
        }// если статус не 400, то выбросить исключение

        fun isInternalServerError() {
            if (response.code != 500) throw StatusResponseMatchersException()
        } // если статус не 500, то выбросить
    }

    inner class BodyResponseActions() {
        fun isNull() {
            if (!response.body.isNullOrEmpty()) throw BodyResponseMatchersException()
        } // если тело не пустое, то выбросить исключение

        fun isNotNull() {
            if (response.body.isNullOrEmpty()) throw BodyResponseMatchersException()
        }
    }
}

open class ResponseMatchersException: Exception() {}
class StatusResponseMatchersException: ResponseMatchersException() {}
class BodyResponseMatchersException: ResponseMatchersException() {}


fun mock() {
    println("This shouldn't display")
}
fun main() {
    val mockClient = Client()
    val response: Response = mockClient.perform(200, "OK").apply {
        andExpect {
            status {
                isOk()
            }
            body {
                isNotNull()
            }
        }
        andDo { response ->
            println(response)
        }
    }.response
}








/*
fun andExpect(responseMatchers: (statusResponseMatchers: () -> Unit, bodyResponseMatchers: () -> Unit) -> Unit) : ResponseActions {
    return this
}

fun status(responseActions: ResponseActions.StatusResponseActions.() -> Unit) {
    this.StatusResponseActions().apply {
        responseActions()
    }
}
fun body(responseActions: ResponseActions.BodyResponseActions.() -> Unit) {
    this.BodyResponseActions().apply {
        responseActions()
    }
}

fun ResponseActions.StatusResponseActions.status(responseAction: ResponseActions.StatusResponseActions.() -> Unit) {
    responseAction()
}

fun ResponseActions.BodyResponseActions.body(responseAction: ResponseActions.BodyResponseActions.() -> Unit) {
    responseAction()
}

fun status(responseFunction:  ResponseActions.StatusResponseActions.() -> Unit) { responseFunction() }
fun body(responseFunction: ResponseActions.BodyResponseActions.() -> Unit) { this.responseFunction() }

fun status(responseActions: ResponseActions, responseFunction: ResponseActions.StatusResponseActions.() -> Unit) {
    responseActions.StatusResponseActions().responseFunction()
}

fun body(responseActions: ResponseActions, responseFunction: ResponseActions.BodyResponseActions.() -> Unit) {
    responseActions.BodyResponseActions().responseFunction()
}
*/