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
    
    private float angleCoxaTarget=0f;
    private float angleFemurTarget=0f;
    private float angleTibiaTarget=0f;
    
    private float angleCoxaZero;
    private float angleFemurZero;
    private float angleTibiaZero;
    
    private float angleCoxaCurrent=0f;
    private float angleFemurCurrent=0f;
    private float angleTibiaCurrent=0f;
    
    public void clearForces()
    {
        controlCoxa.clearForces();
        controlFemur.clearForces();
        controlTibia.clearForces();
    }
    
    public void setAngles(float angleCoxa, float angleFemur, float angleTibia)
    {
        this.angleCoxaTarget = angleCoxa;
        this.angleFemurTarget = angleFemur;
        this.angleTibiaTarget = angleTibia;
    }
    
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
        rot.fromAngleAxis((FastMath.PI /2) * -1, Vector3f.UNIT_Y);
        armShape.addChildShape(box, new Vector3f(0f, 1.5f, 0f),rot);
        return armShape;
    }

    public static CollisionShape createTibiaShape() {
        final CompoundCollisionShape handShape = new CompoundCollisionShape();
        final CapsuleCollisionShape capsule = new CapsuleCollisionShape(.5f, 3.2f);

        final Matrix3f rot = Matrix3f.IDENTITY;
        rot.fromAngleAxis((FastMath.PI /2) * 1, Vector3f.UNIT_X);
        handShape.addChildShape(capsule, new Vector3f(0.0f, 0f,0f),rot);
        
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
        
        jointCoxa = new HingeJoint(baseControl, controlCoxa, pivotBase, new Vector3f(0.0f, 0.8f,0.0f),Vector3f.UNIT_Y, Vector3f.UNIT_Y);
        jointCoxa.setLimit(0f, FastMath.PI);
        jointCoxa.enableMotor(true, 0f, 0);
        controlCoxa.clearForces();
        jointCoxa.setCollisionBetweenLinkedBodys(false);
        
        
        
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
        jointFemur.setLimit(0, FastMath.PI);
        
        
             
        
        
        controlTibia = new RigidBodyControl(createTibiaShape(), MASS_TIBIA);
        nodeTibia = new Node("tibia");
        nodeTibia.addControl(controlTibia);
	placeNode(transform, controlTibia);
	attachChild(nodeTibia);
        
        jointTibia = new HingeJoint(controlFemur, controlTibia, new Vector3f(-0.8f,0.0f, 0f), new Vector3f(0.0f,0.0f, 2.0f), Vector3f.UNIT_X, Vector3f.UNIT_X);
        jointTibia.setCollisionBetweenLinkedBodys(false);
        
        
        
        
        //final Quaternion quaternion = new Quaternion();
        //quaternion.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
        //nodeFemur.rotate(quaternion);
        
        angleCoxaZero = jointCoxa.getHingeAngle();
        angleTibiaZero = jointTibia.getHingeAngle();
        angleFemurZero = jointFemur.getHingeAngle() + FastMath.HALF_PI;
        System.out.println("angleCoxaZero: " + Float.toString(angleCoxaZero*FastMath.RAD_TO_DEG));
        System.out.println("angleFemurZero: " + Float.toString(angleFemurZero*FastMath.RAD_TO_DEG));
        System.out.println("angleTibiaZero: " + Float.toString(angleTibiaZero*FastMath.RAD_TO_DEG)); 
    }
    
    private void placeNode(final Transform transform, final RigidBodyControl node) {
        node.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        node.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        node.setFriction(FLOOR_FRICTION);
        node.setPhysicsLocation(transform.getTranslation());
        node.setPhysicsRotation(transform.getRotation().toRotationMatrix());
    }
    
   public void simpleUpdate(float tpf)
   {
       angleCoxaCurrent = jointCoxa.getHingeAngle() % FastMath.TWO_PI;
       angleFemurCurrent = jointFemur.getHingeAngle() % FastMath.TWO_PI;
       angleTibiaCurrent = jointTibia.getHingeAngle() % FastMath.TWO_PI;
       
       float angleCoxaWantedAngle = (angleCoxaZero + angleCoxaTarget) % FastMath.TWO_PI;
       
       if (angleCoxaCurrent - angleCoxaWantedAngle > FastMath.PI)
       {
           angleCoxaWantedAngle += FastMath.TWO_PI;
       }
       
       if (angleCoxaWantedAngle - angleCoxaCurrent  > FastMath.PI)
       {
           angleCoxaWantedAngle -= FastMath.TWO_PI;
       }
       
       float angleCoxaDiff = Math.abs((angleCoxaCurrent - angleCoxaWantedAngle) % FastMath.TWO_PI);
       if (angleCoxaDiff < (FastMath.PI / 100))
       {
           if (angleCoxaCurrent - angleCoxaWantedAngle < 0)
           {
               jointCoxa.enableMotor(true, 0.1f, 1.0f);
           }
           else
           {
               jointCoxa.enableMotor(true, -0.1f, 1.0f);
           }
       }
       else if (angleCoxaDiff < (FastMath.PI / 80))
       {
           if (angleCoxaCurrent - angleCoxaWantedAngle < 0)
           {
               jointCoxa.enableMotor(true, 0.2f, 1.0f);
           }
           else
           {
               jointCoxa.enableMotor(true, -0.2f, 1.0f);
           }
       }
       else
       {
           if (angleCoxaCurrent - angleCoxaWantedAngle < 0)
           {
               jointCoxa.enableMotor(true, 1f, 1.0f);
           }
           else
           {
               jointCoxa.enableMotor(true, -1f, 1.0f);
           }
       }
       
       /// -----
       float angleFemurWantedAngle = (angleFemurZero + angleFemurTarget) % FastMath.TWO_PI;
       
       if (angleFemurCurrent - angleFemurWantedAngle > FastMath.PI)
       {
           angleFemurWantedAngle += FastMath.TWO_PI;
       }
       
       if (angleFemurWantedAngle - angleFemurCurrent  > FastMath.PI)
       {
           angleFemurWantedAngle -= FastMath.TWO_PI;
       }
       
       float angleFemurDiff = Math.abs((angleFemurCurrent - angleFemurWantedAngle) % FastMath.TWO_PI);
       if (angleFemurDiff < (FastMath.PI / 100))
       {
           if (angleFemurCurrent - angleFemurWantedAngle < 0)
           {
               jointFemur.enableMotor(true, 0.1f, 2.0f);
           }
           else
           {
               jointFemur.enableMotor(true, -0.1f, 2.0f);
           }
       }
       else if (angleFemurDiff < (FastMath.PI / 80))
       {
           if (angleFemurCurrent - angleFemurWantedAngle < 0)
           {
               jointFemur.enableMotor(true, 0.2f, 2.0f);
           }
           else
           {
               jointFemur.enableMotor(true, -0.2f, 2.0f);
           }
       }
       else
       {
           if (angleFemurCurrent - angleFemurWantedAngle < 0)
           {
               jointFemur.enableMotor(true, 1f, 2.0f);
           }
           else
           {
               jointFemur.enableMotor(true, -1f, 2.0f);
           }
       }
       ///
       
       float angleTibiaWantedAngle = (angleTibiaZero + angleTibiaTarget) % FastMath.TWO_PI;
       
       if (angleTibiaCurrent - angleTibiaWantedAngle > FastMath.PI)
       {
           angleTibiaWantedAngle += FastMath.TWO_PI;
       }
       
       if (angleTibiaWantedAngle - angleTibiaCurrent  > FastMath.PI)
       {
           angleTibiaWantedAngle -= FastMath.TWO_PI;
       }
       
       float angleTibiaDiff = Math.abs((angleTibiaCurrent - angleTibiaWantedAngle) % FastMath.TWO_PI);
       if (angleTibiaDiff < (FastMath.PI / 100))
       {
           if (angleTibiaCurrent - angleTibiaWantedAngle < 0)
           {
               jointTibia.enableMotor(true, 0.1f, 1.0f);
           }
           else
           {
               jointTibia.enableMotor(true, -0.1f, 1.0f);
           }
       }
       else if (angleTibiaDiff < (FastMath.PI / 80))
       {
           if (angleTibiaCurrent - angleTibiaWantedAngle < 0)
           {
               jointTibia.enableMotor(true, 0.2f, 1.0f);
           }
           else
           {
               jointTibia.enableMotor(true, -0.2f, 1.0f);
           }
       }
       else
       {
           if (angleTibiaCurrent - angleTibiaWantedAngle < 0)
           {
               jointTibia.enableMotor(true, 1f, 1.0f);
           }
           else
           {
               jointTibia.enableMotor(true, -1f, 1.0f);
           }
       }
   }
   
   public void setTargetAngles(float angleCoxa, float angleFemur, float angleTibia)
   {
       this.angleCoxaTarget = angleCoxa;
       this.angleFemurTarget = angleFemur;
       this.angleTibiaTarget = angleTibia;
       
       angleCoxaCurrent = jointCoxa.getHingeAngle() % FastMath.TWO_PI;
       angleFemurCurrent = jointFemur.getHingeAngle() % FastMath.TWO_PI;
       angleTibiaCurrent = jointTibia.getHingeAngle() % FastMath.TWO_PI;
       
       float angleCoxaWantedAngle = (angleCoxaZero + angleCoxaTarget) % FastMath.TWO_PI;
       
       if (angleCoxaCurrent - angleCoxaWantedAngle > FastMath.PI)
       {
           angleCoxaWantedAngle += FastMath.TWO_PI;
       }
       
       if (angleCoxaWantedAngle - angleCoxaCurrent  > FastMath.PI)
       {
           angleCoxaWantedAngle -= FastMath.TWO_PI;
       }
       
       if (Math.abs((angleCoxaCurrent - angleCoxaWantedAngle) % FastMath.TWO_PI) < FastMath.PI / 90)
       {
           jointCoxa.enableMotor(true, 0f, 5f);
       }
       else
       {
           if ((angleCoxaCurrent - angleCoxaWantedAngle) < 0)
           {
               jointCoxa.enableMotor(true, 1.0f, 5.0f);
           }
           else
           {
               jointCoxa.enableMotor(true, -1.0f, 5.0f);
           }
       }
       /////
       
       
       float angleFemurWantedAngle = (angleFemurZero + angleFemurTarget) % FastMath.TWO_PI;
       
       if (angleFemurCurrent - angleFemurWantedAngle > FastMath.PI)
       {
           angleFemurWantedAngle += FastMath.TWO_PI;
       }
       
       if (angleFemurWantedAngle - angleFemurCurrent  > FastMath.PI)
       {
           angleFemurWantedAngle -= FastMath.TWO_PI;
       }
       
       if (Math.abs((angleFemurCurrent - angleFemurWantedAngle) % FastMath.TWO_PI) < FastMath.PI / 90)
       {
           jointFemur.enableMotor(true, 0f, 5f);
       }
       else
       {
           if ((angleFemurCurrent - angleFemurWantedAngle) < 0)
           {
               jointFemur.enableMotor(true, 1.0f, 5.0f);
           }
           else
           {
               jointFemur.enableMotor(true, -1.0f, 5.0f);
           }
       }
       
       //////
       
       float angleTibiaWantedAngle = (angleTibiaZero + angleTibiaTarget) % FastMath.TWO_PI;
       
       if (angleTibiaCurrent - angleTibiaWantedAngle > FastMath.PI)
       {
           angleTibiaWantedAngle += FastMath.TWO_PI;
       }
       
       if (angleTibiaWantedAngle - angleTibiaCurrent  > FastMath.PI)
       {
           angleTibiaWantedAngle -= FastMath.TWO_PI;
       }
       
       if (Math.abs((angleTibiaCurrent - angleTibiaWantedAngle) % FastMath.TWO_PI) < FastMath.PI / 90)
       {
           jointTibia.enableMotor(true, 0f, 5f);
       }
       else
       {
           if ((angleTibiaCurrent - angleTibiaWantedAngle) < 0)
           {
               jointTibia.enableMotor(true, 1.0f, 5.0f);
           }
           else
           {
               jointTibia.enableMotor(true, -1.0f, 5.0f);
           }
       }
   }
}
