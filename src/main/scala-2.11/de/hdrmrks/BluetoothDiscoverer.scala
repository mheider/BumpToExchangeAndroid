package de.hdrmrks

import java.util.concurrent.{TimeUnit, LinkedBlockingQueue, ThreadPoolExecutor}

import android.bluetooth.{BluetoothAdapter, BluetoothDevice}
import android.content.{IntentFilter, BroadcastReceiver, Intent, Context}
import android.util.Log

import scala.collection.mutable
import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by markus on 27.08.15.
 */
class BluetoothDiscoverer(context: Context) {
  val REQUEST_ENABLE_BT = 1

  var btAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

  val btDeviceList:mutable.MutableList[BluetoothDevice] = mutable.MutableList()

  var doneDiscovery = false

  val TAG = "HelloScaloid"

  implicit  val exec = ExecutionContext.fromExecutor(new ThreadPoolExecutor(100,100,1000,TimeUnit.SECONDS, new
      LinkedBlockingQueue[Runnable]))

  val filter = new IntentFilter(BluetoothDevice.ACTION_FOUND)
  filter.addAction(BluetoothDevice.ACTION_UUID)
  filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
  filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

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
          doneDiscovery = true
          Log.i(TAG, "Discovery finished")
        }
      }
    }
  }

  context.registerReceiver(ActionFoundReceiver, filter)

  private def discoverBluetoothDevices(): List[BluetoothDevice] = {
    doneDiscovery = false;
    btAdapter.startDiscovery()
    while(!doneDiscovery) {} // wait
    doneDiscovery = false
    context.unregisterReceiver(ActionFoundReceiver)
    return btDeviceList.toList
  }


  def getBluetoothDevices: Future[List[BluetoothDevice]] = {
    return Future[List[BluetoothDevice]] {
      discoverBluetoothDevices
    }
  }
}
