import DFiant.*
import compiler._


// if structured pair [x,y], more complex timing structure
extension (arg: DFBit <> VAL) def maxLag(arg2: DFBit <> VAL, time: core.Time): Unit
class clk(using DFC) extends DFDesign:
  val counter = DFBit <> OUT init 0
  // TODO: CONST for 800
  val horizontal_timer = 60.Hz*800 // needs to check vivado after returning Ithaca
  if (pixel_hor_nhz) then
    counter:= counter.prev + 1
    counter.maxLag(pixel_hor_nhz, 0.ns)

// if structured pair [x,y], more complex timing structure
extension (arg: DFBit <> VAL) def maxLag(arg2: DFBit <> VAL, time: core.Time): Unit
class clk(using DFC) extends DFDesign:
  val counter = DFBit <> OUT init 0
  val pixel_ver_nhz = 60.Hz*800/600 // needs to check vivado after returning Ithaca
  if (pixel_ver_nhz) then
    counter:= counter.prev + 1
    counter.maxLag(pixel_ver_nhz, 0.ns)

extension (arg: DFBit <> VAL) def maxLag(arg2: DFBit <> VAL, time: core.Time): Unit
class clk(using DFC) extends DFDesign:
  object videoDefs extends VideoDefs(CORDW)
  val coord_out = videoDefs.Coord <> OUT init videoDefs.Coord(H_STA,V_STA)
  val pixel_clk_nhz = 60.Hz*800 // needs to check vivado after returning Ithaca
  if (pixel_clk_nhz) then
    coord_out:=  coord.prev
    coord_out.maxLag(pixel_clk_nhz, 0.ns)

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

  // coord_out:= coord.prev(1)
  

// @main def hello: Unit =
//   val top = new display_timings_480p_df
//   top.printCodeString
