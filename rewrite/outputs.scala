final case class Color(
    red: DFUInt[4] <> VAL
    green: DFUInt[4] <> VAL
    blue: DFUInt[4] <> VAL
) extends DFStruct
final case class Coord(
    x: DFSInt[16] <> VAL
    y: DFSInt[16] <> VAL
) extends DFStruct
final case class DiagnolCoord(
    x0: DFSInt[16] <> VAL
    x1: DFSInt[16] <> VAL
    y0: DFSInt[16] <> VAL
    y1: DFSInt[16] <> VAL
) extends DFStruct

class display_timings_480p_df extends DFDesign:
  val hsync = DFBit <> OUT
  val vsync = DFBit <> OUT
  val de = DFBit <> OUT
  val frame = DFBit <> OUT init 0
  val line = DFBit <> OUT
  val coord = Coord <> VAR init Coord(x = sd"16'0", y = sd"16'0")
  val coord_out = Coord <> OUT init Coord(x = sd"16'0", y = sd"16'0")
  val fps_timer = Timer(16666.66666666666666666666666666667.us)
  val horizontal_timer = anon3a5e60d6 * 525
  val vertical_timer = horizontal_timer / 525
  hsync := (!((coord.x > sd"16'-144") && (coord.x <= sd"16'-48"))).bit
  vsync := (!((coord.y > sd"16'-35") && (coord.y <= sd"16'-33"))).bit
  de := ((coord.y >= d"16'0") && (coord.x >= d"16'0")).bit
  frame := ((coord.y == sd"16'-45") && (coord.x == sd"16'-160")).bit
  line := ((coord.y >= d"16'0") && (coord.x == sd"16'-160")).bit
  if (vertical_timer.isActive)
    if (coord.y == d"16'479") coord.y := sd"16'-45"
    else coord.y := coord.y.prev + sd"2'1"
  if (horizontal_timer.isActive)
    if (coord.x == d"16'639") coord.x := sd"16'-160"
    else coord.x := coord.x.prev + sd"2'1"
    coord_out := coord.prev
end display_timings_480p_df

class xd_df extends RTDesign(DerivedCfg):
  val inDomain = new RTDomain(DerivedCfg):
    val clk = DFBit <> IN
    val rst = DFBit <> IN
    val i = DFBit <> IN
    val toggle = DFBit <> REG init 0
    toggle.din := toggle ^ i
  val outDomain = new RTDomain(DerivedCfg):
    val clk = DFBit <> IN
    val rst = DFBit <> IN
    val o = DFBit <> OUT
    val shr = DFBits(4) <> REG init h"0"
    shr.din := (shr(2, 0), inDomain.toggle.bits).toBits
    o := shr(3) ^ shr(2)
end xd_df

class bram_sdp_0 extends DFDesign:
  val addr_write = DFUInt(16) <> IN
  val addr_read = DFUInt(16) <> IN
  val data_in = DFUInt(4) <> IN
  val data_out = DFUInt(4) <> OUT
  val we = DFBit <> IN
  val done = DFBit <> VAR
  val memory = DFUInt(4) X 57600 <> VAR
  if (we) memory(addr_write) := data_in
  data_out := memory(addr_read)
end bram_sdp_0

class bram_sdp_1 extends DFDesign:
  val addr_write = DFUInt(9) <> IN
  val addr_read = DFUInt(9) <> IN
  val data_in = DFUInt(4) <> IN
  val data_out = DFUInt(4) <> OUT
  val we = DFBit <> IN
  val done = DFBit <> VAR
  val memory = DFUInt(4) X 320 <> VAR
  if (we) memory(addr_write) := data_in
  data_out := memory(addr_read)
end bram_sdp_1

class bram_sdp_2 extends DFDesign:
  val addr_write = DFUInt(9) <> IN
  val addr_read = DFUInt(9) <> IN
  val data_in = DFUInt(4) <> IN
  val data_out = DFUInt(4) <> OUT
  val we = DFBit <> IN
  val done = DFBit <> VAR
  val memory = DFUInt(4) X 320 <> VAR
  if (we) memory(addr_write) := data_in
  data_out := memory(addr_read)
end bram_sdp_2

