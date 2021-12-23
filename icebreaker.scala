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
    val p1 = Pin()
    //TODO: fill pin by board image

class Icebreaker extends Board:
    val pmod1A = Pmod1A()
    val pmod1B = Pmod1B()
    val fpga = Lattice()
    val app = IntergratingApp()

    fpga.p42() <> pmod1B.P1B7()
    fpga.p36() <> pmod1B.P1B8()
    fpga.p45() <> pmod1A.P1A4()
    fpga.p31() <> pmod1B.P1B4()
    fpga.p44() <> pmod1A.P1A10()

class VgaPmod extends Board:
    val pmod1A = Pmod1A()
    val pmod1B = Pmod1B()
    val vga = VGA()

    vga.hsync <> pmod1B.P1B7()
    vga.vsync <> pmod1B.P1B8()
    vga.RED <> pmod1A.P1A4()
    vga.GREEN <> pmod1B.P1B4()
    vga.BLUE <> pmod1A.P1A10()

class ConnectedBoard extends Board:
    val icebreaker = Icebreaker()
    val vgapmod = VgaPmod()
    icebreaker.pmod <> vgapmod.pmod

class Pmod1A extends Interface:
    val P1A4 = Pin()
    val P1A10 = Pin()

class Pmod1B extends Interface:
    val P1B7 = Pin()
    val P1B8 = Pin()
    val P1B4 = Pin()

class IntergratingApp(app : ProjectFApp):
    def build : Unit ={}
    def simulate : Unit = {}
    val board = ConnectedBoard()
    board.vga <> app.vga

val app1ForLattice = IntergratingApp(ProjectFVGATest)