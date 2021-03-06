import DFiant.*
import compiler._
// scalafmt: { align.tokens = [{code = "<>"}, {code = "="}, {code = "=>"}, {code = ":="}]}

class xd_df(using DFC) extends RTDesign:
  val inDomain = new RTDomain:
    val clk    = DFBit <> IN
    val rst    = DFBit <> IN
    val i      = DFBit <> IN
    val toggle = DFBit <> REG init 0
    toggle.din := toggle ^ i

  // out domian
  val outDomain = new RTDomain:
    val clk = DFBit     <> IN
    val rst = DFBit     <> IN
    val o   = DFBit     <> OUT
    val shr = DFBits(4) <> REG init all(0)
    shr.din := (shr(2, 0), inDomain.toggle)
    o       := shr(3) ^ shr(2)

end xd_df

// @main def hello: Unit =
//   val top = new xd_df
//   top.printCodeString
