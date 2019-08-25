package com.example.arcoreapp;

import android.util.Log;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class CustomArFragmnet extends ArFragment {

    @Override
    protected Config getSessionConfiguration(Session session) {

        // disable handgesture in default arfragment
        getPlaneDiscoveryController().setInstructionView(null);

        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
        this.getArSceneView().setupSession(session);
        if(((AugmentedImagesActivity)getActivity()).setupAugmentedImageDb(config, session)){
            Log.d("image db","setup successful");
        }else Log.d("image db","failed to setup image db");

        return config;
    }
}
