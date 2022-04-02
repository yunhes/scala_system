import DFiant.*
import compiler._

class draw_line_df(
    val CORDW : Int = 16
)(using DFC) extends DFDesign:
  // val clk = DFBit <> IN
  val start = DFBool <> IN
  val oe = DFBool <> IN
  object rectDefs extends RectDefs(CORDW)
  val diagCoord_in = rectDefs.DiagnolCoord <> IN

  object videoDefs extends VideoDefs(CORDW)
  val coord = videoDefs.Coord <> OUT init videoDefs.Coord(0,0)
  val drawing = DFBool <> OUT
  val busy = DFBool <> OUT init 0
  val done = DFBool <> OUT init 0

  val swap = DFBool <> VAR
  val right = DFBool <> VAR
  val diagCoord = rectDefs.DiagnolCoord <> VAR init ?
  val endCoord = videoDefs.Coord <> VAR
  import rectDefs.swapF
  swap := diagCoord_in.y0 > diagCoord_in.y1
  if (swap)
    diagCoord := diagCoord_in.swapF
    // diagCoord.x0 := diagCoord_in.x1
    // diagCoord.x1 := diagCoord_in.x0
    // diagCoord.y0 := diagCoord_in.y1
    // diagCoord.y1 := diagCoord_in.y0
  else
    diagCoord := diagCoord_in
  val err = DFSInt(CORDW+1) <> VAR init 0
  val dx = DFSInt(CORDW+1) <> VAR init 0
  val dy = DFSInt(CORDW+1) <> VAR init 0
  val movx = DFBool <> VAR
  val movy = DFBool <> VAR

  movx := (err >> 2) >= dy
  movy := (err >> 2) <= dx

  enum State extends DFEnum:
    case IDLE, INIT_0, INIT_1, DRAW

  import State.*
  val state = State <> VAR init IDLE
  
  state match
    case DRAW() => 
      if (oe) drawing := true
      else drawing := false
    case _ => drawing := false
  
  val nextState: State <> VAL = state match
  case DRAW() => 
    if (oe) IDLE
    else state
  case INIT_0() => INIT_1
  case INIT_1() => DRAW
  case _ =>
    if (start) INIT_0
    else state

  state := nextState
  
  state match
    case DRAW() => 
      if (oe) 
        if (coord == endCoord) 
          busy := 0
          done := 1
        else 
          if (movx) 
            if (right)
              coord.x := coord.x.prev(1) + 1
            else
              coord.x := coord.x.prev(1) - 1
          err := err.prev(1) + dy.prev(1)

          if (movy) 
            coord.y := coord.y.prev(1) + 1
            err := err.prev(1) + dx.prev(1)

          if (movx && movy) 
            if (right)
              coord.x := coord.x.prev(1) + 1
            else
              coord.x := coord.x.prev(1) - 1
            coord.y := coord.y.prev(1) + 1
            err := err.prev(1) + dy.prev(1) + dx.prev(1)
    case INIT_0() => 
      if (right)
        dx := diagCoord.x1.prev(1) - diagCoord.x0.prev(1)
      else
        dx := diagCoord.x0.prev(1) - diagCoord.x1.prev(1)
      dy := diagCoord.y0.prev(1) - diagCoord.y1.prev(1)
    case INIT_1() => 
      err := dx.prev(1) + dy.prev(1)
      coord.x := diagCoord.x0.prev(1)
      coord.y := diagCoord.y0.prev(1)
      endCoord.x := diagCoord.x1.prev(1)
      endCoord.y := diagCoord.y1.prev(1)
    case _ => 
      done := false
      if (start)
        err := dx.prev(1) + dy.prev(1)
        busy := true
        right := diagCoord.x0.prev(1) < diagCoord.x1.prev(1)
      
// @main def hello: Unit = 
//   import DFiant.compiler.stages.printCodeString
//   val top = new draw_line_df
//   top.printCodeString
