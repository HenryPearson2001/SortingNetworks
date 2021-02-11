# SortingNetworks
University lab task | building concurrent sorting networks using a comparator which takes two input ports and two output ports and outputs the larger of the two inputs in the second output and the smaller in the first output

Tasks:

Concurrent Programming Practical 1: Sorting Networks
This practical builds some networks to sort a list of numbers. The net- works are not very sensible as software sorting networks; however, they could be implemented efficiently in hardware. The main aims of the practical are to help you get used to thinking about concurrent systems, and to gain fa- miliarity with the CSO library.
Deadline: practical sessions in Week 3. Your answer should be in the form of well-commented code, answering the questions below. You can gain a mark of S by giving decent answers to all the non-optional questions; you can gain a mark of S+ by giving good answers to all the questions up to Question 5. The section marked “Just for fun” is just that.
Each sorting network will be built from comparators: simple two-element sorting components. A comparator can be pictured as below:
 x x
0
1
in0 out0 in1 out1
y0 =min(x0,x1) y1 =max(x0,x1)
  comparator
  The comparator has two input channels, in0 and in1, and two output chan- nels, out0 and out1. If it inputs x0 and x1, it outputs their minimum on out0 and their maximum on out1.
Question 1
Implement a comparator with the following signature:
/∗∗ A single comparator, inputting on in0 and in1, and outputting on out0 ∗ (smaller value) and out1 (larger value). ∗/
def comparator(in0: ?[Int], in1: ?[Int], out0: ![Int], out1: ![Int]): PROC
the process should be willing to perform the inputs in either order, and perform the outputs in either order. The process should repeat this behaviour until one of its channels is closed.
Below is a sorting circuit for four inputs using five comparators.
x0 • • y0 x1 • • • y1 x2 • • • y2 x3 •• y3
1
      
The first four comparators direct the smallest and largest values to the top and bottom outputs; the final comparator sorts out the middle two values. Note that the first two comparators can run concurrently, as can the second pair: the longest path involves three comparators.
Question 2
Implement this sorting circuit, using the following signature.1
/∗∗ A sorting network for four values. ∗/
def sort4(ins: List [?[ Int ]], outs: List [![ Int ]]): PROC = {
require(ins.length == 4 && outs.length == 4) ...
}
Test your implementation using the following idea: pick four random Ints2 and send them in on the input channels; receive the outputs and check that they are a sorted3 version of the inputs; repeat many times.
We will now implement a sorting network based on the idea of insertion sort.
Question 3
We want to implement a circuit to insert a value into a sorted list of n ≥ 1 values, with the following signature.
/∗∗ Insert a value input on in into a sorted sequence input on ins.
∗ Pre: ins.length = n && outs.length = n+1, for some n >= 1.
∗ If the values xs input on ins are sorted, and x is input on in, then a ∗ sorted permutation of x::xs is output on ys. ∗/
def insert(ins: List[?[Int]], in: ?[Int], outs: List[![Int]]): PROC = { val n = ins.length; require(n >= 1 && outs.length == n+1)
...
}
Consider the circuit below to implement insert (for n ≥ 2). The box labelled “insertn−1” is (recursively) a circuit to insert the output of the comparator into the sorted list ⟨ins(1), . . . , ins(n-1)⟩ of length n − 1.
1The class List represents lists. If you haven’t used this class before, you might want to look at the API documentation, or a relevant on-line tutorial.
2e.g. using List.fill(4)(scala.util.Random.nextInt(100)).
3You might want to use the sorted method of the List class.
2
 
in
•   outs(0)
 ins(0) •
ins(1)
outs(1) outs(2)
  insertn−1
... ...
ins(n-1) outs(n-1)
Study the circuit and persuade yourself that it indeed implements the re- quirements. Then implement insert based upon the circuit. You will also need a base case. Test the circuit.
Question 4
Optional: The circuit from the previous question had a path containing O(n) comparators. Design, implement and test a circuit for insert such that the longest path has length O(log n).
Question 5
Use your function from either Question 3 or 4 to implement insertion sort. Use the following signature.
/∗∗ Insertion sort. ∗/
def insertionSort(ins: List [?[ Int ]], outs: List [![ Int ]]): PROC = {
val n = ins.length; require(n >= 2 && outs.length == n) ...
}
You should base your implementation on the following cicuit, where the sub- circuit iSortn−1 recursively sorts n − 1 inputs.
ins(0) outs(0)
ins(1) outs(1)
... ...
ins(n-1) outs(n-1)
Test your implementation using the ideas from Question 2. 3
     iSortn−1
...
insertn−1
 
