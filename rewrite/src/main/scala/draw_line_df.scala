import DFiant.*
import compiler._

class draw_line_df(
    val CORDW : Int = 16
)(using DFC) extends DFDesign:
  // val clk = DFBit <> IN
  val start = DFBool <> IN
  val oe = DFBool <> IN
  val x0 = DFSInt(CORDW) <> IN
  val y0 = DFSInt(CORDW) <> IN
  val x1 = DFSInt(CORDW) <> IN
  val y1 = DFSInt(CORDW) <> IN
  val x = DFSInt(CORDW) <> OUT init 0
  val y = DFSInt(CORDW) <> OUT init 0
  val drawing = DFBool <> OUT
  val busy = DFBool <> OUT init 0
  val done = DFBool <> OUT init 0

  val swap = DFBool <> VAR
  val right = DFBool <> VAR
  val xa = DFSInt(CORDW) <> VAR init 0
  val ya = DFSInt(CORDW) <> VAR init 0
  val xb = DFSInt(CORDW) <> VAR init 0
  val yb = DFSInt(CORDW) <> VAR init 0
  val x_end = DFSInt(CORDW) <> VAR
  val y_end = DFSInt(CORDW) <> VAR

  swap := y0 > y1
  if (swap)
    xa := x1
    xb := x0
    ya := y1
    yb := y0
  else
    xa := x0
    xb := x1
    ya := y0
    yb := y1

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
        if (x == x_end && y == y_end) 
          busy := 0
          done := 1
        else 
          if (movx) 
            if (right)
              x := x.prev(1) + 1
            else
              x := x.prev(1) - 1
          err := err.prev(1) + dy.prev(1)

          if (movy) 
            y := y.prev(1) + 1
            err := err.prev(1) + dx.prev(1)

          if (movx && movy) 
            if (right)
              x := x.prev(1) + 1
            else
              x := x.prev(1) - 1
            y := y.prev(1) + 1
            err := err.prev(1) + dy.prev(1) + dx.prev(1)
    case INIT_0() => 
      if (right)
        dx := xb.prev(1) - xa.prev(1)
      else
        dx := xa.prev(1) - xb.prev(1)
      dy := ya.prev(1) - yb.prev(1)
    case INIT_1() => 
      err := dx.prev(1) + dy.prev(1)
      x := xa.prev(1)
      y := ya.prev(1)
      x_end := xb.prev(1)
      y_end := yb.prev(1)
    case _ => 
      done := false
      if (start)
        err := dx.prev(1) + dy.prev(1)
        busy := true
        right := xa.prev(1) < xb.prev(1)
      
// @main def hello: Unit = 
//   import DFiant.compiler.stages.printCodeString
//   val top = new draw_line_df
//   top.printCodeString