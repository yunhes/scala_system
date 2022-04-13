// import DFiant.*
// import compiler._

// class rom_async(
//     val WIDTH : Int = 8,
//     val DEPTH : Int = 256, 
//     val ADDRW : Int = 16
// ) (using DFC) extends RTDesign:
//     val addr = DFBit <> IN
//     val data = DFBit <> OUT

//     var memory = DFBits(WIDTH).X(DEPTH) <> REG init initVector //TODO: init mem vectors

//     data := memory(addr)