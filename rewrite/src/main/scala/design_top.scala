// scalafmt: { align.tokens = [{code = "<>"}, {code = "="}, {code = "=>"}, {code = ":="}]}

import DFiant.internals.CTName

import scala.collection.mutable
case class Pin(ifc: Interface, name: String):
  override def toString(): String = s"${ifc.name}.$name"
object Pin:
  def apply()(using ifc: Interface, n: CTName): Pin =
    val pin = new Pin(ifc, n.value)
    ifc.pins += pin
    pin

abstract class Board:
  val nets = mutable.Map.empty[Pin, Pin]
  extension (lhs: Pin)
    def <>(rhs: Pin): Unit =
      //   println(s"Connecting ${lhs.name} <> ${rhs.name}")
      nets += lhs -> rhs

abstract class Interface(val name: String):
  given Interface = this
  val pins        = mutable.ListBuffer.empty[Pin]

class VGA(name: String) extends Interface(name):
  val RED_0   = Pin()
  val RED_1   = Pin()
  val RED_2   = Pin()
  val GREEN_0 = Pin()
  val GREEN_1 = Pin()
  val GREEN_2 = Pin()
  val BLUE_0  = Pin()
  val BLUE_1  = Pin()
  val BLUE_2  = Pin()
  val hsync   = Pin()
  val vsync   = Pin()

class PmodJB(name: String) extends Interface(name):
  val PJB1  = Pin()
  val PJB2  = Pin()
  val PJB3  = Pin()
  val PJB4  = Pin()
  val PJB7  = Pin()
  val PJB8  = Pin()
  val PJB9  = Pin()
  val PJB10 = Pin()

class PmodJC(name: String) extends Interface(name):
  val PJC1 = Pin()
  val PJC2 = Pin()
  val PJC3 = Pin()
  val PJC4 = Pin()
  val PJC7 = Pin()
  val PJC8 = Pin()

class Artix(name: String) extends Interface(name):
  val E15 = Pin()
  val E16 = Pin()
  val D15 = Pin()
  val C15 = Pin()
  val J17 = Pin()
  val J18 = Pin()
  val K15 = Pin()
  val J15 = Pin()
  val U12 = Pin()
  val V12 = Pin()
  val V10 = Pin()
  val V11 = Pin()
  val U14 = Pin()
  val V14 = Pin()
end Artix

class Lattice(name: String) extends Interface(name):
  val p4  = Pin()
  val p2  = Pin()
  val p47 = Pin()
  val p45 = Pin()
  val p3  = Pin()
  val p48 = Pin()
  val p46 = Pin()
  val p44 = Pin()
  val p43 = Pin()
  val p38 = Pin()
  val p34 = Pin()
  val p31 = Pin()
  val p42 = Pin()
  val p36 = Pin()
end Lattice

class A7 extends Board:
  val pmodJB = new PmodJB("pmodJB")
  val pmodJC = new PmodJC("pmodJC")
  val fpga   = new Artix("fpga")
//   val app    = IntergratingApp()
  fpga.E15 <> pmodJB.PJB1
  fpga.E16 <> pmodJB.PJB2
  fpga.D15 <> pmodJB.PJB3
  fpga.C15 <> pmodJB.PJB4
  fpga.J17 <> pmodJB.PJB7
  fpga.J18 <> pmodJB.PJB8
  fpga.K15 <> pmodJB.PJB9
  fpga.J15 <> pmodJB.PJB10
  fpga.U12 <> pmodJC.PJC1
  fpga.V12 <> pmodJC.PJC2
  fpga.V10 <> pmodJC.PJC3
  fpga.V11 <> pmodJC.PJC4
  fpga.U14 <> pmodJC.PJC7
  fpga.V14 <> pmodJC.PJC8
end A7

class Icebreaker extends Board:
  val pmodJB = new PmodJB("pmodJB")
  val pmodJC = new PmodJC("pmodJC")
  val fpga   = new Lattice("fpga")
