import DFiant.*
import compiler._

class bram_sdp (
    val WIDTH : Int = 8, 
    val DEPTH : Int = 256, 
    val INIT_F : Char = "",
    val ADDRW : 16) (using DFC) extends RTDesign:
//cal clk 
    val addr_write = DFBit <> IN
    val addr_read = DFBit <> IN
    val data_in = DFBit <> IN
    val data_out = DFBit <> OUT
    val we = DFBit <> IN

    var memory = new Array[Int](WIDTH * DEPTH)

    if we then
        memory[addr_write] := data_in

    data_out := memory[addr_read]


    
