class vga() extends Interface:
    //this is only a wiring version of basic vga male port
    val RED = Pin()
    val GREEN = Pin()
    val BLUE = Pin()

    val hsync = Pin()
    val vsync = Pin()

    
class Pin():
    def <> (that : Pin) : Unit = ???

abstract class FPGA extends Interface

abstract class Board

class Lattice extends FPGA:
    val p4 = Pin()
    val p2 = Pin()
    val p47 = Pin()
    val p45 = Pin()
    val p3 = Pin()
    val p48 = Pin()
    val p46 = Pin()
    val p44 = Pin()
    val p43 = Pin()
    val p38 = Pin()
    val p34 = Pin()
    val p31 = Pin()
    val p42 = Pin()
    val p36 = Pin()

class Icebreaker extends Board:
    val pmod1A = Pmod1A()
    val pmod1B = Pmod1B()
    val fpga = Lattice()
    val app = IntergratingApp()

    fpga.p4 <> pmod1A.P1A1
    fpga.p2 <> pmod1A.P1A2
    fpga.p47 <> pmod1A.P1A3
    fpga.p45 <> pmod1A.P1A4
    fpga.p3 <> pmod1A.P1A7
    fpga.p48 <> pmod1A.P1A8
    fpga.p46 <> pmod1A.P1A9
    fpga.p44 <> pmod1A.P1A10
    fpga.p43 <> pmod1B.P1B1
    fpga.p38 <> pmod1B.P1B2
    fpga.p34 <> pmod1B.P1B3
    fpga.p31 <> pmod1B.P1B4
    fpga.p42 <> pmod1B.P1B7
    fpga.p36 <> pmod1B.P1B8

class VgaPmod extends Board:
    val pmod1A = Pmod1A()
    val pmod1B = Pmod1B()
    val vga = VGA()

    vga.hsync <> pmod1B.P1B7()
    vga.vsync <> pmod1B.P1B8()
    //TODO: construct module for the 4-bit resistor vga decoder

class ConnectedBoard extends Board:
    val icebreaker = Icebreaker()
    val vgapmod = VgaPmod()
    icebreaker.pmod1A <> vgapmod.pmod1A
    icebreaker.pmod1B <> vgapmod.pmod1B

class Pmod1A extends Interface:
    val P1A1 = Pin()
    val P1A2 = Pin()
    val P1A3 = Pin()
    val P1A4 = Pin()
    val P1A7 = Pin()
    val P1A8 = Pin()
    val P1A9 = Pin()
    val P1A10 = Pin()

class Pmod1B extends Interface:
    val P1B1 = Pin()
    val P1B2 = Pin()
    val P1B3 = Pin()
    val P1B4 = Pin()
    val P1B7 = Pin()
    val P1B8 = Pin()

class IntergratingApp(app : ProjectFApp):
    def build : Unit ={}
    def simulate : Unit = {}
    val board = ConnectedBoard()
    board.vga <> app.vga

val app1ForLattice = IntergratingApp(ProjectFVGATest)