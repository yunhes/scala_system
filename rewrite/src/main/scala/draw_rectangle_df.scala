import DFiant.*
import compiler._
// scalafmt: { align.tokens = [{code = "<>"}, {code = "="}, {code = "=>"}, {code = ":="}]}

class draw_rectangle_df(
    val CORDW: Int
)(using DFC)
    extends DFDesign:
  // val clk = DFBit <> IN
  val start = DFBool <> IN
  val oe    = DFBool <> IN

  object rectDefs extends RectDefs(CORDW)
  object videoDefs extends VideoDefs(CORDW)
  import rectDefs.*
  import videoDefs.*
  val diagCoord = DiagnolCoord    <> IN init ?
  val coord     = videoDefs.Coord <> OUT
  val drawing   = DFBool          <> OUT
  val busy      = DFBool          <> OUT init 0
  val done      = DFBool          <> OUT init 0

  val line_id    = DFUInt(2)             <> VAR init 0
  val line_start = DFBool                <> VAR init 0
  val line_done  = DFBool                <> VAR
  val line_      = DFBool                <> VAR
  val ldiagCoord = rectDefs.DiagnolCoord <> VAR

  val draw_line_inst = new draw_line_df(CORDW = CORDW)
  draw_line_inst.diagCoord_in <> ldiagCoord
  draw_line_inst.coord        <> coord

  enum State extends DFEnum:
    case IDLE, INIT, DRAW

  import State.*
  val state = State <> VAR init IDLE

  state match
    case INIT =>
      state      := DRAW
      line_start := 1
      ldiagCoord := diagCoord.prev
      if (line_id == 0)
        val dp = diagCoord.prev
        ldiagCoord.x0 := dp.x0
        ldiagCoord.y0 := dp.y0
        ldiagCoord.x1 := diagCoord.x1.prev
        ldiagCoord.y1 := diagCoord.y0.prev
      else if (line_id == 1)
        ldiagCoord.x0 := diagCoord.x1.prev
        ldiagCoord.y0 := diagCoord.y0.prev
        ldiagCoord.x1 := diagCoord.x1.prev
        ldiagCoord.y1 := diagCoord.y0.prev
      else if (line_id == 2)
        ldiagCoord.x0 := diagCoord.x1.prev
        ldiagCoord.y0 := diagCoord.y0.prev
        ldiagCoord.x1 := diagCoord.x0.prev
        ldiagCoord.y1 := diagCoord.y0.prev
      else
        ldiagCoord.x0 := diagCoord.x0.prev
        ldiagCoord.y0 := diagCoord.y0.prev
        ldiagCoord.x1 := diagCoord.x0.prev
        ldiagCoord.y1 := diagCoord.y0.prev
      end if
    case DRAW =>
      line_start := 0
      if (line_done)
        if (line_id == 3)
          state := IDLE
          busy  := 0
          done  := 1
        else
          state   := INIT
          line_id := line_id.prev + 1
      else
        state := DRAW
    case _ =>
      done := 0
      if (start)
        state   := INIT
        line_id := 0
        busy    := 1
  end match
end draw_rectangle_df

// @main def hello: Unit =
//   val top = new draw_rectangle_df(CORDW = 16)
//   top.printCodeString
