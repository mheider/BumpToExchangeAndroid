package de.hdrmrks

import java.io._
import java.nio.charset.StandardCharsets
import java.util.Observable

import android.bluetooth.BluetoothSocket
import android.util.Log
import de.hdrmrks.bmp.Resource

/**
 * Created by markus on 02.09.15.
 */
class BluetoothSocketConnection(socket: BluetoothSocket) extends Observable with Runnable {


  val TAG = "HelloScaloid"
  Log.i(TAG, "BluetoothSocketConnection created")
  var inputStream: InputStream = null
  var outputStream: OutputStream = null

  try {
    inputStream = socket.getInputStream
    outputStream = socket.getOutputStream
  } catch {
    case ioe : IOException => {}
  }

  /**
   * Implementation for Runnable Interface.
   */
  override def run(): Unit = {
    Log.i(TAG, "BluetoothSocketConnection started.")
    var buffer : Array[Byte] = new Array[Byte](1024)
    var bytes: Int = 0
    var run = true
    while(run) {
      try {
        bytes = inputStream.read(buffer)
        Log.i(TAG, new String(buffer, StandardCharsets.UTF_8))
      } catch {
        case ioe: IOException =>  {
          run = false
        }
      }
    }
  }

  /**
   * Send a Resource via the connected BluetoothSocketConnection.
   * @param res
   */
  def send(res : Resource): Unit = {
    Log.i(TAG, "sending... " + res.toString)
    while(outputStream == null) {} // wait
    outputStream.write(res.data)
  }

  /**
   * Close the connection
   */
  def close: Unit = {
    socket.close()
  }
}
