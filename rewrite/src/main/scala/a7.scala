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

class Artix extends FPGA:
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

class A7 extends Board:
    val pmodJB = PmodJB()
    val pmodJC = PmodJC()
    val fpga = Artix()
    val app = IntergratingApp()

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

class VgaPmod extends Board:
    val pmodJB = PmodJB()
    val pmodJC = PmodJC()
    val vga = VGA()

    vga.hsync <> pmodJB.PJC7()
    vga.vsync <> pmodJB.PJC8()

    vga.RED <> pmodJB.()
    vga.GREEN <> pmodJB.()
    vga.BLUE <> pmodJB.()
    //TODO: construct module for the 4-bit resistor vga decoder

class ConnectedBoard extends Board:
    val a7 = A7()
    val vgapmod = VgaPmod()
    a7.pmodJB <> vgapmod.pmodJB
    a7.pmodJC <> vgapmod.pmodJC

class PmodJB extends Interface:
    val PJB1 = Pin()
    val PJB2 = Pin()
    val PJB3 = Pin()
    val PJB4 = Pin()
    val PJB7 = Pin()
    val PJB8 = Pin()
    val PJB9 = Pin()
    val PJB10 = Pin()

    

class PmodJC extends Interface:
    val PJC1 = Pin()
    val PJC2 = Pin()
    val PJC3 = Pin()
    val PJC4 = Pin()
    val PJC7 = Pin()
    val PJC8 = Pin()

class IntergratingApp(app : ProjectFApp):
    def build : Unit ={}
    def simulate : Unit = {}
    val board = ConnectedBoard()
    board.vga <> app.vga

val app1ForLattice = IntergratingApp(ProjectFVGATest)