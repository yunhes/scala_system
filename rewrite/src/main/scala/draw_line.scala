import DFiant.*
import compiler._

class draw_line(
    val CORDW : Int = 16
)(using DFC) extends RTDesign:
  // val clk = DFBit <> IN
  val start = DFBool <> IN
  val oe = DFBool <> IN
  val x0 = DFSInt(CORDW) <> IN
  val y0 = DFSInt(CORDW) <> IN
  val x1 = DFSInt(CORDW) <> IN
  val y1 = DFSInt(CORDW) <> IN
  val x = DFSInt(CORDW) <> OUT
  val y = DFSInt(CORDW) <> OUT
  val drawing = DFBool <> OUT
  val busy = DFBool <> OUT init 0
  val done = DFBool <> OUT init 0

  val swap = DFBool <> VAR
  val right = DFBool <> VAR
  val xa = DFSInt(CORDW) <> VAR
  val ya = DFSInt(CORDW) <> VAR
  val xb = DFSInt(CORDW) <> VAR
  val yb = DFSInt(CORDW) <> VAR
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

  val err = DFSInt(CORDW+1) <> VAR
  val dx = DFSInt(CORDW+1) <> VAR
  val dy = DFSInt(CORDW+1) <> VAR
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
  case DRAW() 
    if (oe) => IDLE
  case INIT_0() => INIT_1
  case INIT_1() => DRAW
  case _ 
    if (start) => INIT_0
  
    // ============== TODO: so.... how to do this? ==============

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
              x := x.reg(1) + 1
            else
              x := x.reg(1) - 1
          err := err.reg(1) + dy.reg(1)

          if (movy) 
            y := y.reg(1) + 1
            err := err.reg(1) + dx.reg(1)

          if (movx && movy) 
            if (right)
              x := x.reg(1) + 1
            else
              x := x.reg(1) - 1
            y := y.reg(1) + 1
            err := err.reg(1) + dy.reg(1) + dx.reg(1)
    case INIT_0() => 
      if (right)
        dx := xb.reg(1) - xa.reg(1)
      else
        dx := xa.reg(1) - xb.reg(1)
      dy := ya.reg(1) - yb.reg(1)
    case INIT_1() => 
      err := dx.reg(1) + dy.reg(1)
      x := xa.reg(1)
      y := ya.reg(1)
      x_end := xb.reg(1)
      y_end := yb.reg(1)
    case _ => 
      done := false
      if (start)
        err := dx.reg(1) + dy.reg(1)
        busy := true
        right := xa.reg(1) < xb.reg(1)
      
// @main def hello: Unit = 
//   import DFiant.compiler.stages.printCodeString
//   val top = new draw_line
//   top.printCodeString