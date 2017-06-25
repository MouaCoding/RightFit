package com.example.matt.rightfit.firebase_entry;

/**
 * Created by Matt on 5/9/2017.
 */

public class City {
    public String   CityName,
                    Country,
                    State,
                    Picture,
                    NumFavorites;

    public City()
    {
         CityName = Picture = NumFavorites = Country = State =  null;
    }

    public City(String aCityName, String aState, String aCountry, String aPicture, String aNumFavorites)
    {
        CityName = aCityName;
        State = aState;
        Country = aCountry;
        Picture = aPicture;
        NumFavorites = aNumFavorites;
    }
}
