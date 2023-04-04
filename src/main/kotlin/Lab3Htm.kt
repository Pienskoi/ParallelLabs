import kotlinx.coroutines.*
import java.sql.DriverManager
import java.util.concurrent.Executors
import kotlin.system.measureNanoTime

class Lab3Htm(private val dbUrl: String, private val dbUser: String, private val dbPassword: String) {
    private val dispatcher: ExecutorCoroutineDispatcher = Executors.newFixedThreadPool(5).asCoroutineDispatcher()

    fun main() = runBlocking {
        DriverManager.getConnection(dbUrl, dbUser, dbPassword).use { connection ->
            log("Initializing database")
            connection.createStatement().execute("DROP TABLE IF EXISTS TestHtm")
            connection.createStatement()
                .execute("CREATE TABLE IF NOT EXISTS TestHtm (id SERIAL, value INTEGER)")
            connection.createStatement().executeUpdate("INSERT INTO TestHtm (value) VALUES (0)")

            log("Starting execution")

            val duration = measureNanoTime {
                val jobs = mutableListOf<Job>()
                for (i in 0 until 500) {
                    jobs.add(launch(dispatcher) {
                        log("Starting execution of SQL statement")
                        connection.createStatement().executeUpdate("UPDATE TestHtm SET value = value + 1 WHERE id = 1")
                        log("SQL statement executed")
                    })
                }
                jobs.joinAll()
            }

            dispatcher.close()
            val durationInMillis = duration / 1000000
            println("Execution took $durationInMillis milliseconds")
        }
    }
}