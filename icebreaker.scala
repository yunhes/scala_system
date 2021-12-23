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
    val pmod = Pmod()
    val fpga = lattice()
    val app = IntergratingApp()

    fpga.p42() <> pmod.P1B7()
    fpga.p36() <> pmod.P1B8()
    fpga.p45() <> pmod.P1A4()
    fpga.p31() <> pmod.P1B4()
    fpga.p44() <> pmod.P1A10()

class VgaPmod extends Board:
    val pmod = Pmod()
    val vga = VGA()

    vga.hsync <> pmod.P1B7()
    vga.vsync <> pmod.P1B8()
    vga.RED <> pmod.P1A4()
    vga.GREEN <> pmod.P1B4()
    vga.BLUE <> pmod.P1A10()

class ConnectedBoard extends Board:
    val icebreaker = Icebreaker()
    val vgapmod = VgaPmod()
    icebreaker.pmod <> vgapmod.pmod

class Pmod extends Interface:
    val P1B7 = Pin()
    val P1B8 = Pin()
    val P1A4 = Pin()
    val P1B4 = Pin()
    val P1A10 = Pin()

class IntergratingApp(app : ProjectFApp):
    def build : Unit ={}
    def simulate : Unit = {}
    val board = ConnectedBoard()
    board.vga <> app.vga

val app1ForLattice = IntergratingApp(ProjectFVGATest)