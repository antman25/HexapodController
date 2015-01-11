/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexapod;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 *
 * @author ANTMAN
 */
public class HexapodLeg extends Node{
    private static final float MASS_COXA = 5f;
    private static final float MASS_FEMUR =5f;
    private static final float MASS_TIBIA = 1f;
    private static final float FLOOR_FRICTION = 10000f;
    
    
    private HingeJoint jointCoxa;
    private HingeJoint jointFemur;
    private HingeJoint jointTibia;
    
    private RigidBodyControl controlCoxa;
    private RigidBodyControl controlFemur;
    private RigidBodyControl controlTibia;    
    
    private Node nodeCoxa;
    private Node nodeFemur;
    private Node nodeTibia;
    
    private float angleCoxaTarget;
    private float angleFemurTarget;
    private float angleTibiaTarget;
    
    private float angleCoxaZero;
    private float angleFemurZero;
    private float angleTibiaZero;
    
    private float angleCoxaCurrent;
    private float angleFemurCurrent;
    private float angleTibiaCurrent;
    
    public Float getCoxaAngle()
    {
        if (jointCoxa == null)
            return 0f;
        return jointCoxa.getHingeAngle();
    }
    
    public Float getFemurAngle()
    {
        if (jointFemur == null)
            return 0f;
        return jointFemur.getHingeAngle();
    }
    
    public Float getTibiaAngle()
    {
        if (jointTibia == null)
            return 0f;
        return jointTibia.getHingeAngle();
    }
    
    
    public Node getCoxaNode()
    {
        return nodeCoxa;
    }
    
    public Node getFemurNode()
    {
        return nodeFemur;
    }
    
    public Node getTibiaNode()
    {
        return nodeTibia;
    }
    
    public static CollisionShape createCoxaShape() {
        final CompoundCollisionShape epauleShape = new CompoundCollisionShape();
        final CylinderCollisionShape cylinder = new CylinderCollisionShape(new Vector3f(0.50f,1.5f,0.50f));
        //final BoxCollisionShape box = new BoxCollisionShape()
        
        //epauleShape.addChildShape(cylinder , new Vector3f(0, 0, 0.65f));
        
        final Matrix3f rot = Matrix3f.IDENTITY;
        rot.fromAngleAxis((FastMath.PI / 2) * 1, Vector3f.UNIT_X);
        epauleShape.addChildShape(cylinder, new Vector3f(0, -.025f, 0f),rot);
        return epauleShape;
        
        //CylinderCollisionShape coxaShape = new CylinderCollisionShape(new Vector3f(0.5f,1.0f,3.0f));
    }

    public static CollisionShape createFemurShape() {
        final CompoundCollisionShape armShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(0.25f,1.5f,0.25f));
        