class bram_sdp_3 extends DFDesign:
  val addr_write = DFUInt(9) <> IN
  val addr_read = DFUInt(9) <> IN
  val data_in = DFUInt(4) <> IN
  val data_out = DFUInt(4) <> OUT
  val we = DFBit <> IN
  val done = DFBit <> VAR
  val memory = DFUInt(4) X 320 <> VAR
  if (we) memory(addr_write) := data_in
  data_out := memory(addr_read)
end bram_sdp_3

class line_buffer extends DFDesign:
  val clk_in = DFBit <> IN
  val clk_out = DFBit <> IN
  val rst_in = DFBit <> IN
  val rst_out = DFBit <> IN
  val data_req = DFBit <> OUT
  val en_in = DFBool <> IN
  val en_out = DFBool <> IN
  val frame = DFBool <> IN
  val line = DFBool <> IN
  val din_0 = DFUInt(4) <> IN
  val din_1 = DFUInt(4) <> IN
  val din_2 = DFUInt(4) <> IN
  val dout_0 = DFUInt(4) <> OUT
  val dout_1 = DFUInt(4) <> OUT
  val dout_2 = DFUInt(4) <> OUT
  val set_end = DFBool <> VAR
  val get_data = DFBool <> VAR
  val addr_out = DFUInt(9) <> VAR
  val sys_timer = Timer(0.01.us)
  val pixel_timer = Timer(0.03703703703703703703703703703703704.us)
  val xd_req_inst = new xd_df
  line_buffer.xd_req_inst.outDomain.clk <> sys_timer.isActive.bit
  line_buffer.xd_req_inst.inDomain.clk <> pixel_timer.isActive.bit
  line_buffer.xd_req_inst.inDomain.i <> get_data.bit
  data_req <> line_buffer.xd_req_inst.outDomain.o
  val addr_in = DFUInt(9) <> VAR
  val cnt_v = DFUInt(1) <> VAR
  val cnt_h = DFUInt(1) <> VAR
  val bram_sdp_inst0 = new bram_sdp_1
  bram_sdp_inst0.we <> en_in.bit
  bram_sdp_inst0.addr_write <> addr_in
  bram_sdp_inst0.addr_read <> addr_out
  bram_sdp_inst0.data_in <> din_0
  dout_0 <> bram_sdp_inst0.data_out
  val bram_sdp_inst1 = new bram_sdp_2
  bram_sdp_inst1.we <> en_in.bit
  bram_sdp_inst1.addr_write <> addr_in
  bram_sdp_inst1.addr_read <> addr_out
  bram_sdp_inst1.data_in <> din_1
  dout_1 <> bram_sdp_inst1.data_out
  val bram_sdp_inst2 = new bram_sdp_3
  bram_sdp_inst2.we <> en_in.bit
  bram_sdp_inst2.addr_write <> addr_in
  bram_sdp_inst2.addr_read <> addr_out
  bram_sdp_inst2.data_in <> din_2
  dout_2 <> bram_sdp_inst2.data_out
  if (frame)
    addr_out := d"9'0"
    cnt_h := d"1'0"
    cnt_v := d"1'0"
    set_end := true
  else if (en_out && (!set_end))
    if (cnt_h == d"1'1")
      cnt_h := d"1'0"
      if (addr_out == d"9'319")
        addr_out := d"9'0"
        if (cnt_v == d"1'1")
          cnt_v := d"1'0"
          set_end := true
        else cnt_v := cnt_v + d"1'1"
      else addr_out := addr_out + d"1'1"
    else cnt_h := cnt_h + d"1'1"
  else if (get_data) set_end := false
  if (rst_out)
    addr_out := d"9'0"
    cnt_h := d"1'0"
    cnt_v := d"1'0"
    set_end := false
  get_data := line && set_end
  if (en_in)
    if (addr_in == d"9'319") addr_in := d"9'0"
    else addr_in := addr_in + d"1'1"
  if (data_req) addr_in := d"9'0"
  if (rst_in) addr_in := d"9'0"
end line_buffer

class rom_async_df extends DFDesign:
  val addr = DFUInt(5) <> IN
  val data = DFUInt(12) <> OUT
  val memory = DFUInt(12) X 32 <> VAR
  data := memory(addr)
end rom_async_df

