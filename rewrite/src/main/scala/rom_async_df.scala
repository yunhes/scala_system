import DFiant.*
import compiler._
// scalafmt: { align.tokens = [{code = "<>"}, {code = "="}, {code = "=>"}, {code = ":="}]}

class rom_async_df(
    val WIDTH: Int,
    val DEPTH: Int,
    val INIT_F: String
)(using DFC)
    extends DFDesign:


  val addr = DFUInt.until(DEPTH) <> IN
  val data = DFUInt(WIDTH)       <> OUT

  val memory = DFUInt(WIDTH) X DEPTH <> VAR
  // memory.fill(DEPTH).(0)

  data := memory(addr)
end rom_async_df

// @main def hello: Unit =
//     val top = new rom_async_df(
//         WIDTH = 12,
//         DEPTH = 2 << 4,
//         INIT_F = ""
//     )
//     top.printCodeString
