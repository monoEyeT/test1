package pubgradar.struct.CMD

import pubgradar.deserializer.channel.ActorChannel.Companion.droppedItemToItem
import pubgradar.struct.Actor
import pubgradar.struct.Bunch
import pubgradar.struct.NetGuidCacheObject
import pubgradar.util.debugln

object DroppedItemCMD
{

  fun process(actor : Actor, bunch : Bunch, repObj : NetGuidCacheObject?, waitingHandle : Int, data : HashMap<String, Any?>) : Boolean
  {
    try
    {
      with(bunch) {
        when (waitingHandle)
        {
          16   ->
          {
            val (itemguid, item) = readObject()
            droppedItemToItem[actor.netGUID] = itemguid
          }
          17   ->
          {//struct FSkinData SkinData | SkinTargetDatas TArray<struct FSkinTargetData> | struct FName TargetName, class USkinDataConfig* SkinDataConfig
            readUInt16() //arraySize
            var index = readIntPacked()
            var loopcount = 0
            while (index != 0)
            {
              when ((index - 1) % 2)
              {
                0 -> readObject() //SkinDataConfig
                1 -> readName() //TargetName
              }
              index = readIntPacked()
                loopcount +=1
              if(loopcount %1000 == 0 )
                println("DroppedItem: ${loopcount}");
            }
          }
          else -> ActorCMD.process(actor, bunch, repObj, waitingHandle, data)
        }
        return true
      }
    }
    catch (e : Exception)
    {
      debugln { ("DroppedItemCMD is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
    }
    return false
  }
}