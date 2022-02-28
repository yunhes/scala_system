import DFiant.*
import compiler._

extension (x : DFUInt.type)
  def until(sup : Int) = DFUInt((sup-1).bitsWidth(false))
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
  val busy = DFBool <> OUT
  val done = DFBool <> OUT

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

  // val nextState: State <> VAL = (state, oe, condition0, start) match
  //   case (DRAW(), 1, 1, _) => IDLE
  //   case (INIT_0(), _, _, _) => INIT_1
  //   case (INIT_1(), _, _, _) => DRAW
  //   case (_, _, _, 1) => INIT_0
  
  val nextState: State <> VAL = state match
  case DRAW() 
    if (oe) => IDLE
  case INIT_0() => INIT_1
  case INIT_1() => DRAW
  case _
    if (start) => INIT_0

  state := state.reg
  state := nextState

  
  x := x.reg
  y := y.reg
  err := err.reg
  busy := busy.reg
  done := done.reg
  x_end := x_end.reg
  y_end := y_end.reg

  state match
    case DRAW() => 
      if (oe) 
        if (x == x_end && y == y_end) 
          state := IDLE
          busy := 0
          done := 1
        else 
          if (movx) 
            if (right)
              x := x + 1
            else
              x := x - 1
          err := err + dy

          if (movy) 
            y := y + 1
            err := err + dx

          if (movx && movy) 
            if (right)
              x := x + 1
            else
              x := x - 1
            y := y + 1
            err := err + dy + dx
    case INIT_0() => 
      if (right)
        dx := xb - xa
      else
        dx := xa - xb
      dy := ya - yb
    case INIT_1() => 
      err := dx + dy
      x := xa
      y := ya
      x_end := xb
      y_end := yb
    case _ => 
      done := false
      if (start)
        err := dx + dy
        busy := true
        right := xa < xb
      
@main def hello: Unit = 
  import DFiant.compiler.stages.printCodeString
  val top = new draw_line
  top.printCodeString