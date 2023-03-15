fun main() {
    val dimension = 200
    log("Starting variant with local variables")
    Lab1Local().main(dimension)
    log("Starting variant with global variables and locks")
    Lab1Global(dimension).main()
}
