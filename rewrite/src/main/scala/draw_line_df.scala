import DFiant.*
import compiler._

class draw_line_df(
    val CORDW : Int
)(using DFC) extends DFDesign:
  // val clk = DFBit <> IN
  val start = DFBool <> IN
  val oe = DFBool <> IN
  object rectDefs extends RectDefs(CORDW)
  val diagCoord_in = rectDefs.DiagnolCoord <> IN init ?

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
    diagCoord := diagCoord_in.swapF //TODO: confusion... do we need .prev?
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
    case DRAW => 
      if (oe) 
        state := IDLE
        drawing := true
        if (coord == endCoord) 
          busy := 0
          done := 1
        else 
          if (movx) 
            if (right)
              coord.x := coord.x.prev + 1
            else
              coord.x := coord.x.prev - 1
          err := err.prev + dy.prev

          if (movy) 
            coord.y := coord.y.prev + 1
            err := err.prev + dx.prev

          if (movx && movy) 
            if (right)
              coord.x := coord.x.prev + 1
            else
              coord.x := coord.x.prev - 1
            coord.y := coord.y.prev + 1
            err := err.prev + dy.prev + dx.prev
      else 
        drawing := false
        state := state.prev // TODO: how to do this globally?
    case INIT_0 => 
      state := INIT_1
      drawing := false // TODO: how to do this globally?
      if (right)
        dx := diagCoord.x1.prev - diagCoord.x0.prev
      else
        dx := diagCoord.x0.prev - diagCoord.x1.prev
      dy := diagCoord.y0.prev - diagCoord.y1.prev
    case INIT_1 => 
      state := DRAW
      drawing := false
      err := dx.prev + dy.prev
      coord.x := diagCoord.x0.prev
      coord.y := diagCoord.y0.prev
      endCoord.x := diagCoord.x1.prev
      endCoord.y := diagCoord.y1.prev
    case _ => 
      drawing := false
      done := false
      if (start)
        state := INIT_0
        err := dx.prev + dy.prev
        busy := true
        right := diagCoord.x0.prev < diagCoord.x1.prev
      else
        state := state.prev
      
// @main def hello: Unit = 
//   val top = new draw_line_df(CORDW = 16)
//   top.printCodeString
