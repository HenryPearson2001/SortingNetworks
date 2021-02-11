// Lab 1 Question 2

import io.threadcso._

class sorter {

    // A single comparator, inputting on in0 and in1, and outputting on out0 (smaller value) and out1 (larger value).
    private def comparator(in0: ?[Int], in1: ?[Int], out0: ![Int], out1: ![Int]): PROC = proc {
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

    def sort4(ins: List [?[ Int ]], outs: List [![ Int ]]): PROC = {
        require(ins.length == 4 && outs.length == 4)
        // create the channels for communicating between comparators. CC{position}{version} is the naming convention here
        // where position is which of the four line on the diagram it is
        val CC0, CC10, CC11, CC20, CC21, CC3 = OneOne[Int]
        // create the comparators (using the specified structure)
        val comparator1 = comparator(ins(0), ins(2), CC0, CC20)
        val comparator2 = comparator(ins(1), ins(3), CC10, CC3)
        val comparator3 = comparator(CC0, CC10, outs(0), CC11)
        val comparator4 = comparator(CC20, CC3, CC21, outs(3))
        val comparator5 = comparator(CC11, CC21, outs(1), outs(2))
        // return the process of the concurrent comparators
        (comparator1 || comparator2 || comparator3 || comparator4 || comparator5)
    }
}

import scala.util.Random

object sort4Test {
    // Range of input values
    val Max = 100

    def doTest = {
        val xs = Array.fill(4)(Random.nextInt(Max))
        val ys = new Array[Int](4)
        val ins, outs = List.fill(4)(OneOne[Int])
        val s = new sorter
        def sender = proc {
            for(i <- 0 until 4) {
                ins(i)!(xs(i))
                ins(i).close
            }
        }
        def receiver = proc {
            var i = 0
            for(i <- 0 until 4) {
                ys(i) = outs(i)?()
                outs(i).close
            }
        }
        run(sender || s.sort4(ins, outs) || receiver)
        assert(xs.sorted.sameElements(ys))
    }

    def main(args : Array[String]) = {
        for(i <- 0 until 1000){ doTest; if(i%10 == 0) print(".") }
        println; exit
    }
}