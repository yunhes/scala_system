import DFiant.*
import compiler._

class rom_async_df(
    val WIDTH : Int, 
    val DEPTH : Int, 
    val INIT_F : String
) (using DFC) extends DFDesign:

    // val bit_wdith = DFUInt.until(DEPTH)

    val addr = DFUInt.until(DEPTH) <> IN
    val data = DFUInt(WIDTH) <> OUT


    val memory = DFUInt(WIDTH) X DEPTH <> VAR
    // memory.fill(DEPTH).(0)

    data := memory(addr)


// @main def hello: Unit =
//     val top = new rom_async_df(
//         WIDTH = 12, 
//         DEPTH = 2 << 4,
//         INIT_F = "" 
//     )
//     top.printCodeString