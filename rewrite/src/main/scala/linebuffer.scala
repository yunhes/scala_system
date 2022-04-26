import DFiant.*
import compiler._
// scalafmt: { align.tokens = [{code = "<>"}, {code = "="}, {code = "=>"}, {code = ":="}]}

class line_buffer(
    val WIDTH: Int,
    val LEN: Int,
    val SCALE: Int
)(using DFC)
    extends DFDesign:
  val clk_in   = DFBit         <> IN
  val clk_out  = DFBit         <> IN
  val rst_in   = DFBit         <> IN
  val rst_out  = DFBit         <> IN
  val data_req = DFBit         <> OUT
  val en_in    = DFBool        <> IN
  val en_out   = DFBool        <> IN
  val frame    = DFBool        <> IN
  val line     = DFBool        <> IN
  val din_0    = DFUInt(WIDTH) <> IN
  val din_1    = DFUInt(WIDTH) <> IN
  val din_2    = DFUInt(WIDTH) <> IN
  val dout_0   = DFUInt(WIDTH) <> OUT
  val dout_1   = DFUInt(WIDTH) <> OUT
  val dout_2   = DFUInt(WIDTH) <> OUT

  val set_end  = DFBool            <> VAR
  val get_data = DFBool            <> VAR
  val addr_out = DFUInt.until(LEN) <> VAR

  val sys_timer   = Timer(100.MHz)
  val pixel_timer = Timer(27.MHz)

  val xd_req_inst = new xd_df
  xd_req_inst.outDomain.clk <> sys_timer.isActive
  xd_req_inst.inDomain.clk  <> pixel_timer.isActive
  xd_req_inst.inDomain.i    <> get_data
  xd_req_inst.outDomain.o   <> data_req

  val addr_in = DFUInt.until(LEN) <> VAR

  val cnt_v = DFUInt.until(SCALE) <> VAR
  val cnt_h = DFUInt.until(SCALE) <> VAR

  val bram_sdp_inst0 = new bram_sdp(
    WIDTH  = WIDTH,
    DEPTH  = LEN,
    INIT_F = ""
  )
  bram_sdp_inst0.we         <> en_in
  bram_sdp_inst0.addr_write <> addr_in
  bram_sdp_inst0.addr_read  <> addr_out
  bram_sdp_inst0.data_in    <> din_0
  bram_sdp_inst0.data_out   <> dout_0

  val bram_sdp_inst1 = new bram_sdp(
    WIDTH  = WIDTH,
    DEPTH  = LEN,
    INIT_F = ""
  )
  bram_sdp_inst1.we         <> en_in
  bram_sdp_inst1.addr_write <> addr_in
  bram_sdp_inst1.addr_read  <> addr_out
  bram_sdp_inst1.data_in    <> din_1
  bram_sdp_inst1.data_out   <> dout_1

  val bram_sdp_inst2 = new bram_sdp(
    WIDTH  = WIDTH,
    DEPTH  = LEN,
    INIT_F = ""
  )
  bram_sdp_inst2.we         <> en_in
  bram_sdp_inst2.addr_write <> addr_in
  bram_sdp_inst2.addr_read  <> addr_out
  bram_sdp_inst2.data_in    <> din_2
  bram_sdp_inst2.data_out   <> dout_2

//clk_out
  if (frame)
    addr_out := 0
    cnt_h    := 0
    cnt_v    := 0
    set_end  := 1
  else if (en_out && !set_end)
    if (cnt_h == SCALE - 1)
      cnt_h := 0
      if (addr_out == LEN - 1)
        addr_out := 0
        if (cnt_v == SCALE - 1)
          cnt_v   := 0
          set_end := 1
        else cnt_v := cnt_v + 1
      else addr_out := addr_out + 1
    else cnt_h := cnt_h + 1
  else if (get_data)
    set_end := 0
  end if
  if (rst_out)
    addr_out := 0
    cnt_h    := 0
    cnt_v    := 0
    set_end  := 0
  get_data := line && set_end

  // read data in
  // clk in
  if (en_in)
    if (addr_in == LEN - 1)
      addr_in := 0
    else
      addr_in := addr_in + 1
  if (data_req)
    addr_in := 0
  if (rst_in)
    addr_in := 0
end line_buffer

// @main def hello: Unit =
//   val top = new line_buffer(
//         WIDTH = 4,
//         LEN = 320,
//         SCALE = 2
//     )
//   top.printCodeString
