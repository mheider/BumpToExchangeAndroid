package de.hdrmrks

import java.io.{RandomAccessFile, FileInputStream, File, IOException}
import java.nio.charset.{StandardCharsets, Charset}
import java.util.UUID

import android.bluetooth.{BluetoothSocket, BluetoothDevice}
import android.util.Log
import de.hdrmrks.bmp.Resource

/**
 * Created by markus on 30.08.15.
 */
class BluetoothSender(devices : List[BluetoothDevice], fileToSend: File) extends Thread {

  val TAG = "HelloScaloid"

  var mmSocket: BluetoothSocket = null

  val fileBytes : Array[Byte] = {
    val f = new RandomAccessFile(fileToSend, "r")
    try {
      // Get and check length
      val longlength = f.length()
      val length = longlength.asInstanceOf[Int]
      if (length != longlength)
        throw new IOException("File size >= 2 GB")
      // Read file and return data
      val data = new Array[Byte](length)
      f.readFully(data)
      data
    } finally {
      f.close()
    }
  }

  for (device <- devices) {
    Log.i(TAG, "Trying to establish RfCommSocket " + device.toString)
    if (device.toString.matches("D0:59:E4:C8:9E:81")) {
      try {
        val tmp: BluetoothSocket = device.createRfcommSocketToServiceRecord(Constant.APP_UUID)
        mmSocket = tmp
        Log.i(TAG, "Opened BluetoothRFCOMMONSocket " + mmSocket.toString)
      } catch {
        case ioe: IOException => {
          Log.w(TAG, "could not connect")
        }
      }
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
    val resource = new Resource(fileBytes, fileToSend.getName)
    bluetoothSender.send(resource)
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
