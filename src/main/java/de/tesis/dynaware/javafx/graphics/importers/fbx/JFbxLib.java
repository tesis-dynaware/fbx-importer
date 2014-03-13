/*
 * Copyright (C) 2014 TESIS DYNAware GmbH.
 * All rights reserved. Use is subject to license terms.
 * 
 * This file is licensed under the Eclipse Public License v1.0, which accompanies this
 * distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package de.tesis.dynaware.javafx.graphics.importers.fbx;

/**
 * Wrapper class for the C++ library <b>jfbxlib</b>.
 * 
 * <p>
 * The native methods here are implemented in src/main/cpp/JFbxLib.cpp.
 * </p>
 */
public class JFbxLib {

    /**
     * Creates a new JFbxLib instance, which can be used to load data from FBX files.
     */
    public JFbxLib() {
        System.loadLibrary("jfbxlib");
    }

    /**
     * Opens the FBX file and sets the current node to the root node of the FBX graph.
     * 
     * @param filePath the absolute path to the FBX file
     * @return <tt>true</tt> if the file was opened successfully
     */
    public native boolean open(String filePath);

    /**
     * Deallocates the memory used by the library. This must be called when everything has been loaded and the library
     * is no longer needed. Do not forget this or there will be a memory leak!
     */
    public native void close();

    /**
     * Iterates to the next child in the FBX node graph, if it exists. Does nothing otherwise.
     * 
     * @return <tt>true</tt> if the child was found
     */
    public native boolean nextChild();

    /**
     * Iterates to the next sibling in the FBX node graph, if it exists. Does nothing otherwise.
     * 
     * @return <tt>true</tt> if the sibling was found
     */
    public native boolean nextSibling();

    /**
     * Iterates to the next parent in the FBX node graph, if it exists. Does nothing otherwise.
     * 
     * @return <tt>true</tt> if the parent was found
     */
    public native boolean nextParent();

    /**
     * Gets the name of the current node in the FBX node graph.
     * 
     * @return the name of the current node
     */
    public native String getNodeName();

    /**
     * Gets the global affine transformation associated to the current node in the FBX graph.
     * 
     * <p>
     * For the affine matrix
     * </p>
     * 
     * <pre>
     *   [  mxx  mxy  mxz  tx  ]
     *   [  myx  myy  myz  ty  ]
     *   [  mzx  mzy  mzz  tz  ]
     *   [  0    0    0    1   ]
     * </pre>
     * <p>
     * a 1D array is returned consisting of the 4 columns stacked on top of each other.
     * </p>
     * 
     * @return the 16 elements of the affine transformation matrix
     */
    public native double[] getNodeGlobalAffineTransformation();
    
    /**
     * Gets the geometric translation of the node.
     * 
     * <p>
     * This should be applied <b>in addition to</b> the global affine transformation.
     * </p>
     *
     * @return the x, y, and z values of the node's geometric translation
     */
    public native double[] getNodeGeometricTranslation();

    /**
     * Gets the number of attributes attached to the current node.
     * 
     * @return the number of attributes
     */
    public native int getNodeAttributeCount();

    /**
     * Gets the name of an attribute attached to the current node.
     * 
     * @param i the index of the attribute
     * @return the name of the i'th attribute
     */
    public native String getNodeAttributeName(int i);

    /**
     * Gets the type of an attribute attached to the current node.
     * 
     * @param i the index of the attribute
     * @return the type of the i'th attribute
     */
    public native String getNodeAttributeType(int i);

    /**
     * Indicates whether an attribute attached to the current node is a triangle mesh.
     * 
     * @param i the index of the attribute
     * @return <tt>true</tt> if the i'th attribute is a triangular mesh, otherwise <tt>false</tt>
     */
    public native boolean isTriangleMesh(int i);

    /**
     * Converts a patch, NURBS, NURBS surface, or a higher-order polygon mesh to a triangle mesh for an attribute
     * attached to the current node.
     * 
     * @param i the index of the attribute
     */
    public native void triangulate(int i);

