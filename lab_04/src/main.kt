import kotlin.math.abs
import kotlin.math.pow
import org.math.plot.*
import javax.swing.*
import org.math.plot.Plot2DPanel

class Parameters()
{
    val a1 = 0.0134
    val b1 = 1.0
    val c1 = 4.35e-4
    val m1 = 1.0
    val a2 = 2.049
    val b2 = 0.563e-3
    val c2 = 0.528e5
    val m2 = 1.0
    val alphaZero = 0.05
    val alphaN = 1e-2
    val l = 10.0
    val tZero = 300.0
    val r = 5e-1
    val fZero = 50.0
    val h = 1e-3
    val t = 1.0
    val epsilon = 1e-2
}

val parameters = Parameters()

fun plusApprox(function: (Double) -> Double, n: Double, step: Double): Double
{
    return (function(n) + function(n + step)) / 2
}

fun minusApprox(function: (Double) -> Double, n: Double, step: Double): Double
{
    return (function(n) + function(n - step)) / 2
}

val kFun = { x: Double -> parameters.a1 * (parameters.b1 + parameters.c1 * parameters.m1.pow(x)) }

val cFun = { x: Double -> parameters.a2 + parameters.b2 * x.pow(parameters.m2) - (parameters.c2 / x.pow(2)) }

fun alphaFun(x: Double): Double
{
    val s1 = (parameters.alphaN * parameters.l) / (parameters.alphaN - parameters.alphaZero)
    val s2 = -parameters.alphaZero * s1
    return s2 / (x - s1)
}

val pFun = { x: Double -> alphaFun(x) * 2 / parameters.r }

val fFun = { x: Double -> alphaFun(x) * 2 * parameters.tZero / parameters.r }

val aAFun = { x: Double -> parameters.t / parameters.h * minusApprox(kFun, x, parameters.t) }

val dDFun = { x: Double -> parameters.t / parameters.h * plusApprox(kFun, x, parameters.t) }

val bBFun =
    { x: Double, t: Double -> aAFun(t) + dDFun(t) + parameters.h * cFun(t) + parameters.h * parameters.t * pFun(x) }

val fFFun = { x: Double, t: Double -> parameters.h * parameters.t * fFun(x) + t * parameters.h * cFun(t) }

fun leftexitConditions(tList: MutableList<Double>): Triple<Double, Double, Double>
{
    val c = plusApprox(cFun, tList[0], parameters.t)
    val k = plusApprox(kFun, tList[0], parameters.t)

    val kZero =
        parameters.h / 8 * c + parameters.h / 4 * cFun(tList[0]) +
                parameters.t / parameters.h * k + parameters.t * parameters.h / 8 * pFun(
            parameters.h / 2
        ) + parameters.t * parameters.h / 4 * pFun(0.0)

    val mZero =
        parameters.h / 8 * c - parameters.t / parameters.h * k +
                parameters.t * parameters.h / 8 * pFun(parameters.h / 2)

    val pZero =
        parameters.h / 8 * c * (tList.first() + tList[1]) +
                parameters.h / 4 * cFun(tList.first()) * tList.first() +
                parameters.fZero * parameters.t + parameters.t * parameters.h / 8 * (3 * fFun(
            0.0
        ) + fFun(parameters.h))

    return Triple(kZero, mZero, pZero)
}

fun rightexitConditions(tList: MutableList<Double>): Triple<Double, Double, Double>
{
    val c = minusApprox(cFun, tList.last(), parameters.t)
    val k = minusApprox(kFun, tList.last(), parameters.t)

    val kN =
        parameters.h / 8 * c + parameters.h / 4 * cFun(tList.last()) +
                parameters.t / parameters.h * k +
                parameters.t * parameters.alphaN +
                parameters.t * parameters.h / 8 * pFun(
            parameters.l - parameters.h / 2
        ) + parameters.t * parameters.h / 4 * pFun(parameters.l)

    val mN =
        parameters.h / 8 * c - parameters.t / parameters.h * k +
                parameters.t * parameters.h / 8 * pFun(parameters.l - parameters.h / 2)

    val pN =
        parameters.h / 8 * c * (tList.last() + tList[tList.size - 2]) +
                parameters.h / 4 * cFun(tList.last()) * tList.last() +
                parameters.t * parameters.alphaN * parameters.tZero +
                parameters.t * parameters.h / 4 * (fFun(
            parameters.l
        ) + fFun(parameters.l - parameters.h / 2))

    return Triple(kN, mN, pN)
}

