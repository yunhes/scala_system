
class Pin():
    def <> (that : Pin) : Unit = ???

abstract class Interface

class VGA() extends Interface:
    val hsync = Pin()
    val vsync = Pin()
    val r,g,b = Pin()


class Buttons extends Interface

class PMod extends Interface:
    val p1,p2,p10 = Pin()

abstract class FPGA extends Interface

abstract class Board

class A7Board extends Board:
    val fpga = XilinxA7()
    val pmod = PMod_A7Board()
    val buttons = Buttons()

    fpga.AA <> pmod.p1
    fpga.X2 <> pmod.p10

class IceBreaker extends Board:
    val fpga = IceBreaker()
    val pmod = PMod_IceBreaker()
    val buttons = Buttons()


class PModVGA extends Board:
    val pmod = PMod()
    val vga = VGA()
    
    pmod.p1 <> vga.hsync
    pmod.p10 <> vga.r

class IceBreakerVGA extends Board:
//TODO: connect PmodVGA to IceBreaker Pmod
// set_io -nowarn hsync       47
// set_io -nowarn vsync       45
// set_io -nowarn rrggbb[5]          3
// set_io -nowarn rrggbb[4]          48
// set_io -nowarn rrggbb[3]          46
// set_io -nowarn rrggbb[2]          44
// set_io -nowarn rrggbb[1]          4
// set_io -nowarn rrggbb[0]          2
class IceBreaker extends Board:
    val pmod = PModVGA()
    val fpga = IceBreaker()

    pmod.p3 <> vga.blue[1]
    pmod.p48 <> vga.blue[0]
    pmod.p46 <> vga.green[1]
    pmod.p44 <> vga.green[0]
    pmod.p4 <> vga.red[1]
    pmod.p2 <> vga.red[0]

    pmod.p47 <> vga.hsync
    pmod.p45 <> vga.vsync


class Pll extends DFDesign

class DE10Board extends Board
    val fpga = DE10Board()
    //TODO: on board VGA port support

//class IceBreakerBoard extends Board

abstract class DFDesign

abstract class DFInterface

abstract class ProjectFApp extends DFDesign:
    val vga = VGA()
    val buttons = Buttons()
    val clock = Clock() //local clock
    val reset = Reset() //local reset

class ProjectFVGATest extends ProjectFApp
    //Your application migration from project F
    val result = drawLineTest()

class drawLineTest extends ProjectFVGATest
    //val clock    = Input(UInt(n.W))
    //val reset    = Input(UInt(1.W))
    val hsync  = Output(UInt(1.W))
    val vsync = Output(UInt(1.W))
    val vgaRed = Output(UInt(4.W))
    val vgaGreen = Output(UInt(4.W))
    val vgaBlue = Output(UInt(4.W))

class framebuffer extends drawLineTest
//TODO: drawline buffer here and subsequent module???


class IntegrationA7(app : ProjectFApp):
    def build : Unit ={}
    def simulate : Unit = {}
    val board = A7Board()
    board.vga <> app.vga

val app1ForA7 = IntegrationA7(ProjectFVGATest)

app1ForA7.build