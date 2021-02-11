// Lab 1 Question 3
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

    // Insert a value input on in into a sorted sequence input on ins.
    // Pre: ins.length = n && outs.length = n+1, for some n >= 1.
    // If the values xs input on ins are sorted, and x is input on in,
    // then a sorted permutation of x::xs is output on ys.
    def insert(ins: List[?[Int]], in: ?[Int], outs: List[![Int]]): PROC = {
        val n = ins.length; require(n >= 1 && outs.length == n+1)
        // split top input/output channel and other inputs/outputs
        val in0::otherIns = ins
        val out0::otherOuts = outs
        if (otherIns.length != 0) {
            // new channel for inter comparator communication - CC1 is for line 1 in circuit and CC
            val CC1 = OneOne[Int]
            val firstComparator = comparator(in, in0, out0, CC1)
            // return the first comparator parallelised with the rest of the circuit (with correct connections)
            firstComparator || (insert(otherIns, CC1, otherOuts))
        }
        else comparator(in, in0, out0, otherOuts(0))
    }
}

import scala.util.Random

object insertSortTest {
    // Range of input values
    val N = 100; val Max = 100

    def doTest = {
        // create a sorted array to insert into
        val xs = Array.fill(N - 1)(Random.nextInt(Max)).sorted
        // value getting inserted
        val insertValue = Random.nextInt(Max)
        val ys = new Array[Int](N)
        val in::ins, outs = List.fill(N)(OneOne[Int])
        val s = new sorter
        def sender = proc {
            in!(insertValue)
            in.close
            for(i <- 0 until N - 1) {
                ins(i)!(xs(i))
                ins(i).close
            }
        }
        def receiver = proc {
            var i = 0
            for(i <- 0 until N) {
                ys(i) = outs(i)?()
                outs(i).close
            }
        }
        run(sender || s.insert(ins, in, outs) || receiver)
        // what the array should look like after the insert
        val zs = xs ++ Array(insertValue)
        assert(zs.sorted.sameElements(ys))
    }

    def main(args : Array[String]) = {
        for(i <- 0 until 1000){ doTest; if(i%10 == 0) print(".") }
        println; exit
    }
}