package com.example.arcoreapp;

import android.util.Log;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class CloudArFragment extends ArFragment {

    @Override
    protected Config getSessionConfiguration(Session session) {

        // disable handgesture in default arfragment
        getPlaneDiscoveryController().setInstructionView(null);

        Config config = super.getSessionConfiguration(session);

        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);

        return config;
    }
}
