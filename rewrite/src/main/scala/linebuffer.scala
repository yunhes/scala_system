import DFiant.*
import compiler._

class line_buffer (
    val WIDTH : Int = 8,
    val LEN : Int = 640,
    val SCALE : Int = 1
)(using DFC) extends DFDesign:
    val clk_in = DFBit <> IN
    val clk_out = DFBit <> IN
    val rst_in = DFBit <> IN
    val rst_out = DFBit <> IN   
    val data_req = DFBool <> OUT
    val en_in = DFBit <> IN
    val en_out = DFBit <> IN
    val frame = DFBit <> IN
    val line = DFBit <> IN
    val din_0 = DFUInt(WIDTH) <> IN
    val din_1 = DFUInt(WIDTH) <> IN
    val din_2 = DFUInt(WIDTH) <> IN
    val dout_0 = DFUInt(WIDTH) <> OUT
    val dout_1 = DFUInt(WIDTH) <> OUT
    val dout_2 = DFUInt(WIDTH) <> OUT

    val set_end = DFBool <> VAR
    val get_data = DFBool <> VAR
    val addr_out = DFUInt(LEN) <> VAR

    val xd_req_inst = new xd_df
    xd_req_inst.i <> get_data
    xd_req_inst.o <> data_req

    val addr_in = DFUInt.until(LEN) <> VAR

    val cnt_v = DFUInt.until(SCALE) <> VAR
    val cnt_h = DFUInt.until(SCALE) <> VAR

    val bram_sdp_inst0 = new bram_sdp(
        WIDTH = WIDTH,
        DEPTH = LEN
    )
    bram_sdp_inst0.we <> en_in
    bram_sdp_inst0.addr_write <> addr_in
    bram_sdp_inst0.addr_read <> addr_out
    bram_sdp_inst0.data_in <> din_0
    bram_sdp_inst0.data_out <> dout_0

    val bram_sdp_inst1 = new bram_sdp(
        WIDTH = WIDTH,
        DEPTH = LEN
    )
    bram_sdp_inst1.we <> en_in
    bram_sdp_inst1.addr_write <> addr_in
    bram_sdp_inst1.addr_read <> addr_out
    bram_sdp_inst1.data_in <> din_1
    bram_sdp_inst1.data_out <> dout_1

    val bram_sdp_inst2 = new bram_sdp(
        WIDTH = WIDTH,
        DEPTH = LEN
    )
    bram_sdp_inst1.we <> en_in
    bram_sdp_inst1.addr_write <> addr_in
    bram_sdp_inst1.addr_read <> addr_out
    bram_sdp_inst1.data_in <> din_2
    bram_sdp_inst1.data_out <> dout_2


//clk_out
    if(frame)
        addr_out := 0
        cnt_h := 0
        cnt_v := 0
        set_end := 0
    else if(en_out && !set_end)
        if(cnt_h == SCALE-1)
            cnt_h := 0
            if(addr_out == LEN-1)
                addr_out := 0
                if(cnt_v == SCALE-1)
                    cnt_v := 0
                    set_end := 0
                else
                    cnt_v := cnt_v+1
            else
                addr_out := addr_out + 1
        else
            cnt_h := cnt_h + 1
    else
        set_end := 0
    if(rst_out)
        addr_out := 0
        cnt_h := 0
        cnt_v := 0
        set_end := 0
    get_data := line && set_end

    //read data in
    //clk in
    if (en_in) 
        if(addr_in == LEN-1)
            addr_in := 0
        else
            addr_in := 1
    if(data_req) 
        addr_in := 0
    if(rst_in)
        addr_in := 0


// @main def hello: Unit = 
//   val top = new line_buffer
//   top.printCodeString

