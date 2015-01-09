/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import hexapod.HexapodLeg;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author ANTMAN
 */
public class HexapodBuildFrame extends SimpleApplication{

    private Spatial sceneModel;
    private RigidBodyControl scene;
    private BulletAppState bulletAppState;
    
    private static final float MASS_BASE = 0f;

    private Map<HexapodLegEnum, Map<HexapodArticulation, HingeJoint>> joints;
	{
            this.joints = new EnumMap<HexapodLegEnum, Map<HexapodArticulation, HingeJoint>>(HexapodLegEnum.class);
            for (final HexapodLegEnum leg : HexapodLegEnum.values()) {
                this.joints.put(leg, new EnumMap<HexapodArticulation, HingeJoint>(HexapodArticulation.class));
            }
	}

	/** Model for joints */
	private Map<HexapodLegEnum, Map<HexapodArticulation, Float>> wantedAngles;
	{
            this.wantedAngles = new EnumMap<HexapodLegEnum, Map<HexapodArticulation, Float>>(HexapodLegEnum.class);
            for (final HexapodLegEnum leg : HexapodLegEnum.values()) {
                this.wantedAngles.put(leg, new EnumMap<HexapodArticulation, Float>(HexapodArticulation.class));
                for (final HexapodArticulation articulation : HexapodArticulation.values()) {
                        this.wantedAngles.get(leg).put(articulation, 0f);
                }
            }
	}

    
    public static void main(String[] args) {
        HexapodBuildFrame frame = new HexapodBuildFrame();
        frame.start();
    }
    
    @Override
    public void simpleInitApp() {
        mouseInput.setCursorVisible(false);
        cam.setLocation(new Vector3f(0f, 10f, -10f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(200);
        
	bulletAppState = new BulletAppState();
	stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        
        initScene();
        initHexapod();
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
    }
    
    private void initHexapod()
    {
        final RigidBodyControl baseControl = new RigidBodyControl(HexapodShapeFactory.createBaseShape(), MASS_BASE);
        final Node baseNode = new Node("base");
        baseNode.addControl(baseControl);
        baseNode.setName("base");
        baseControl.setGravity(Vector3f.ZERO);       
        
        //baseNode.getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(0f,10f,0f));
        
        rootNode.attachChild(baseNode);
        bulletAppState.getPhysicsSpace().add(baseNode);
        

        
        HexapodLeg legLF = new HexapodLeg(baseControl, new Vector3f(3.0f, 0f, 4.5f), +0.588f);
        HexapodLeg legLM = new HexapodLeg(baseControl, new Vector3f(3.8f, 0f, 0f), FastMath.HALF_PI);
        HexapodLeg legLR = new HexapodLeg(baseControl, new Vector3f(3.0f, 0f, -4.5f), FastMath.PI - 0.588f);
        
        HexapodLeg legRF = new HexapodLeg(baseControl, new Vector3f(-3.0f, 0f, 4.5f), -0.588f);
        HexapodLeg legRM = new HexapodLeg(baseControl, new Vector3f(-3.8f, 0f, 0f), -FastMath.HALF_PI);
        HexapodLeg legRR = new HexapodLeg(baseControl, new Vector3f(-3.0f, 0f, -4.5f), 0.588f - FastMath.PI);
        
        
        rootNode.attachChild(legLF);
        rootNode.attachChild(legLM);
        rootNode.attachChild(legLR);
        
        rootNode.attachChild(legRF);
        rootNode.attachChild(legRM);
        rootNode.attachChild(legRR);
        
        
	bulletAppState.getPhysicsSpace().addAll(legLF);
        /*bulletAppState.getPhysicsSpace().add(legLM);
        bulletAppState.getPhysicsSpace().add(legLR);
        
        bulletAppState.getPhysicsSpace().add(legRF);
        bulletAppState.getPhysicsSpace().add(legRM);
        bulletAppState.getPhysicsSpace().add(legRR);*/
        
        
    }
    
    
}
