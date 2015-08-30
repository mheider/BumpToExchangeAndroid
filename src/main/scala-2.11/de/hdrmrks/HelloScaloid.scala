package de.hdrmrks

import java.util
import java.util.{Observable, Observer}
import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.hardware.SensorManager
import android.util.Log
import android.widget.TextView
import org.scaloid.common._
import bump._

import scala.concurrent.ExecutionContext

class HelloScaloid extends SActivity with Observer {
  var sender = true

  var bumpDetector : BumpDetector = null

  var infoView: TextView = null

  val TAG = "HelloScaloid"

  var searching = false;



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
    setContentView(R.layout.hello_scaloid)
    infoView = findViewById(R.id.infoView).asInstanceOf[TextView]
    bumpDetector = new BumpDetector(getSystemService(Context.SENSOR_SERVICE)
      .asInstanceOf[SensorManager])
    bumpDetector.addBumpObserver(this)
    setInfoViewText("Doing nothing")
  }

  def setInfoViewText(text:String): Unit = {
    runOnUiThread(infoView.setText(text))
  }

  override def update(observable: Observable, data: scala.Any): Unit = {
    Log.i(TAG, "BUMP")
    if (sender && !searching) {
      vibrator.vibrate(100)
      setInfoViewText("Searching...")
      this.synchronized {
        searching = true
      }
      val devicesFuture = new BluetoothDiscoverer(getApplicationContext).getBluetoothDevices
      devicesFuture.onSuccess {
        case result: List[BluetoothDevice] => {
          setInfoViewText("Found " + result.length + "devices")
          this.synchronized {
            searching = false
          }
        }
      }
    }
  }
}
