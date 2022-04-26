import DFiant.*
// scalafmt: { align.tokens = [{code = "<>"}, {code = "="}, {code = "=>"}, {code = ":="}]}

class VideoDefs(val CORDW: Int):
  case class Coord(
      x: DFSInt[CORDW.type] <> VAL,
      y: DFSInt[CORDW.type] <> VAL
  ) extends DFStruct

class FBDefs(val FB_CHANW: Int):
  case class Color(
      red: DFUInt[FB_CHANW.type] <> VAL,
      green: DFUInt[FB_CHANW.type] <> VAL,
      blue: DFUInt[FB_CHANW.type] <> VAL
  ) extends DFStruct

class RectDefs(val CORDW: Int):
  case class DiagnolCoord(
      x0: DFSInt[CORDW.type] <> VAL,
      x1: DFSInt[CORDW.type] <> VAL,
      y0: DFSInt[CORDW.type] <> VAL,
      y1: DFSInt[CORDW.type] <> VAL
  ) extends DFStruct
  extension (dc: DiagnolCoord <> VAL)(using DFC, DFiant.core.Container.Domain.DF)
    def swapF: DiagnolCoord <> VAL =
      val ret = DiagnolCoord <> VAR
      ret    := dc
      ret.x0 := dc.x1
      ret.x1 := dc.x0
      ret.y0 := dc.y1
      ret.y1 := dc.y0
      ret
end RectDefs

