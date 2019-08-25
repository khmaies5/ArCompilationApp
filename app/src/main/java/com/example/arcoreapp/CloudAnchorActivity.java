package com.example.arcoreapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class CloudAnchorActivity extends AppCompatActivity {

    private CloudArFragment fragment;
    private Anchor cloudAnchor;
    private enum AppAnchorState{
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }

    SnackbarHelper snackbarHelper = new SnackbarHelper();
    private AppAnchorState appAnchorState = AppAnchorState.NONE;
    StorageManager storageManager = new StorageManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_anchor);

        fragment = (CloudArFragment) getSupportFragmentManager().findFragmentById(R.id.cloudFragment);
        fragment.getPlaneDiscoveryController().hide();
        fragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
        Button clearButton = (Button) findViewById(R.id.clearButton);
        Button resolveButton = (Button) findViewById(R.id.resolveButton);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCloudAnchor(null);
            }
        });
        resolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cloudAnchor != null){
                    snackbarHelper.showMessageWithDismiss(getParent(),"Please clear anchor");
                    return;
                }
                ResolveDialogFragment dialog = new ResolveDialogFragment();
                dialog.setOkListener(CloudAnchorActivity.this::onResolveOkPressed);
                dialog.show(getSupportFragmentManager(), "Resolve");
            }
        });
        fragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) ->{

                    if(plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING || appAnchorState != AppAnchorState.NONE){

                        return;
                    }
                    Anchor anchor = fragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor());
                    setCloudAnchor(anchor);

                    appAnchorState = AppAnchorState.HOSTING;
                    snackbarHelper.showMessage(this, "Now Hosting anchor...");

                    placeObject(fragment,cloudAnchor, Uri.parse("cat.sfb"));
                }


        );

    }

    void onUpdateFrame(FrameTime frameTime){
checkForUpdatedAnchor();
    }

    // let only one thread access it at any point of time
    //to ensure that no unexpected error will happen whene doing multi-thread work
    synchronized void checkForUpdatedAnchor(){

        if(appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING){
            return;
        }
        Anchor.CloudAnchorState cloudAnchorState = cloudAnchor.getCloudAnchorState();
        if(appAnchorState == AppAnchorState.HOSTING) {
            if (cloudAnchorState.isError()) {
                snackbarHelper.showMessageWithDismiss(this, "Error hosting anchor..." + cloudAnchorState);
                appAnchorState = AppAnchorState.NONE;
            } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {
                int shortCode = storageManager.nextShortCode(this);
                //142
                storageManager.storeUsingShortCode(this, shortCode, cloudAnchor.getCloudAnchorId());
                snackbarHelper.showMessageWithDismiss(this, "Anchor hosted cloud short code: " + shortCode);
                appAnchorState = AppAnchorState.HOSTED;
            }
        }else if(appAnchorState != AppAnchorState.RESOLVING){
            if (cloudAnchorState.isError()) {
                snackbarHelper.showMessageWithDismiss(this, "Error resolving anchor..." + cloudAnchorState);
                appAnchorState = AppAnchorState.NONE;
            }else if(appAnchorState != AppAnchorState.RESOLVING){
                snackbarHelper.showMessageWithDismiss(this, "Resolved ...");
                appAnchorState = AppAnchorState.RESOLVED;
            }
        }
    }

    //get anchor from the shortcode typed in the dialog
    void onResolveOkPressed(String dialogValue){

        int shortCode = Integer.parseInt(dialogValue);
        String cloudAnchorId = storageManager.getCloudAnchorID(this, shortCode);
        Anchor resolvedAnchor = fragment.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorId);
        setCloudAnchor(resolvedAnchor);
        placeObject(fragment,cloudAnchor,Uri.parse("cat.sfb"));
        snackbarHelper.showMessage(this,"now resolving anchor...");
        appAnchorState = AppAnchorState.RESOLVING;
    }
    void setCloudAnchor(Anchor newanchor){

        if(cloudAnchor != null){
            cloudAnchor.detach();
        }
        cloudAnchor = newanchor;
        appAnchorState = AppAnchorState.NONE;
        snackbarHelper.hide(this);
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
