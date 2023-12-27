import com.fazecast.jSerialComm.SerialPort
import java.util.*

fun main() {
    val console = Scanner(System.`in`)
    println("List COM ports")
    val comPorts = SerialPort.getCommPorts()
    for (i in comPorts.indices) println("comPorts[" + i + "] = " + comPorts[i].descriptivePortName)
    val port = 0 // array index to select COM port
    comPorts[port].openPort()
    println("open port comPorts[" + port + "]  " + comPorts[port].descriptivePortName)
    comPorts[port].setBaudRate(115200)
    try {
        while (true) {
            // if keyboard token entered read it
            if (System.`in`.available() > 0) {
                //System.out.println("enter chars ");
                val s = console.nextLine() + "\n" // read token
                val writeBuffer = s.toByteArray()
                comPorts[port].writeBytes(writeBuffer, writeBuffer.size)
                //System.out.println("write " + writeBuffer.length);
            }
            // read serial port  and display data
            while (comPorts[port].bytesAvailable() > 0) {
                val readBuffer = ByteArray(comPorts[port].bytesAvailable())
                val numRead = comPorts[port].readBytes(readBuffer, readBuffer.size)
                //System.out.print("Read " + numRead + " bytes from COM port: ");
                for (i in readBuffer.indices) print(Char(readBuffer[i].toUShort()))
                //System.out.println();
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    comPorts[port].closePort()
}