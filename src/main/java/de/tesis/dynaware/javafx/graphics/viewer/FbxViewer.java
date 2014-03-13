/*
 * Copyright (C) 2014 TESIS DYNAware GmbH.
 * All rights reserved. Use is subject to license terms.
 * 
 * This file is licensed under the Eclipse Public License v1.0, which accompanies this
 * distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package de.tesis.dynaware.javafx.graphics.viewer;
	
import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Viewer application for FBX files.
 */
public class FbxViewer extends Application {
    
	@Override
	public void start(Stage stage) throws IOException {

        final URL location = getClass().getResource("FbxViewer.fxml");
        final FXMLLoader loader = new FXMLLoader();
        final VBox root = (VBox) loader.load(location.openStream());

        final Scene scene = new Scene(root, 800, 600, true);
	    
        stage.setScene(scene);
        stage.setTitle("FBX Viewer");
        stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
