package ConvolutionalLayer
import chisel3._
import IEEEConversions.FPConvert._
import BasicDesigns.MatMult
import BasicDesigns.MatMult.MatMult1
import MemoryModule.ROM_Module
import MemoryModule.ROM_Module.{inputs_ROM, weights_ROM}

import java.io.PrintWriter
import scala.io.Source

object Convolution  extends App {
  class Matrix_Controller (MatARowSize: Int,MatBRowSize: Int, bw: Int) extends Module {
    val io = IO(new Bundle {
      val input = Input(Vec((MatARowSize * MatARowSize), UInt(bw.W)))
      val weight = Input(Vec((MatBRowSize * MatBRowSize), UInt(bw.W)))
      val BiasValue = Input(UInt(bw.W))
      val single_out = Output(Vec((MatARowSize - MatBRowSize + 1) * (MatARowSize - MatBRowSize + 1), UInt(bw.W)))
    })
//    val pw = new PrintWriter("outputfile")

//    val input = Module(new inputs_ROM(MatARowSize * MatARowSize, bw)) // for the inputs
//    val weight = Module(new weights_ROM(MatBRowSize * MatBRowSize, bw)) // for the weights
    val MatrixMuls = for(i <- 0 until (MatARowSize-2) * (MatARowSize-2)) yield {
      val MatrixMul = Module(new MatMult1(MatBRowSize * MatBRowSize, MatBRowSize * MatBRowSize, bw))
      MatrixMul
    } // size of MatA should be same as MatB to be able to perform element wise matrix multiplication
    val MatrixMuls_io = MatrixMuls.map(_.io).toVector
    var position = 0
    var row = 0
    var col = 0
    var stride = 1
    var index = 0
    var count = 0
    val values = Wire(Vec((MatARowSize-2) * (MatARowSize-2) * MatBRowSize * MatBRowSize, UInt(bw.W)))
    for(i <- 0 until (MatARowSize-2)) {
      for(j <- 0 until (MatARowSize-2)) {
        for(n <- 0 until MatBRowSize) {
          for (m <- 0 until MatBRowSize) {
            row = i * stride + n
            col = j * stride + m
            index = col + MatARowSize * row
            values(count) := io.input(index)
            count = count + 1
          }
        }
      }
    }
    for (i <- 0 until (MatARowSize-2) * (MatARowSize-2)) {
      for (j <- 0  until MatBRowSize * MatBRowSize) {
        MatrixMuls_io(i).matA(j) := values(position + j)
        MatrixMuls_io(i).matB(j) := io.weight(j)
      }
      MatrixMuls_io(i).biasParameter := io.BiasValue
      io.single_out(i) := MatrixMuls_io(i).f_mac
      position = position + (MatBRowSize * MatBRowSize)
    }
//    pw.println(s"${element1.io.f_mac}")
//    pw.close()
  }
}
