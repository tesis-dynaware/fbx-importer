/*
 * Copyright (C) 2014 TESIS DYNAware GmbH.
 * All rights reserved. Use is subject to license terms.
 * 
 * This file is licensed under the Eclipse Public License v1.0, which accompanies this
 * distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

#ifndef JFBXLIB_H_
#define JFBXLIB_H_

#include <stdlib.h>
#include <fbxsdk.h>
#include "../../../build/generated/de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib.h"

static FbxManager *sdkManager;
static FbxNode *currentNode;

/*
 * Checks whether a FBX file is currently open.
 */
inline bool isOpen() {
	return (sdkManager && currentNode);
}

/*
 * Checks whether the given attribute index is valid.
 */
inline bool checkAttributeBounds(int attributeIndex) {
	return (attributeIndex>=0 && attributeIndex<currentNode->GetNodeAttributeCount());
}

/*
 * Checks whether the given material index is valid.
 */
inline bool checkMaterialBounds(int materialIndex) {
	return (materialIndex>=0 && materialIndex<currentNode->GetMaterialCount());
}

/*
 * Checks whether the given attribute index & type combination is valid.
 */
inline bool isValidType(int attributeIndex, FbxNodeAttribute::EType type) {
	return currentNode->GetNodeAttributeByIndex(attributeIndex)->GetAttributeType()==type;
}

/*
 * Throws a new IOException back to the Java class calling this library.
 */
inline void throwIOException(JNIEnv *env) {
	env->ThrowNew(env->FindClass("java/io/IOException"), "jfbxlib: Corrupt or invalid FBX file.\n");
}

/*
 * Throws a new RuntimeException back to the Java class calling this library.
 */
inline void throwFileClosedException(JNIEnv *env) {
	env->ThrowNew(env->FindClass("java/lang/RuntimeException"), "jfbxlib: File must be opened before data can be queried.\n");
}

/*
 * Throws a new ArrayOutOfBoundsException back to the Java class calling this library.
 */
inline void throwArrayOutOfBoundsException(JNIEnv *env) {
	env->ThrowNew(env->FindClass("java/lang/ArrayIndexOutOfBoundsException"), "jfbxlib: Index out of bounds.\n");
}

/*
 * Throws a new OutOfMemoryError back to the Java class calling this library.
 */
inline void throwOutOfMemoryError(JNIEnv *env) {
	env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), "jfbxlib: Out of memory.\n");
}

/*
 * Gets the color of a material.
 */
jdoubleArray getMaterialColorByProperty(JNIEnv *env, jint materialIndex, FbxDouble3 fbxColor, FbxDouble fbxColorFactor);

/*
 * Gets the texture of a material.
 */
jstring getMaterialMapByProperty(JNIEnv *env, jint materialIndex, const char *property);

#endif /* JFBXLIB_H_ */
