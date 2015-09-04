package de.hdrmrks

import java.util
import java.util.{Observable, Observer}
import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import android.bluetooth.{BluetoothAdapter, BluetoothDevice}
import android.content.{Intent, Context}
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.{CompoundButton, CheckBox, Button, TextView}
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
    receiverCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener {
      override def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean): Unit =  {
        if(receiverCheckBox.isChecked) {
          vibrator.vibrate(100)
          setInfoViewText("Waiting for Sender")
          val enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
          enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
          startActivityForResult(enableBtIntent, REQUEST_DEVICE_DISCOVERABLE);
          val bluetoothReceiver = new BluetoothReceiver(BluetoothAdapter.getDefaultAdapter)
          new Thread(bluetoothReceiver).start()
        }
      }
    })
    bumpDetector = new BumpDetector(getSystemService(Context.SENSOR_SERVICE)
      .asInstanceOf[SensorManager])
    bumpDetector.addBumpObserver(this)
    setInfoViewText("Doing nothing")
  }

  def setInfoViewText(text:String): Unit = {
    runOnUiThread(infoView.setText(text))
  }

  override def update(observable: Observable, data: scala.Any): Unit = {
    if (observable.isInstanceOf[BumpDetector]) {
      Log.i(TAG, "BUMP")
      bump()
    }
    else if (observable.isInstanceOf[BluetoothReceiver]) {
      Log.i(TAG, "GOT DATA!!!")
      toast(data.toString)
    }
  }

  val REQUEST_DEVICE_DISCOVERABLE: Int = 1

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
          new BluetoothSender(result)
        }
      }
    }
  }

  private def isReceiver() : Boolean = {return receiverCheckBox.isChecked}

}
