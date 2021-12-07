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

class XilinxA7 extends FPGA:
    val p1 = Pin()
    //TODO: fill pin by board image

class A7Board extends Board:
    val fpga = a7()
    val app = IntergratingApp()
    val vga = VGA()

    fpga.p1() <> vga.hsync()
    //TODO: fill pin from fpga to out io pin



class IntegrationA7(app : ProjectFApp):
    def build : Unit ={}
    def simulate : Unit = {}
    val board = A7Board()
    board.vga <> app.vga

val app1ForA7 = IntegrationA7(ProjectFVGATest)