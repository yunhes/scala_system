import DFiant.*
import compiler._

class framebuffer(
    val CORDW : Int = 16,
    val WIDTH : Int = 8,
    val HEIGHT : Int = 180,
    val CIDXW : Int = 4,
    val CHANW : Int = 4,
    val SCALE : Int = 4,
    //F_IMAGE F_PALETTE 
)(using DFC) extends RTDesign: 
    val clk_sys = DFBit <> IN  // system clock
    val clk_pix = DFBit <> IN    // pixel clock
    val rst_sys = DFBit <> IN    // reset (clk_sys)
    val rst_pix = DFBit <> IN    // reset (clk_pix)
    val de = DFBit <> IN        // data enable for display (clk_pix)
    val frame = DFBit <> IN      // start a new frame (clk_pix)
    val line = DFBit <> IN       // start a new screen line (clk_pix)
    val we = DFBit <> IN        // write enable   
    val x = DFBits(CORDW) <> IN    //
    val y = DFBits(CORDW) <> IN    //
    val cidx = DFBits(CIDXW) <> IN    //
    
    val busy = DFBit <> OUT    //
    val clip = DFBit <> OUT    //
    val red = DFBits(CHANW) <> OUT
    val green = DFBits(CHANW) <> OUT 
    val blue = DFBits(CHANW) <> OUT

    val frame_sys : DFBit <> WIRE

    //local var
    val FB_PIXELS : Int = WIDTH * HEIGHT;
    val FB_ADDRW  : Int = $clog2(FB_PIXELS);
    val FB_DEPTH  : Int = DFBitsFB_PIXELS;
    val FB_DATAW  : Int = DFBitsCIDXW;
    val FB_DUALPORT : Int =1;  // separate read and write ports?

    val fb_addr_read : DFBits(FB_ADDRW) <> WIRE
    val fb_addr_write : DFBits(FB_ADDRW) <> WIRE
    val fb_cidx_read : DFBits(FB_DATAW) <> WIRE
    val fb_cidx_read_p1 : DFBits(FB_DATAW) <> WIRE

    val x_add : DFBits(CORDW) <> WIRE
    val fb_addr_line : DFBits(FB_ADDRW) <> WIRE

    //clk_sys
    fb_addr_line := WIDTH * y
    x_add := x
    fb_addr_write := fb_addr_line + x_add

    val fb_we : DFBit <> WIRE
    val we_in_p1 : DFBit <> WIRE

    val fb_cidx_write : DFBits(FB_DATAW) <> WIRE
    val cidx_in_p1 : DFBits(FB_DATAW) <> WIRE  

    //clk_sys
    we_in_p1 := we;
    cidx_in_p1 := cidx;  // draw colour
    clip := (y < 0 or y >= HEIGHT or x < 0 or x >= WIDTH);  // clipped?
    // second stage
    if(busy || clip)
        fb_we := 0
    else
        fb_we := we_in_p1
    fb_cidx_write := cidx_in_p1;

    val bram_sdp_inst1 = new bram_sdp(
    WIDTH = FB_DATAW
    DEPTH = FB_DEPTH
    )
    bram_sdp_inst1.clk_write <> clk_sys
    bram_sdp_inst1.clk_out <> clk_sys
    bram_sdp_inst1.we <> fb_we
    bram_sdp_inst1.addr_write <> fb_addr_write
    bram_sdp_inst1.addr_out <> fb_addr_read
    bram_sdp_inst1.data_in <> fb_cidx_write
    bram_sdp_inst1.data_out <> fb_cidx_read


    val xd_frame = new xd_frame
    xd_frame.clk_i <> clk_pix
    xd_frame.clk_sys <> clk_sys
    xd_frame.rst_i <> rst_i
    xd_frame.rst_o <> rst_o
    xd_frame.i <> frame
    xd_frame.o <> frame_sys

    //local var
    val LB_SCALE : Int = SCALE
    val LB_LEN : Int = WIDTH
    val LB_BPC : Int = CHANW

    val lb_data_req : DFBit <> WIRE
    val cnt_h : DFBits($clog2(LB_LEN+1)) <> WIRE  //DFUInt.until(LB_LEN) unsigned int DFbit until...

    val lb_en_in : DFBit <> WIRE
    val lb_en_out : DFBit <> WIRE

    lb_en_in := cnt_h < LB_LEN
    lb_en_out := de

    val LAT : Int = 3
    val lb_en_in_sr : DFBits(LAT) <> WIRE 
    
    lb_en_in_sr := {lb_en_in, lb_en_in_sr[LAT-1:1]}
    if (rst_sys) lb_en_in_sr := 0

    if (fb_addr_read < FB_PIXELS-1)
        if (lb_data_req)
            cnt_h := 0  // start new line
            if (!FB_DUALPORT) busy := 1;    // set busy flag if not dual port
        else if (cnt_h < LB_LEN)  // advance to start of next line
            cnt_h := cnt_h + 1
            fb_addr_read := fb_addr_read + 1
        
    else cnt_h := LB_LEN;
    if (frame_sys) 
        fb_addr_read := 0;  // new frame
        busy := 0;  // LB reads don't cross frame boundary
    
    if (rst_sys) 
        fb_addr_read := 0;
        busy := 0;
        cnt_h := LB_LEN;  // don't start reading after reset
    
    if (lb_en_in_sr == 3'b100) busy := 0;  // LB read done: match latency `LAT`

    val lb_in_0 : DFBits(LB_BPC) <> WIRE 
    val lb_in_1 : DFBits(LB_BPC) <> WIRE 
    val lb_in_2 : DFBits(LB_BPC) <> WIRE 

    val lb_out_0 : DFBits(LB_BPC) <> WIRE 
    val lb_out_1 : DFBits(LB_BPC) <> WIRE 
    val lb_out_2 : DFBits(LB_BPC) <> WIRE 

    val line_buffer = new line_buffer(
        WIDTH = LB_BPC
        LEN = LB_LEN
        SCALE = LB_SCALE
    )
    line_buffer.clk_in <> clk_sys
    line_buffer.clk_out <> clk_pix
    line_buffer.rst_in <> rst_sys
    line_buffer.rst_out<> rst_pix
    line_buffer.data_req <> lb_data_req
    line_buffer.en_in <> lb_en_in_sr[0]
    line_buffer.en_out <> lb_en_out
    line_buffer.din_0 <> lb_in_0        // data in (clk_in)
    line_buffer.din_1 <> lb_in_1
    line_buffer.din_2 <> lb_in_2
    line_buffer.dout_0 <> lb_out_0       // data out (clk_out)
    line_buffer.dout_1 <> lb_out_1
    line_buffer.dout_2 <> lb_out_2    

    if (clk_sys)
    fb_cidx_read_p1 := fb_cidx_read;

    val CLUTW : 3 * CHANW
    val clut_colr : DFBits(CLUTW) <> WIRE 

    val clut = new rom_async (
        WIDTH = CLUTW
        DEPTH = 2**CIDXW
        INIT_F = F_PALETTE        
    )
    clut.addr <> fb_cidx_read_p1
    clut.data <> clut_colr

        // map colour index to palette using CLUT and read into LB
    // always_ff @(posedge clk_sys) {lb_in_2, lb_in_1, lb_in_0} <= clut_colr;

    val lb_en_out_p1 : DFBit <> WIRE
    if (clk_pix) 
        lb_en_out_p1 := lb_en_out
    
    if(lb_en_out_p1)
        red := lb_out_2
    else 
        red := 0
    
    if(lb_en_out_p1)
        green := lb_out_1
    else 
        green := 0

    if(lb_en_out_p1)
        blue := lb_out_0
    else 
        blue := 0

