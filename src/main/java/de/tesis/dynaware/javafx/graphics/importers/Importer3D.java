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
package de.tesis.dynaware.javafx.graphics.importers;

import java.io.IOException;

import de.tesis.dynaware.javafx.graphics.importers.fbx.FbxImporter;
import javafx.scene.Group;

/**
 * Base Importer for all supported 3D file formats.
 */
public final class Importer3D {

    /**
     * Get array of extension filters for supported file formats.
     * 
     * @return array of extension filters for supported file formats
     */
    public static String[] getSupportedFormatExtensionFilters() {
        return new String[] { "*.fbx"};
    }

    /**
     * Load a 3D file, always loaded as TriangleMesh.
     * 
     * @param fileUrl the url of the 3D file to load
     * @return the loaded Node which could be a MeshView or a Group
     * @throws IOException if there is a problem loading the file
     */
    public static Group load(final String fileUrl) throws IOException {

        final int dot = fileUrl.lastIndexOf('.');
        if (dot <= 0) {
            throw new IOException("Unknown 3D file format, url missing extension [" + fileUrl + "]");
        }
        final String extension = fileUrl.substring(dot + 1, fileUrl.length()).toLowerCase();

        switch (extension) {
        case "fbx":
            FbxImporter fbxImporter = new FbxImporter(fileUrl);
            return fbxImporter.getRoot();
        default:
            throw new IOException("Unsupported 3D file format [" + extension + "]");
        }
    }
}
