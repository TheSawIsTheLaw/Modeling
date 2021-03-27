// Linear interpolation. Works only for (sort #'> table)
// Returns null/not null values so check it out properly
fun linearInterpolation(table: List<Pair<Double, Double>>, findX: Double): Double?
{
    if (findX > table.last().first)
        return null

    var firstDot: Pair<Double, Double>? = null
    var secondDot: Pair<Double, Double>? = null

    var curPairInd: Int = 0
    while ((curPairInd < table.size - 1) && (firstDot == null))
    {
        if (table[curPairInd].first < findX)
        {
            firstDot = table[curPairInd]
            secondDot = table[curPairInd + 1]
        }
        curPairInd++
    }

    return firstDot!!.second +
            (secondDot!!.second - firstDot.second) / (secondDot.first - firstDot.first) * (findX - firstDot.first)
}

fun main()
{
    var testTable: List<Pair<Double, Double>> = listOf(Pair(2.0, 5.0), Pair(5.0, 11.0))
    println("Found interpolated value for x = 3 is: ${linearInterpolation(testTable, 3.0)}")

    testTable = listOf(Pair(2.0, 4.0), Pair(5.0, 25.0))
    println("Found interpolated value for x = 3 is: ${linearInterpolation(testTable, 3.0)}")
}