fun formNewTList(list: MutableList<Double>): MutableList<Double>
{
    val zeroTriple = leftexitConditions(list)
    val nTriple = rightexitConditions(list)

    val xiList: MutableList<Double> = mutableListOf(0.0, -zeroTriple.second / zeroTriple.first)
    val etaList: MutableList<Double> = mutableListOf(0.0, zeroTriple.third / zeroTriple.first)

    var curX = parameters.h
    var curN = 1

    while (curX + parameters.h < parameters.l)
    {
        val curT = list[curN]
        val dm = bBFun(curX, curT) - aAFun(curT) * xiList[curN]

        xiList.add(dDFun(curT) / dm)
        etaList.add((fFFun(curX, curT) + aAFun(curT) * etaList[curN]) / dm)

        curX += parameters.h
        curN++
    }

    val outT = mutableListOf<Double>()
    for (i in 0..curN)
        outT.add(0.0)

    outT[curN] =
        (nTriple.third - nTriple.second * etaList[curN]) / (nTriple.first + nTriple.second * xiList[curN])

    for (i in curN - 1 downTo 0)
        outT[i] = xiList[i + 1] * outT[i + 1] + etaList[i + 1]

    return outT
}

fun simpleIteration(): Pair<MutableList<MutableList<Double>>, Double>
{
    var tList = mutableListOf<Double>()
    var newTList = mutableListOf<Double>()

    for (i in 0..(parameters.l / parameters.h).toInt())
    {
        tList.add(parameters.tZero)
        newTList.add(0.0)
    }

    val outList = mutableListOf(tList)

    var curT = 0.0
    var exitCondition = true
    while (exitCondition)
    {
        var tempList = tList
        var max = 1.0

        while (max >= 1)
        {
            newTList = formNewTList(tempList)
            max = abs((tList.first() - newTList.first()) / newTList.first())

            for (ind in tList.indices)
            {
                if (abs((tList[ind] - newTList[ind]) / newTList[ind]) > max)
                    max = abs((tList[ind] - newTList[ind]) / newTList[ind])
            }

            tempList = newTList
        }

        outList.add(newTList)
        curT += parameters.t

        exitCondition = false
        for (ind in tList.indices)
        {
            if (abs(tList[ind] - newTList[ind]) / newTList[ind] > parameters.epsilon)
            {
                exitCondition = true
                break
            }
        }

        tList = newTList
    }

    return Pair(outList, curT)
}

fun main()
{
    val out = simpleIteration()

    val xList = mutableListOf<Double>()
    var i = 0.0
    while (i < parameters.l)
    {
        xList.add(i)
        i += parameters.h
    }

    val plot = Plot2DPanel()
    for (curY in out.first.indices)
    {
        if (curY % 2 == 0)
            plot.addLinePlot(curY.toString(), xList.toDoubleArray(), out.first[curY].toDoubleArray())
    }

    val frame = JFrame("Plots in Kotlin oh")
    plot.addLegend("SOUTH")
    plot.setAxisLabel(0, "x")
    plot.setAxisLabel(1, "T")
    frame.setSize(1000, 1000)
    frame.contentPane = plot
    frame.isVisible = true

    val secList = mutableListOf<Double>()
    i = 0.0
    while (i < out.second && secList.size != out.first.size)
    {
        secList.add(i)
        i += parameters.t
    }

    val sPlot = Plot2DPanel()
    var k = 0.0
    while (k < parameters.l / 3)
    {
        val curList = mutableListOf<Double>()
        for (curF in out.first)
            curList.add((curF[(k / parameters.h).toInt()]))
        sPlot.addLinePlot(k.toString(), secList.toDoubleArray(), curList.toDoubleArray())

        k += 0.1
    }
    sPlot.addLegend("SOUTH")
    val newFrame = JFrame("Second plot")
    newFrame.setSize(1000, 1000)
    newFrame.contentPane = sPlot
    newFrame.isVisible = true

//    val x = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
//    val y = doubleArrayOf(45.0, 89.0, 6.0, 32.0, 63.0, 12.0)
//    val y2 = doubleArrayOf(33.0, 666.0, 66.0, 66.0, 66.0, 0.0)
//
//    val plot = Plot2DPanel()
//    plot.addLegend("SOUTH")
//
//    plot.addLinePlot("my plot", x, y)
//    plot.addLinePlot("shit", x, y2)
//
//    val frame = JFrame("a plot panel")
//    frame.setSize(600, 600)
//    frame.contentPane = plot
//    frame.isVisible = true

}