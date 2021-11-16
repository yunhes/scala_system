//top
module top_rectangles (
    input  wire logic clk_100m,     // 100 MHz clock
    input  wire logic btn_rst,      // reset button (active low)
    output      logic vga_hsync,    // horizontal sync
    output      logic vga_vsync,    // vertical sync
    output      logic [3:0] vga_r,  // 4-bit VGA red
    output      logic [3:0] vga_g,  // 4-bit VGA green
    output      logic [3:0] vga_b   // 4-bit VGA blue
    );
	
	//domain corss module
	module xd (
		input  wire logic clk_i,  //  input clock: source domain
		input  wire logic clk_o,  // output clock: destination domain
		input  wire logic rst_i,  //        reset: source domain
		input  wire logic rst_o,  //        reset: destination domain
		input  wire logic i,      //  input pulse: source domain
		output      logic o       // output pulse: destination domain
		);
	

//framebuffer top
module framebuffer_bram #(
    parameter CORDW=16,      // signed coordinate width (bits)
    parameter WIDTH=320,     // width of framebuffer in pixels
    parameter HEIGHT=180,    // height of framebuffer in pixels
    parameter CIDXW=4,       // colour index data width: 4=16, 8=256 colours
    parameter CHANW=4,       // width of RGB colour channels (4 or 8 bit)
    parameter SCALE=4,       // display output scaling factor (>=1)
    parameter F_IMAGE="",    // image file to load into framebuffer
    parameter F_PALETTE=""   // palette file to load into CLUT
    ) (
    input  wire logic clk_sys,    // system clock
    input  wire logic clk_pix,    // pixel clock
    input  wire logic rst_sys,    // reset (clk_sys)
    input  wire logic rst_pix,    // reset (clk_pix)
    input  wire logic de,         // data enable for display (clk_pix)
    input  wire logic frame,      // start a new frame (clk_pix)
    input  wire logic line,       // start a new screen line (clk_pix)
    input  wire logic we,         // write enable
    input  wire logic signed [CORDW-1:0] x,  // horizontal pixel coordinate
    input  wire logic signed [CORDW-1:0] y,  // vertical pixel coordinate
    input  wire logic [CIDXW-1:0] cidx,   // framebuffer colour index
    output      logic busy,               // busy with reading for display output
    output      logic clip,               // pixel coordinate outside buffer
    output      logic [CHANW-1:0] red,    // colour output to display (clk_pix)
    output      logic [CHANW-1:0] green,  //     "    "    "    "    "
    output      logic [CHANW-1:0] blue    //     "    "    "    "    "
)
	
	module bram_sdp #(
		parameter WIDTH=8, 
		parameter DEPTH=256, 
		parameter INIT_F="",
		localparam ADDRW=$clog2(DEPTH)
		) (
		input wire logic clk_write,                 // write clock (port a)
		input wire logic clk_read,                  // read clock (port b)
		input wire logic we,                        // write enable (port a)
		input wire logic [ADDRW-1:0] addr_write,    // write address (port a)
		input wire logic [ADDRW-1:0] addr_read,     // read address (port b)
		input wire logic [WIDTH-1:0] data_in,       // data in (port a)
		output     logic [WIDTH-1:0] data_out       // data out (port b)
	);
		
	module linebuffer #(
		parameter WIDTH=8,    // data width of each channel
		parameter LEN=640,    // length of line
		parameter SCALE=1     // scaling factor (>=1)
		) (
		input  wire logic clk_in,    // input clock
		input  wire logic clk_out,   // output clock
		input  wire logic rst_in,    // reset (clk_in)
		input  wire logic rst_out,   // reset (clk_out)
		output      logic data_req,  // request input data (clk_in)
		input  wire logic en_in,     // enable input (clk_in)
		input  wire logic en_out,    // enable output (clk_out)
		input  wire logic frame,     // start a new frame (clk_out)
		input  wire logic line,      // start a new line (clk_out)
		input  wire logic [WIDTH-1:0] din_0,  din_1,  din_2,  // data in (clk_in)
		output      logic [WIDTH-1:0] dout_0, dout_1, dout_2  // data out (clk_out)
	);
	
	module rom_async #(
		parameter WIDTH=8,
		parameter DEPTH=256,
		parameter INIT_F="",
		localparam ADDRW=$clog2(DEPTH)
		) (
		input wire logic [ADDRW-1:0] addr,
		output     logic [WIDTH-1:0] data
    );

//function
module draw_any #(parameter CORDW=16) (  // signed coordinate width
    input  wire logic clk,             // clock
    input  wire logic rst,             // reset
    input  wire logic start,           // start rectangle drawing
    input  wire logic oe,              // output enable
    input  wire logic signed [CORDW-1:0] x0, y0,  // vertex 0
    input  wire logic signed [CORDW-1:0] x1, y1,  // vertex 2
    output      logic signed [CORDW-1:0] x,  y,   // drawing position
    output      logic drawing,         // actively drawing
    output      logic busy,            // drawing request in progress
    output      logic done             // drawing is complete (high for one tick)
);
	
	module draw_controller (  // signed coordinate width

    );
	
	module draw_line #(parameter CORDW=16) (  // signed coordinate width
		input  wire logic clk,             // clock
		input  wire logic rst,             // reset
		input  wire logic start,           // start line drawing
		input  wire logic oe,              // output enable
		input  wire logic signed [CORDW-1:0] x0, y0,  // point 0
		input  wire logic signed [CORDW-1:0] x1, y1,  // point 1
		output      logic signed [CORDW-1:0] x,  y,   // drawing position
		output      logic drawing,         // actively drawing
		output      logic busy,            // drawing request in progress
		output      logic done             // drawing is complete (high for one tick)
    );

//pll
module clock_gen_480p #(
    parameter MULT_MASTER=31.5,   // master clock multiplier (2.000-64.000)
    parameter DIV_MASTER=5,       // master clock divider (1-106)
    parameter DIV_PIX=25,         // pixel clock divider (1-128)
    parameter IN_PERIOD=10.0      // period of master clock in ns
    ) (
    input  wire logic clk,        // board oscillator
    input  wire logic rst,        // reset
    output      logic clk_pix,    // pixel clock
    output      logic clk_locked  // generated clocks locked?
    );
	
module display_timings_480p #(
    CORDW=16,   // signed coordinate width (bits)
    H_RES=640,  // horizontal resolution (pixels)
    V_RES=480,  // vertical resolution (lines)
    H_FP=16,    // horizontal front porch
    H_SYNC=96,  // horizontal sync
    H_BP=48,    // horizontal back porch
    V_FP=10,    // vertical front porch
    V_SYNC=2,   // vertical sync
    V_BP=33,    // vertical back porch
    H_POL=0,    // horizontal sync polarity (0:neg, 1:pos)
    V_POL=0     // vertical sync polarity (0:neg, 1:pos)
    ) (
    input  wire logic clk_pix,  // pixel clock
    input  wire logic rst,      // reset
    output      logic hsync,    // horizontal sync
    output      logic vsync,    // vertical sync
    output      logic de,       // data enable (low in blanking intervals)
    output      logic frame,    // high at start of frame
    output      logic line,     // high at start of active line
    output      logic signed [CORDW-1:0] sx,  // horizontal screen position
    output      logic signed [CORDW-1:0] sy   // vertical screen position
);