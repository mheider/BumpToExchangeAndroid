package de.hdrmrks

import java.io.IOException

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.nfc.Tag
import android.util.Log

/**
 * Created by markus on 30.08.15.
 */
class BluetoothReceiver(bluetoothAdapter: BluetoothAdapter) extends Thread{

  val TAG = "HelloScaloid"

  try {
    var serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BumpToExchange", Constant.APP_UUID)
  } catch {
    case ioe : IOException => Log.e(TAG, "Could not create a server socket.")
  }

  override def run(): Unit = {

  }

}
