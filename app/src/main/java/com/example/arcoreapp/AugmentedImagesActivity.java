package com.example.arcoreapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class AugmentedImagesActivity extends AppCompatActivity {

    CustomArFragmnet arFragment;
    boolean shouldAddModel = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented_images);

        arFragment = (CustomArFragmnet)getSupportFragmentManager().findFragmentById(R.id.augmented_fragment);

        //disable plane default plane detection
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);


    }

    private void onUpdateFrame(FrameTime frameTime){

        Frame frame = arFragment.getArSceneView().getArFrame();

        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

        for ( AugmentedImage augmentedImage : augmentedImages){
            if(augmentedImage.getTrackingState() == TrackingState.TRACKING){

                if(augmentedImage.getName().equals("car") && shouldAddModel){
                    placeObject(arFragment,
                            augmentedImage.createAnchor(augmentedImage.getCenterPose()),
                            Uri.parse("car.sfb"));
                    shouldAddModel=false;
                }
            }
        }
    }

// add image to db so that google ar can look for them
    public boolean setupAugmentedImageDb(Config config, Session session){

        AugmentedImageDatabase augmentedImageDatabase;

        Bitmap bitmap = loadAugmentedImage();

        if(bitmap == null){
            return false;
        }

        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("car", bitmap);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;

    }
// convert image to bitmap in order to add them to db
    private Bitmap loadAugmentedImage(){
        try (InputStream is = getAssets().open("car.jpg")){

            return BitmapFactory.decodeStream(is);
        }
        catch (IOException e){
            Log.d("loadImage","IO exeption while loading image",e);
        }
        return null;
    }

    // Anchor = position/orientation of the object
    void placeObject(ArFragment fragment, Anchor anchor, Uri model){
        ModelRenderable.builder()
                .setSource(fragment.getContext(), model)
                .build()
                .thenAccept(renderlable -> addNodeToScene(fragment, anchor, renderlable))
                .exceptionally((throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Error")
                            .setMessage(throwable.getMessage());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return null;
                }));

    }
    // Renderable = is the model being built but placeObject
    // node in arcore is a place where object can be attached
    // there are two types of node: 1-anchor node cannot be changed in orientation or position
    // 2-transformable node are based on anchor node but can be edited (position, rotation, scale)
    void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable){
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }
}
