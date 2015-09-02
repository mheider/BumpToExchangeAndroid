package de.hdrmrks

import java.io.IOException
import java.util.UUID

import android.bluetooth.{BluetoothSocket, BluetoothDevice}
import android.util.Log

/**
 * Created by markus on 30.08.15.
 */
class BluetoothConnection(devices : List[BluetoothDevice]) extends Thread{

  val TAG = "HelloScaloid"

  var mmSocket: BluetoothSocket = null

  for (device <- devices) {
    Log.i(TAG, "Trying to connect to " + device.toString)
    try {
      val tmp: BluetoothSocket = device.createRfcommSocketToServiceRecord(Constant.APP_UUID)
      mmSocket = tmp
      Log.i(TAG, "Opened BluetoothRFCOMMONSocket " + mmSocket.toString)
    } catch {
      case ioe: IOException => { Log.w(TAG, "could not connect") }
    }
  }
  if (mmSocket == null)
    throw new IOException("could not connect to the devices.")



  val connectionThread = new Thread(new Runnable {
    override def run(): Unit = {
      try {
        mmSocket.connect()
        Log.i(TAG, "bluetooth connected..");
      } catch {
        case ioe: IOException => {
          Log.e(TAG, "could not connect  "  + ioe.getMessage)
          try {
            mmSocket.close()
          } catch {
            case ioe2: IOException => Log.e(TAG, "THIS WENT WRONG!")
          }
        }
      }
    }
  })
  Log.i(TAG, "Starting connectionThread...")
  connectionThread.start()


}
