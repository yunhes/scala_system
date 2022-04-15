import DFiant.*
import compiler._

class bram_sdp (
    val WIDTH : Int = 8, 
    val DEPTH : Int = 256, 
    val INIT_F : String = ""
) (using DFC) extends DFDesign:
    // val ADDRW = (DEPTH-1).bitsWidth(false)
//cal clk 
    val addr_write = DFUInt.until(DEPTH) <> IN
    val addr_read = DFUInt.until(DEPTH) <> IN
    val data_in = DFUInt.until(DEPTH) <> IN
    val data_out = DFUInt.until(DEPTH) <> OUT
    val we = DFBit <> IN
    
    // val initVector : Vector[DFBits[Int] <> TOKEN] = ???
    val done = DFBit <> WIRE
    val memory = DFUInt.until(DEPTH) X(DEPTH) <> VAR 
    
    if (we)
      memory(addr_write) := data_in
    
    data_out := memory(addr_read)

    
