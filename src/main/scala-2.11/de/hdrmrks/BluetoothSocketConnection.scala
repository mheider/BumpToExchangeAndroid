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
  var inputStream: ObjectInputStream = null
  var outputStream: ObjectOutputStream = null

  try {
    Log.i(TAG, "Creating streams")
    outputStream = new ObjectOutputStream(socket.getOutputStream)
    outputStream.flush()
    inputStream = new ObjectInputStream(socket.getInputStream)
    Log.i(TAG, "streams created")
  } catch {
    case ioe : IOException => {
      Log.e(TAG, "Could not open input/output Stream")
      Log.e(TAG, ioe.getMessage)
    }
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
        val resource = inputStream.readObject().asInstanceOf[Resource]
        Log.i(TAG, "Got a resource. Name = " + resource.name)
        setChanged()
        notifyObservers(resource)
        run = false
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
    outputStream.writeObject(res)
  }

  /**
   * Close the connection
   */
  def close: Unit = {
    socket.close()
  }
}
