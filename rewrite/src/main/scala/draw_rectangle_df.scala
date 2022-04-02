import DFiant.*
import compiler._

class draw_rectangle_df(
    val CORDW : Int = 16
)(using DFC) extends DFDesign:
	// val clk = DFBit <> IN
  val start = DFBool <> IN
  val oe = DFBool <> IN

  object rectDefs extends RectDefs(CORDW)
  import rectDefs.DiagnolCoord
  val diagCoord = DiagnolCoord <> IN init ?//rectDefs.DiagnolCoord(?,?,?,?)
  
  val x = DFSInt(CORDW) <> OUT
  val y = DFSInt(CORDW) <> OUT
  val drawing = DFBool <> OUT
  val busy = DFBool <> OUT init 0
  val done = DFBool <> OUT init 0

  val line_id = DFUInt(2) <> VAR init 0
  val line_start = DFBool <> VAR init 0
  val line_done = DFBool <> VAR
  val line_ = DFBool <> VAR
  val ldiagCoord = rectDefs.DiagnolCoord <> VAR

  val draw_line_inst = new draw_line_df
  draw_line_inst.diagCoord_in <> ldiagCoord

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
      ldiagCoord := diagCoord.prev
      if (line_id == 0)
        val dp = diagCoord.prev
        ldiagCoord.x0 := dp.x0
        ldiagCoord.y0 := dp.y0
        ldiagCoord.x1 := diagCoord.x1.prev(1)
        ldiagCoord.y1 := diagCoord.y0.prev(1)
      else if (line_id == 1)
        ldiagCoord.x0 := diagCoord.x1.prev(1) 
        ldiagCoord.y0 := diagCoord.y0.prev(1)
        ldiagCoord.x1 := diagCoord.x1.prev(1) 
        ldiagCoord.y1 := diagCoord.y0.prev(1)
      else if (line_id == 2)
        ldiagCoord.x0 := diagCoord.x1.prev(1)
        ldiagCoord.y0 := diagCoord.y0.prev(1)
        ldiagCoord.x1 := diagCoord.x0.prev(1)
        ldiagCoord.y1 := diagCoord.y0.prev(1)
      else
        ldiagCoord.x0 := diagCoord.x0.prev(1)
        ldiagCoord.y0 := diagCoord.y0.prev(1)
        ldiagCoord.x1 := diagCoord.x0.prev(1) 
        ldiagCoord.y1 := diagCoord.y0.prev(1)
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
