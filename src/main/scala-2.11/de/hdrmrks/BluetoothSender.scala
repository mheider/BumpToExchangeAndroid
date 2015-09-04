package de.hdrmrks

import java.io.IOException
import java.nio.charset.{StandardCharsets, Charset}
import java.util.UUID

import android.bluetooth.{BluetoothSocket, BluetoothDevice}
import android.util.Log
import de.hdrmrks.bmp.Resource

/**
 * Created by markus on 30.08.15.
 */
class BluetoothSender(devices : List[BluetoothDevice]) extends Thread {

  val TAG = "HelloScaloid"

  var mmSocket: BluetoothSocket = null

  for (device <- devices) {
    Log.i(TAG, "Trying to establish RfCommSocket " + device.toString)
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



  Log.i(TAG, "starting to connect...")
  try {
    mmSocket.connect()
    Log.i(TAG, "connected...");
    val bluetoothSender = new BluetoothSocketConnection(mmSocket)
    val senderThread = new Thread(bluetoothSender)
    senderThread.start()
    Log.i(TAG, "Creating Resource")
    val resData = "Hello World!".getBytes(StandardCharsets.UTF_8)
    Log.i(TAG, resData.toString)
    val res = new Resource(resData, "Hello")
    Log.i(TAG, "Attempt to send")
    bluetoothSender.send(res)
    bluetoothSender.close
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
