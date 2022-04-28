import DFiant.*
import compiler._
// scalafmt: { align.tokens = [{code = "<>"}, {code = "="}, {code = "=>"}, {code = ":="}]}

class display_timings_480p_df(
    val CORDW: Int
)(using DFC)
    extends DFDesign:
  val hsync = DFBit <> OUT
  val vsync = DFBit <> OUT
  val de    = DFBit <> OUT
  val frame = DFBit <> OUT init 0
  val line  = DFBit <> OUT

  object videoDefs extends VideoDefs(CORDW)
  val coord     = videoDefs.Coord <> VAR init videoDefs.Coord(H_STA, V_STA)
  val coord_out = videoDefs.Coord <> OUT init videoDefs.Coord(H_STA, V_STA)

  val pixel_timer    = Timer(videoDefs.FPS.Hz) * videoDefs.AREA
  val vertical_timer = pixel_timer / videoDefs.HEIGHT

  val H_STA  = 0 - videoDefs.H_FP - videoDefs.H_SYNC - videoDefs.H_BP
  val HS_STA = H_STA + videoDefs.H_FP
  val HS_END = HS_STA + videoDefs.H_SYNC
  val HA_STA = 0
  val HA_END = videoDefs.H_RES - 1

  val V_STA  = 0 - videoDefs.V_FP - videoDefs.V_SYNC - videoDefs.V_BP
  val VS_STA = V_STA + videoDefs.V_FP
  val VS_END = VS_STA + videoDefs.V_SYNC
  val VA_STA = 0
  val VA_END = videoDefs.V_RES - 1

  if videoDefs.H_POL then hsync := (coord.x > HS_STA && coord.x <= HS_END)
  else hsync                    := !(coord.x > HS_STA && coord.x <= HS_END)
  if videoDefs.V_POL then vsync := (coord.y > VS_STA && coord.y <= VS_END)
  else vsync                    := !(coord.y > VS_STA && coord.y <= VS_END)

  de    := (coord.y >= VA_STA && coord.x >= HA_STA)
  frame := (coord.y == V_STA && coord.x == H_STA)
  line  := (coord.y >= VA_STA && coord.x == H_STA)

  if (vertical_timer.isActive)
    if (coord.y == VA_END)
      coord.y := V_STA
    else
      coord.y := coord.y.prev + 1

  if (pixel_timer.isActive)
    if (coord.x == HA_END)
      coord.x := H_STA
    else
      coord.x := coord.x.prev + 1
    coord_out := coord.prev
end display_timings_480p_df

// @main def hello: Unit =
//   val top = new display_timings_480p_df(
//   videoDefs.FPS = 60,
//   CORDW = 16,
//   videoDefs.H_RES = 640,
//   videoDefs.V_RES = 480,
//   videoDefs.H_FP = 16,
//   videoDefs.H_SYNC = 96,
//   videoDefs.H_BP = 48,
//   videoDefs.V_FP = 10,
//   videoDefs.V_SYNC = 2,
//   videoDefs.V_BP = 33,
//   videoDefs.H_POL = false,
//   videoDefs.V_POL = false)
//   top.printCodeString
