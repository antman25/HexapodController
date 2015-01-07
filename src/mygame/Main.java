package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;

/**
 * test
 * @author normenhansen
 */



public class Main extends SimpleApplication {
    
    private Spatial sceneModel;
    WaterFilter water;
    private Vector3f lightDir = new Vector3f(-4.0F,-1.0F, 5F);
    
    private RigidBodyControl scene;
    private BulletAppState bulletAppState;
    private ThirdPersonPlayerNode player;
    
    
    
    public static final Quaternion ROLL045  = new Quaternion().fromAngleAxis(FastMath.PI/4,   new Vector3f(0,0,1));
    
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        mouseInput.setCursorVisible(false);
	//flyCam.setEnabled(false);
        flyCam.setMoveSpeed(400);
	bulletAppState = new BulletAppState();
	stateManager.attach(bulletAppState);
	//bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        
        
        
        initHexapod();
        initScene();
        initLight();
        //initSimpleWater();
        //initPPcWater();
        fire();
        //initPlayer();
    }
    
    public void setTextureScale(Spatial spatial, Vector2f vector) 
    {
        if (spatial instanceof Node) {
            Node findingnode = (Node) spatial;
            for (int i = 0; i < findingnode.getQuantity(); i++) {
                Spatial child = findingnode.getChild(i);
                setTextureScale(child, vector);
            }
        } else if (spatial instanceof Geometry) {
            ((Geometry) spatial).getMesh().scaleTextureCoordinates(vector);
        }
    }

    protected void setTextureOfObject(Spatial spatial)
    {
        Texture tex = assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg");
        tex.setWrap(WrapMode.Repeat);

        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setTexture("ColorMap", tex);

        setTextureScale(spatial, new Vector2f(1f, 1f));

        spatial.setMaterial(mat1);
    } 
    
    private void initHexapod()
    {       
        Box box = new Box(130f,10,3.0f);
        Spatial wall = new Geometry("Box", box );
        //Material mat_brick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //Texture tex = assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg");
        //tex.
        //mat_brick.setTexture("ColorMap", tex);
        //wall.setMaterial(mat_brick);
        
         
        
        wall.setLocalTranslation(0.0f,-20.0f,130.0f);
        setTextureOfObject(wall);
        
        rootNode.attachChild(wall);
        
        /** Load a model. Uses model and texture from jme3-test-data library! */ 
        Spatial LFleg = assetManager.loadModel("Models/PlateTibia.j3o");
        //Material defaultMat = new Material( assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material defaultMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"); 
        defaultMat.setBoolean("UseMaterialColors", true); 
        defaultMat.setColor("Ambient", ColorRGBA.Green); 
        defaultMat.setColor("Diffuse", ColorRGBA.Green);
        
        //Texture aluminum = assetManager.loadTexture("Textures/Grass.jpg");
        //defaultMat.setTexture("ColorMap", aluminum);
        //defaultMat.setColor("Unshaded", ColorRGBA.Yellow);
        
        LFleg.setMaterial(defaultMat);
        LFleg.setLocalTranslation(4.3f, -10,-8.2f);
        LFleg.rotate(FastMath.PI / 2.0F,FastMath.PI / 3.0F, 0.0F);
        
        
        
        Spatial LMleg = assetManager.loadModel("Models/PlateTibia.j3o");
        LMleg.setMaterial(defaultMat);
        LMleg.setLocalTranslation(6.3f, -10f, 0f);
        LMleg.rotate(FastMath.PI / 2.0F,0.0F, 0.0F);
        
        
        Spatial LRleg = assetManager.loadModel("Models/PlateTibia.j3o");
        LRleg.setMaterial(defaultMat);
        LRleg.setLocalTranslation(4.3f, -10f, 8.2f);
        LRleg.rotate(FastMath.PI / 2.0F,-FastMath.PI / 3.0F, 0.0F);
        
        
        Spatial RFleg = assetManager.loadModel("Models/PlateTibia.j3o");
        RFleg.setMaterial(defaultMat);
        RFleg.setLocalTranslation(-4.3f, -10f, -8.2f);
        RFleg.rotate(FastMath.PI / 2.0F,FastMath.PI / 3.0F, 0.0F);
        
        Spatial RMleg = assetManager.loadModel("Models/PlateTibia.j3o");
        RMleg.setMaterial(defaultMat);
        RMleg.setLocalTranslation(-6.3f, 0f, 0f);
        RMleg.rotate(FastMath.PI / 2.0F,0.0F, 0.0F);
        
        
        Spatial RRleg = assetManager.loadModel("Models/PlateTibia.j3o");
        RRleg.setMaterial(defaultMat);
        RRleg.setLocalTranslation(-4.3f, 0f, 8.2f);
        RRleg.rotate(FastMath.PI / 2.0F,-FastMath.PI / 3.0F, 0.0F);
        
        Spatial LFFemur = assetManager.loadModel("Models/PlateFemur.j3o");
        LFFemur.setMaterial(defaultMat);
        LFFemur.setLocalTranslation(-4.3f, 0f, 8.2f);
        LFFemur.rotate(FastMath.PI / 2.0F,FastMath.PI / 3.0F, 0.0F);
        
        
        
        rootNode.attachChild(LFleg);
        rootNode.attachChild(LMleg);
        rootNode.attachChild(LRleg);
        rootNode.attachChild(RFleg);
        rootNode.attachChild(RMleg);
        rootNode.attachChild(RRleg);
        
        rootNode.attachChild(LFFemur);
        
        
        /* A colored lit cube. Needs light source! */ 

    
    
    }
    
    private void initPlayer()
    {
        Spatial playerModel = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
	player = new ThirdPersonPlayerNode(playerModel, inputManager, cam);
	player.getCharacterControl().setPhysicsLocation(new Vector3f(-5f,2f,5f));
	rootNode.attachChild(player);
	bulletAppState.getPhysicsSpace().add(player);
    }
    
    private void initScene()
    {
        sceneModel =assetManager.loadModel("Scenes/robotScene.j3o");
        sceneModel.setLocalTranslation(0f, 0f, 0f);
        //sceneModel = assetManager.loadModel("Scenes/ManyLights/Main.scene");
	//sceneModel.scale(1f,.5f,1f); //Make scenery short enough to jump on. =P
        
        
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape((Node) sceneModel);
	scene = new RigidBodyControl(sceneShape, 0);
	sceneModel.addControl(scene);
        rootNode.attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(scene);
    }
    
    /*public void initSimpleWater()
    { 
        SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionScene(sceneModel); 
        Vector3f waterLocation=new Vector3f(0,-6,0); 
        waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, waterLocation.dot(Vector3f.UNIT_Y))); 
        viewPort.addProcessor(waterProcessor); waterProcessor.setWaterDepth(10); // transparency of water 
        waterProcessor.setDistortionScale(0.05f); // strength of waves 
        waterProcessor.setWaveSpeed(0.05f); // speed of waves 
        Quad quad = new Quad(800,800); 
        quad.scaleTextureCoordinates(new Vector2f(6f,6f)); 
        water=new Geometry("water", quad); 
        water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X)); 
        water.setLocalTranslation(-400, 0.32f, 400); water.setShadowMode(RenderQueue.ShadowMode.Receive); 
        water.setMaterial(waterProcessor.getMaterial()); rootNode.attachChild(water); 
    } */
    
    public void initPPcWater()
    { 
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager); 
        water = new WaterFilter(rootNode, lightDir); 
        water.setCenter(new Vector3f(0,30, 0)); 
        water.setRadius(10); 
        water.setWaveScale(0.003f); 
        water.setMaxAmplitude(1f); 
        water.setFoamExistence(new Vector3f(1f,4f, 0.5f)); 
        water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg")); 
        water.setRefractionStrength(0.2f); 
        water.setWaterHeight(0.01f); 
        fpp.addFilter(water); 
        viewPort.addProcessor(fpp); 
    }
    
    private void fire()
    {
            /** Uses Texture from jme3-test-data library! */
        ParticleEmitter fireEffect = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        //fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect.setMaterial(fireMat);
        fireEffect.setImagesX(2); fireEffect.setImagesY(2); // 2x2 texture animation
        fireEffect.setEndColor( new ColorRGBA(1f, 0f, 0f, 1f) );   // red
        fireEffect.setStartColor( new ColorRGBA(1f, 1f, 0f, 0.5f) ); // yellow
        fireEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect.setStartSize(0.6f);
        fireEffect.setEndSize(0.1f);
        fireEffect.setGravity(0f,0f,0f);
        fireEffect.setLowLife(0.5f);
        fireEffect.setHighLife(3f);
        fireEffect.getParticleInfluencer().setVelocityVariation(0.3f);
        fireEffect.setLocalTranslation(10F, 10F, 10F);
        rootNode.attachChild(fireEffect);

    }
    
    
    private void initLight()
    {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);        
        rootNode.addLight(ambient);     
        /** A white, directional light source */ 
        /*DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun); */
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
        //player.update();
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    private static class HexapodShapeFactory {
        public CollisionShape createBaseShape() {
                final CompoundCollisionShape chassisShape = new CompoundCollisionShape();
                final CapsuleCollisionShape rf_lb = new CapsuleCollisionShape(.3f, 10.81f);
                final CapsuleCollisionShape lf_rb = new CapsuleCollisionShape(.3f, 10.81f);
                final CapsuleCollisionShape lm_rm = new CapsuleCollisionShape(.3f, 7.6f);

                final Matrix3f rotX = new Matrix3f();
                rotX.fromAngleNormalAxis(FastMath.HALF_PI, Vector3f.UNIT_X);

                final Matrix3f rotY = new Matrix3f();
                rotY.fromAngleAxis(0.588f, Vector3f.UNIT_Y);

                chassisShape.addChildShape(rf_lb, Vector3f.ZERO, rotY.mult(rotX));

                rotY.fromAngleAxis(-0.588f, Vector3f.UNIT_Y);

                chassisShape.addChildShape(lf_rb, Vector3f.ZERO, rotY.mult(rotX));

                final Matrix3f rotZ = new Matrix3f();
                rotZ.fromAngleNormalAxis(FastMath.HALF_PI, Vector3f.UNIT_Z);

                chassisShape.addChildShape(lm_rm, Vector3f.ZERO, rotZ);

                return chassisShape;
        }

        public CollisionShape createShoulderShape() {
                final CompoundCollisionShape epauleShape = new CompoundCollisionShape();
                epauleShape.addChildShape(new BoxCollisionShape(new Vector3f(0.35f, .35f, 0.65f)), new Vector3f(
                                0, 0, 0.65f));
                return epauleShape;
        }

        public CollisionShape createArmShape() {
                final CompoundCollisionShape armShape = new CompoundCollisionShape();
                final BoxCollisionShape box = new BoxCollisionShape(new Vector3f(.25f, 2.3f, 1f));

                final Matrix3f rot = Matrix3f.IDENTITY;
                rot.fromAngleAxis((FastMath.PI / 9) * 1, Vector3f.UNIT_X.mult(-1));
                armShape.addChildShape(box, new Vector3f(.5f, 2.3f, .0f));
                return armShape;
        }

        public CollisionShape createHandShape() {
                final CompoundCollisionShape handShape = new CompoundCollisionShape();
                handShape.addChildShape(new CapsuleCollisionShape(.5f, 11.2f), new Vector3f(.5f, -2.9f, .5f));
                return handShape;
        }
    }
}
