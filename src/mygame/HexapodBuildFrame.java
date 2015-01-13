/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Cylinder;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import hexapod.HexapodLeg;


/**
 *
 * @author ANTMAN
 */
public class HexapodBuildFrame extends SimpleApplication implements ScreenController{

    private Spatial sceneModel;
    private RigidBodyControl scene;
    private BulletAppState bulletAppState;
    private Nifty nifty;
    NiftyJmeDisplay niftyDisplay;
    BitmapText hudText;
    
    HexapodLeg legLF;
    HexapodLeg legLM;
    HexapodLeg legLR;
    
    HexapodLeg legRF;
    HexapodLeg legRM;
    HexapodLeg legRR;
    float angle = 0.0f;
    
    private static final float MASS_BASE = 10f;
    
    public static void main(String[] args) {
        HexapodBuildFrame frame = new HexapodBuildFrame();
        frame.setPauseOnLostFocus(true);
        frame.start();
    }
    
    @Override
    public void simpleInitApp() {      
        initGUI();
        initCam();
	initPhysics();
        initControls();
        initScene();
        initHexapod();
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf); //To change body of generated methods, choose Tools | Templates.
        
        legLF.simpleUpdate(tpf);
        legLM.simpleUpdate(tpf);
        legLR.simpleUpdate(tpf);
        
