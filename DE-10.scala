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

class AlteraMax10 extends FPGA:
    val PIN_AA1 = Pin()
    val PIN_V1 = Pin()
    val PIN_Y2 = Pin()
    val PIN_Y1 = Pin()
    val PIN_W1 = Pin()
    val PIN_T2 = Pin()
    val PIN_R2 = Pin()
    val PIN_R1 = Pin()
    val PIN_P1 = Pin()
    val PIN_T1 = Pin()
    val PIN_P4 = Pin()
    val PIN_N2 = Pin()
    val PIN_N3 = Pin()
    val PIN_N1 = Pin()

class DE10 extends Board:
    val fpga = AlteraMax10()
    val app = IntergratingApp()
    val vga = VGA()

    fpga.PIN_N3() <> vga.hsync()
    fpga.PIN_N1() <> vga.vsync()
    //TODO: construct module for the 4-bit resistor vga decoder



class IntegrationA7(app : ProjectFApp):
    def build : Unit ={}
    def simulate : Unit = {}
    val board = A7Board()
    board.vga <> app.vga

val app1ForA7 = IntegrationA7(ProjectFVGATest)