import DFiant.*
import compiler._

class xd_df(using DFC) extends DFDesign:
	// val clk = DFBit <> IN
  val i = DFBit <> IN
  val o = DFBit <> OUT

  val toggle_i = DFBit <> VAR init 0
  val shr_o = DFBits(4) <> VAR init b"0000"

  toggle_i := toggle_i.prev(1) ^ i
  shr_o := (shr_o.prev(1).bits(2,0), toggle_i.prev(1))

  o := shr_o.bits(3) ^ shr_o.bits(2)

// @main def hello: Unit = 
//   val top = new xd_df
//   top.printCodeString