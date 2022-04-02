import DFiant.*
import compiler._

class display_timings_480p_df(
  val CORDW : Int = 16,
  val H_RES : Int = 640,
  val V_RES : Int = 480,
  val H_FP : Int = 16,
  val H_SYNC : Int = 96,
  val H_BP : Int = 48,
  val V_FP : Int = 10,
  val V_SYNC : Int = 2,
  val V_BP : Int = 33,
  val H_POL : Boolean = false,
  val V_POL : Boolean = false
)(using DFC) extends DFDesign:
  val hsync = DFBit <> OUT
  val vsync = DFBit <> OUT
  val de = DFBit <> OUT
  val frame = DFBit <> OUT init 0
  val line = DFBit <> OUT

  object videoDefs extends VideoDefs(CORDW)
  val coord_out = videoDefs.Coord <> OUT init videoDefs.Coord(H_STA,V_STA)

  val coord = videoDefs.Coord <> VAR init videoDefs.Coord(H_STA,V_STA)

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

  if H_POL then hsync := (coord.x > HS_STA && coord.x <= HS_END)
  else hsync := !(coord.x > HS_STA && coord.x <= HS_END)
  if V_POL then vsync := (coord.y > VS_STA && coord.y <= VS_END)
  else vsync := !(coord.y > VS_STA && coord.y <= VS_END)

  de := (coord.y >= VA_STA && coord.x >= HA_STA)
  frame := (coord.y == V_STA  && coord.x == H_STA)
  line := (coord.y >= VA_STA && coord.x == H_STA)

  if coord.x == HA_END then 
    coord.x := H_STA
    if coord.y == VA_END then
      coord.y := V_STA
    else
      coord.y := coord.y.prev(1) + 1
  else
    coord.x := coord.x.prev(1) + 1

  coord_out:= coord.prev(1)
  
// @main def hello: Unit = 
//   import DFiant.compiler.stages.printCodeString
//   val top = new display_timings_480p_df
//   top.printCodeString