    /**
     * Gets the vertices of a mesh attribute attached to the current node.
     * 
     * @param i the index of the attribute
     * @return the array of vertices in the form (x1, y1, z1, x2, y2, z2, ... )
     */
    public native float[] getMeshVertices(int i);

    /**
     * Gets the UV texture coordinates of a mesh attribute attached to the current node.
     * 
     * @param i the index of the attribute
     * @return the array of texture coordinates in the form (u1, v1, u2, v2, ... )
     */
    public native float[] getMeshTexCoords(int i);

    /**
     * Gets the face coordinates of a mesh attribute attached to the current node.
     * 
     * @param i the index of the attribute
     * @return the array of face coordinates in the form ( p1, t1, p2, t2, p3, t3, ... ) where the p's are vertex
     *         indices and the t's are texture coordinate indices.
     */
    public native int[] getMeshFaces(int i);

    /**
     * Gets the face smoothing groups of a mesh attribute attached to the current node.
     * 
     * <p>
     * If face smoothing groups are not defined for the mesh, but normals or edge smoothing groups <b>are</b> defined,
     * then these will automatically be converted to face smoothing groups.
     * </p>
     * 
     * @param i the index of the attribute
     * @return the array of face smoothing groups, or <tt>null</tt> if no smoothing is defined
     */
    public native int[] getMeshFaceSmoothingGroups(int i);

    /**
     * Gets the number of materials attached to the current node.
     * 
     * @return the number of materials
     */
    public native int getMaterialCount();

    /**
     * Gets the name of a material attached to the current node.
     * 
     * @param j the index of the material
     * @return the name of the j'th material
     */
    public native String getMaterialName(int j);

    /**
     * Gets the diffuse color of a material attached to the current node.
     * 
     * @param j the index of the material
     * @return the diffuse color of the j'th material in the form (r, g, b, o) where o is the opacity, or <tt>null</tt>
     *         if no diffuse color is defined
     */
    public native double[] getMaterialDiffuseColor(int j);

    /**
     * Gets the specular color of a material attached to the current node.
     * 
     * @param j the index of the material
     * @return the diffuse color of the j'th material in the form (r, g, b, o) where o is the opacity, or <tt>null</tt>
     *         if no specular color is defined
     */
    public native double[] getMaterialSpecularColor(int j);

    /**
     * Gets the specular power (shininess) of a material attached to the current node.
     * 
     * @param j the index of the material
     * @return the specular power of the j'th material, or <tt>null</tt> if no specular power is defined
     */
    public native double getMaterialSpecularPower(int j);

    /**
     * Gets the absolute path to the texture file for the (first-layer) diffuse map of a material attached to the
     * current node.
     * 
     * @param j the index of the material
     * @return the absolute path to the diffuse texture map of the j'th material, or <tt>null</tt> if no diffuse texture
     *         map is defined
     */
    public native String getMaterialDiffuseMap(int j);

    /**
     * Gets the absolute path to the texture file for the (first-layer) specular map of a material attached to the
     * current node.
     * 
     * @param j the index of the material
     * @return the absolute path to the specular texture map of the j'th material, or <tt>null</tt> if no specular
     *         texture map is defined
     */
    public native String getMaterialSpecularMap(int j);

    /**
     * Gets the absolute path to the texture file for the (first-layer) bump map of a material attached to the current
     * node.
     * 
     * @param j the index of the material
     * @return the absolute path to the bump map of the j'th material, or <tt>null</tt> if no bump map is defined
     */
    public native String getMaterialBumpMap(int j);

    /**
     * Gets the absolute path to the texture file for the (first-layer) self-illumination map of a material attached to
     * the current node.
     * 
     * @param j the index of the material
     * @return the absolute path to the self-illumination map of the j'th material, or <tt>null</tt> if no
     *         self-illumination map is defined
     */
    public native String getMaterialSelfIlluminationMap(int j);
}