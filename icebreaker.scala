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

class icebreaker extends Board:
    val pmod = Pmod()
    val fpga = lattice()
    val app = IntergratingApp()

    fpga.p1() <> Pmod.p42
    //TODO: fill pin from fpga to out io pin

class Pmod extends Interface:
    val p42, p45, p36, p31, p44 = Pin()
    val vga = VGA()
    //TODO: val hdmi = Hdmi()

    vga.hsync <> p42
    vga.vsync <> p36
    vga.r <> p45
    vga.g <> p31
    vga.b <> p44

class IntegrationA7(app : ProjectFApp):
    def build : Unit ={}
    def simulate : Unit = {}
    val board = A7Board()
    board.vga <> app.vga

val app1ForA7 = IntegrationA7(ProjectFVGATest)