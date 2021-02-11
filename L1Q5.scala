// Lab 1 Question 3
import io.threadcso._

class insertionSort {
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
    // Will use n comparators with longest path of log n
    private def insert(ins: List[?[Int]], in: ?[Int], outs: List[![Int]]): PROC = {
        val n = ins.length; require(n >= 1 && outs.length == n+1)
        if (n > 2) {
            // split input and outputs in half
            val halfway = n/2
            val (firstIns, in0::secondIns) = ins.splitAt(halfway)
            val (firstOuts, secondOuts) = outs.splitAt(halfway + 1)
            // new channel for inter comparator communication
            val CC1, CC2 = OneOne[Int]
            val currentComparator = comparator(in, in0, CC1, CC2)
            // return the first comparator parallelised with the other two parts of the circuit (with correct connections)
            currentComparator || (insert(firstIns, CC1, firstOuts)) || (insert(secondIns, CC2, secondOuts))
        }
        // in base case of two in ins channel return appropriate comparator circuit
        else if (n == 2) {
            // channel for inter comparator communication
            val CC = OneOne[Int]
            val comparator1 = comparator(in, ins(0), outs(0), CC)
            val comparator2 = comparator(CC, ins(1), outs(1), outs(2))
            comparator1 || comparator2
        }
        // in base case of one in ins channel just return the comparator of the inputs
        else comparator(ins(0), in, outs(0), outs(1))
    }

    // Insertion sort
    def insertionSort(ins: List [?[ Int ]], outs: List [![ Int ]]): PROC = {
        val n = ins.length; require(n >= 2 && outs.length == n)
        val in0::otherIns = ins
        if (n > 2) {
            // channles to communicate between insertion sort and insert
            val iSC = List.fill(n - 1)(OneOne[Int])
            insertionSort(otherIns, iSC) || insert(iSC, in0, outs)
        }
        else comparator(ins(0), ins(1), outs(0), outs(1))
    }
}

import scala.util.Random

object newInsertionSortTest {
    // Range of input values
    val N = 10; val Max = 100

    def doTest = {
        // create a sorted array to insert into
        val xs = Array.fill(N)(Random.nextInt(Max))
        val ys = new Array[Int](N)
        val ins, outs = List.fill(N)(OneOne[Int])
        val s = new insertionSort
        def sender = proc {
            for(i <- 0 until N) {
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
        run(sender || s.insertionSort(ins, outs) || receiver)
        assert(xs.sorted.sameElements(ys))
    }

    def main(args : Array[String]) = {
        for(i <- 0 until 1000){ doTest; if(i%10 == 0) print(".") }
        println; exit
    }
}