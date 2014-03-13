/*
 * Copyright (C) 2014 TESIS DYNAware GmbH.
 * All rights reserved. Use is subject to license terms.
 * 
 * This file is licensed under the Eclipse Public License v1.0, which accompanies this
 * distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
 
// These methods are JNI methods. For documentation see JFbxLib.java.

#include "JFbxLib.h"

JNIEXPORT jboolean JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_open(JNIEnv *env, jobject obj, jstring filePath) {

	sdkManager = FbxManager::Create();

	FbxIOSettings *ios = FbxIOSettings::Create(sdkManager, IOSROOT);

	sdkManager->SetIOSettings(ios);

	FbxImporter *importer = FbxImporter::Create(sdkManager,"");

	if(!importer->Initialize(env->GetStringUTFChars(filePath, 0), -1, sdkManager->GetIOSettings())) {
		throwIOException(env);
	}

	FbxScene *scene = FbxScene::Create(sdkManager,"");

	importer->Import(scene);
	importer->Destroy();

	currentNode = scene->GetRootNode();

	return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_close(JNIEnv *env, jobject obj) {

	// Check FBX file has been opened.
	if (!isOpen()) { return; }

	sdkManager->Destroy();
}

JNIEXPORT jboolean JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_nextChild(JNIEnv *env, jobject obj) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	if (currentNode->GetChildCount(false)>0) {
		currentNode = currentNode->GetChild(0);
		return JNI_TRUE;
	}
	else {
		return JNI_FALSE;
	}
}

JNIEXPORT jboolean JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_nextSibling(JNIEnv *env, jobject obj) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	FbxNode *parent = currentNode->GetParent();

	if (parent!=NULL) {
		const int siblingCount = parent->GetChildCount(false);

		for (int i=0; i<siblingCount-1; i++) {
			FbxNode *sibling = parent->GetChild(i);
			if (currentNode == sibling) {
				currentNode = parent->GetChild(i+1);
				return JNI_TRUE;
			}
		}
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_nextParent(JNIEnv *env, jobject obj) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	if (currentNode->GetParent()!=NULL) {
		currentNode = currentNode->GetParent();
		return JNI_TRUE;
	}
	else {
		return JNI_FALSE;
	}
}

JNIEXPORT jstring JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getNodeName(JNIEnv *env, jobject obj) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	return env->NewStringUTF(currentNode->GetName());
}

JNIEXPORT jdoubleArray JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getNodeGlobalAffineTransformation(JNIEnv *env, jobject obj) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	double *fbxAffine = (double *)currentNode->EvaluateGlobalTransform();

	jdoubleArray affine = env->NewDoubleArray(16);

	// Check memory could be allocated.
	if (affine == NULL) { throwOutOfMemoryError(env); }

	env->SetDoubleArrayRegion(affine, 0, 16, fbxAffine);

	return affine;
}

JNIEXPORT jdoubleArray JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getNodeGeometricTranslation(JNIEnv *env, jobject obj) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	double *fbxTranslation = (double *)currentNode->GetGeometricTranslation(FbxNode::eSourcePivot);

	jdoubleArray translation = env->NewDoubleArray(3);

	// Check memory could be allocated.
	if (translation == NULL) { throwOutOfMemoryError(env); }

	env->SetDoubleArrayRegion(translation, 0, 3, fbxTranslation);

	return translation;
}

JNIEXPORT jint JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getNodeAttributeCount(JNIEnv *env, jobject obj) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	return currentNode->GetNodeAttributeCount();
}

JNIEXPORT jstring JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getNodeAttributeName(JNIEnv *env, jobject obj, jint attributeIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	return env->NewStringUTF(currentNode->GetNodeAttributeByIndex(attributeIndex)->GetName());
}

JNIEXPORT jstring JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getNodeAttributeType(JNIEnv *env, jobject obj, jint attributeIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check attribute index bounds for safety.
	if (!checkAttributeBounds(attributeIndex)) { throwArrayOutOfBoundsException(env); }

	FbxNodeAttribute::EType eType = currentNode->GetNodeAttributeByIndex(attributeIndex)->GetAttributeType();

	switch(eType) {
	case FbxNodeAttribute::EType::eNull: return env->NewStringUTF("null");
	case FbxNodeAttribute::EType::eMarker: return env->NewStringUTF("marker");
	case FbxNodeAttribute::EType::eSkeleton: return env->NewStringUTF("skeleton");
	case FbxNodeAttribute::EType::eMesh: return env->NewStringUTF("mesh");
	case FbxNodeAttribute::EType::eNurbs: return env->NewStringUTF("nurbs");
	case FbxNodeAttribute::EType::ePatch: return env->NewStringUTF("patch");
	case FbxNodeAttribute::EType::eCamera: return env->NewStringUTF("camera");
	case FbxNodeAttribute::EType::eCameraStereo: return env->NewStringUTF("stereo");
	case FbxNodeAttribute::EType::eCameraSwitcher: return env->NewStringUTF("camera switcher");
	case FbxNodeAttribute::EType::eLight: return env->NewStringUTF("light");
	case FbxNodeAttribute::EType::eOpticalReference: return env->NewStringUTF("optical reference");
	case FbxNodeAttribute::EType::eOpticalMarker: return env->NewStringUTF("marker");
	case FbxNodeAttribute::EType::eNurbsCurve: return env->NewStringUTF("nurbs curve");
	case FbxNodeAttribute::EType::eTrimNurbsSurface: return env->NewStringUTF("trim nurbs surface");
	case FbxNodeAttribute::EType::eBoundary: return env->NewStringUTF("boundary");
	case FbxNodeAttribute::EType::eNurbsSurface: return env->NewStringUTF("nurbs surface");
	case FbxNodeAttribute::EType::eShape: return env->NewStringUTF("shape");
	case FbxNodeAttribute::EType::eLODGroup: return env->NewStringUTF("lodgroup");
	case FbxNodeAttribute::EType::eSubDiv: return env->NewStringUTF("subdiv");
	default: return env->NewStringUTF("unknown");
	}
}

JNIEXPORT jboolean JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_isTriangleMesh(JNIEnv *env, jobject obj, jint attributeIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check attribute index bounds for safety.
	if (!checkAttributeBounds(attributeIndex)) { throwArrayOutOfBoundsException(env); }

	// Check attribute type for safety.
	if (!isValidType(attributeIndex, FbxNodeAttribute::EType::eMesh)) { return false; }

	return ((FbxMesh*) currentNode->GetNodeAttributeByIndex(attributeIndex))->IsTriangleMesh();
}

JNIEXPORT void JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_triangulate(JNIEnv *env, jobject obj, jint attributeIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check attribute index bounds for safety.
	if (!checkAttributeBounds(attributeIndex)) { throwArrayOutOfBoundsException(env); }

	// Check attribute type for safety.
	if (!isValidType(attributeIndex, FbxNodeAttribute::EType::eMesh) &&
		!isValidType(attributeIndex, FbxNodeAttribute::EType::ePatch) &&
		!isValidType(attributeIndex, FbxNodeAttribute::EType::eNurbs) &&
		!isValidType(attributeIndex, FbxNodeAttribute::EType::eNurbsSurface)) { return; }

	FbxGeometryConverter *converter = new FbxGeometryConverter(sdkManager);
	converter->Triangulate(currentNode->GetNodeAttributeByIndex(attributeIndex), true);
}

JNIEXPORT jfloatArray JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMeshVertices(JNIEnv *env, jobject obj, jint attributeIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check attribute index bounds for safety.
	if (!checkAttributeBounds(attributeIndex)) { throwArrayOutOfBoundsException(env); }

	// Check attribute type for safety.
	if (!isValidType(attributeIndex, FbxNodeAttribute::EType::eMesh)) { return NULL; }

	FbxMesh* mesh = (FbxMesh*)currentNode->GetNodeAttributeByIndex(attributeIndex);

	FbxVector4 *controlPoints    = mesh->GetControlPoints();
	const int controlPointsCount = mesh->GetControlPointsCount();

	jfloatArray vertices = env->NewFloatArray(3*controlPointsCount);

	// Check memory could be allocated.
	if (vertices == NULL) {	throwOutOfMemoryError(env); }

	for (int i=0; i<controlPointsCount; i++)	{
		jfloat vertex[3];

		vertex[0] = controlPoints[i][0];
		vertex[1] = controlPoints[i][1];
		vertex[2] = controlPoints[i][2];

		env->SetFloatArrayRegion(vertices, 3*i, 3, vertex);
	}

	return vertices;
}

JNIEXPORT jfloatArray JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMeshTexCoords(JNIEnv *env, jobject obj, jint attributeIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check attribute index bounds for safety.
	if (!checkAttributeBounds(attributeIndex)) { throwArrayOutOfBoundsException(env); }

	// Check attribute type for safety.
	if (!isValidType(attributeIndex, FbxNodeAttribute::EType::eMesh)) { return NULL; }

	FbxMesh* mesh = (FbxMesh*)currentNode->GetNodeAttributeByIndex(attributeIndex);

	// Currently we only read from the first layer.
	FbxLayerElementUV *firstLayer = mesh->GetElementUV(0);

	if (firstLayer==NULL) {	return NULL; }

	const int uvCount = firstLayer->GetDirectArray().GetCount();

	jfloatArray texCoords = env->NewFloatArray(2*uvCount);
	if (texCoords == NULL) {
		return NULL;
	}

	for (int i=0; i<uvCount; i++) {
		jfloat uv[2];

		// Note that we invert the 'v' index to match the JavaFX (DirectX?) convention.
		uv[0] = firstLayer->GetDirectArray().GetAt(i)[0];
		uv[1] = 1-firstLayer->GetDirectArray().GetAt(i)[1];

		env->SetFloatArrayRegion(texCoords, 2*i, 2, uv);
	}

	return texCoords;
}

JNIEXPORT jintArray JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMeshFaces(JNIEnv *env, jobject obj, jint attributeIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check attribute index bounds for safety.
	if (!checkAttributeBounds(attributeIndex)) { throwArrayOutOfBoundsException(env); }

	// Check attribute type for safety.
	if (!isValidType(attributeIndex, FbxNodeAttribute::EType::eMesh)) { return NULL; }

	FbxMesh* mesh = (FbxMesh*)currentNode->GetNodeAttributeByIndex(attributeIndex);

	bool byPolygonVertex = false;
	bool index           = false;
	bool indexToDirect   = false;
	bool direct          = false;

	FbxLayerElementUV *firstLayer = mesh->GetElementUV(0);
	if (firstLayer!=NULL) {
		byPolygonVertex = firstLayer->GetMappingMode()==FbxLayerElement::EMappingMode::eByPolygonVertex;
		index           = firstLayer->GetReferenceMode()==FbxLayerElement::EReferenceMode::eIndex;
		indexToDirect   = firstLayer->GetReferenceMode()==FbxLayerElement::EReferenceMode::eIndexToDirect;
		direct          = firstLayer->GetReferenceMode()==FbxLayerElement::EReferenceMode::eDirect;
	}

	const int polygonCount = mesh->GetPolygonCount();

	jintArray faces = env->NewIntArray(6*polygonCount);

	// Check memory could be allocated.
	if (faces == NULL) { throwOutOfMemoryError(env); }

	for (int i=0; i<polygonCount; i++) {
		jint face[6];

		// Assume we are working with a triangle mesh.
		face[0] = mesh->GetPolygonVertex(i,0);
		face[2] = mesh->GetPolygonVertex(i,1);
		face[4] = mesh->GetPolygonVertex(i,2);

		if (byPolygonVertex && (index || indexToDirect)) {
			face[1] = mesh->GetTextureUVIndex(i, 0);
			face[3] = mesh->GetTextureUVIndex(i, 1);
			face[5] = mesh->GetTextureUVIndex(i, 2);
		}
		else if (direct) {
			face[1] = 3*i;
			face[3] = 3*i+1;
			face[5] = 3*i+2;
		}
		else {
			face[1] = 0;
			face[3] = 0;
			face[5] = 0;
		}

		env->SetIntArrayRegion(faces, 6*i, 6, face);
	}

	return faces;
}

JNIEXPORT jintArray JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMeshFaceSmoothingGroups(JNIEnv *env, jobject obj, jint attributeIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check attribute index bounds for safety.
	if (!checkAttributeBounds(attributeIndex)) { throwArrayOutOfBoundsException(env); }

	// Check attribute type for safety.
	if (!isValidType(attributeIndex, FbxNodeAttribute::EType::eMesh)) { return NULL; }

	FbxMesh* mesh = (FbxMesh*)currentNode->GetNodeAttributeByIndex(attributeIndex);

	FbxGeometryElementSmoothing* smoothingElement = mesh->GetElementSmoothing(0);

	// If smoothing is not defined explicitly, try to convert from normals. Convert edge-smoothing to face-smoothing.
	if (!smoothingElement || smoothingElement->GetMappingMode() == FbxGeometryElement::eByEdge) {

		FbxGeometryConverter geometryConverter(sdkManager);
		if (!smoothingElement) {
			geometryConverter.ComputeEdgeSmoothingFromNormals(mesh);
			smoothingElement = mesh->GetElementSmoothing(0);
		}
		if (smoothingElement->GetMappingMode() == FbxGeometryElement::eByEdge) {
			geometryConverter.ComputePolygonSmoothingFromEdgeSmoothing(mesh);
		}
	}

	const int polygonCount = mesh->GetPolygonCount();

	jintArray faceSmoothingGroups = env->NewIntArray(polygonCount);

	// Check memory could be allocated.
	if (faceSmoothingGroups == NULL) { throwOutOfMemoryError(env); }

	for (int i=0; i<polygonCount; i++) {
		jint iValue = smoothingElement->GetDirectArray().GetAt(i);
		env->SetIntArrayRegion(faceSmoothingGroups, i, 1, &iValue);
	}

	return faceSmoothingGroups;
}

JNIEXPORT jint JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMaterialCount(JNIEnv *env, jobject obj) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	return currentNode->GetMaterialCount();
}

JNIEXPORT jstring JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMaterialName(JNIEnv *env, jobject obj, jint materialIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check material index bounds for safety.
	if (!checkMaterialBounds(materialIndex)) { throwArrayOutOfBoundsException(env); }

	return env->NewStringUTF(currentNode->GetMaterial(materialIndex)->GetName());
}

JNIEXPORT jdoubleArray JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMaterialDiffuseColor(JNIEnv *env, jobject obj, jint materialIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check j index bounds for safety.
	if (!checkMaterialBounds(materialIndex)) { throwArrayOutOfBoundsException(env); }

	FbxClassId shaderType = currentNode->GetMaterial(materialIndex)->GetClassId();

	// Exit if the material is neither Lambert nor Phong shaded.
	if (!shaderType.Is(FbxSurfaceLambert::ClassId) && !shaderType.Is(FbxSurfacePhong::ClassId)) { return NULL; }

	// Note: the lines below are not specific to Lambert surfaces -- FbxSurfacePhong inherits from FbxSurfaceLambert.
	FbxDouble3 fbxColor      = ((FbxSurfaceLambert *) currentNode->GetMaterial(materialIndex))->Diffuse;
	FbxDouble fbxColorFactor = ((FbxSurfaceLambert *) currentNode->GetMaterial(materialIndex))->DiffuseFactor;

	return getMaterialColorByProperty(env, materialIndex, fbxColor, fbxColorFactor);
}

JNIEXPORT jdoubleArray JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMaterialSpecularColor(JNIEnv *env, jobject obj, jint materialIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check j index bounds for safety.
	if (!checkMaterialBounds(materialIndex)) { throwArrayOutOfBoundsException(env); }

	FbxClassId shaderType = currentNode->GetMaterial(materialIndex)->GetClassId();

	// Exit if the material is not Phong shaded.
	if (!shaderType.Is(FbxSurfacePhong::ClassId)) { return NULL; }

	// Note: the lines below are not specific to Lambert surfaces, as FbxSurfacePhong inherits from FbxSurfaceLambert.
	FbxDouble3 fbxColor      = ((FbxSurfacePhong *) currentNode->GetMaterial(materialIndex))->Specular;
	FbxDouble fbxColorFactor = ((FbxSurfacePhong *) currentNode->GetMaterial(materialIndex))->SpecularFactor;

	return getMaterialColorByProperty(env, materialIndex, fbxColor, fbxColorFactor);
}

JNIEXPORT jdouble JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMaterialSpecularPower(JNIEnv *env, jobject obj, jint materialIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check j index bounds for safety.
	if (!checkMaterialBounds(materialIndex)) { throwArrayOutOfBoundsException(env); }

	FbxClassId shaderType = currentNode->GetMaterial(materialIndex)->GetClassId();

	// Exit with -1 if the material is not Phong shaded.
	if (!shaderType.Is(FbxSurfacePhong::ClassId)) { return -1; }

	return ((FbxSurfacePhong *) currentNode->GetMaterial(materialIndex))->Shininess;
}

JNIEXPORT jstring JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMaterialDiffuseMap(JNIEnv *env, jobject obj, jint materialIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check j index bounds for safety.
	if (!checkMaterialBounds(materialIndex)) { throwArrayOutOfBoundsException(env); }

	const char *type = currentNode->GetMaterial(materialIndex)->sDiffuse;

	return getMaterialMapByProperty(env, materialIndex, type);
}

JNIEXPORT jstring JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMaterialSpecularMap(JNIEnv *env, jobject obj, jint materialIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check j index bounds for safety.
	if (!checkMaterialBounds(materialIndex)) { throwArrayOutOfBoundsException(env); }

	const char *type = currentNode->GetMaterial(materialIndex)->sSpecular;

	return getMaterialMapByProperty(env, materialIndex, type);
}

JNIEXPORT jstring JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMaterialBumpMap(JNIEnv *env, jobject obj, jint materialIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check j index bounds for safety.
	if (!checkMaterialBounds(materialIndex)) { throwArrayOutOfBoundsException(env); }

	const char *type = currentNode->GetMaterial(materialIndex)->sBump;

	return getMaterialMapByProperty(env, materialIndex, type);
}

JNIEXPORT jstring JNICALL Java_de_tesis_dynaware_javafx_graphics_importers_fbx_JFbxLib_getMaterialSelfIlluminationMap(JNIEnv *env, jobject obj, jint materialIndex) {

	// Check FBX file has been opened.
	if (!isOpen()) { throwFileClosedException(env); }

	// Check j index bounds for safety.
	if (!checkMaterialBounds(materialIndex)) { throwArrayOutOfBoundsException(env); }

	const char *property = currentNode->GetMaterial(materialIndex)->sEmissive;

	return getMaterialMapByProperty(env, materialIndex, property);
}

jdoubleArray getMaterialColorByProperty(JNIEnv *env, jint materialIndex, FbxDouble3 fbxColor, FbxDouble fbxColorFactor) {

	jdoubleArray color = env->NewDoubleArray(4);

	// Check memory could be allocated.
	if (color == NULL) { throwOutOfMemoryError(env); }

	FbxDouble transparencyFactor = ((FbxSurfaceLambert *) currentNode->GetMaterial(materialIndex))->TransparencyFactor;
	FbxDouble opacity;

	// Older FBX files may define the 'Opacity' property instead of 'TransparencyFactor'.
	FbxProperty opacityProperty = currentNode->GetMaterial(materialIndex)->FindProperty("Opacity");
	if (opacityProperty.IsValid()) {
		opacity = opacityProperty.Get<FbxDouble>();
	}
	else {
		opacity = 1-transparencyFactor;
	}

	double colorArray[4];
	for (int i=0; i<3; i++) {
		colorArray[i] = fbxColorFactor*fbxColor[i];
	}
	colorArray[3] = opacity;

	env->SetDoubleArrayRegion(color, 0, 4, colorArray);

	return color;
}

jstring getMaterialMapByProperty(JNIEnv *env, jint materialIndex, const char *property) {

	FbxTexture *texture = currentNode->GetMaterial(materialIndex)->FindProperty(property).GetSrcObject<FbxTexture>();

	if (texture) {
		FbxFileTexture *fileTexture = FbxCast<FbxFileTexture>(texture);

		if (fileTexture) {
			return env->NewStringUTF(fileTexture->GetFileName());
		}
	}
	return NULL;
}
