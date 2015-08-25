package de.hdrmrks

import org.scaloid.common._
import android.graphics.Color
import scroll.internal.Compartment
import annotations.Role
import android.util.Log
import de.hdrmrks.bmp._

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import android.widget.TextView

import scala.collection.mutable.MutableList

class HelloScaloid extends SActivity {

  val TAG = "HelloScaloid"
  val REQUEST_ENABLE_BT = 1

  var btAdapter : BluetoothAdapter = null;

  val btDeviceList:MutableList[BluetoothDevice] = MutableList();



  class BaseGreeter {
    def sayHello():String = "Hello"
  }

  class GreeterRole {
    def sayHello():String = "Hello from a role"
  }

  onCreate {
    Log.i(TAG, "Init play")

    val filter = new IntentFilter(BluetoothDevice.ACTION_FOUND)
    filter.addAction(BluetoothDevice.ACTION_UUID)
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

    registerReceiver(ActionFoundReceiver, filter)

    btAdapter = BluetoothAdapter.getDefaultAdapter()
    checkBTState()

  }

  onDestroy {
    if(btAdapter != null) {
      btAdapter.cancelDiscovery()
    }
    unregisterReceiver(ActionFoundReceiver)
  }


  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if(requestCode == REQUEST_ENABLE_BT)
      checkBTState()
  }

  private def checkBTState() {
    if(btAdapter == null) {
      Log.w(TAG, "Bluetooth NOT supported. ABORTING!")
      return ;
    }

    if(btAdapter.isEnabled()) {
      Log.i(TAG, "Bluetooth is enabled")
      btAdapter.startDiscovery()
    } else {
      val enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }
  }

  val ActionFoundReceiver : BroadcastReceiver = new BroadcastReceiver() {

    override def onReceive(context: Context, intent :Intent) {
      val action = intent.getAction();
      if(BluetoothDevice.ACTION_FOUND.equals(action)) {
        val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.i(TAG, "\n  Device: " + device.getName() + ", " + device);
        btDeviceList += device
      } else {
        if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
          Log.i(TAG, "\nDiscovery Started...");
        } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
          Log.i(TAG, "DiscoveryFinished")
          for(device <- btDeviceList) {
            Log.i(TAG, device.getName())
          }
        }
      }
    }
  }
}

