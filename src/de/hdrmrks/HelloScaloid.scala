package de.hdrmrks

import org.scaloid.common._
import android.graphics.Color
import scroll.internal.Compartment
import android.util.Log

class HelloScaloid extends SActivity {

  val TAG = "HelloScaloid"

  class BaseGreeter {
    def sayHello():String = "Hello"
  }

  class GreeterRole {
    def sayHello():String = "Hello from a role"
  }

  onCreate {
    Log.i(TAG, "Init play")
    var greeter: BaseGreeter = new BaseGreeter();
    var roleGreeter : GreeterRole = new GreeterRole();

    Log.i(TAG, greeter.sayHello());
    Log.i(TAG, roleGreeter.sayHello());

    new Compartment() {
      Log.i(TAG, "Hello I am a Compartment");
      val testGreeter = new BaseGreeter() play new GreeterRole()
      val myString : String = (+testGreeter).sayHello()
      Log.i(TAG, myString)
    }
  }
}
