import DFiant.*
import compiler._

class draw_rectangle_df(
    val CORDW : Int = 16
)(using DFC) extends DFDesign:
	// val clk = DFBit <> IN
  val start = DFBool <> IN
  val oe = DFBool <> IN
  
  val x0 = DFSInt(CORDW) <> IN init ?
  val y0 = DFSInt(CORDW) <> IN init ?
  val x1 = DFSInt(CORDW) <> IN init ?
  val y1 = DFSInt(CORDW) <> IN init ?
  val x = DFSInt(CORDW) <> OUT
  val y = DFSInt(CORDW) <> OUT
  val drawing = DFBool <> OUT
  val busy = DFBool <> OUT init 0
  val done = DFBool <> OUT init 0

  val line_id = DFUInt(2) <> VAR init 0
  val line_start = DFBool <> VAR init 0
  val line_done = DFBool <> VAR
  val line_ = DFBool <> VAR
  val lx0 = DFSInt(CORDW) <> VAR
  val ly0 = DFSInt(CORDW) <> VAR
  val lx1 = DFSInt(CORDW) <> VAR
  val ly1 = DFSInt(CORDW) <> VAR

  val draw_line_inst = new draw_line
  draw_line_inst.x0 <> lx0
  draw_line_inst.x1 <> lx1
  draw_line_inst.y0 <> ly0
  draw_line_inst.y1 <> ly1
  draw_line_inst.done <> line_done

  enum State extends DFEnum:
    case IDLE, INIT, DRAW

  import State.*
  val state = State <> VAR init IDLE

  val nextState: State <> VAL = state match
  case INIT() => DRAW
  case DRAW() => 
    if (line_done)
        if (line_id == 3) 
          IDLE
        else 
          INIT
    else
      DRAW
  case _ 
    if (start) => INIT

  state := nextState

  state match
    case INIT() =>
      line_start := 1
      if (line_id == 0)
        lx0 := x0.prev(1)
        ly0 := y0.prev(1)
        lx1 := x1.prev(1)
        ly1 := y0.prev(1)
      else if (line_id == 1)
        lx0 := x1.prev(1) 
        ly0 := y0.prev(1)
        lx1 := x1.prev(1) 
        ly1 := y1.prev(1)
      else if (line_id == 2)
        lx0 := x1.prev(1)
        ly0 := y1.prev(1)
        lx1 := x0.prev(1)
        ly1 := y1.prev(1)
      else
        lx0 := x0.prev(1)
        ly0 := y1.prev(1)
        lx1 := x0.prev(1) 
        ly1 := y0.prev(1)
    case DRAW() =>
      line_start := 0
      if (line_done) 
        if (line_id == 3)
          busy := 0
          done := 1
        else 
          line_id := line_id.prev(1) + 1
    case _ =>
      done := 0
      if (start) 
        line_id := 0
        busy := 1

// @main def hello: Unit = 
//   import DFiant.compiler.stages.printCodeString
//   val top = new draw_rectangle_df
//   top.printCodeString