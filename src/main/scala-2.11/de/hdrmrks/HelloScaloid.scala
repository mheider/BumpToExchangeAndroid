package de.hdrmrks

import java.io.{FileOutputStream, IOException, RandomAccessFile, File}
import java.util
import java.util.{Observable, Observer}
import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import android.app.Activity
import android.bluetooth.{BluetoothAdapter, BluetoothDevice}
import android.content.{Intent, Context}
import android.database.Cursor
import android.hardware.SensorManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget._
import de.hdrmrks.bmp.Resource
import org.scaloid.common._
import bump._

import scala.concurrent.ExecutionContext

class HelloScaloid extends SActivity with Observer {

  val FILE_SELECT_CODE = 0

  // Default the device is receiving data
  var sender = false

  var bumpDetector : BumpDetector = null

  /*
   * Connection to Android-Widgets
   */
  var infoView: TextView = null
  var bumpButton: Button = null
  var receiverCheckBox : CheckBox = null
  var selectResourceButton: Button = null

  val TAG = "HelloScaloid"

  var searching = false

  var selectedFile: File = null

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
//          val enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//          enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
//          startActivityForResult(enableBtIntent, REQUEST_DEVICE_DISCOVERABLE);
          val bluetoothReceiver = new BluetoothReceiver(BluetoothAdapter.getDefaultAdapter)
          addAsObserver(bluetoothReceiver)
          new Thread(bluetoothReceiver).start()
        }
      }
    })
    selectResourceButton = findViewById(R.id.selectResourceButton).asInstanceOf[Button]
    selectResourceButton.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        showFileChooser()
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


  override def onActivityResult(requestCode: Int, resultCode: Int, data:Intent): Unit =  {
    requestCode match {
      case FILE_SELECT_CODE => {
        if (resultCode == Activity.RESULT_OK) {
          val uri: Uri = data.getData
          val file = new File(uri.getPath)
          Log.i(TAG, "Selected File-URI: " + uri.toString)
          this.selectedFile = file
        }
      }
    }
  }


  override def update(observable: Observable, data: scala.Any): Unit = {
    if (observable.isInstanceOf[BumpDetector]) {
      Log.i(TAG, "BUMP")
      bump()
    }
    else if (observable.isInstanceOf[BluetoothReceiver]) {
      val resource = data.asInstanceOf[Resource]
      val file = saveResource(resource)
      Log.i(TAG, "Saved resource to " + file.getPath)
      openFileWithIntent(file)
    }
  }

  val REQUEST_DEVICE_DISCOVERABLE: Int = 1

  private def bump(): Unit = {
    if (!isReceiver && !searching && (this.selectedFile != null)) {
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
          new BluetoothSender(result, selectedFile)
        }
      }
    }
  }

  private def isReceiver() : Boolean = {return receiverCheckBox.isChecked}


  private def showFileChooser(): Unit =  {
    val intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("*/*");
    //intent.addCategory(Intent.CATEGORY_OPENABLE);
    try {
      startActivityForResult(
        Intent.createChooser(intent, "Select a File to Upload"),
        FILE_SELECT_CODE);
    } catch  {
      case anfe: android.content.ActivityNotFoundException => {
        // Potentially direct the user to the Market with a Dialog
        Toast.makeText (this, "Please install a File Manager.",
        Toast.LENGTH_SHORT).show ()
      }
    }
  }

  private def addAsObserver(obs: Observable): Unit = {
    obs.addObserver(this)
  }

   private def saveResource(resource: Resource): File = {
     val path = getExternalFilesDir(null)
     val file = new File(path, resource.name)
     val fos = new FileOutputStream(file)
     try {
       fos.write(resource.data)
     } catch {
       case ioe: IOException => {

         Log.e(TAG, "could not save data")
       }
     } finally {
       fos.close
     }
     return file
   }

  private def openFileWithIntent(file: File): Unit = {
    val intent = new Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(Uri.fromFile(file), "text/plain")
    startActivity(intent)
  }
}
