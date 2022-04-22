import DFiant.*
import compiler._

class xd_df(using DFC) extends RTDesign:
	// val clk = DFBit <> IN
  val inDomain = new RTDomain(
    clkCfg = ClkCfg.Explicit("clk", ClkCfg.Edge.Rising),
    rstCfg = RstCfg.Explicit("rst", RstCfg.Mode.Sync, RstCfg.Active.High)
  ):
    val clk    = DFBit <> IN
    val rst    = DFBit <> IN
    val i      = DFBit <> IN
    val toggle = DFBit <> REG init 0
    toggle.din := toggle ^ i

  //out domian
  val outDomain = new RTDomain(
    clkCfg = ClkCfg.Explicit("clk", ClkCfg.Edge.Rising),
    rstCfg = RstCfg.Explicit("rst", RstCfg.Mode.Sync, RstCfg.Active.High)
  ):
    val clk = DFBit     <> IN
    val rst = DFBit     <> IN
    val o   = DFBit     <> OUT
    val shr = DFBits(4) <> REG init all(0)
    shr.din := (shr(2, 0), inDomain.toggle)
    o       := shr(3) ^ shr(2)

end xd_df
  // val i = DFBit <> IN
  // val o = DFBit <> OUT

  // val toggle_i = DFBit <> VAR init 0
  // val shr_o = DFBits(4) <> VAR init b"0000"

  // toggle_i := toggle_i.prev ^ i
  // shr_o := (shr_o.prev(1).bits(2,0), toggle_i.prev)

  // o := shr_o.bits(3) ^ shr_o.bits(2)

  
// @main def hello: Unit = 
//   val top = new xd_df
//   top.printCodeString