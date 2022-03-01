import DFiant.*
import compiler._

class top_rectangles(using DFC) extends RTDesign: 
  val vga_hsync = DFBit <> OUT
  val vga_vsync = DFBit <> OUT
  val vga_r = DFBits(4) <> OUT
  val vga_g = DFBits(4) <> OUT
  val vga_b = DFBits(4) <> OUT

  // display timings
  val CORDW = 16
  val sx = DFSInt(CORDW) <> VAR
  val sy = DFSInt(CORDW) <> VAR
  val hsync = DFBit <> VAR
  val vsync = DFBit <> VAR
  val frame = DFBit <> VAR
  val line = DFBit <> VAR

  val display_timings_inst = new display_timings_480p
  display_timings_inst.sx <> sx
  display_timings_inst.sy <> sy
  display_timings_inst.hsync <> hsync
  display_timings_inst.vsync <> vsync
  display_timings_inst.frame <> frame
  display_timings_inst.line <> line

  val frame_sys = DFBit <> VAR
  val xd_frame = new xd
  xd_frame.i <> frame
  xd_frame.o <> frame_sys

  // framebuffer (FB)
  val FB_WIDTH   = 320
  val FB_HEIGHT  = 180
  val FB_CIDXW   = 4
  val FB_CHANW   = 4
  val FB_SCALE   = 2
  val FB_IMAGE   = ""
  val FB_PALETTE = "16_colr_4bit_palette.mem"

  val fb_we = DFBit <> VAR
  val fb_busy = DFBit <> VAR
  val fbx = DFSInt(CORDW) <> VAR
  val fby = DFSInt(CORDW) <> VAR
  val fb_cidx = DFSInt(FB_CIDXW) <> VAR
  val fb_red = DFSInt(FB_CHANW) <> VAR
  val fb_green = DFSInt(FB_CHANW) <> VAR
  val fb_blue = DFSInt(FB_CHANW) <> VAR

  // val fb_inst = new framebuffer_bram

  // draw rectangles in framebuffer
  val SHAPE_CNT = 64
  val shape_id = DFUInt(SHAPE_CNT.bitsWidth(false)) <> VAR
  val vx0 = DFSInt(CORDW) <> VAR
  val vy0 = DFSInt(CORDW) <> VAR
  val vx1 = DFSInt(CORDW) <> VAR
  val vy1 = DFSInt(CORDW) <> VAR
  val draw_start = DFBit <> VAR
  val drawing = DFBit <> VAR
  val draw_done = DFBit <> VAR

  // draw state machine
  enum State extends DFEnum:
    case IDLE, INIT, DRAW, DONE

  import State.*
  val state = State <> VAR init IDLE
  
  val nextState: State <> VAL = state match
  case INIT() => DRAW
  case DRAW() => 
    if (draw_done)
      if (shape_id == SHAPE_CNT-1)
        DONE
      else 
        INIT
    else
      DRAW
  case DONE() => DONE
  case _ 
    if (frame_sys) => INIT

  state match
    case INIT() =>
      draw_start := 1
      vx0 :=  60 + shape_id.reg(1)
      vy0 :=  15 + shape_id.reg(1)
      vx1 := 260 - shape_id.reg(1)
      vy1 := 165 - shape_id.reg(1)
      fb_cidx := shape_id.reg(1).bits(3:0)
    case DRAW() =>
      draw_start := 0
      if (draw_done)
        if (shape_id != SHAPE_CNT-1)
          shape_id := shape_id.reg(1) + 1

  // control drawing speed with output enable
  val FRAME_WAIT = 300
  val PIX_FRAME = 200
  val cnt_frame_wait = DFUInt(FRAME_WAIT.bitsWidth(false)) <> VAR
  val cnt_pix_frame = DFUInt(PIX_FRAME.bitsWidth(false)) <> VAR
  val draw_req = DFBit <> VAR


  // TODO any good ways of doing it
  if (frame_sys) 
      if (cnt_frame_wait != FRAME_WAIT-1) 
        cnt_frame_wait := cnt_frame_wait.reg(1) + 1
      cnt_pix_frame := 0
  else
      draw_req := 0
  if (!fb_busy) 
    if (cnt_frame_wait == FRAME_WAIT-1 && cnt_pix_frame != PIX_FRAME-1) 
      draw_req := 1
      cnt_pix_frame := cnt_pix_frame.reg(1) + 1
    else
      draw_req := 0
  else
        draw_req := 0

  val draw_rectangle = new draw_rectangle
  draw_rectangle.start <> draw_start
  draw_rectangle.oe <> (draw_req && !fb_busy)
  draw_rectangle.x0 <> vx0
  draw_rectangle.y0 <> vy0
  draw_rectangle.x1 <> vx1
  draw_rectangle.y1 <> vy1
  draw_rectangle.x <> fbx
  draw_rectangle.y <> fby
  draw_rectangle.drawing <> drawing
  draw_rectangle.done <> draw_done

  fb_we := drawing

  val hsync_p1 = DFBit <> VAR 
  val vsync_p1 = DFBit <> VAR

  hsync_p1 := hsync.reg(1)
  vsync_p1 := vsync.reg(1)
  vga_hsync := hsync_p1.reg(1)
  vga_vsync := vsync_p1.reg(1)
  vga_r := fb_red.reg(1)
  vga_g := fb_green.reg(1)
  vga_b := fb_blue.reg(1)


@main def hello: Unit = 
  import DFiant.compiler.stages.printCodeString
  val top = new top_rectangles
  top.printCodeString