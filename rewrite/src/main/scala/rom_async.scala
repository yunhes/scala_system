import DFiant.*
import compiler._

class rom_async(
    val WIDTH : Int = 8,
    val DEPTH : Int = 256, 
    val ADDRW : Int = 16
) (using DFC) extends RTDesign:
    val addr = DFBit <> IN
    val data = DFBit <> OUT

    var memory = new Array[Int](WIDTH * DEPTH)

    data := memory[addr]