// import DFiant.*
// import compiler._

// class bram_sdp (
//     val WIDTH : Int = 8, 
//     val DEPTH : Int = 256, 
//     val INIT_F : String = ""
// ) (using DFC) extends DFDesign:
//     val ADDRW = (DEPTH-1).bitsWidth(false)
// //cal clk 
//     val addr_write = DFBits(ADDRW) <> IN
//     val addr_read = DFBits(ADDRW) <> IN
//     val data_in = DFBits(WIDTH) <> IN
//     val data_out = DFBits(WIDTH) <> OUT
//     val we = DFBit <> IN
    
//     val initVector : Vector[DFBits[Int] <> TOKEN] = ???
//     val done = DFBit <> WIRE
//     val memory = DFBits(WIDTH).X(DEPTH) <> REG init initVector
    
//     if (we)
//       memory(addr_write) := data_in
    
//     data_out := memory(addr_read)

    
