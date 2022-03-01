import DFiant.*
import compiler._

class display_timings_480p(
  val CORDW : Int = 16,
  val H_RES : Int = 16,
  val V_RES : Int = 16,
  val H_FP : Int = 16,
  val H_SYNC : Int = 16,
  val H_BP : Int = 16,
  val V_FP : Int = 16,
  val V_SYNC : Int = 16,
  val V_BP : Int = 16,
  val H_POL : Boolean = false,
  val V_POL : Boolean = false
)(using DFC) extends DFDesign:
  val hsync = DFBit <> OUT
  val vsync = DFBit <> OUT
  val de = DFBit <> OUT
  val frame = DFBit <> OUT init 0
  val line = DFBit <> OUT
  val sx = DFSInt(CORDW) <> IN init H_STA
  val sy = DFSInt(CORDW) <> IN init V_STA

  val H_STA  = 0 - H_FP - H_SYNC - H_BP    
  val HS_STA = H_STA + H_FP                
  val HS_END = HS_STA + H_SYNC             
  val HA_STA = 0                           
  val HA_END = H_RES - 1                   

  val V_STA  = 0 - V_FP - V_SYNC - V_BP    
  val VS_STA = V_STA + V_FP                
  val VS_END = VS_STA + V_SYNC             
  val VA_STA = 0                           
  val VA_END = V_RES - 1 

  val x = DFSInt(CORDW) <> VAR init H_STA
  val y = DFSInt(CORDW) <> VAR init V_STA

  if H_POL then hsync := (x > HS_STA && x <= HS_END)
  else hsync := !(x > HS_STA && x <= HS_END)
  if V_POL then vsync := (y > VS_STA && y <= VS_END)
  else vsync := !(y > VS_STA && y <= VS_END)

  de := (y >= VA_STA && x >= HA_STA)
  frame := (y == V_STA  && x == H_STA)
  line := (y >= VA_STA && x == H_STA)

  if x == HA_END then 
    x := H_STA
    if y == VA_END then
      y := V_STA
    else
      y := y.reg(1) + 1
  else
    x := x.reg(1) + 1

  sx := x.reg(1)
  sy := y.reg(1)
  
// @main def hello: Unit = 
//   import DFiant.compiler.stages.printCodeString
//   val top = new display_timings_480p
//   top.printCodeString