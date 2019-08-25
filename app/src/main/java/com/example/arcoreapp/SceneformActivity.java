package com.example.arcoreapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class SceneformActivity extends AppCompatActivity {

    private ArFragment fragment;
    Uri selectedObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneform);

        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
initGallery();

fragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) ->{

            if(plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING){
                return;
            }
            Anchor anchor = hitResult.createAnchor();
            placeObject(fragment,anchor,selectedObject);
        }


        );
    }

    void initGallery(){
        LinearLayout gallery = findViewById(R.id.gallery_layout);

        ImageView chair = new ImageView(this);
        chair.setImageResource(R.drawable.chair_thumb);
        chair.setContentDescription("Chair");
        chair.setOnClickListener(v -> {selectedObject = Uri.parse("chair.sfb");});
        gallery.addView(chair);

        ImageView couch = new ImageView(this);
        couch.setImageResource(R.drawable.couch_thumb);
        couch.setContentDescription("couch");
        couch.setOnClickListener(v -> {selectedObject = Uri.parse("couch.sfb");});
        gallery.addView(couch);

        ImageView LampPost = new ImageView(this);
        LampPost.setImageResource(R.drawable.lamp_thumb);
        LampPost.setContentDescription("LampPost");
        LampPost.setOnClickListener(v -> {selectedObject = Uri.parse("LampPost.sfb");});
        gallery.addView(LampPost);
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
