package com.idiaz.fha.houseupdater.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.io.*;
import java.nio.ByteBuffer;

@Service("firebaseService")
public class FirebaseServiceImpl implements FirebaseService {
    private Logger log = LogManager.getLogger(FirebaseServiceImpl.class);

    @Autowired
    private ResourceLoader rl;

    @Value("${app.firebase.db.url}")
    private String dbFbUrl;

    private Storage storage;

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

            Resource resourceS = rl.getResource("classpath:./inmobusinessgt-557bb-firebase-adminsdk-z6wqx-bc1d770bb6.json");
            InputStream serviceAccountS = resourceS.getInputStream();
            this.storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountS))
                    .build()
                    .getService();

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

        DatabaseReference inmoRef = ref.child(location.getName().toLowerCase().trim().replace(" ", ""));
        inmoRef.setValueAsync(location);
    }

    @Override
    public void insertImageOnFirebaseStorage(Inmueble inmueble, String src, String name) {

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(src));
        } catch (FileNotFoundException e) {
            log.error(e);
        }
        BlobInfo blobInfo = BlobInfo.newBuilder("inmobusinessgt-557bb.appspot.com","fha-immo/"+inmueble.getCode()+ name).setContentType("image/jpeg").build();
        storage.create(blobInfo);

        try (WriteChannel writer = storage.writer(blobInfo)) {
            byte[] buffer = new byte[1024];
            int limit;
            try {
                while ((limit = inputStream.read(buffer)) >= 0) {
                    writer.write(ByteBuffer.wrap(buffer, 0, limit));
                }

            } catch (Exception ex) {
                log.error(ex);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        } catch (IOException e) {
            log.error(e);
        }catch (Exception e){
            log.error(e);
        }
    }

}
