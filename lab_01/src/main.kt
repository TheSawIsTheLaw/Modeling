import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.round

const val xF = 10
const val picF = 120
const val picFfo = picF / 4 - 1
const val eulF = 40
const val rkF = 40

fun firstApprox(x: Double) : Double
{
    return x * x * x / 3
}

fun secondApprox(x: Double) : Double
{
    return firstApprox(x) + x.pow(7) / 63
}

fun thirdApprox(x: Double) : Double
{
    return secondApprox(x) + x.pow(11) * 2 / 2079 + x.pow(15) / 59535
}

fun fourthApprox(x: Double) : Double
{
    return secondApprox(x) + 2 * x.pow(11) / 2079 + 13 * x.pow(15) / 218295 + 82 * x.pow(19) / 37328445 +
            662 * x.pow(23) / 10438212015 + 4 * x.pow(27) / 3341878155 +
            x.pow(31) / 109876902975
}

fun picardMet(xStart: Double, step: Double, numOfIters: Int) : MutableList<MutableList<Double>>
{
    val outList: MutableList<MutableList<Double>> = mutableListOf()

    var curX = xStart

    outList.add(mutableListOf(curX, firstApprox(curX), secondApprox(curX), thirdApprox(curX), fourthApprox(curX)))

    for (i in 1..numOfIters)
    {
        curX += step
        outList.add(mutableListOf(curX, firstApprox(curX), secondApprox(curX), thirdApprox(curX), fourthApprox(curX)))
    }

    return outList
}

fun curMathFun(x: Double, y: Double) : Double { return x * x + y * y}

fun eulerMet(xStart: Double, yStart: Double, step: Double, numOfIters: Int) : MutableList<Double>
{
    val outList: MutableList<Double> = mutableListOf()

    var curX = xStart
    var curY = yStart

    outList.add(curY)
    for (i in 1..numOfIters)
    {
        curY += step * curMathFun(curX, curY)

        outList.add(curY)

        curX += step
    }

    return outList
}

fun rungeKutMet(xStart: Double, yStart: Double, alpha: Double, step: Double, numOfIters: Int) : MutableList<Double>
{
    val outList: MutableList<Double> = mutableListOf()

    var curX = xStart
    var curY = yStart

    outList.add(curY)
    for (i in 1..numOfIters)
    {
        curY += step * ((1 - alpha) * curMathFun(curX, curY) +
                alpha * curMathFun(curX + step / (2 * alpha),curY + step * curMathFun(curX, curY) / (2 * alpha)))

        outList.add(curY)

        curX += step
    }

    return outList
}

fun printHead()
{
    print(String.format("|%-${xF}s|%-${picF - 1}s|%-${eulF}s|%-${rkF}s|\n", "", "", "", "").replace(' ', '-'))
    print(String.format("|%-${xF}s|%-${picF - 1}s|%-${eulF}s|%-${rkF}s|\n", "x", "Метод Пикара", "Метод Эйлера", "Метод Рунге-Кутта 2-го порядка точности"))

    print(String.format("|%${xF}s|", " "))
    for (i in 1..4)
        print(String.format("%-${picFfo}s|", "").replace(' ', '-'))
    println(String.format("%${eulF}s|%${rkF}s|", "", ""))

    print(String.format("|%${xF}s|", " "))
    for (i in 1..4)
        print(String.format("%-${picFfo}s|", "Приближение $i"))
    print(String.format("%${eulF}s|%${rkF}s|\n", "", ""))

    print(String.format("|%${xF}s|", " ").replace(' ', '-'))
    for (i in 1..4)
        print(String.format("%-${picFfo}s|", "").replace(' ', '-'))
    println(String.format("%${eulF}s|%${rkF}s|", "", "").replace(' ', '-'))
}

fun printAnswers(picAnsw: MutableList<MutableList<Double>>, eulAnsw: MutableList<Double>, runAnsw: MutableList<Double>)
{
    for (i in 0 until picAnsw.size step 100)
    {
        val curPic = picAnsw[i]
        println(
            String.format(
                "|%-${xF}.2f|%-${picFfo}.2f|%${picFfo}.2f|%${picFfo}.2f|%${picFfo}.2f|%${eulF}.2f|%${rkF}.2f|",
                curPic[0], curPic[1], curPic[2], curPic[3], curPic[4], eulAnsw[i], runAnsw[i]
            )
        )
        print(String.format("|%${xF}s|", " ").replace(' ', '-'))
        for (i in 1..4)
            print(String.format("%-${picFfo}s|", "").replace(' ', '-'))
        println(String.format("%${eulF}s|%${rkF}s|", "", "").replace(' ', '-'))
    }
}

fun main()
{
    val xStart = 0.0
    val xStop = 2.3
    val yStart = 0.0
    val step = 10e-5

    val iters = ceil(abs(xStop - xStart) / step).toInt()

    val picAnsw = picardMet(xStart, step, iters)
    val eulAnsw = eulerMet(xStart, yStart, step, iters)
    val runAnsw = rungeKutMet(xStart, yStart, 0.5, step, iters)

    printHead()
    printAnswers(picAnsw, eulAnsw, runAnsw)
}