class framebuffer extends DFDesign:
  val clk_sys = DFBit <> IN
  val clk_pix = DFBit <> IN
  val rst_sys = DFBit <> IN
  val rst_pix = DFBit <> IN
  val de = DFBool <> IN
  val frame = DFBool <> IN
  val line = DFBool <> IN
  val we = DFBool <> IN
  val sCoord = Coord <> IN
  val sColor = Color <> OUT
  val cidx = DFUInt(4) <> IN
  val busy = DFBit <> OUT
  val clip = DFBool <> OUT
  val frame_sys = DFBit <> VAR
  val fb_addr_read = DFUInt(16) <> VAR
  val fb_addr_write = DFUInt(16) <> VAR
  val fb_cidx_read = DFUInt(4) <> VAR
  val fb_cidx_read_p1 = DFUInt(4) <> VAR
  val x_add = DFSInt(16) <> VAR
  val fb_addr_line = DFUInt(16) <> VAR
  val sys_timer = Timer(0.01.us)
  val pixel_timer = Timer(0.03703703703703703703703703703703704.us)
  val xd_frame = new xd_df
  fb_inst.xd_frame.outDomain.clk <> sys_timer.isActive.bit
  fb_inst.xd_frame.inDomain.clk <> pixel_timer.isActive.bit
  fb_inst.xd_frame.inDomain.i <> frame.bit
  frame_sys <> fb_inst.xd_frame.outDomain.o
  fb_addr_line := (sCoord.y * sd"10'320").bits.uint
  fb_addr_write := fb_addr_line + sCoord.x.bits.uint.pipe
  val fb_we = DFBool <> VAR
  val we_in_p1 = DFBool <> VAR
  val fb_cidx_write = DFBits(4) <> VAR
  val cidx_in_p1 = DFBits(4) <> VAR
  we_in_p1 := we
  cidx_in_p1 := cidx.bits
  clip := (((sCoord.y < d"16'0") || (sCoord.y >= d"16'180")) || (sCoord.x < d"16'0")) || (sCoord.x >= d"16'320")
  if (busy || clip.bit) fb_we := false
  else fb_we := we_in_p1
  fb_cidx_write := cidx_in_p1
  val bram_sdp_inst1 = new bram_sdp_0
  bram_sdp_inst1.we <> fb_we.bit
  bram_sdp_inst1.data_in <> fb_cidx_write.uint
  fb_cidx_read <> bram_sdp_inst1.data_out
  val lb_data_req = DFBool <> VAR
  val cnt_h = DFUInt(9) <> VAR
  val lb_en_in = DFBit <> VAR
  val lb_en_out = DFBool <> VAR
  lb_en_in := (cnt_h < d"9'320").bit
  lb_en_out := de
  val lb_en_in_sr = DFBits(3) <> VAR
  lb_en_in_sr
  lb_en_in_sr := (lb_en_in.bits, lb_en_in_sr(2, 1)).toBits
  if (fb_addr_read < d"16'57599")
    if (lb_data_req) cnt_h := d"9'0"
    else if (cnt_h < d"9'320")
      cnt_h := cnt_h + d"1'1"
      fb_addr_read := fb_addr_read + d"1'1"
    else cnt_h := d"9'320"
  if (frame_sys)
    fb_addr_read := d"16'0"
    busy := 0
  if (lb_en_in_sr == b"100") busy := 0
  val lb_in_0 = DFUInt(4) <> VAR
  val lb_in_1 = DFUInt(4) <> VAR
  val lb_in_2 = DFUInt(4) <> VAR
  val lb_out_0 = DFUInt(4) <> VAR
  val lb_out_1 = DFUInt(4) <> VAR
  val lb_out_2 = DFUInt(4) <> VAR
  val line_buffer = new line_buffer
  line_buffer.rst_in <> rst_sys
  line_buffer.rst_out <> rst_pix
  lb_en_in_sr
  line_buffer.en_in <> lb_en_in_sr(0).bool
  line_buffer.en_out <> lb_en_out
  line_buffer.din_0 <> lb_in_0
  line_buffer.din_1 <> lb_in_1
  line_buffer.din_2 <> lb_in_2
  lb_out_0 <> line_buffer.dout_0
  lb_out_1 <> line_buffer.dout_1
  lb_out_2 <> line_buffer.dout_2
  if (fb_addr_read < d"16'57599")
    if (line_buffer.data_req)
      cnt_h := d"9'0"
      if (false) busy := 1
      else if (cnt_h < d"9'320") cnt_h := cnt_h + d"1'1"
      fb_addr_read := fb_addr_read + d"1'1"
  else cnt_h := d"9'320"
  if (frame_sys)
    fb_addr_read := d"16'0"
    busy := 0
  if (rst_sys)
    fb_addr_read := d"16'0"
    busy := 0
    cnt_h := d"9'320"
  if (lb_en_in_sr == b"100") busy := 0
  fb_cidx_read_p1 := bram_sdp_inst1.data_in
  val clut_colr = DFBits(12) <> VAR
  val clut = new rom_async_df
  clut.addr <> fb_cidx_read_p1.resize(5)
  lb_in_2 := clut.data.bits(11, 8).uint
  lb_in_1 := clut.data.bits(7, 4).uint
  lb_in_0 := clut.data.bits(3, 0).uint
  val lb_en_out_p1 = DFBool <> VAR
  lb_en_out_p1 := lb_en_out
  if (lb_en_out_p1) sColor.red := lb_out_2
  else sColor.red := d"4'0"
  if (lb_en_out_p1) sColor.green := lb_out_1
  else sColor.green := d"4'0"
  if (lb_en_out_p1) sColor.blue := lb_out_0
  else sColor.blue := d"4'0"
