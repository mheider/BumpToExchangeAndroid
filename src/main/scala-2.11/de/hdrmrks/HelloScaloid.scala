package de.hdrmrks

import java.util
import java.util.{Observable, Observer}
import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import android.bluetooth.{BluetoothAdapter, BluetoothDevice}
import android.content.Context
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{CheckBox, Button, TextView}
import org.scaloid.common._
import bump._

import scala.concurrent.ExecutionContext

class HelloScaloid extends SActivity with Observer {

  // Default the device is receiving data
  var sender = false

  var bumpDetector : BumpDetector = null

  /*
   * Connection to Android-Widgets
   */
  var infoView: TextView = null
  var bumpButton: Button = null
  var receiverCheckBox : CheckBox = null

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
    bumpButton = findViewById(R.id.bumpButton).asInstanceOf[Button]
    bumpButton.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        Log.i(TAG, "bump button")
        bump()
      }
    })
    receiverCheckBox = findViewById(R.id.receiverCheckBox).asInstanceOf[CheckBox]
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
    bump()
  }

  private def bump(): Unit = {
    if (!isReceiver && !searching) {
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
          new BluetoothConnection(result)
        }
      }
    }
    if (isReceiver) {
      vibrator.vibrate(100)
      setInfoViewText("Waiting for Sender")
      new BluetoothReceiver(BluetoothAdapter.getDefaultAdapter)
    }
  }

  private def isReceiver() : Boolean = {return receiverCheckBox.isChecked}

}
