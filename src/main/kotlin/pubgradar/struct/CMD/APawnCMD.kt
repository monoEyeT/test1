package pubgradar.struct.CMD

import pubgradar.deserializer.channel.ActorChannel.Companion.visualActors
import pubgradar.struct.Actor
import pubgradar.struct.Archetype.Other
import pubgradar.struct.Bunch
import pubgradar.struct.NetGuidCacheObject
import pubgradar.util.debugln

object APawnCMD
{
   fun process(actor : Actor , bunch : Bunch , repObj : NetGuidCacheObject? , waitingHandle : Int , data : HashMap<String , Any?>) : Boolean
  {
    try
    {
      with(bunch) {
        when (waitingHandle)
        {
          8    -> if (readBit())
          {//bHidden
            visualActors.remove(actor.netGUID)
          }
          9    -> if (!readBit())
          {//bReplicateMovement
            if (!actor.isVehicle)
              visualActors.remove(actor.netGUID)
          }
          10    -> if (readBit())
          {//bTearOff
            visualActors.remove(actor.netGUID)
          }
          14    ->
          {//struct FRepMovement
            repMovement(actor)
            if (actor.type != Other)
              visualActors[actor.netGUID] = actor
          }
          16   -> propertyObject() //Controller
          17   -> propertyObject() //PlayerState
          18   -> readUInt16() //RemoteViewPitch
          else -> return ActorCMD.process(actor, bunch, repObj, waitingHandle, data)
        }
        return true
      }
    }
    catch (e : Exception)
    {
      debugln { ("APawnCMD is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
    }
    return false
  }
}