        legRF.simpleUpdate(tpf);
        legRM.simpleUpdate(tpf);
        legRR.simpleUpdate(tpf);
        if (hudText != null && legLF != null)
        {
            String output = "LF Coxa: " + Float.toString(FastMath.RAD_TO_DEG*legLF.getCoxaAngle()) + " Femur: " + Float.toString(FastMath.RAD_TO_DEG*legLF.getFemurAngle()) + "Tibia: " + Float.toString(FastMath.RAD_TO_DEG*legLF.getTibiaAngle()) + "\n";
            output += "LM Coxa: " + Float.toString(FastMath.RAD_TO_DEG*legLM.getCoxaAngle()) + " Femur: " + Float.toString(FastMath.RAD_TO_DEG*legLM.getFemurAngle()) + "Tibia: " + Float.toString(FastMath.RAD_TO_DEG*legLM.getTibiaAngle()) + "\n";
            output += "LR Coxa: " + Float.toString(FastMath.RAD_TO_DEG*legLR.getCoxaAngle()) + " Femur: " + Float.toString(FastMath.RAD_TO_DEG*legLR.getFemurAngle()) + "Tibia: " + Float.toString(FastMath.RAD_TO_DEG*legLR.getTibiaAngle()) + "\n";
            
            output += "RF Coxa: " + Float.toString(FastMath.RAD_TO_DEG*legRF.getCoxaAngle()) + " Femur: " + Float.toString(FastMath.RAD_TO_DEG*legRF.getFemurAngle()) + "Tibia: " + Float.toString(FastMath.RAD_TO_DEG*legRF.getTibiaAngle()) + "\n";
            output += "RM Coxa: " + Float.toString(FastMath.RAD_TO_DEG*legRM.getCoxaAngle()) + " Femur: " + Float.toString(FastMath.RAD_TO_DEG*legRM.getFemurAngle()) + "Tibia: " + Float.toString(FastMath.RAD_TO_DEG*legRM.getTibiaAngle()) + "\n";
            output += "RR Coxa: " + Float.toString(FastMath.RAD_TO_DEG*legRR.getCoxaAngle()) + " Femur: " + Float.toString(FastMath.RAD_TO_DEG*legRR.getFemurAngle()) + "Tibia: " + Float.toString(FastMath.RAD_TO_DEG*legRR.getTibiaAngle()) + "\n";
            hudText.setText(output);
        }
            
    }
    
    
    
    private void initCam()
    {
        cam.setLocation(new Vector3f(0f, 20f, -20f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(100);
        flyCam.setDragToRotate(true);
    }
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean pressed, float tpf){
            System.out.println(name + " = " + pressed);
            
            if (name.equals("console"))
            {
                if (pressed == true)
                {
                    guiViewPort.addProcessor(niftyDisplay);
                }
                else
                {
                    guiViewPort.removeProcessor(niftyDisplay);
                }
            }
            
            if (name.equals("space"))
            {
                //clearAllForces();
                angle -= FastMath.PI / 180;
                legLF.setAngles(angle, 0.0f, 0.0f);
                System.out.println("Setting LFCoxa to : " + Float.toString(angle * FastMath.RAD_TO_DEG));
                
            }
            
            if (name.equals("click_left"))
            {
                CollisionResults results = new CollisionResults();
                // Aim the ray from camera location in camera direction
                // (assuming crosshairs in center of screen).
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                // Collect intersections between ray and all nodes in results list.
                rootNode.collideWith(ray, results);
                // Print the results so we see what is going on
                for (int i = 0; i < results.size(); i++) {
                  // For each “hit”, we know distance, impact point, geometry.
                  float dist = results.getCollision(i).getDistance();
                  Vector3f pt = results.getCollision(i).getContactPoint();
                  String target = results.getCollision(i).getGeometry().getName();
                  System.out.println("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away.");
                }
            }
        }
    };
    public AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            System.out.println(name + " = " + value);
        }
    };
    
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind( " + screen.getScreenId() + ")");
    }

    public void onStartScreen() {
        System.out.println("onStartScreen");
    }

    public void onEndScreen() {
        System.out.println("onEndScreen");
    }

    public void quit(){
        nifty.gotoScreen("end");
    }


    private void initGUI()
    {
        niftyDisplay = new NiftyJmeDisplay(assetManager,
                                                          inputManager,
                                                          audioRenderer,
                                                          guiViewPort);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/test2.xml", "start", this);
        
        hudText = new BitmapText(guiFont, false);          
        hudText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        hudText.setColor(ColorRGBA.Blue);                             // font color
        hudText.setText("You can write any string here");             // the text
        hudText.setLocalTranslation(400, 200 + hudText.getLineHeight(), 0); // position
        guiNode.attachChild(hudText);
        
        //nifty.setDebugOptionPanelColors(true);
    }
    
    private void clearAllForces()
    {
        legLF.clearForces();
        legLM.clearForces();
        legLR.clearForces();
        
        legRF.clearForces();
        legRM.clearForces();
        legRR.clearForces();
        
    }
    
    private void initPhysics()
    {
        bulletAppState = new BulletAppState();
	stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(true);
        bulletAppState.getPhysicsSpace().setAccuracy(0.005f);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0.0f,-20.0f,0.0f));
    }
    
    private void initControls()
    {
        mouseInput.setCursorVisible(true);
        inputManager.deleteMapping( SimpleApplication.INPUT_MAPPING_MEMORY );        
        inputManager.addMapping("space",     new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("click_left",     new MouseButtonTrigger(MouseInput.BUTTON_LEFT) );
        inputManager.addMapping("click_mid",      new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE) );
        inputManager.addMapping("click_right",    new MouseButtonTrigger(MouseInput.BUTTON_RIGHT) );
        inputManager.addMapping("console",            new KeyTrigger(KeyInput.KEY_TAB));

        // Test multiple listeners per mapping
        inputManager.addListener(actionListener, "space");
        inputManager.addListener(actionListener, "click_left");
        inputManager.addListener(actionListener, "click_mid");
        inputManager.addListener(actionListener, "click_right");
        inputManager.addListener(actionListener, "console");
        
        //inputManager.addListener(analogListener, "My Action");

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
        sceneModel.setLocalTranslation(0f, -2f, 0f);
        
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape((Node) sceneModel);
	scene = new RigidBodyControl(sceneShape, 0);
	sceneModel.addControl(scene);
        rootNode.attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(scene);
        
        attachCoordinateAxes(new Vector3f(0.0f,0.0f,0.0f));
    }
       
    private void initHexapod()
    {
        final RigidBodyControl hook = new RigidBodyControl(new BoxCollisionShape(new Vector3f(.5f, 0.5f, 0.5f)),0f);
        
        
        
        /* A colored lit cube. Needs light source! */ 
        Cylinder boxMesh = new Cylinder(30, 30, 0.5f, 1.5f); 
        Geometry boxGeo = new Geometry("Colored Box", boxMesh); 
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); 
        //boxGeo.rotate(FastMath.HALF_PI, 0f, 0f);
        // monkeyTex = assetManager.loadTexture("Interface/Logo/Monkey.jpg"); 
        //boxMat.setTexture("ColorMap", monkeyTex); 
        boxGeo.addControl(hook);
        boxMat.setColor("Color", ColorRGBA.Gray);
        boxGeo.setMaterial(boxMat); 
        //rootNode.attachChild(boxGeo);
       
        
        final RigidBodyControl baseControl = new RigidBodyControl(HexapodShapeFactory.createBaseShape(), MASS_BASE);
        final Node baseNode = new Node("base");
        baseNode.addControl(baseControl);
        baseNode.setName("base");
        //baseControl.setGravity(Vector3f.ZERO);       
        //baseControl.setKinematic(true);
        
        HingeJoint joint = new HingeJoint(hook, baseControl, Vector3f.ZERO, new Vector3f(0.0f, -5f,0.0f),Vector3f.UNIT_Y, Vector3f.UNIT_Y);
        //joint.enableMotor(false, 1f, 0.1f);
        
        bulletAppState.getPhysicsSpace().addAll(boxGeo);
        
        legLF = new HexapodLeg(baseControl, new Vector3f(3.0f, 10f, 4.5f), .588f);
        legLF.rotate(new Quaternion(0.0f, 1.0f, 0f, FastMath.HALF_PI));
        legLF.setAngles(0.0f, 0.0f, 0.0f);
        legLM = new HexapodLeg(baseControl, new Vector3f(3.8f, 0f, 0f), FastMath.HALF_PI);
        //legLM.setAngles(0.0f, FastMath.QUARTER_PI, 0.0f);
        
        legLR = new HexapodLeg(baseControl, new Vector3f(3.0f, 0f, -4.5f), FastMath.PI - 0.588f);
        //legLR.setAngles(0.0f, FastMath.QUARTER_PI, 0.0f);
        
        legRF = new HexapodLeg(baseControl, new Vector3f(-3.0f, 0f, 4.5f), -0.588f);
        //legRF.setAngles(0.0f, FastMath.QUARTER_PI, 0.0f);
        
        legRM = new HexapodLeg(baseControl, new Vector3f(-3.8f, 0f, 0f), -FastMath.HALF_PI);
        //legRM.setAngles(0.0f, FastMath.QUARTER_PI, 0.0f);
        
        legRR = new HexapodLeg(baseControl, new Vector3f(-3.0f, 0f, -4.5f), 0.588f - FastMath.PI);
        //legRR.setAngles(0.0f, FastMath.QUARTER_PI, 0.0f);
        
        //legLF.getCoxaNode().attachChild(boxGeo);
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
        legLF.getCoxaNode().attachChild(boxGeo);
        //clearAllForces();
        
    }

    
    
    
}
