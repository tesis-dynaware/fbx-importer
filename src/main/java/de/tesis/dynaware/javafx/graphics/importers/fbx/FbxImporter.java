/*
 * Copyright (C) 2014 TESIS DYNAware GmbH.
 * All rights reserved. Use is subject to license terms.
 * 
 * This file is licensed under the Eclipse Public License v1.0, which accompanies this
 * distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package de.tesis.dynaware.javafx.graphics.importers.fbx;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 * Responsible for loading a FBX model from a file into a {@link MeshView}.
 * 
 * <p>
 * The data is read from the FBX file (which can be binary or ASCII) via the native methods in {@link JFbxLib}.
 * </p>
 */
public class FbxImporter {

    private static final String UTF_8 = "utf-8";
    
    private static final String MESH = "mesh";
    private static final String PATCH = "patch";
    private static final String NURBS = "nurbs";
    private static final String NURBS_SURFACE = "nurbs surface";
    
    private Group root = new Group();
    private Map<String, Material> materials = new HashMap<>();

    /**
     * Creates a new FBX importer for the given FBX file.
     * 
     * @param url the URL string of the FBX file to be imported
     * @throws IOException if the URL string cannot be parsed
     */
    public FbxImporter(String url) throws IOException {

        String filePath = new File(new URL(URLDecoder.decode(url,  UTF_8)).getFile()).getPath();
        read(filePath);
    }
    
    /**
     * Returns the root JavaFX node that the FBX file is loaded into.
     *  
     * @return the root node of the imported FBX file
     */
    public Group getRoot() {
        return root;
    }
    
    /**
     * Reads a FBX file using the native library jfbxlib.
     * 
     * @param filePath the path of the fbx file
     * @throws IOException if there is a problem loading the file
     */
    private void read(String filePath) throws IOException {

        JFbxLib jFbxLib = new JFbxLib();

        if (jFbxLib.open(filePath)) {

            // Read nodes from FBX graph recursively, starting from root.
            readNode(jFbxLib);
        }

        jFbxLib.close();
    }

    /**
     * Reads a the current node of the FBX file.
     * 
     * @param jFbxLib the {@link JFbxLib} instance that has the file open
     * @throws IOException if there is a problem loading the file
     */
    private void readNode(JFbxLib jFbxLib) throws IOException {
        
        // Loop over all attributes of the current FBX node (usually there should be just 1?).
        for (int i=0; i< jFbxLib.getNodeAttributeCount(); i++) {
            
            String type = jFbxLib.getNodeAttributeType(i);
            
            // The following types can all be converted to triangle meshes by the FBX SDK.
            if (MESH.equals(type) || PATCH.equals(type) || NURBS.equals(type) || NURBS_SURFACE.equals(type)) {
                               
                MeshView meshView = new MeshView();
                meshView.setId(jFbxLib.getNodeName());

                if (!jFbxLib.isTriangleMesh(i)) {
                    jFbxLib.triangulate(i);
                }
                
                float vertices[]      = jFbxLib.getMeshVertices(i);
                float texCoords[]     = jFbxLib.getMeshTexCoords(i);
                int faces[]           = jFbxLib.getMeshFaces(i);
                int smoothingGroups[] = jFbxLib.getMeshFaceSmoothingGroups(i);

                TriangleMesh mesh = new TriangleMesh();
                
                // Vertices & faces must be non-null to have a sensible mesh.
                if (vertices!=null && faces!=null) {
                    mesh.getPoints().setAll(vertices);
                    mesh.getFaces().setAll(faces);
                    
                    // If no UV coordinates are found, set (u,v)=(0,0) to stop JavaFX spazzing out.
                    if (texCoords!=null) {
                        mesh.getTexCoords().setAll(texCoords);
                    } else {
                        mesh.getTexCoords().setAll(new float[]{0, 0});
                    }
                    
                    // Smoothing groups are optional.
                    if (smoothingGroups!=null) {
                        mesh.getFaceSmoothingGroups().setAll(smoothingGroups);
                    }
                }

                meshView.setMesh(mesh);
                
                // Are there materials attached to the current node? If so, use the first one.
                if (jFbxLib.getMaterialCount()>0) {

                    String name = jFbxLib.getMaterialName(0);

                    // Only load the material if we didn't already.
                    if (!materials.containsKey(name)) {

                        PhongMaterial material = new PhongMaterial();
                        
                        double[] diffuseColorArray = jFbxLib.getMaterialDiffuseColor(0);
                        if (diffuseColorArray!=null) {
                            material.setDiffuseColor(createColor(diffuseColorArray));
                         }
                        
                        double[] specularColorArray = jFbxLib.getMaterialSpecularColor(0);
                        if (specularColorArray!=null) {
                            material.setSpecularColor(createColor(specularColorArray));
                        }
                        
                        double specularPower = jFbxLib.getMaterialSpecularPower(0);
                        if (specularPower!=-1) {
                            material.setSpecularPower(specularPower);
                        }
                        
                        String diffuseMapFile = jFbxLib.getMaterialDiffuseMap(0);
                        if (diffuseMapFile!=null) {
                            material.setDiffuseMap(createImage(diffuseMapFile));
                        }
                        
                        String specularMapFile = jFbxLib.getMaterialSpecularMap(0);
                        if (specularMapFile!=null) {
                            material.setSpecularMap(createImage(specularMapFile));
                        }
                        
                        String bumpMapFile = jFbxLib.getMaterialBumpMap(0);
                        if (bumpMapFile!=null) {
                            material.setBumpMap(createImage(bumpMapFile));
                        }
                        
                        String selfIlluminationMapFile = jFbxLib.getMaterialSelfIlluminationMap(0);
                        if (selfIlluminationMapFile!=null) {
                            material.setSelfIlluminationMap(createImage(selfIlluminationMapFile));
                        }

                        materials.put(name, material);
                    }
                    meshView.setMaterial(materials.get(name));
                }

                double g[] = jFbxLib.getNodeGlobalAffineTransformation();
                if (g!=null) {
                    // In the FBX SDK the indices go *down* the columns of the affine matrix.
                    meshView.getTransforms().add(Transform.affine(g[0],g[4],g[8],g[12],g[1],g[5],g[9],g[13],g[2],g[6],g[10],g[14]));
                }
                
                double s[] = jFbxLib.getNodeGeometricTranslation();
                if (s!=null) {
                    meshView.getTransforms().add(new Translate(s[0], s[1], s[2]));
                }

                root.getChildren().add(meshView);
            } 
        } 

        // Repeat this process for the next node in the graph.
        if (jFbxLib.nextChild()) {
            readNode(jFbxLib);
        }
        else if (jFbxLib.nextSibling()) {
            readNode(jFbxLib);
        }
        while (jFbxLib.nextParent()) {
            if (jFbxLib.nextSibling()) {
                readNode(jFbxLib);
            }
        }
    }

    /**
     * Creates a new color for the given values.
     * 
     * @param colorArray
     * @return the new color
     */
    private Color createColor(double[] colorArray) {
        
        // Transparency doesn't seem to work properly yet in JavaFX 3D, just set alpha to 1.
        return new Color(colorArray[0], colorArray[1], colorArray[2], 1);
    }
    
    /**
     * Creates a new image for the given texture-map file path.
     * 
     * @param mapFile the file path of a texture map
     * @return an {@link Image} of the texture
     * @throws MalformedURLException if the given file path could not be transformed to a valid URL
     */
    private Image createImage(String mapFile) throws MalformedURLException {
        return new Image(new File(mapFile).toURI().toURL().toString());
    }
}