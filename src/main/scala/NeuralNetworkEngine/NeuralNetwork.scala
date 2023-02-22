package NeuralNetworkEngine
import ConvolutionalLayer.Convolution.Matrix_Controller
import FullyConnectedLayer.FCLayer.FC
import IEEEConversions.FPConvert.convert_string_to_IEEE_754
import MemoryModule.ROM_Module.{inputs_ROM, weights_ROM}
import PoolingLayer.PoolingController.Pooling_Controller
import SoftMaxLayer.SoftMax.DigitPrediction
import chisel3._

object NeuralNetwork extends App{
  class CNNEngine (InputMatrix_A_row : Int, InputMatrix_A_col : Int, InputMatrix_B_row : Int, InputMatrix_B_col : Int, bw: Int) extends Module {
    require (InputMatrix_A_row == InputMatrix_A_col && InputMatrix_B_row == InputMatrix_B_col && InputMatrix_A_row >= InputMatrix_B_row)
    val io = IO(new Bundle {
      val ConOut = Output(Vec(64, UInt(bw.W)))  //64 - convolutional 16- pooling layer
      val Digit = Output(UInt(bw.W))
    })
    val ConvolutionBiasParameter = 1  // Bias Parameter for Convolution Module

    val input = Module(new inputs_ROM(InputMatrix_A_row * InputMatrix_A_col, bw)) // for the inputs
    val weight = Module(new weights_ROM(InputMatrix_B_row * InputMatrix_B_col, bw)) // for the weights

    val MatController = Module(new Matrix_Controller(InputMatrix_A_row, InputMatrix_B_row, bw))
    for (i <- 0 until InputMatrix_A_row * InputMatrix_A_col) {
      MatController.io.input(i) := input.io.out_s(i)
    }
    for (i <- 0 until InputMatrix_B_row * InputMatrix_B_col) {
      MatController.io.weight(i) := weight.io.out_s(i)
    }
    MatController.io.BiasValue := ConvolutionBiasParameter.U

    var PoolingMatSize = InputMatrix_A_row - InputMatrix_B_row +1
    val PoolingControl = Module(new Pooling_Controller(PoolingMatSize,bw))
    for (i <- 0 until PoolingMatSize * PoolingMatSize) {
      io.ConOut(i) := MatController.io.single_out(i)                    // Testing Convolutional Layer outputs
      PoolingControl.io.InputMatrix(i) := MatController.io.single_out(i)
    }

    var FCSize = (PoolingMatSize/2) * (PoolingMatSize/2)
    val FullyConnected = Module(new FC(bw, 10, FCSize, FCSize, 1))
    for(i <- 0 until FCSize) {
      //io.ConOut(i) := PoolingControl.io.single_out(i)                     // Testing Pooling layer output
      FullyConnected.io.matB(i) := PoolingControl.io.single_out(i)
    }
    val mem = scala.io.Source.fromFile("FCLayerWeights.txt").getLines().toIndexedSeq.map(x => convert_string_to_IEEE_754(x, bw).asUInt)
    val file = WireInit(VecInit(mem))
    for(i <- 0 until 10*FCSize) {
      FullyConnected.io.matA(i) := file(i)
    }
    val mem2 = scala.io.Source.fromFile("FCLayerBiasParameter.txt").getLines().toIndexedSeq.map(y => convert_string_to_IEEE_754(y, bw).asUInt)
    val file2 = WireInit(VecInit(mem2))
    for(i <- 0 until 10) {
      FullyConnected.io.biasParameter(i) := file2(i)
    }

    val SoftMaxModule = Module(new DigitPrediction(bw))
    for(i <- 0 until 10) {
      //io.ConOut(i) := FullyConnected.io.fC_out(i)                        // Testing Softmax layer output
      SoftMaxModule.io.input(i) := FullyConnected.io.fC_out(i)
    }
    io.Digit := SoftMaxModule.io.Digit
  }
//  println("[{(Generating Verilog)}")
//  (new chisel3.stage.ChiselStage).emitVerilog(new CNNEngine(10, 10,3,3,32))
}
