package Testing

import FloatingPointDesigns.FPArithmetic.FP_Comparitor
import IEEEConversions.FPConvert.{convert_long_to_float, convert_string_to_IEEE_754}
import NeuralNetworkEngine.NeuralNetwork.CNNEngine
import NeuralNetworkSWModel.SWModel.compute
import Testing.ConvolutionCircuit.bw
import chisel3.getVerilogString
import chisel3.tester.testableData
import chiseltest.RawTester.test

import java.io.PrintWriter

object NeuralCircuit {
  def main(args: Array[String]):Unit= {
    val bw = 16

    test(new CNNEngine(10,10,3,3,bw)){c =>
      println("\n ----------HW Results---------\n")
      println(s"Digit: ${c.io.Digit.peek().litValue}")
      // 64- convolutional 16- pooling layer
      for(i <- 0 until 64)
        println(s"Controller: ${convert_long_to_float(c.io.ConOut(i).peek().litValue, bw)}")
    }

    println("\n -----------SW Results----------\n")
    compute(10,3)
//    println("[{(Generating Verilog)}")
//    val pw = new PrintWriter("FP_Comparitor_32.v")
//    pw.println(getVerilogString(new FP_Comparitor(32)))
//    pw.close()

    println("[{(Generating Verilog)}")
    val pw = new PrintWriter("CNNEngine_16.v")
    pw.println(getVerilogString(new CNNEngine(10, 10,3,3,bw)))
    pw.close()
  }
}
