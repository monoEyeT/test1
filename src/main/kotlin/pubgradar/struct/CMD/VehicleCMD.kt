package pubgradar.struct.CMD

import pubgradar.struct.Actor
import pubgradar.struct.Bunch
import pubgradar.struct.NetGuidCacheObject
import pubgradar.struct.Vehicle
import pubgradar.util.debugln

object VehicleCMD
{
   fun process(actor : Actor , bunch : Bunch , repObj : NetGuidCacheObject? , waitingHandle : Int , data : HashMap<String , Any?>) : Boolean
   {
    //try
    //{
      actor as Vehicle
      with(bunch) {
        when (waitingHandle)
        {
          17   ->
          {
            val (netguid) = propertyObject()
            actor.driverPlayerState = netguid
          }
          else -> return APawnCMD.process(actor, bunch, repObj, waitingHandle, data)
        }
        return true
      }
    //}
    //catch (e : Exception)
    //{
    //  debugln { ("VehicleCMD is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
    //}
    //return false
   }
}