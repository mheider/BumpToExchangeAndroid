package de.hdrmrks

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import android.bluetooth.BluetoothDevice
import android.util.Log
import org.scaloid.common._

import scala.concurrent.ExecutionContext

class HelloScaloid extends SActivity {

  val TAG = "HelloScaloid"
  implicit  val exec = ExecutionContext.fromExecutor(new ThreadPoolExecutor(100,100,1000,TimeUnit.SECONDS, new
      LinkedBlockingQueue[Runnable]))


  class BaseGreeter {
    def sayHello():String = "Hello"
  }

  class GreeterRole {
    def sayHello():String = "Hello from a role"
  }

  onCreate {
    Log.i(TAG, "Init play")
    val devicesFuture = new BluetoothDiscoverer(getBaseContext).getBluetoothDevices
    devicesFuture.onSuccess {
      case result => {
        for (elemt <- result) {
          Log.i(TAG, elemt.toString)
        }

        // send to the first device
        val firstDevice : BluetoothDevice = result.head
        new BluetoothConnection(firstDevice)
      }
    }
  }
}
