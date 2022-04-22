import DFiant.*
import compiler._

class display_timings_480p_df(
  val FPS : Int,
  val CORDW : Int,
  val H_RES : Int,
  val V_RES : Int,
  val H_FP : Int,
  val H_SYNC : Int,
  val H_BP : Int,
  val V_FP : Int,
  val V_SYNC : Int,
  val V_BP : Int,
  val H_POL : Boolean,
  val V_POL : Boolean
)(using DFC) extends DFDesign:
  val hsync = DFBit <> OUT
  val vsync = DFBit <> OUT
  val de = DFBit <> OUT
  val frame = DFBit <> OUT init 0
  val line = DFBit <> OUT

  object videoDefs extends VideoDefs(CORDW)
  val coord = videoDefs.Coord <> VAR init videoDefs.Coord(H_STA,V_STA)
  val coord_out = videoDefs.Coord <> OUT init videoDefs.Coord(H_STA,V_STA)

  val fps_timer = Timer(FPS.Hz)
  val WIDTH = H_RES+H_FP+H_SYNC+H_BP
  val HEIGHT = V_RES+V_FP+V_SYNC+V_BP
  val horizontal_timer = fps_timer*WIDTH*HEIGHT
  val vertical_timer = horizontal_timer/HEIGHT

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


  if(vertical_timer.isActive)
    if (coord.y == VA_END)
        coord.y := V_STA
    else
      coord.y := coord.y.prev + 1

  if(horizontal_timer.isActive)
    if (coord.x == HA_END) 
      coord.x := H_STA
    else
      coord.x := coord.x.prev + 1
    coord_out:= coord.prev
  

// @main def hello: Unit =
//   val top = new display_timings_480p_df(
//   FPS = 60,
//   CORDW = 16,
//   H_RES = 640,
//   V_RES = 480,
//   H_FP = 16,
//   H_SYNC = 96,
//   H_BP = 48,
//   V_FP = 10,
//   V_SYNC = 2,
//   V_BP = 33,
//   H_POL = false,
//   V_POL = false)
//   top.printCodeString
