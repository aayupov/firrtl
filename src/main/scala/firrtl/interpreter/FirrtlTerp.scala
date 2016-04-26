/*
Copyright (c) 2014 - 2016 The Regents of the University of
California (Regents). All Rights Reserved.  Redistribution and use in
source and binary forms, with or without modification, are permitted
provided that the following conditions are met:
   * Redistributions of source code must retain the above
     copyright notice, this list of conditions and the following
     two paragraphs of disclaimer.
   * Redistributions in binary form must reproduce the above
     copyright notice, this list of conditions and the following
     two paragraphs of disclaimer in the documentation and/or other materials
     provided with the distribution.
   * Neither the name of the Regents nor the names of its contributors
     may be used to endorse or promote products derived from this
     software without specific prior written permission.
IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF
ANY, PROVIDED HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION
TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
MODIFICATIONS.
*/

package firrtl.interpreter

import java.io.File

import firrtl._

// TODO: Support stop and print
// TODO: Support reset on regs
// TODO: Support Memory
// TODO: Add poison concept
// TODO: Make into separate repo
// TODO: Support Multiple modules
// TODO: Work through more operators
// TODO: Figure out what to do about clock and reset inputs
// TODO: Implement VCD parser and emitter (https://github.com/impedimentToProgress/ProcessVCD.git)?
// TODO: Get official Firrtl to LoFirrtl transformer

class FirrtlTerp(ast: Circuit) {
  var lastStopResult: Option[Int] = None
  def stopped: Boolean = lastStopResult.nonEmpty
  def stopResult = lastStopResult.get

  val lowered_ast = ToLoFirrtl.lower(ast)
  println("LoFirrtl" + "="*120)
  println(lowered_ast.serialize)
  println(s"ast $lowered_ast")

  val interpreterCircuit = new InterpreterCircuit(lowered_ast)

  var inputUpdater: InputUpdater = new RandomInputUpdater(interpreterCircuit)
  def setInputUpdater(newInputUpdater: InputUpdater): Unit = {
    inputUpdater = newInputUpdater
  }
  var sourceState = CircuitState(interpreterCircuit)

  def updateInputs(): Unit = {
    inputUpdater.updateInputs(sourceState)
  }

  def doOneCycle() = {
    updateInputs()

    val evaluator = new LoFirrtlExpressionEvaluator(
      startKeys = interpreterCircuit.dependencyGraph.keys,
      dependencyGraph = interpreterCircuit.dependencyGraph,
      circuitState = sourceState
    )
    evaluator.resolveDependencies()
    lastStopResult = evaluator.checkStops()
    println(s"After cycle ${"-"*80}\n${sourceState.prettyString()}")

    sourceState = sourceState.getNextState
  }

  def doCycles(n: Int): Unit = {
    println(s"Initial state ${"-"*80}\n${sourceState.prettyString()}")

    for(cycle <- 1 to n) {
      println(s"Cycle $cycle ${"-"*80}")
      doOneCycle()
      if(stopped) return
    }
  }

}

object FirrtlTerp {
  def apply(input: String): FirrtlTerp = {
    val ast = Parser.parse("", input.split("\n").toIterator)
    new FirrtlTerp(ast)
  }

  def main(args: Array[String]) {

    val input = if(args.isEmpty) {
      println("Usage: FirrtlTerp file_name")
      """circuit Test :
        |  module Test :
        |    input clk : Clock
        |    input a : UInt<1>
        |    input b : UInt<1>
        |    input select : UInt<1>
        |    output c : UInt<2>
        |    reg w : UInt<1>, clk
        |    reg x : UInt<1>, clk
        |    reg y : UInt<1>, clk
        |
        |    w <= a
        |    x <= w
        |    y <= x
        |    c <= y
      """.stripMargin
    }
    else io.Source.fromFile(args.head).mkString

//    if()
//    val input =
//      """circuit Test :
//        |  module Test :
//        |    input clk : Clock
//        |    input a : UInt<1>
//        |    input b : UInt<1>
//        |    input select : UInt<1>
//        |    output c : UInt<2>
//        |    reg w : UInt<1>, clk
//        |    reg x : UInt<1>, clk
//        |    reg y : UInt<1>, clk
//        |
//        |    w <= a
//        |    x <= w
//        |    y <= x
//        |    c <= y
//      """.stripMargin
//    val input =
//    """circuit Test :
//      |  module Test :
//      |    input clk : Clock
//      |    input a : UInt<1>
//      |    input b : UInt<1>
//      |    input select : UInt<1>
//      |    output c : UInt<1>
//      |    c <= mux(select, a, b)
//    """.stripMargin
//    val input =
//      """circuit Test :
//        |  module Test :
//        |    input clk : Clock
//        |    input a : UInt<1>
//        |    input b : UInt<1>
//        |    input select : UInt<1>
//        |    output c : UInt<1>
//        |    mem m :
//        |      data-type => { a : UInt<8>, b : UInt<8>}[2]
//        |      depth => 32
//        |      read-latency => 0
//        |      write-latency => 1
//        |      reader => read
//        |      writer => write
//        |    m.read.clk <= clk
//        |    m.read.en <= UInt<1>(1)
//        |    m.read.addr is invalid
//        |    node x = m.read.data
//        |    node y = m.read.data[0].b
//        |
//        |    m.write.clk <= clk
//        |    m.write.en <= UInt<1>(0)
//        |    m.write.mask is invalid
//        |    m.write.addr is invalid
//        |    wire w : { a : UInt<8>, b : UInt<8>}[2]
//        |    w[0].a <= UInt<4>(2)
//        |    w[0].b <= UInt<4>(3)
//        |    w[1].a <= UInt<4>(4)
//        |    w[1].b <= UInt<4>(5)
//        |    m.write.data <= w
//        |    c <= a
//      """.stripMargin

    val interpreter = FirrtlTerp(input)

    val inputUpdater = new MappedInputUpdater(interpreter.interpreterCircuit) {
      override def step_values: Array[Map[String, BigInt]] = Array(
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 1),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0),
        Map("io_a" -> 6, "io_b" -> 2, "io_e" -> 0)
      )
    }

//    interpreter.setInputUpdater(inputUpdater)

    interpreter.doCycles(6)
  }
}
