/*
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.tesis.dynaware.javafx.graphics.viewer;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * Stores the content of the 3D scene including meshes, lights, and cameras.
 */
public class FbxViewerModel {
    
    private final ObjectProperty<Node> contentProperty = new SimpleObjectProperty<>();
    private final Group root = new Group();
    private final SubScene subScene;
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Rotate cameraXRotate = new Rotate(-20, 0, 0, 0, Rotate.X_AXIS);
    private final Rotate cameraYRotate = new Rotate(-20, 0, 0, 0, Rotate.Y_AXIS);
    private final Translate cameraPosition = new Translate(0, 0, -20);
    private double dragStartX, dragStartY, dragStartRotateX, dragStartRotateY;
    private RotateTransition rotateTransition;
    private boolean isRotating;
    private double scaleFactor = 1;

    /**
     * Creates a content model for the 3D scene.
     */
    public FbxViewerModel() {
        
        subScene = new SubScene(root, 400, 400, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.ALICEBLUE);
        
        camera.setNearClip(1);
        camera.setFarClip(10000);
        camera.getTransforms().addAll(cameraXRotate, cameraYRotate, cameraPosition);
        
        subScene.setCamera(camera);
        root.getChildren().add(camera);

        subScene.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(final MouseEvent event) {
                
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    dragStartX = event.getSceneX();
                    dragStartY = event.getSceneY();
                    dragStartRotateX = cameraXRotate.getAngle();
                    dragStartRotateY = cameraYRotate.getAngle();
                } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    double xDelta = event.getSceneX() - dragStartX;
                    double yDelta = event.getSceneY() - dragStartY;
                    cameraXRotate.setAngle(dragStartRotateX - (yDelta * 0.7));
                    cameraYRotate.setAngle(dragStartRotateY + (xDelta * 0.7));
                }
            }
        });
        
        subScene.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            
            @Override
            public void handle(final ScrollEvent event) {
                
                double z = cameraPosition.getZ() - (event.getDeltaY() * 0.1 * scaleFactor);
                z = Math.min(z, 0);
                cameraPosition.setZ(z);
            }
        });
        
        contentProperty.addListener(new ChangeListener<Node>() {

            @Override
            public void changed(final ObservableValue<? extends Node> ov, final Node oldContent, final Node newContent) {
                
                root.getChildren().remove(oldContent);
                
                if (newContent!=null) {
                    root.getChildren().add(newContent);
    
                    adjustForSize();             
                    
                    rotateTransition = new RotateTransition();
                    rotateTransition.setAxis(Rotate.Y_AXIS);
                    rotateTransition.setDelay(Duration.millis(4));
                    rotateTransition.setDuration(Duration.millis(5000));
                    rotateTransition.setCycleCount(Animation.INDEFINITE);
                    rotateTransition.setAutoReverse(false);
                    rotateTransition.setInterpolator(Interpolator.LINEAR);
                    rotateTransition.setByAngle(360);
                    rotateTransition.setNode(newContent);
    
                    if (isRotating) {
                        rotateTransition.play();
                    }
                }
            }
        });
    }

    /**
     * Property for the content of the 3D scene.
     * 
     * @return the content property of the 3D scene
     */
    public ObjectProperty<Node> contentProperty() {
        return contentProperty;
    }

    /**
     * Gets the current content of the 3D scene.
     * 
     * @return the current content of the 3D scene
     */
    public Node getContent() {
        return contentProperty.get();
    }
    
    /**
     * Sets the content to be displayed in the 3D scene.
     * 
     * @return the content to be displayed
     */
    public void setContent(Node content) {
        contentProperty.set(content);
    }

    /**
     * Gets the sub-scene that the 3D model is displayed in.
     * 
     * @return the sub-scene the 3D model is displayed in
     */
    public SubScene getSubScene() {
        return subScene;
    }

    /**
     * Toggles rotation of the content on and off.
     */
    public void toggleRotation() {
        
        if (isRotating) {
            rotateTransition.pause();
            isRotating = false;
        } else {
            rotateTransition.play();
            isRotating = true;
        }
    }
    
    /**
     * Gets the 3D scene's camera.
     * 
     * @return the perspective camera of the 3D scene
     */
    public PerspectiveCamera getCamera() {
        return camera;
    }
    
    /**
     * Sets initial translate values and camera settings based on the size of the model.
     */
    private void adjustForSize() {
        
        Node content = contentProperty.get();
        
        double width = content.getLayoutBounds().getWidth();
        double height = content.getLayoutBounds().getHeight();
        double depth = content.getLayoutBounds().getDepth();
        
        content.setTranslateX(-content.getLayoutBounds().getMinX() - width / 2);
        content.setTranslateY(-content.getLayoutBounds().getMinY() - height / 2);
        content.setTranslateZ(-content.getLayoutBounds().getMinZ() - depth / 2);
        
        scaleFactor = Math.max(Math.max(width, height), depth)/25;
        
        cameraPosition.setZ(-60*scaleFactor);
    }
}
