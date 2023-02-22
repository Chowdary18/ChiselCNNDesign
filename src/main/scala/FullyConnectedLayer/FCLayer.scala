package FullyConnectedLayer
import FloatingPointDesigns.FPArithmetic.{FP_adder, FP_multiply_sum}
import chisel3._
object FCLayer extends App{

  class FC (bw: Int, MatARows: Int, MatACol: Int, MatBRows: Int, MatBCol: Int) extends Module {
    require(MatACol == MatBRows)
    // Mat A will the weigths and Mat B will be the ouput of Pooling Layer.
    // The Output of Fully Connected Layer strictly should be a 10 elements only for digit recognition.
    // Mat A = 10 * x  and Mat B = x * 1; x can be any value.
    val io = IO(new Bundle {
      val matA = Input(Vec(MatACol*MatARows, UInt(bw.W)))
      val matB = Input(Vec(MatBCol*MatBRows, UInt(bw.W)))
      val biasParameter = Input(Vec(MatARows*MatBCol, UInt(bw.W)))
      val fC_out = Output(Vec(MatARows * MatBCol, UInt(bw.W)))
    })
    for (i <- 0 until MatARows*MatBCol) {
      val mul = Module(new FP_multiply_sum(MatACol,bw))
      for (j <- 0 until MatACol) {
        mul.io.in_a(j) := io.matA(j + (i * MatACol))
        mul.io.in_b(j) := io.matB(j)
      }
      val fp_adder = Module(new FP_adder(bw))
      fp_adder.io.in_a := mul.io.out_s
      fp_adder.io.in_b := io.biasParameter(i)
      io.fC_out(i) := fp_adder.io.out_s
    }
  }
//  println("[{(Generating Verilog)}")
//  (new chisel3.stage.ChiselStage).emitVerilog(new FC(32, 2,5,5,1))
}
