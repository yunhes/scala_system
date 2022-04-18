import DFiant.*
import compiler._

class rom_async_df(
    val WIDTH : Int, 
    val DEPTH : Int, 
    val INIT_F : String = ""
) (using DFC) extends DFDesign:

    // val bit_wdith = DFUInt.until(DEPTH)

    val addr = DFUInt.until(DEPTH) <> IN
    val data = DFUInt(WIDTH) <> OUT


    var memory = DFUInt(WIDTH) X DEPTH <> VAR
    // memory.fill(DEPTH).(0)

    data := memory(addr)