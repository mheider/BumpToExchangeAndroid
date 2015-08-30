package de.hdrmrks

import java.io.IOException
import java.util.UUID

import android.bluetooth.{BluetoothSocket, BluetoothDevice}
import android.util.Log

/**
 * Created by markus on 30.08.15.
 */
class BluetoothConnection(device : BluetoothDevice) extends Thread{

  val TAG = "HelloScaloid"

  Log.i(TAG, "Trying to connect to " + device.toString)
  val mmSocket : BluetoothSocket = device.createRfcommSocketToServiceRecord(Constant.APP_UUID);
  Log.i(TAG, "Opened BluetoothRFCOMMONSocket " + mmSocket.toString)

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
