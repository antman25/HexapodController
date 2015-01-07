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

/**
 *
 * @author ANTMAN
 */
public class HexapodBuildFrame extends SimpleApplication{

    private Spatial sceneModel;
    private RigidBodyControl scene;
    private BulletAppState bulletAppState;
    
    private static final float FLOOR_FRICTION = 10000f;
    private static final float MASS_BASE = 50f;
    private static final float MASS_SHOULDER = 5f;
    private static final float MASS_ARM = 5f;
    private static final float MASS_HAND = 10f;

    
    public static void main(String[] args) {
        HexapodBuildFrame frame = new HexapodBuildFrame();
        frame.start();
    }
    
    @Override
    public void simpleInitApp() {
        mouseInput.setCursorVisible(false);
        cam.setLocation(new Vector3f(-10f, 10f, 10f));
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
        sceneModel.setLocalTranslation(0f, 0f, 0f);
        
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
        baseNode.getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(0f,10f,0f));
        
        
        rootNode.attachChild(baseNode);
        bulletAppState.getPhysicsSpace().add(baseNode);
        
        createLeg(baseControl, new Vector3f(3.0f, 1f, 4.5f), +0.588f);
        createLeg(baseControl, new Vector3f(3.8f, 1f, 0f), FastMath.HALF_PI);
        createLeg(baseControl, new Vector3f(3.0f, 1f, -4.5f), FastMath.PI - 0.588f);
        createLeg(baseControl, new Vector3f(-3.0f, 1f, 4.5f), -0.588f);
        createLeg(baseControl, new Vector3f(-3.8f, 1f, 0f), -FastMath.HALF_PI);
        createLeg(baseControl, new Vector3f(-3.0f, 1f, -4.5f), 0.588f - FastMath.PI);
    }
    
    private void createLeg(final RigidBodyControl base, final Vector3f pivotBase, final float angle) {
        final Transform transform = new Transform(pivotBase, new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y));
        final RigidBodyControl shoulderControl = this.createShoulder(transform);

        final HingeJoint shoulder = new HingeJoint(base, shoulderControl, pivotBase, Vector3f.ZERO, Vector3f.UNIT_Y, Vector3f.UNIT_Y);
        shoulder.enableMotor(true, 0, 1);
        shoulder.setCollisionBetweenLinkedBodys(false);
        //this.joints.get(leg).put(HexapodArticulation.SHOULDER, shoulder);
    }
    
    private void placeNode(final Transform transform, final RigidBodyControl node) {
        node.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        node.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        node.setFriction(FLOOR_FRICTION);
        node.setPhysicsLocation(transform.getTranslation());
        node.setPhysicsRotation(transform.getRotation().toRotationMatrix());
    }
    
    private RigidBodyControl createShoulder(final Transform transform) {
		final RigidBodyControl shoulderControl = new RigidBodyControl(HexapodShapeFactory.createShoulderShape(), MASS_SHOULDER);
		final Node shoulderNode = new Node("shoulder");
		shoulderNode.addControl(shoulderControl);
		this.placeNode(transform, shoulderControl);
		rootNode.attachChild(shoulderNode);

		/*final RigidBodyControl armControl = this.createArm(new Transform(new Vector3f(0, 0, 2f)).combineWithParent(transform));

		final HingeJoint elbow = new HingeJoint(shoulderControl, armControl, new Vector3f(0, 0, 1.3f),
				Vector3f.ZERO, Vector3f.UNIT_X, Vector3f.UNIT_X);
		elbow.setCollisionBetweenLinkedBodys(false);
		elbow.enableMotor(true, 0, 1);*/
		//this.joints.get(leg).put(HexapodArticulation.ELBOW, elbow);

		return shoulderControl;
	}
}
