package pubgradar.util.settings


import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.io.File

class Settings
{

   data class jsonsettings(
           ////
           //// Style Settings
           ////
           val miniMapWindowWidth : Float = 360f ,
           val miniMapRadius : Float = 1000 * 100f ,
           val playerRadius : Float = 10000f ,
           val healthBarWidth : Float = 18000f ,
           val healthBarHeight : Float = 3000f ,
           val directionRadius : Float = 25000f ,
           val fov : Float = 90f ,
           val aimLineWidth : Float = 1000f ,
           val aimLineRange : Float = 50000f ,
           val aimCircleRadius : Float = 200f ,
           val firingLineLength : Float = 20000f ,
           val itemZoomThreshold : Float = 0.08f ,
           val airDropTextScale : Float = 1000f ,

           //
           // Scales
           //
           val itemScale : Float = 24f ,
           val staticItemScale : Float = 180f ,
           val mapMarkerScale : Float = 150f ,
           val airDropScale : Float = 250f ,
           val vehicleScale : Float = 30f ,
           val vehicleScalemini : Float = 46f ,
           val planeScale : Float = 350f ,
           val grenadeScale : Float = 15f ,
           val corpseScale : Float = 50f ,
           val redzoneBombScale : Float = 30f ,
           val aimTimeThreshold : Int = 2000 ,

           //
           // Timers
           //
           val attackLineDuration : Int = 1000 ,
           val attackMeLineDuration : Int = 10000 ,
           val firingLineDuration : Int = 500 ,

           //sight
           val Holosight : Boolean = true,
           val DotSight : Boolean = true,
           val Aimpoint : Boolean = true,
           val CQBSS : Boolean = true,
           val ACOG : Boolean = true,
           val PM2 : Boolean = true,
           val SC3x : Boolean = true,
           val SC6x : Boolean = true,
           //
           // Armor / Head / Bag Etc
           //
           val Level1Armor : Boolean = false,
           val Level1Head : Boolean = false,
           val Level1Head1 : Boolean = false,
           val Level1Back : Boolean = false,
           val Level1Back1 : Boolean = false,
           val Level2Armor : Boolean = true,
           val Level2Head : Boolean = true,
           val Level2Head1 : Boolean = true,
           val Level2Back : Boolean = true,
           val Level2Back1 : Boolean = true,
           val Level3Armor : Boolean = true,
           val Level3Head : Boolean = true,
           val Level3Back : Boolean = true,
           val Level3Back1 : Boolean = true,
           //
           // Decent Weapons
           //
           val AK47 : Boolean = true,
           val AUG : Boolean = true,
           val AWM : Boolean = true,
           val Berreta686 : Boolean = false, //s686
           val DP28 : Boolean = true,
           val Groza : Boolean = true,
           val HK416 : Boolean = true,
           val Kar98k : Boolean = true,
           val M16A4 : Boolean = true,
           val M24 : Boolean = true,
           val M249 : Boolean = true,
           val Mini14 : Boolean = false,
           val MK14 : Boolean = true,
           val Saiga12 : Boolean = false,
           val SCARL : Boolean = true,
           var SKS : Boolean = true,
           val Thompson : Boolean = false, //"Tommy Gun"
           val UMP : Boolean = false,
           val UZI : Boolean = false,
           val Vector : Boolean = false,
           val VSS : Boolean = false,
           val Win94 : Boolean = false, //"Win94"
           val Winchester : Boolean = false, //"S1897",
           val FNFal : Boolean = true, //SLR

           // Pistols
           //
           val FlareGun : Boolean = true,
           val G18 : Boolean = false, //P18C
           val M1911 : Boolean = false, //"P1911"
           val M9 : Boolean = false, //"P92"
           val Rhino45 : Boolean = false, //"R45",
           val R1895 : Boolean = false,//ItemName_Weapon_NagantM1895
           val SawenOff : Boolean = false, //"Sawed-Off",
           //
           //
           // Meds
           //
           val Bandage : Boolean = false,
           val MedKit : Boolean = true,
           val FirstAid : Boolean = true,
           val PainKiller : Boolean = true,
           val EnergyDrink : Boolean = true,
           val Syringe : Boolean = true,
           // FUEL
           val JerryCan : Boolean = true,
           //
           // Attachments
           //
           var QDSnipe : Boolean = true,
           var ExSR : Boolean = true,
           var ExSMG : Boolean = false,
           var ExQuickAR : Boolean = true,
           var ExtQuickSMG : Boolean = false,
           var ExAR : Boolean = true,
           var CheekSR : Boolean = true,
           var LoopsSR : Boolean = false,
           var StockAR : Boolean = true,
           var SuppressorSR : Boolean = true,
           var SuppressorAR : Boolean = true,
           var SuppressorSMG : Boolean = false,
           var FlashHiderSMG : Boolean = false,
           var FlashHiderSR : Boolean = true,
           var FlashHiderAR : Boolean = true,
           var CompensatorSR : Boolean = true,
           var CompensatorAR : Boolean = true,
           var CompensatorSMG : Boolean = false,
           var Foregrip : Boolean = true,
           var AngledForegrip : Boolean = true,
           val Duckbill : Boolean = true,
           val ThumbGrip : Boolean = true,
           val LightWeightForeGrip : Boolean = true,
           val HalfGrip : Boolean = true,
           //
           // Ammo
           //

           var Ammo_762mm : Boolean = true,
           var Ammo_556mm : Boolean = true,
           var Ammo_300Magnum : Boolean = true,
           var Weapon_Pan : Boolean = true,
           var Ammo_9mm : Boolean = false,
           var Ammo_45ACP : Boolean = false,
           var Ammo_Flare : Boolean = true,
           var Ammo_12Guage : Boolean = false,
           var Grenade : Boolean = true,
           var FlashBang : Boolean = false,
           var SmokeBomb : Boolean = false,
           var Molotov  : Boolean = false,
           var Ghillie  : Boolean = true,
           ///
           /// Information Toggles
           /// Default Item Information Toggles
           // -1 Disabled
           // 1  Enabled
           var drawAll : Int =  -1 ,
           var enableRoate : Int =  1 ,
           var filterScope : Int =  1 ,
           var filterHeals : Int =  1 ,
           var filterAmmo : Int =  1 ,
           var filterThrow : Int =  1 ,

           // Draw Compass
           var drawcompass : Int =  1 ,
           // Draw Grid
           var drawgrid: Int =  1 ,
           // Draw Menu
           var drawmenu : Int = -1  ,

           // Toggle View Line
           var toggleView : Int = 1 ,

           // Toggle Mini-Map
           var drawDaMap : Int = 1 ,
           // private var toggleVehicles = -1
           //  private var toggleVNames = -1

           // Player Info Toggles 1-4
           var nameToggles : Int = 3 ,

           // Filter Equipment 1-2
           var filterLvl2 : Int = 4 ,

           // Vehicle Information Toggles 1-2
           var VehicleInfoToggles : Int = 1 ,

           // Zoom Toggles 1-3
           var ZoomToggles : Int = 1 ,


           //
           //  Key settings
           //  Scarjit: Im using strings here for user comfort
           //
           val nameToogle_Key : String = Input.Keys.toString(Input.Keys.F1) ,
           val drawcompass_Key : String = Input.Keys.toString(Input.Keys.F2) ,
           val drawDaMap_Key : String = Input.Keys.toString(Input.Keys.F3) ,
           val toggleView_Key : String = Input.Keys.toString(Input.Keys.F4) ,
           val VehicleInfoToggles_Key : String = Input.Keys.toString(Input.Keys.F5) ,
           val drawgrid_Key : String = Input.Keys.toString(Input.Keys.F6) ,
           val rotateToogle_Key : String = Input.Keys.toString(Input.Keys.F7) ,

           val drawmenu_Key : String = Input.Keys.toString(Input.Keys.F12) ,

           val enableAll_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_1) ,
           val enableRoate_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_2) ,
           val filterPrintLOC : String = Input.Keys.toString(Input.Keys.NUMPAD_3) ,
           val filterThrow_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_4) ,
           val filterAttach_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_5) ,
           val filterScope_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_6) ,
           val filterAmmo_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_7) ,
           val ZoomToggles_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_8) ,

           val camera_zoom_Minus_Key : String = Input.Keys.toString(Input.Keys.MINUS) ,
           val camera_zoom_Plus_Key : String = Input.Keys.toString(Input.Keys.PLUS) ,


           //
           //  Font settings
           //

           //AGENCYFB
           val hubFont_size : Int = 30 ,
           val hubFont_color : Color = Color.WHITE ,
           val hubFontShadow_color : Color = Color(1f , 1f , 1f , 0.4f) ,

           val espFont_size : Int = 16 ,
           val espFont_color : Color = Color.WHITE ,
           val espFontShadow_color : Color = Color(1f , 1f , 1f , 0.2f) ,

           //NUMBER
           val largeFont_size : Int = 38 ,
           val largeFont_color : Color = Color.WHITE ,

           //GOTHICB
           val largeFont_size2 : Int = 38 ,
           val largeFont_color2 : Color = Color.WHITE ,

           val littleFont_size : Int = 15 ,
           val littleFont_color : Color = Color.WHITE ,

           val nameFont_size : Int = 15 ,
           val nameFont_color : Color = Color.WHITE ,
           val distanceFont_size : Int = 15 ,
           val distanceFont_color : Color = Color.RED ,

           val itemFont_size : Int = 13 ,
           val itemFont_color : Color = Color.WHITE ,

           val compaseFont_size : Int = 10 ,
           val compaseFont_color : Color = Color(0f , 0.95f , 1f , 1f) ,

           val compaseFontShadow_color : Color = Color(0f , 0f , 0f , 0.5f) ,

           val littleFont_size2 : Int = 15 ,
           val littleFont_color2 : Color = Color.WHITE ,

           val littleFontShadow_color : Color = Color(0f , 0f , 0f , 0.5f) ,

           val menuFont_size : Int = 12 ,
           val menuFont_color : Color = Color.WHITE ,

           val menuFontText_size : Int = 12 ,
           val menuFontText_color : Color = Color.WHITE ,

           val menuFontOn_size : Int = 12 ,
           val menuFontOn_color : Color = Color.GREEN ,

           val menuFontOFF_size : Int = 12 ,
           val menuFontOFF_color : Color = Color.RED ,

           val hporange_size : Int = 13 ,
           val hporange_color : Color = Color.ORANGE ,

           val hpgreen_size : Int = 13 ,
           val hpgreen_color : Color = Color.GREEN ,

           val hpred_size : Int = 13 ,
           val hpred_color : Color = Color.RED


   )

   val settingsname = "settings.json"

   fun loadsettings() : jsonsettings
   {
      checkifsettingsexists()
      val f = File(settingsname)
      if (! f.canRead())
      {
         throw SecurityException("Can't read settings.json")
      }


      val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
      val adapter = moshi.adapter(jsonsettings::class.java)
      val set = adapter.fromJson(f.readText())
      if (set != null)
      {
         return set
      }
      else
      {
         throw NullPointerException()
      }
   }

   fun savesettings(settings : jsonsettings)
   {
      val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
      val json = moshi.adapter(jsonsettings::class.java).indent("  ").toJson(settings)
      val f = File(settingsname)
      f.writeText(json)
   }

   fun checkifsettingsexists()
   {
      val f = File(settingsname)
      if (! f.exists())
      {
         savesettings(jsonsettings())
      }
   }

}