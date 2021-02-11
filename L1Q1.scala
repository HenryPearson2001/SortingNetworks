// Lab 1 Question 1

import io.threadcso._

class Comparator {

    // A single comparator, inputting on in0 and in1, and outputting on out0 (smaller value) and out1 (larger value).
    def comparator(in0: ?[Int], in1: ?[Int], out0: ![Int], out1: ![Int]): PROC = proc {
    // keep receiving inputs until one of the channels closes
        repeat {
            // receive inputs
            var input0 = 0
            var input1 = 0
            def getInput0 = proc { input0 = in0?() }
            def getInput1 = proc { input1 = in1?() }
            run (getInput0 || getInput1)
            // output the largest and smallest inputs along the correct channels
            if (input0 < input1) {
                def sendOutput0 = proc { out0!(input0) }
                def sendOutput1 = proc { out1!(input1) }
                run (sendOutput0 || sendOutput1)
            }
            else {
                def sendOutput0 = proc { out0!(input1) }
                def sendOutput1 = proc { out1!(input0) }
                run (sendOutput0 || sendOutput1)
            }
        }
        // close the channels
        in0.closeIn
        in1.closeIn
        out0.closeOut
        out1.closeOut
    }
}

import scala.util.Random
import scala.math.{max, min}

object ComparatorTest {
    // number of inputs passed into the comparator
    private val N = 100
    // max value of these inputs
    private val Max = 100

    // takes two arrays as inputs and returns the arrays of min and max corresponding elements
    private def minMaxArray(array0: Array[Int], array1: Array[Int]): (Array[Int], Array[Int]) = {
        // create the output arrays
        val minArray = new Array[Int](N)
        val maxArray = new Array[Int](N)
        for (i <- 0 until N) {
            // add the min and max elements to appropriate arrays
            minArray(i) = min(array0(i), array1(i))
            maxArray(i) = max(array0(i), array1(i))
        }
        (minArray, maxArray)
    }

    // performs a test of one comparator unit - passes N comparisons to the unit
    private def doTest = {
        // create the input and output arrays for the comparators
        val inputs0 = Array.fill(N)(Random.nextInt(Max))
        val inputs1 = Array.fill(N)(Random.nextInt(Max))
        val outputs0 = new Array[Int](N)
        val outputs1 = new Array[Int](N)
        val comparator = new Comparator
        // create input and output channels
        var in0, in1, out0, out1 = OneOne[Int]
        // process to send the inputs
        def sender = proc {
            for (i <- 0 until N) {
                in0!(inputs0(i))
                in1!(inputs1(i))
            }
            in0.close
            in1.close
        }
        // process to receive the output
        def receiver = proc {
            for (i <- 0 until N) {
                outputs0(i) = out0?()
                outputs1(i) = out1?()
            }
            out0.close
            out1.close
        }
        run(sender || comparator.comparator(in0, in1, out0, out1) || receiver)
        // check the outputs are correct
        val (minArray, maxArray) = minMaxArray(inputs0, inputs1)
        assert(minArray.sameElements(outputs0))
        assert(maxArray.sameElements(outputs1))
    }

    def main(args : Array[String]) = {
        // perform the test 1000 times
        for(i <- 0 until 1000){ doTest; if(i%10 == 0) print(".") }
        println; exit
    }
}