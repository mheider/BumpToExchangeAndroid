package de.hdrmrks

import java.util.{Observable, Observer}
import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.hardware.SensorManager
import android.util.Log
import org.scaloid.common._
import bump._

import scala.concurrent.ExecutionContext

class HelloScaloid extends SActivity with Observer {

  var bumpDetector : BumpDetector = null

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
    bumpDetector = new BumpDetector(getSystemService(Context.SENSOR_SERVICE)
      .asInstanceOf[SensorManager])
    bumpDetector.addBumpObserver(this)
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

  override def update(observable: Observable, data: scala.Any): Unit = {
    Log.i(TAG, "BUMP")
    vibrator.vibrate(700)
  }
}
