fun main() {
    val dimension = 400
//    log("Starting variant with local variables")
//    Lab1Local().main(dimension)
//    log("Starting variant with global variables and locks")
//    Lab1Global(dimension).main()
    log("Starting variant with java.util.concurrent.lock")
    Lab2(dimension).main()
}
