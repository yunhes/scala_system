
class Pin():
    def <> (that : Pin) : Unit = ???

abstract class timming
    class Pll extends timming

    class clk(frequency : Int, pin : Pin) extends timming:
        val clkFrequency = frequency
        val clkPin = pin

abstract class Interface



class vgaMale() extends Interface:
    //this is only a wiring version of basic vga male port
    val RED = Pin(1)
    val GREEN = Pin(2)
    val BLUE = Pin(3)

    val GND = Pin(5)
    val hsync = Pin(13)
    val vsync = Pin(14)

class vgaFemale() extends Interface:
    val RED = Pin(1)
    val GREEN = Pin(2)
    val BLUE = Pin(3)
    val ID2 = Pin(4)
    val GND_Hsync = Pin(5)
    val RED_RTN = Pin(6)
    val GREEN_RTN = Pin(7)
    val BLUE_RTN = Pin(8)
    val PWRE = Pin(9)
    val GND_Vsync = Pin(10)
    val ID0 = Pin(11)
    val ID1 = Pin(12)
    val hsync = Pin(13)
    val vsync = Pin(14)
    val ID3 = Pin(15)


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
    val clk12 = clk(1200, 35)


class PModVGA extends Board:
    val pmod = PMod()
    val vga = VGA()
    //pmod.p3 <> vga.blue[1]
    pmod.p44 <> vga.blue[0]
    //pmod.p46 <> vga.green[1]
    pmod.p31 <> vga.green[0]
    //pmod.p4 <> vga.red[1]
    pmod.p45 <> vga.red[0]

    pmod.p42 <> vga.hsync
    pmod.p36 <> vga.vsync

class IceBreaker extends Board:
    val pmod = PModVGA()
    val fpga = IceBreaker()



class DE10Board extends Board
    val fpga = DE10Board()
    val vga = VGA() //since interface VGA is the same

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

    //TODO: connect module with VGA pmod
    //module pong(clk12, vga_h_sync, vga_v_sync, vga_R, vga_G, vga_B, quadA, quadB);

class framebuffer extends drawLineTest
//TODO: drawline buffer here and subsequent module???


class IntegrationA7(app : ProjectFApp):
    def build : Unit ={}
    def simulate : Unit = {}
    val board = A7Board()
    board.vga <> app.vga

val app1ForA7 = IntegrationA7(ProjectFVGATest)

app1ForA7.build