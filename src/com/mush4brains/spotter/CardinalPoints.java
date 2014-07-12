package com.mush4brains.spotter;

/*
* http://en.wikipedia.org/wiki/Boxing_the_compass
* Class provides method of converting an azimuth (0 to 360 degrees) into a
* 16 cardinal point direction or abbreviation
* E.g. 360 degrees is North or N
*
* Calling
* =======
* CardinalPoints cp = new CardinalPoints();
* cp.getCardinalDirection(azimuth)
* cp.getCardinalAbbreviation(azimuth);
* 
*/
public class CardinalPoints {

  //constructor
   public CardinalPoints(){
     
   }
   
   
   public String getCardinalDirection(double azimuth){
     double az = azimuth;
     if (az < 0)
       az = 360 + az;
     
     if (az < 11.25 || az >= 348.75)
       return "North";
     else if (az >= 11.25 && az < 33.25)
       return "North-northeast";
     else if (az >= 33.25 && az < 56.25)
       return "Northeast";
     else if (az >= 56.25 && az < 78.25)
       return "East-northeast";
     else if (az >= 78.25 && az < 101.25)
       return "East";
     else if (az >= 101.25 && az < 123.75)
       return "East-southeast";
     else if (az >= 123.75 && az < 146.25)
       return "Southeast";
     else if (az >= 146.25 && az < 168.75)
       return "South-southeast";
     else if (az >= 168.75 && az < 191.25)
       return "South";
     else if (az >= 191.25 && az < 213.75)
       return "South-southwest";
     else if (az >= 213.75 && az < 236.25)
       return "Southwest";
     else if (az >= 236.25 && az < 258.75)
       return "West-southwest";
     else if (az >= 258.75 && az < 281.25)
       return "West";
     else if (az >= 281.25 && az < 303.75)
       return "West-northwest";
     else if (az >= 303.75 && az < 326.25)
       return "Northwest";
     else if (az >= 326.25 && az < 348.75)
       return "North-northwest";
     else
       return "Unk";
     
   }
   
   public String getCardinalAbbreviation(double azimuth){
     
     double az = azimuth;
     if (az < 0)
       az = 360 + az;
     
     if (az < 11.25 || az >= 348.75)
       return "N";
     else if (az >= 11.25 && az < 33.25)
       return "NNE";
     else if (az >= 33.25 && az < 56.25)
       return "NE";
     else if (az >= 56.25 && az < 78.25)
       return "ENE";
     else if (az >= 78.25 && az < 101.25)
       return "E";
     else if (az >= 101.25 && az < 123.75)
       return "ESE";
     else if (az >= 123.75 && az < 146.25)
       return "SE";
     else if (az >= 146.25 && az < 168.75)
       return "SSE";
     else if (az >= 168.75 && az < 191.25)
       return "S";
     else if (az >= 191.25 && az < 213.75)
       return "SSW";
     else if (az >= 213.75 && az < 236.25)
       return "SW";
     else if (az >= 236.25 && az < 258.75)
       return "WSW";
     else if (az >= 258.75 && az < 281.25)
       return "W";
     else if (az >= 281.25 && az < 303.75)
       return "WNW";
     else if (az >= 303.75 && az < 326.25)
       return "NW";
     else if (az >= 326.25 && az < 348.75)
       return "NNW";
     else
       return "Unk";
     
   }
   
//   private int mod(int x, int y){
//     int result = x % y;
//     return result < 0 ? result + y: result;
//   }
   
   
}
