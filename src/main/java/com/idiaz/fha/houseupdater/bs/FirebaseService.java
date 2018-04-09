package com.idiaz.fha.houseupdater.bs;

import com.idiaz.fha.houseupdater.bo.Inmueble;
import com.idiaz.fha.houseupdater.bo.Location;

public interface FirebaseService {

    void insertToFirebase(Inmueble inmueble);

    void insertToLocations(Location location);

    void insertImageOnFirebaseStorage(Inmueble inmueble, String src, String name);
}