class top_rectangles_df(using DFC) extends DFDesign:
  // framebuffer (FB)
  val FB_WIDTH   = 320
  val FB_HEIGHT  = 180
  val FB_CIDXW   = 4
  val FB_CHANW   = 4
  val FB_SCALE   = 2
  val FB_IMAGE   = ""
  val FB_PALETTE = "16_colr_4bit_palette.mem"

  val vga_hsync = DFBit     <> OUT
  val vga_vsync = DFBit     <> OUT
  val vga_r     = DFBits(4) <> OUT
  val vga_g     = DFBits(4) <> OUT
  val vga_b     = DFBits(4) <> OUT

  // display timings
  val CORDW = 16
  object videoDefs extends VideoDefs(CORDW)
  object fBDefs extends FBDefs(FB_CHANW)
  object rectDefs extends RectDefs(CORDW)
  // export videoDefs.*
  val sCoord = videoDefs.Coord <> VAR
  val sColor = fBDefs.Color    <> VAR
  val hsync  = DFBit           <> VAR
  val vsync  = DFBit           <> VAR
  val frame  = DFBit           <> VAR
  val line   = DFBit           <> VAR

  val display_timings_inst =
    new display_timings_480p_df(FPS = 60, CORDW = 16, H_RES = 640, V_RES = 480, H_FP = 16,
      H_SYNC = 96, H_BP = 48, V_FP = 10, V_SYNC = 2, V_BP = 33, H_POL = false, V_POL = false)
  display_timings_inst.coord_out <> sCoord
  display_timings_inst.hsync     <> hsync
  display_timings_inst.vsync     <> vsync
  display_timings_inst.frame     <> frame
  display_timings_inst.line      <> line

  val frame_sys = DFBit <> VAR

  val sys_timer   = Timer(100.MHz)
  val pixel_timer = Timer(27.MHz)
  val xd_frame    = new xd_df
  xd_frame.outDomain.clk <> sys_timer.isActive
  xd_frame.inDomain.clk  <> pixel_timer.isActive
  xd_frame.inDomain.i    <> frame
  xd_frame.outDomain.o   <> frame_sys

  // object fbDefs extends FBDefs(FB_CHANW)

  val fb_we    = DFBit            <> VAR
  val fb_busy  = DFBit            <> VAR
  val fb_coord = videoDefs.Coord  <> VAR
  val fb_cidx  = DFUInt(FB_CIDXW) <> VAR
  val fb_color = fBDefs.Color     <> VAR

  // TODO generate pixel clock

  // TODO framebuffer (FB)
  val fb_inst = new framebuffer(
    CORDW     = 16, WIDTH = 320, HEIGHT = 180, CIDXW = 4, CHANW = 4, SCALE = 2, F_IMAGE = " ",
    F_PALETTE = " "
  )
  // fb_inst.clk_sys <> sys_timer.isActive
  // fb_inst.clk_pix <> pix_timer.isActive
  fb_inst.rst_sys <> 0
  fb_inst.rst_pix <> 0
  fb_inst.de      <> (sCoord.y >= 60 && sCoord.y < 420 && sCoord.x >= 0)
  // fb_inst.frame <>
  // fb_inst.line <>
  fb_inst.we     <> fb_we
  fb_inst.sCoord <> fb_coord
  fb_inst.sColor <> fb_color
  fb_inst.cidx   <> fb_cidx
  // clip
  fb_inst.busy <> fb_busy

  // draw rectangles in framebuffer
  val SHAPE_CNT  = 64
  val shape_id   = DFUInt(SHAPE_CNT.bitsWidth(false)) <> VAR init 0
  val v          = rectDefs.DiagnolCoord              <> VAR
  val draw_start = DFBit                              <> VAR

  // control drawing speed with output enable
  val FRAME_WAIT     = 300
  val PIX_FRAME      = 200
  val cnt_frame_wait = DFUInt(FRAME_WAIT.bitsWidth(false)) <> VAR init 0
  val cnt_pix_frame  = DFUInt(PIX_FRAME.bitsWidth(false))  <> VAR init 0
  val draw_req       = DFBit                               <> VAR
  val draw_rectangle = new draw_rectangle_df(CORDW = CORDW)
  val done           = DFBool                              <> VAR
  val drawing        = DFBool                              <> VAR
  draw_rectangle.start     <> draw_start
  draw_rectangle.oe        <> (draw_req && !fb_busy)
  draw_rectangle.diagCoord <> v
  draw_rectangle.coord     <> fb_coord
  draw_rectangle.done      <> done
  draw_rectangle.drawing   <> drawing

  // draw state machine
  enum State extends DFEnum:
    case IDLE, INIT, DRAW, DONE

  import State.*
  val state = State <> VAR init IDLE
  state match
    case INIT =>
      state      := DRAW
      draw_start := 1
      v.x0       := shape_id.prev +^ 60
      v.y0       := shape_id.prev +^ 15
      v.x1       := shape_id.prev -^ 260
      v.y1       := shape_id.prev -^ 165
      fb_cidx    := shape_id.prev.bits(3, 0)
    case DRAW =>
      draw_start := 0
      if (done)
        if (shape_id != SHAPE_CNT - 1)
          state    := DONE
          shape_id := shape_id.prev + 1
        else state := INIT
      else
        state := DRAW
    case DONE =>
      state := DONE
    case _ =>
      if (frame_sys)
        state := INIT
  end match
  // TODO any good ways of doing it? if not mentioned, default
  draw_req := 0
  if (frame_sys)
    if (cnt_frame_wait != FRAME_WAIT - 1)
      cnt_frame_wait := cnt_frame_wait.prev + 1
    cnt_pix_frame    := 0
  if (!fb_busy)
    if (cnt_frame_wait == FRAME_WAIT - 1 && cnt_pix_frame != PIX_FRAME - 1)
      draw_req      := 1
      cnt_pix_frame := cnt_pix_frame.prev + 1

  fb_we := drawing

  val hsync_p1 = DFBit <> VAR
  val vsync_p1 = DFBit <> VAR

  vga_hsync := hsync.pipe(2)
  vga_vsync := vsync.pipe(2)
  vga_r     := fb_color.red.pipe.bits
  vga_g     := fb_color.green.pipe.bits
  vga_b     := fb_color.blue.pipe.bits

end top_rectangles_df

@main def hello: Unit =
  val top = new top_rectangles_df
  top.printCodeString