//   val app    = IntergratingApp()
  fpga.p4  <> pmodJB.PJB1
  fpga.p2  <> pmodJB.PJB2
  fpga.p47 <> pmodJB.PJB3
  fpga.p45 <> pmodJB.PJB4
  fpga.p3  <> pmodJB.PJB7
  fpga.p48 <> pmodJB.PJB8
  fpga.p46 <> pmodJB.PJB9
  fpga.p44 <> pmodJB.PJB10
  fpga.p43 <> pmodJC.PJC1
  fpga.p38 <> pmodJC.PJC2
  fpga.p34 <> pmodJC.PJC3
  fpga.p31 <> pmodJC.PJC4
  fpga.p42 <> pmodJC.PJC7
  fpga.p36 <> pmodJC.PJC8
end Icebreaker

class VgaPmod extends Board:
  val pmodJB = new PmodJB("pmodJB")
  val pmodJC = new PmodJC("pmodJC")
  val vga    = new VGA("vga")
  pmodJC.PJC7 <> vga.hsync
  pmodJC.PJC8 <> vga.vsync
  pmodJB.PJB1 <> vga.RED_0
  pmodJB.PJB2 <> vga.RED_1
  pmodJB.PJB3 <> vga.RED_2
  pmodJC.PJC1 <> vga.GREEN_0
  pmodJC.PJC2 <> vga.GREEN_1
  pmodJC.PJC3 <> vga.GREEN_2
  pmodJB.PJB7 <> vga.BLUE_0
  pmodJB.PJB8 <> vga.BLUE_1
  pmodJB.PJB9 <> vga.BLUE_2
end VgaPmod

class ConnectedBoard extends Board:
  val fpga    = new Icebreaker()
  val vgapmod = new VgaPmod()
  fpga.pmodJB.PJB1  <> vgapmod.pmodJB.PJB1
  fpga.pmodJB.PJB2  <> vgapmod.pmodJB.PJB2
  fpga.pmodJB.PJB3  <> vgapmod.pmodJB.PJB3
  fpga.pmodJB.PJB4  <> vgapmod.pmodJB.PJB4
  fpga.pmodJB.PJB7  <> vgapmod.pmodJB.PJB7
  fpga.pmodJB.PJB8  <> vgapmod.pmodJB.PJB8
  fpga.pmodJB.PJB9  <> vgapmod.pmodJB.PJB9
  fpga.pmodJB.PJB10 <> vgapmod.pmodJB.PJB10
  fpga.pmodJC.PJC1  <> vgapmod.pmodJC.PJC1
  fpga.pmodJC.PJC2  <> vgapmod.pmodJC.PJC2
  fpga.pmodJC.PJC3  <> vgapmod.pmodJC.PJC3
  fpga.pmodJC.PJC4  <> vgapmod.pmodJC.PJC4
  fpga.pmodJC.PJC7  <> vgapmod.pmodJC.PJC7
  fpga.pmodJC.PJC8  <> vgapmod.pmodJC.PJC8
end ConnectedBoard

// TODO, application, reset, clk setting
// // class IntergratingApp(app : ProjectFApp):
// //     def build : Unit ={}
// //     def simulate : Unit = {}
// //     val board = ConnectedBoard()
// //     board.vga <> app.vga

// // val app1ForLattice = IntergratingApp(ProjectFVGATest)

@main def hello: Unit =
  val top = new ConnectedBoard
  for ((k0, v0) <- top.fpga.nets)
    for ((k1, v1) <- top.nets)
      if (v0.name == k1.name)
        for ((k2, v2) <- top.vgapmod.nets)
          if (v1.name == k2.name)
            printf(
              "set_property -dict {PACKAGE_PIN %s  IOSTANDARD TMDS_33} [get_ports {%s}]\n",
              k0.name,
              v2.name
            )
