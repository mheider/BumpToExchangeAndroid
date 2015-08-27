package de.hdrmrks

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit, ThreadPoolExecutor}

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

import scala.collection.mutable
import scala.collection.mutable.MutableList
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.util.Try

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
      }
    }
  }


}
