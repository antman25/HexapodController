/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexapod;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
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
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

/**
 *
 * @author ANTMAN
 */
public class HexapodLeg extends Node{
    private static final float MASS_COXA = 1f;
    private static final float MASS_FEMUR = 1f;
    private static final float MASS_TIBIA = 10f;
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
    
    private float angleCoxaCurrent;
    private float angleFemurCurrent;
    private float angleTibiaCurrent;
    
    public static CollisionShape createCoxaShape() {
        final CompoundCollisionShape epauleShape = new CompoundCollisionShape();
        epauleShape.addChildShape(new BoxCollisionShape(new Vector3f(0.35f, .35f, 0.65f)), new Vector3f(0, 0, 0.65f));
        //new BoxCollisionShape()
        return epauleShape;
    }

    public static CollisionShape createFemurShape() {
        final CompoundCollisionShape armShape = new CompoundCollisionShape();
        final BoxCollisionShape box = new BoxCollisionShape(new Vector3f(.25f, 2.3f, .25f));

        final Matrix3f rot = Matrix3f.IDENTITY;
        rot.fromAngleAxis((FastMath.PI / 9) * 1, Vector3f.UNIT_X.mult(-1));
        armShape.addChildShape(box, new Vector3f(.5f, 2.3f, .250f));
        return armShape;
    }

    public static CollisionShape createTibiaShape() {
        final CompoundCollisionShape handShape = new CompoundCollisionShape();
        handShape.addChildShape(new CapsuleCollisionShape(.5f, 11.2f), new Vector3f(.5f, -2.9f, .5f));
        return handShape;
    }
    
    public HexapodLeg(RigidBodyControl baseControl, Vector3f pivotBase, float angle)
    {       
        final Transform transform = new Transform(pivotBase, new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y));
        
        controlCoxa = new RigidBodyControl(createCoxaShape(), MASS_COXA);

        nodeCoxa = new Node("coxa");
        nodeCoxa.addControl(controlCoxa);
        placeNode(transform, controlCoxa);
        attachChild(nodeCoxa);
        
        jointCoxa = new HingeJoint(baseControl, controlCoxa, pivotBase, Vector3f.ZERO,Vector3f.UNIT_Y, Vector3f.UNIT_Y);
        jointCoxa.setLimit(0f, FastMath.PI);
        jointCoxa.enableMotor(true, 0, 1);
        jointCoxa.setCollisionBetweenLinkedBodys(false);
        
        controlFemur = new RigidBodyControl(createFemurShape(), MASS_FEMUR);

        nodeFemur = new Node("femur");
        nodeFemur.addControl(controlFemur);
        this.placeNode(transform, controlFemur);
        attachChild(nodeFemur);
        
        jointFemur = new HingeJoint(controlCoxa, controlFemur, new Vector3f(0, 0, 1.3f), Vector3f.ZERO, Vector3f.UNIT_X, Vector3f.UNIT_X);
        jointFemur.setCollisionBetweenLinkedBodys(false);
        jointFemur.enableMotor(true, 0f, 1);
        
        controlTibia = new RigidBodyControl(createTibiaShape(), MASS_TIBIA);
        nodeTibia = new Node("tibia");
        nodeTibia.addControl(controlTibia);
	placeNode(transform, controlTibia);
	attachChild(nodeTibia);
        final Quaternion quaternion = new Quaternion();
        quaternion.fromAngleAxis(FastMath.PI / 3, Vector3f.UNIT_X);
        jointTibia = new HingeJoint(controlFemur, controlTibia, new Vector3f(0.25f, 4.6f, 0.25f), Vector3f.ZERO, Vector3f.UNIT_X, Vector3f.UNIT_X);
        jointTibia.setCollisionBetweenLinkedBodys(false);
        jointTibia.enableMotor(true, 0f, 1f);
    }
    
    private void placeNode(final Transform transform, final RigidBodyControl node) {
        node.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        node.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        node.setFriction(FLOOR_FRICTION);
        node.setPhysicsLocation(transform.getTranslation());
        node.setPhysicsRotation(transform.getRotation().toRotationMatrix());
    }
    
    @Override
    public void setLocalTransform(final Transform t) {
            super.setLocalTransform(t);

            for (final Spatial child : this.getChildren()) {
                    final RigidBodyControl control = child.getControl(RigidBodyControl.class);
                    control.setPhysicsLocation(control.getPhysicsLocation().add(t.getTranslation()));
                    control.setPhysicsRotation(control.getPhysicsRotation().mult(t.getRotation()));
                    child.setLocalTransform(child.getLocalTransform().combineWithParent(t));
            }
    }
}
