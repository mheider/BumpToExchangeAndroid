package de.hdrmrks

import java.io.IOException
import java.util.Observable

import android.bluetooth.{BluetoothSocket, BluetoothServerSocket, BluetoothAdapter}
import android.content.Context
import android.nfc.Tag
import android.util.Log

/**
 * Created by markus on 30.08.15.
 */
class BluetoothReceiver(bluetoothAdapter: BluetoothAdapter) extends Observable with Runnable{

  val TAG = "HelloScaloid"
  var serverSocket: BluetoothServerSocket = null

  try {
    Log.i(TAG, "Starting server socket")
    serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BumpToExchange", Constant.APP_UUID)
  } catch {
    case ioe : IOException => Log.e(TAG, "Could not create a server socket.")
  }

  override def run(): Unit = {
    var clientSocket : BluetoothSocket = null
    var run = true
    while(run) {
      try {
        Log.i(TAG, "Waiting for client")
        clientSocket = serverSocket.accept()
      } catch {
        case ioe: IOException => {run = false}
      }
      if (clientSocket != null) {
        Log.i(TAG, "created connection!")
        serverSocket.close()
        run = false
      }
    }
    val bluetoothSocketConnection  = new BluetoothSocketConnection(clientSocket)
    new Thread(bluetoothSocketConnection).start()
  }
}