        final Matrix3f rot = Matrix3f.IDENTITY;
        rot.fromAngleAxis((FastMath.PI /2) * 1, Vector3f.UNIT_Y);
        armShape.addChildShape(box, new Vector3f(0f, 1.5f, 0f),rot);
        return armShape;
    }

    public static CollisionShape createTibiaShape() {
        final CompoundCollisionShape handShape = new CompoundCollisionShape();
        final CapsuleCollisionShape capsule = new CapsuleCollisionShape(.5f, 4.2f);
        //handShape.addChildShape(, new Vector3f(.5f, -4.4f, .5f));
        final Matrix3f rot = Matrix3f.IDENTITY;
        rot.fromAngleAxis((FastMath.PI /2) * 1, Vector3f.UNIT_X);
        //handShape.addChildShape(capsule, new Vector3f(-0.8f, -1f,1f),rot);
        handShape.addChildShape(capsule, new Vector3f(0.0f, 0f,0f),rot);
        
        return handShape;
    }
    
    
    public HexapodLeg(AssetManager assetManager, RigidBodyControl baseControl, Vector3f pivotBase, float angle)
    {
        Box boxMesh = new Box(1f,1f,1f); 
        Geometry boxGeo = new Geometry("Colored Box", boxMesh); 
        final Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"); 
        boxMat.setBoolean("UseMaterialColors", true); 
        boxMat.setColor("Ambient", ColorRGBA.Green); 
        boxMat.setColor("Diffuse", ColorRGBA.Green); 
        /* A colored lit cube. Needs light source! */ 
    
    
    
        final Transform transform = new Transform(pivotBase, new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y));
        
        controlCoxa = new RigidBodyControl(createCoxaShape(), MASS_COXA);
        //controlCoxa.setKinematic(true);
        
        //controlCoxa.setGravity(new Vector3f(1.0f,0.0f,0.0f));
        nodeCoxa = new Node("coxa");
        nodeCoxa.attachChild(boxGeo);
        nodeCoxa.addControl(controlCoxa);
        nodeCoxa.setMaterial(boxMat);
        //nodeCoxa.setLocalTranslation(transform.getTranslation());
        //nodeCoxa.setLocalRotation(transform.getRotation().toRotationMatrix());
        placeNode(transform, controlCoxa);
        attachChild(nodeCoxa);
        
        jointCoxa = new HingeJoint(baseControl, controlCoxa, pivotBase, new Vector3f(0.0f, 0.8f,0.0f),Vector3f.UNIT_Y, Vector3f.UNIT_Y);
        //jointCoxa.setLimit(0f, FastMath.PI);
        jointCoxa.enableMotor(true, 0f, 0);
        controlCoxa.clearForces();
        jointCoxa.setCollisionBetweenLinkedBodys(false);
        angleCoxaZero = jointCoxa.getHingeAngle();
        System.out.println("angleCoxaZero: " + Float.toString(angleCoxaZero*FastMath.RAD_TO_DEG));
        
        controlFemur = new RigidBodyControl(createFemurShape(), MASS_FEMUR);
        //controlFemur.setKinematic(true);
        controlFemur.clearForces();
        nodeFemur = new Node("femur");
        nodeFemur.addControl(controlFemur);
        placeNode(transform, controlFemur);
        
        attachChild(nodeFemur);
        controlFemur.clearForces();
        
        jointFemur = new HingeJoint(controlCoxa, controlFemur, new Vector3f(0.75f,0f,0.0f), new Vector3f(0.0f,3.0f,0.0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
        jointFemur.setCollisionBetweenLinkedBodys(false);
        jointFemur.enableMotor(true, 0f, 0);
        //jointFemur.setLimit(0, FastMath.PI);
        
        angleFemurZero = jointFemur.getHingeAngle();
        System.out.println("angleFemurZero: " + Float.toString(angleFemurZero*FastMath.RAD_TO_DEG));     
        
        
        controlTibia = new RigidBodyControl(createTibiaShape(), MASS_TIBIA);
        nodeTibia = new Node("tibia");
        nodeTibia.addControl(controlTibia);
	placeNode(transform, controlTibia);
	attachChild(nodeTibia);
        //final Quaternion quaternion = new Quaternion();
        //quaternion.fromAngleAxis(FastMath.PI / 3, Vector3f.UNIT_X);
        jointTibia = new HingeJoint(controlFemur, controlTibia, new Vector3f(-0.8f,0.0f, 0f), new Vector3f(0.0f,0.0f, 2.0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
        //jointTibia.setCollisionBetweenLinkedBodys(false);
        
        angleTibiaZero = jointTibia.getHingeAngle();
        System.out.println("angleTibiaZero: " + Float.toString(angleTibiaZero*FastMath.RAD_TO_DEG)); 
    }
    
    private void placeNode(final Transform transform, final RigidBodyControl node) {
        node.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        node.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        node.setFriction(FLOOR_FRICTION);
        node.setPhysicsLocation(transform.getTranslation());
        node.setPhysicsRotation(transform.getRotation().toRotationMatrix());
    }
    
    /*@Override
    public void setLocalTransform(final Transform t) {
            super.setLocalTransform(t);

            for (final Spatial child : this.getChildren()) {
                    final RigidBodyControl control = child.getControl(RigidBodyControl.class);
                    control.setPhysicsLocation(control.getPhysicsLocation().add(t.getTranslation()));
                    control.setPhysicsRotation(control.getPhysicsRotation().mult(t.getRotation()));
                    child.setLocalTransform(child.getLocalTransform().combineWithParent(t));
            }
    }*/
    
}
