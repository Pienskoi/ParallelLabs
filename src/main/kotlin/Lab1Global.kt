import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.system.measureNanoTime

class Lab1Global(private val dimension: Int = 100) {
    private val B = generateRandomVector(dimension)
    private val D = generateRandomVector(dimension)
    private val MD = generateRandomMatrix(dimension, dimension)
    private val ME = generateRandomMatrix(dimension, dimension)
    private val MM = generateRandomMatrix(dimension, dimension)

    var MF: Matrix = Matrix(Array(MD.columnSize) { DoubleArray(MD.rowSize) })
    var E: Matrix = Matrix(Array(B.columnSize) { DoubleArray(B.rowSize) })

    private val dispatcher: ExecutorCoroutineDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    fun main() {
        log("Writing inputs to files")
        writeMatrixToFile(B, "input", "B")
        writeMatrixToFile(D, "input", "D")
        writeMatrixToFile(MD, "input", "MD")
        writeMatrixToFile(ME, "input", "ME")
        writeMatrixToFile(MM, "input", "MM")

        log("Starting execution")
        val duration = measureNanoTime {
            val threads = mutableListOf<Thread>()
            val mutexMF = Mutex()
            val mutexE = Mutex()
            threads.add(thread { calculateMF(mutexMF) })
            threads.add(thread { calculateE(mutexE) })
            threads.forEach { it.join() }
        }
        dispatcher.close()
        val durationInMillis = duration / 1000000
        println("Execution took $durationInMillis milliseconds")

        log("Writing result to files")
        writeMatrixToFile(MF, "output/2", "MF")
        writeMatrixToFile(E, "output/2", "E")
    }

    // MF=MD*(ME+MM)-ME*MM
    private fun calculateMF(mutexMF: Mutex) = runBlocking {
        val jobs = mutableListOf<Job>()
        jobs.add(
            launch(dispatcher) {
                log("Calculating MD*(ME+MM)")
                val productMDsumMEMM = MD * (ME + MM)
                log("Calculation of MD*(ME+MM) is done")
                mutexMF.withLock { MF += productMDsumMEMM }
            }
        )
        jobs.add(
            launch(dispatcher) {
                log("Calculating ME*MM ...")
                val productMEMM = ME * MM
                log("Calculation of ME*MM is done")
                mutexMF.withLock { MF -= productMEMM }
            }
        )
        jobs.joinAll()
    }

    // E=В*МE+D*max(MM)
    private fun calculateE(mutexE: Mutex) = runBlocking {
        val jobs = mutableListOf<Job>()
        jobs.add(
            launch(dispatcher) {
                log("Calculating B*ME")
                val productBME = B * ME
                log("Calculation of B*ME is done")
                mutexE.withLock { E += productBME }
            }
        )
        jobs.add(
            launch(dispatcher) {
                log("Calculating D*max(MM) ...")
                val productDmaxMM = D * MM.max()
                log("Calculation of D*max(MM) is done")
                mutexE.withLock { E += productDmaxMM }
            }
        )
        jobs.joinAll()
    }
}
