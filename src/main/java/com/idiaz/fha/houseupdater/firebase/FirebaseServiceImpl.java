package com.idiaz.fha.houseupdater.firebase;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import com.idiaz.fha.houseupdater.bo.Inmueble;
import com.idiaz.fha.houseupdater.bo.Location;
import com.idiaz.fha.houseupdater.bs.FirebaseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@Service("firebaseService")
public class FirebaseServiceImpl implements FirebaseService {
    private Logger log = LogManager.getLogger(FirebaseServiceImpl.class);

    @Autowired
    private ResourceLoader rl;

    @Value("${app.firebase.db.url}")
    String dbFbUrl;

    @PostConstruct
    private void init() {
        try {
            Resource resource = rl.getResource("classpath:./inmobusinessgt-557bb-firebase-adminsdk-z6wqx-bc1d770bb6.json");
            InputStream serviceAccount = resource.getInputStream();
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(dbFbUrl)
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (FileNotFoundException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }

    }

    @Override
    public void insertToFirebase(Inmueble inmueble) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("fha/immovables");

        DatabaseReference inmoRef = ref.child(inmueble.getCode());
        inmoRef.setValueAsync(inmueble);
    }

    @Override
    public void insertToLocations(Location location) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("fha/locations-fha");

        DatabaseReference inmoRef = ref.child(location.getName().trim());
        inmoRef.setValueAsync(location);
    }
}
