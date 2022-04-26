import DFiant.*
import compiler._
// scalafmt: { align.tokens = [{code = "<>"}, {code = "="}, {code = "=>"}, {code = ":="}]}

class bram_sdp(
    val WIDTH: Int,
    val DEPTH: Int,
    val INIT_F: String
)(using DFC)
    extends DFDesign:
  // val ADDRW = (DEPTH-1).bitsWidth(false)
//cal clk
  val addr_write = DFUInt.until(DEPTH) <> IN
  val addr_read  = DFUInt.until(DEPTH) <> IN
  val data_in    = DFUInt(WIDTH)       <> IN
  val data_out   = DFUInt(WIDTH)       <> OUT
  val we         = DFBit               <> IN

  // val initVector : Vector[DFBits[Int] <> TOKEN] = ???
  val done   = DFBit                   <> WIRE
  val memory = DFUInt(WIDTH) X (DEPTH) <> VAR

  if (we)
    memory(addr_write) := data_in

  data_out := memory(addr_read)
end bram_sdp

// @main def hello: Unit =
//   val top = new bram_sdp(
//     WIDTH = 4,
//     DEPTH = 320*180,
//     INIT_F = ""
//     )
//   top.printCodeString
