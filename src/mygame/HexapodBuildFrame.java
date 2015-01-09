/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import hexapod.HexapodLeg;

/**
 *
 * @author ANTMAN
 */
public class HexapodBuildFrame extends SimpleApplication{

    private Spatial sceneModel;
    private RigidBodyControl scene;
    private BulletAppState bulletAppState;
    
    private static final float MASS_BASE = 1f;
    
    public static void main(String[] args) {
        HexapodBuildFrame frame = new HexapodBuildFrame();
        frame.start();
    }
    
    @Override
    public void simpleInitApp() {
        mouseInput.setCursorVisible(false);
        cam.setLocation(new Vector3f(0f, 10f, -10f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(75);
        
	bulletAppState = new BulletAppState();
	stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        
        initScene();
        initHexapod();
    }
    
    private void attachCoordinateAxes(Vector3f pos){
        Arrow arrow = new Arrow(Vector3f.UNIT_X);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Y);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Z);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
    }

    private Geometry putShape(Mesh shape, ColorRGBA color){
        Geometry g = new Geometry("coordinate axis", shape);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        rootNode.attachChild(g);
        return g;
    }

    private void initScene()
    {
        sceneModel =assetManager.loadModel("Scenes/robotScene.j3o");
        sceneModel.setLocalTranslation(0f, -10f, 0f);
        
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape((Node) sceneModel);
	scene = new RigidBodyControl(sceneShape, 0);
	sceneModel.addControl(scene);
        rootNode.attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(scene);
        
        attachCoordinateAxes(new Vector3f(0.0f,0.0f,0.0f));
    }
    
    private void initHexapod()
    {
        final RigidBodyControl baseControl = new RigidBodyControl(HexapodShapeFactory.createBaseShape(), MASS_BASE);
        final Node baseNode = new Node("base");
        baseNode.addControl(baseControl);
        baseNode.setName("base");
        baseControl.setGravity(Vector3f.ZERO);       
        baseControl.setKinematic(true);
        
        /* A colored lit cube. Needs light source! */ 
        Box boxMesh = new Box(1f,1f,1f); 
        Geometry boxGeo = new Geometry("Colored Box", boxMesh); 
        Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"); 
        boxMat.setBoolean("UseMaterialColors", true); 
        boxMat.setColor("Ambient", ColorRGBA.Green); 
        boxMat.setColor("Diffuse", ColorRGBA.Green); 
        boxGeo.setMaterial(boxMat); 
         rootNode.attachChild(boxGeo);
        
        //baseNode.getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(0f,10f,0f));
        
        //rootNode.attachChild(baseNode);
        //bulletAppState.getPhysicsSpace().add(baseNode);
                
        HexapodLeg legTest = new HexapodLeg(assetManager, baseControl, new Vector3f(-3.8f, 0f, 0f), FastMath.HALF_PI);
        rootNode.attachChild(legTest);
        bulletAppState.getPhysicsSpace().addAll(legTest);
        /*HexapodLeg legLF = new HexapodLeg(baseControl, new Vector3f(3.0f, -0.750f, 4.5f), +0.588f);
        HexapodLeg legLM = new HexapodLeg(baseControl, new Vector3f(3.8f, 0f, 0f), FastMath.HALF_PI);
        HexapodLeg legLR = new HexapodLeg(baseControl, new Vector3f(3.0f, 0f, -4.5f), FastMath.PI - 0.588f);
        
        HexapodLeg legRF = new HexapodLeg(baseControl, new Vector3f(-3.0f, 0f, 4.5f), -0.588f);
        HexapodLeg legRM = new HexapodLeg(baseControl, new Vector3f(-3.8f, 0f, 0f), -FastMath.HALF_PI);
        HexapodLeg legRR = new HexapodLeg(baseControl, new Vector3f(-3.0f, 0f, -4.5f), 0.588f - FastMath.PI);
        
        legLF.getCoxaNode().attachChild(boxGeo);
        rootNode.attachChild(legLF);
        rootNode.attachChild(legLM);
        rootNode.attachChild(legLR);
        
        rootNode.attachChild(legRF);
        rootNode.attachChild(legRM);
        rootNode.attachChild(legRR);
        
        
	bulletAppState.getPhysicsSpace().addAll(legLF);
        /*bulletAppState.getPhysicsSpace().addAll(legLM);
        bulletAppState.getPhysicsSpace().addAll(legLR);
        
        bulletAppState.getPhysicsSpace().addAll(legRF);
        bulletAppState.getPhysicsSpace().addAll(legRM);
        bulletAppState.getPhysicsSpace().addAll(legRR);*/
        
        
    }
    
    
}