end framebuffer

class draw_line_df extends DFDesign:
  enum State(val value: DFUInt[2] <> TOKEN) extends DFEnum.Manual(2):
    case IDLE extends State(d"2'0")
    case INIT_0 extends State(d"2'1")
    case INIT_1 extends State(d"2'2")
    case DRAW extends State(d"2'3")

  val start = DFBool <> IN
  val oe = DFBool <> IN
  val diagCoord_in = DiagnolCoord <> IN init DiagnolCoord(x0 = ?, x1 = ?, y0 = ?, y1 = ?)
  val coord = Coord <> OUT init Coord(x = sd"16'0", y = sd"16'0")
  val drawing = DFBool <> OUT
  val busy = DFBool <> OUT init false
  val done = DFBool <> OUT init false
  val swap = DFBool <> VAR
  val right = DFBool <> VAR
  val diagCoord = DiagnolCoord <> VAR init DiagnolCoord(x0 = ?, x1 = ?, y0 = ?, y1 = ?)
  val endCoord = Coord <> VAR
  swap := diagCoord_in.y0 > diagCoord_in.y1
  if (swap)
    val ret = DiagnolCoord <> VAR
    ret := diagCoord_in
    ret.x0 := diagCoord_in.x1
    ret.x1 := diagCoord_in.x0
    ret.y0 := diagCoord_in.y1
    ret.y1 := diagCoord_in.y0
    diagCoord := ret
  else diagCoord := diagCoord_in
  val err = DFSInt(17) <> VAR init sd"17'0"
  val dx = DFSInt(17) <> VAR init sd"17'0"
  val dy = DFSInt(17) <> VAR init sd"17'0"
  val movx = DFBool <> VAR
  val movy = DFBool <> VAR
  movx := (err >> 2) >= dy
  movy := (err >> 2) <= dx
  val state = State <> VAR init State.IDLE
  state match
    case State.DRAW =>
      if (oe)
        state := State.IDLE
        drawing := true
        if (coord == endCoord)
          busy := false
          done := true
        else
          if (movx)
            if (right) coord.x := coord.x.prev + sd"2'1"
            else coord.x := coord.x.prev - sd"2'1"
          err := err.prev + dy.prev
          if (movy)
            coord.y := coord.y.prev + sd"2'1"
            err := err.prev + dx.prev
          if (movx && movy)
            if (right) coord.x := coord.x.prev + sd"2'1"
            else coord.x := coord.x.prev - sd"2'1"
            coord.y := coord.y.prev + sd"2'1"
            err := (err.prev + dy.prev) + dx.prev
      else
        drawing := false
        state := state.prev
    case State.INIT_0 =>
      state := State.INIT_1
      drawing := false
      if (right) dx := (diagCoord.x1.prev - diagCoord.x0.prev).resize(17)
      else dx := (diagCoord.x0.prev - diagCoord.x1.prev).resize(17)
      dy := (diagCoord.y0.prev - diagCoord.y1.prev).resize(17)
    case State.INIT_1 =>
      state := State.DRAW
      drawing := false
      err := dx.prev + dy.prev
      coord.x := diagCoord.x0.prev
      coord.y := diagCoord.y0.prev
      endCoord.x := diagCoord.x1.prev
