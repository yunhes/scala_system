import DFiant.*
//import compiler._
// scalafmt: { align.tokens = [{code = "<>"}, {code = "="}, {code = "=>"}, {code = ":="}]}

class framebuffer(
    val CORDW: Int,
    val WIDTH: Int,
    val HEIGHT: Int,
    val CIDXW: Int,
    val CHANW: Int,
    val SCALE: Int,
    F_IMAGE: String,
    F_PALETTE: String
)(using DFC)
    extends DFDesign:
//struct def
  object videoDefs extends VideoDefs(CORDW)
  object fBDefs extends FBDefs(CHANW)
  // val sCoord = videoDefs.Coord <> VAR

  val clk_sys = DFBit           <> IN // system clock
  val clk_pix = DFBit           <> IN // pixel clock
  val rst_sys = DFBit           <> IN // reset (clk_sys)
  val rst_pix = DFBit           <> IN // reset (clk_pix)
  val de      = DFBool          <> IN // data enable for display (clk_pix)
  val frame   = DFBool          <> IN // start a new frame (clk_pix)
  val line    = DFBool          <> IN // start a new screen line (clk_pix)
  val we      = DFBool          <> IN // write enable
  val sCoord  = videoDefs.Coord <> IN //
  // val x = x.until(CORDW)
  val sColor = fBDefs.Color <> OUT //
  // val sCoord.y = DFSInt(CORDW) <> IN    //
  val cidx = DFUInt(CIDXW) <> IN //

  val busy = DFBit  <> OUT //
  val clip = DFBool <> OUT //

  val frame_sys = DFBit <> VAR

  // local var
  val FB_PIXELS   = WIDTH * HEIGHT
  val FB_ADDRW    = FB_PIXELS >> 2 // root 2
  val FB_DEPTH    = FB_PIXELS
  val FB_DATAW    = CIDXW
  val FB_DUALPORT = true // separate read and write ports?

  val fb_addr_read  = DFUInt.until(FB_PIXELS) <> VAR
  val fb_addr_write = DFUInt.until(FB_PIXELS) <> VAR

  val fb_cidx_read    = DFUInt(CIDXW) <> VAR
  val fb_cidx_read_p1 = DFUInt(CIDXW) <> VAR

  val x_add        = DFSInt(CORDW)           <> VAR
  val fb_addr_line = DFUInt.until(FB_PIXELS) <> VAR
  val sys_timer    = Timer(100.MHz)
  val pixel_timer  = Timer(videoDefs.FPS.Hz) * videoDefs.AREA

  val xd_frame = new xd_df
  xd_frame.outDomain.clk <> sys_timer.isActive
  xd_frame.inDomain.clk  <> pixel_timer.isActive
  xd_frame.inDomain.i    <> frame
  xd_frame.outDomain.o   <> frame_sys

  // clk_sys
  fb_addr_line  := (sCoord.y * WIDTH).bits.uint
  fb_addr_write := fb_addr_line + sCoord.x.bits.uint.pipe

  val fb_we    = DFBool <> VAR
  val we_in_p1 = DFBool <> VAR

  val fb_cidx_write = DFBits(FB_DATAW) <> VAR
  val cidx_in_p1    = DFBits(FB_DATAW) <> VAR

  // clk_sys
  we_in_p1   := we;
  cidx_in_p1 := cidx; // draw colour
  clip := (sCoord.y < 0 || sCoord.y >= HEIGHT || sCoord.x < 0 || sCoord.x >= WIDTH) // (y < 0 || y >= HEIGHT || x < 0 || x >= WIDTH);  // clipped?
  // second stage
  if (busy || clip)
    fb_we := 0
  else
    fb_we       := we_in_p1
  fb_cidx_write := cidx_in_p1;

  val bram_sdp_inst1 = new bram_sdp(
    WIDTH  = FB_DATAW,
    DEPTH  = FB_DEPTH,
    INIT_F = ""
  )

  bram_sdp_inst1.we <> fb_we
  bram_sdp_inst1.addr_write <> fb_addr_write
  bram_sdp_inst1.data_in  <> fb_cidx_write
  bram_sdp_inst1.data_out <> fb_cidx_read

  // local var
  val LB_SCALE = SCALE
  val LB_LEN   = WIDTH
  val LB_BPC   = CHANW

  val lb_data_req = DFBool <> VAR
  val cnt_h = DFUInt.until(LB_LEN + 1) <> VAR // DFUInt.until(LB_LEN) unsigned int DFbit until...

  val lb_en_in  = DFBit  <> VAR
  val lb_en_out = DFBool <> VAR

  lb_en_in  := cnt_h < LB_LEN
  lb_en_out := de

  val LAT         = 3
  val lb_en_in_sr = DFBits(LAT) <> VAR

  // TODO: CONCAT
  lb_en_in_sr := (lb_en_in, lb_en_in_sr.bits(LAT - 1, 1))
  // if (rst_sys) lb_en_in_sr := 0

  if (fb_addr_read < FB_PIXELS - 1)
    if (lb_data_req)
      cnt_h                  := 0 // start new line
      if (!FB_DUALPORT) busy := 1 // set busy flag if not dual port
    else if (cnt_h < LB_LEN) // advance to start of next line
      cnt_h        := cnt_h + 1;
      fb_addr_read := fb_addr_read + 1;
    else cnt_h := LB_LEN

  // TODO: reset alike error
  if (frame_sys){
    fb_addr_read := 0
    busy         := 0
  }


  if (rst_sys){
      fb_addr_read := 0
      busy := 0
      cnt_h := LB_LEN
  }

  if (lb_en_in_sr == b"100"){
    busy := 0
  }

  val lb_in_0 = DFUInt(LB_BPC) <> VAR
  val lb_in_1 = DFUInt(LB_BPC) <> VAR
  val lb_in_2 = DFUInt(LB_BPC) <> VAR

  val lb_out_0 = DFUInt(LB_BPC) <> VAR
  val lb_out_1 = DFUInt(LB_BPC) <> VAR
  val lb_out_2 = DFUInt(LB_BPC) <> VAR

  val line_buffer = new line_buffer(
    WIDTH = LB_BPC,
    LEN   = LB_LEN,
    SCALE = LB_SCALE
  )

  line_buffer.rst_in  <> rst_sys
  line_buffer.rst_out <> rst_pix
  line_buffer.data_req <> lb_data_req
  line_buffer.en_in  <> lb_en_in_sr.bits(0)
  line_buffer.en_out <> lb_en_out
  line_buffer.din_0  <> lb_in_0 // data in (clk_in)
  line_buffer.din_1  <> lb_in_1
  line_buffer.din_2  <> lb_in_2
  line_buffer.dout_0 <> lb_out_0 // data out (clk_out)
  line_buffer.dout_1 <> lb_out_1
  line_buffer.dout_2 <> lb_out_2

  if (fb_addr_read < FB_PIXELS - 1)
    if (line_buffer.data_req)
      cnt_h                  := 0 // start new line
      if (!FB_DUALPORT) busy := 1; // set busy flag if not dual port
    else if (cnt_h < LB_LEN) // advance to start of next line
      cnt_h        := cnt_h + 1
      fb_addr_read := fb_addr_read + 1
  else cnt_h := LB_LEN;

  if (frame_sys)
    fb_addr_read := 0; // new frame
    busy         := 0; // LB reads don't cross frame boundary

  if (rst_sys)
    fb_addr_read := 0;
    busy         := 0;
    cnt_h        := LB_LEN; // don't start reading after reset

  if (lb_en_in_sr == b"100") busy := 0; // LB read done: match latency `LAT`

  fb_cidx_read_p1 := bram_sdp_inst1.data_in

  val CLUTW     = 3 * CHANW
  val clut_colr = DFBits(CLUTW) <> VAR

  val clut = new rom_async_df(
    WIDTH = CLUTW,
    DEPTH  = 2 << CIDXW, // scala.math.pow(2,CIDXW),
    INIT_F = ""
  )
  clut.addr <> fb_cidx_read_p1
  clut.data <> clut_colr

  // map colour index to palette using CLUT and read into LB

  lb_in_2 := clut.data.bits(CLUTW - 1, 2 * CHANW)
  lb_in_1 := clut.data.bits(2 * CHANW - 1, CHANW)
  lb_in_0 := clut.data.bits(CHANW - 1, 0)

  val lb_en_out_p1 = DFBool <> VAR

  lb_en_out_p1 := lb_en_out

  if (lb_en_out_p1)
    sColor.red := lb_out_2
  else
    sColor.red := 0

  if (lb_en_out_p1)
    sColor.green := lb_out_1
  else
    sColor.green := 0

  if (lb_en_out_p1)
    sColor.blue := lb_out_0
  else
    sColor.blue := 0
end framebuffer

// @main def hello: Unit =
// val top = new framebuffer(
//     CORDW  = 16,
//     WIDTH  = 320,
//     HEIGHT = 180,
//     CIDXW  = 4,
//     CHANW  = 4,
//     SCALE  = 2,
//     F_IMAGE = " ",
//     F_PALETTE = " "
// )
//     top.printCodeString
