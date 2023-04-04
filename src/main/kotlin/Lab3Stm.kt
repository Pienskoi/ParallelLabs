import kotlinx.coroutines.*
import org.multiverse.api.StmUtils
import org.multiverse.api.references.TxnInteger
import java.sql.DriverManager
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.system.measureNanoTime

class Lab3Stm(private val dbUrl: String, private val dbUser: String, private val dbPassword: String) {
    private val dispatcher: ExecutorCoroutineDispatcher = Executors.newFixedThreadPool(5).asCoroutineDispatcher()

    private var value: TxnInteger = StmUtils.newTxnInteger(0)

    fun main() = runBlocking {
        DriverManager.getConnection(dbUrl, dbUser, dbPassword).use { connection ->
            log("Initializing database")
            connection.createStatement().execute("DROP TABLE IF EXISTS TestStm")
            connection.createStatement()
                .execute("CREATE TABLE IF NOT EXISTS TestStm (id SERIAL, value INTEGER)")
            connection.createStatement().executeUpdate("INSERT INTO TestStm (value) VALUES (0)")

            log("Starting execution")
            val duration = measureNanoTime {
                val jobs = mutableListOf<Job>()
                for (i in 0 until 500) {
                    jobs.add(launch(dispatcher) {
                        StmUtils.atomic(Callable {
                            log("Starting execution of SQL statement")
                            value.increment()
                            val preparedStatement =
                                connection.prepareStatement("UPDATE TestStm SET value = ? WHERE id = 1")
                            preparedStatement.setInt(1, value.get())
                            preparedStatement.executeUpdate()
                            log("SQL statement executed")
                        })
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